package shop.mtcoding.blog.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import shop.mtcoding.blog._core.util.util.Script;


@RequiredArgsConstructor // final이 붙은 애들에 대한 생성자를 만들어줌
@Controller
public class UserController {

    // 자바는 final 변수는 반드시 초기화가 되어야함.
    private final UserRepository userRepository;
    private final HttpSession session;

    @PostMapping("/user/update")
    public String update( UserRequest.UpdateDTO requestDTO) {
        // 1. 인증 체크
        User sessionUser = (User) session.getAttribute("sessionUser") ;
        if(sessionUser == null){
            return "redirect:/loginForm";
        }
        // 2. 권한 체크
        User user = userRepository.findById(sessionUser.getId());

        if(user == null){
            return "error/400";
        }

        // 3. 핵심 로직
        // update user_tb set password=? where id=?
        userRepository.passwordUpdate(requestDTO, user.getId());
        user.setPassword(requestDTO.getPassword());
        session.setAttribute("sessionUser", user);
        System.out.println("수정 완료! 다시 로그인 하세요");
        session.invalidate();
        return "redirect:/loginForm";
    }
    @GetMapping("/user/updateForm")
    public String updateForm(HttpServletRequest request) {
        // 1. 인증 체크
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null) {
            return "redirect:/loginForm";
        }

        User user = userRepository.findById(sessionUser.getId());

        request.setAttribute("user", user);

        return "user/updateForm";
    }


    // 왜 조회인데 post임? 민간함 정보는 body로 보낸다.
    // 로그인만 예외로 select인데 post 사용
    // select * from user_tb where username=? and password=?
    @PostMapping("/login")
    public String login(UserRequest.LoginDTO requestDTO){


        System.out.println(requestDTO); // toString -> @Data

        if(requestDTO.getUsername().length() < 3){
            return "error/400"; // ViewResolver 설정이 되어 있음. (앞 경로, 뒤 경로)
        }

        User user = userRepository.findByUsernameAndPassword(requestDTO);
        session.setAttribute("sessionUser", user); // 락카에 담음 (StateFul)

        return "redirect:/"; // 컨트롤러가 존재하면 무조건 redirect 외우기
    }

    @PostMapping("/join")
    public String join(UserRequest.JoinDTO requestDTO){ //responseBody를 붙이면 이 메세지 그대로 응답한다.
        System.out.println(requestDTO);

        try{
            userRepository.save(requestDTO); // 모델에 위임하기
        }catch (Exception e){
            throw new RuntimeException("아이디가 중복되었어요");
        }

        return "redirect:/loginForm";
    }

    @GetMapping("/joinForm")
    public String joinForm() {
        return "user/joinForm";
    }

    @GetMapping("/loginForm")
    public String loginForm() {
        return "user/loginForm";
    }



    @GetMapping("/logout")
    public String logout() {
        session.invalidate();
        return "redirect:/";
    }
}