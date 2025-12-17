package k23cnt3.nguyencongtung.project3.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class NctGlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ModelAndView nctHandleException(HttpServletRequest request, Exception ex) {
        System.err.println("=== NCT ERROR DEBUG ===");
        System.err.println("URL: " + request.getRequestURL());
        System.err.println("Error: " + ex.getMessage());
        ex.printStackTrace();
        System.err.println("======================");

        ModelAndView mav = new ModelAndView();
        mav.addObject("error", ex.getMessage());
        mav.addObject("url", request.getRequestURL());
        mav.setViewName("error");
        return mav;
    }
}