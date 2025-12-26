package k23cnt3.nguyencongtung.project3.controller.auth;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MyErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());

            if (statusCode == 404) {
                return "error/404";
            }

            // --- THÊM ĐOẠN NÀY ---
            if (statusCode == 403) {
                return "error/403"; // Trả về file templates/error/403.html
            }
            // ---------------------

            if (statusCode == 500) {
                return "error/500";
            }
        }
        return "error/error";
    }
}