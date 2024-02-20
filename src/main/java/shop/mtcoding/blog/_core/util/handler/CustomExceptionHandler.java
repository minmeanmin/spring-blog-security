package shop.mtcoding.blog._core.util.handler;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import shop.mtcoding.blog._core.util.util.Script;


@ControllerAdvice // 응답 에러 컨트롤러(view리턴)
public class CustomExceptionHandler {

    @ExceptionHandler(Exception.class)
    public @ResponseBody String error1(Exception e){
        return Script.back(e.getMessage());
    }
}
