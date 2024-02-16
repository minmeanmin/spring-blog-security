package shop.mtcoding.blog.board;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import shop.mtcoding.blog.reply.ReplyRepository;
import shop.mtcoding.blog.user.User;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class BoardController {

    private final HttpSession session;
    private final BoardRepository boardRepository;
    private final ReplyRepository replyRepository;

    @PostMapping("/board/{id}/update")
    public String update(@PathVariable int id, BoardRequest.UpdateDTO requestDTO){
        //  1. 인증 확인
        User sessionUser = (User) session.getAttribute("sessionUser");
        if(sessionUser == null){
            return "redirect:/loginForm";
        }
        //  2. 권한 확인
        Board board = boardRepository.findById(id);
        if (board.getUserId() != sessionUser.getId()){
            return "error/403";
        }
        //  3. 핵심 로직
        //  update board_tb set title = ?, content = ? where id = ?;
        boardRepository.update(requestDTO, id);

        return "redirect:/board/"+id;
    }

    @GetMapping("/board/{id}/updateForm")
    public String updateForm(@PathVariable int id, HttpServletRequest request){
        //  인증 확인
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null) {   //  error : 401
            return "redirect:/loginForm";
        }
        //  권한 확인


        //  Model위임 id로 board를 조회
        Board board = boardRepository.findById(id);
        if(board.getUserId() != sessionUser.getId()){
            return "error/403";
        }

        //  가방에 담기
        request.setAttribute("board",board);

        return "board/updateForm";
    }

    // localhost:8080?page=1 -> page 값이 1
    // localhost:8080  -> page 값이 0
    @GetMapping("/")
    public String index(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "") String keyword) {

        // isEmpty -> null, 공백
        // isBlank -> null, 공백, 화이트 스페이스

        if(keyword.isBlank()){
            List<Board> boardList = boardRepository.findAll(page);
            // 전체 페이지 개수
            int count = boardRepository.count().intValue();
            // 5 -> 2page
            // 6 -> 2page
            // 7 -> 3page
            // 8 -> 3page
            int namerge = count % 3 == 0 ? 0 : 1;
            int allPageCount = count / 3 + namerge;

            request.setAttribute("boardList", boardList);
            request.setAttribute("first", page == 0);
            request.setAttribute("last", allPageCount == page + 1);
            request.setAttribute("prev", page - 1);
            request.setAttribute("next", page + 1);
            request.setAttribute("keyword", ""); //검색을 안 한 경우이기 때문에 공백을 담아줬다.

        }else{
            List<Board> boardList = boardRepository.findAll(page, keyword);
            // 전체 페이지 개수
            int count = boardRepository.count(keyword).intValue();
            // 5 -> 2page
            // 6 -> 2page
            // 7 -> 3page
            // 8 -> 3page
            int namerge = count % 3 == 0 ? 0 : 1;
            int allPageCount = count / 3 + namerge;

            request.setAttribute("boardList", boardList);
            request.setAttribute("first", page == 0);
            request.setAttribute("last", allPageCount == page + 1);
            request.setAttribute("prev", page - 1);
            request.setAttribute("next", page + 1);
            request.setAttribute("keyword", keyword);
        }

        return "index";
    }

    @PostMapping("/board/{id}/delete")
    public String delete (@PathVariable int id, HttpServletRequest request){

        //  1. 인증 X
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null) {   //  error : 401
            return "redirect:/loginForm";
        }
        //  2. 권한 X
        Board board = boardRepository.findById(id);
        if (board.getUserId() != sessionUser.getId()){
            request.setAttribute("status", 403);
            request.setAttribute("msg", "권한이 없습니다.");
            return "error/40x";
        }

        boardRepository.deleteById(id);

        return "redirect:/";
    }

    @PostMapping("/board/save")
    public String save(BoardRequest.SaveDTO requestDTO, HttpServletRequest request){
        //  0. 인증 체크
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null){
            return "redirect:/loginForm";
        }

        //  1. body data 받기
        System.out.println(requestDTO);

        if (requestDTO.getTitle().length() > 30){
            request.setAttribute("status", 400);
            request.setAttribute("msg", "글 제목의 길이가 30자를 초과해서는 안돼");
            return "error/40x";     //  BadRequest : 잘못된 요청
        }

        //  3. Model 위임
        //  INSERT INTO board_tb(title, content, user_idm created_at) VALUES(?,?,?,now());
        boardRepository.save(requestDTO, sessionUser.getId());   //  title과 content뿐, 나머지는 session에서 가져오기

        return "redirect:/";
    }

    //  게시글 작성
    @GetMapping("/board/saveForm")
    public String saveForm() {

        //  Session 영역에 sessionUser 키값이 user객체에 있는지 체크
        User sessionUser = (User) session.getAttribute("sessionUser");

        if (sessionUser == null){
            return "redirect:/loginForm";
        }
        return "board/saveForm";
    }

    @GetMapping("/board/{id}")
    public String detail(@PathVariable int id, HttpServletRequest request) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        BoardResponse.DetailDTO boardDTO = boardRepository.findByIdWithUser(id);
        boardDTO.isBoardOwner(sessionUser);

        List<BoardResponse.ReplyDTO> replyDTOList = replyRepository.findByBoardId(id, sessionUser);

        request.setAttribute("board", boardDTO);
        request.setAttribute("replyList", replyDTOList);

        System.out.println(replyDTOList);

        return "board/detail";
    }
}