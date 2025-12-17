package k23cnt3.nguyencongtung.project3.controller.auth;

import k23cnt3.nguyencongtung.project3.entity.NctUser;
import k23cnt3.nguyencongtung.project3.service.NctUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class NctAuthController {

    private final NctUserService nctUserService;

    @Autowired
    public NctAuthController(NctUserService nctUserService) {
        this.nctUserService = nctUserService;
    }

    @GetMapping("/login")
    public String nctLoginPage(Model model) {
        model.addAttribute("nctPageTitle", "Đăng nhập - NCT Otaku Store");
        return "auth/nct-login";
    }

    @GetMapping("/register")
    public String nctRegisterPage(Model model) {
        model.addAttribute("nctUser", new NctUser());
        model.addAttribute("nctPageTitle", "Đăng ký - NCT Otaku Store");
        return "auth/nct-register";
    }

    @PostMapping("/register")
    public String nctRegisterUser(NctUser nctUser, RedirectAttributes nctRedirectAttributes) {
        try {
            if (nctUserService.nctUsernameExists(nctUser.getNctUsername())) {
                nctRedirectAttributes.addFlashAttribute("nctError", "Tên đăng nhập đã tồn tại");
                return "redirect:/auth/register";
            }

            if (nctUserService.nctEmailExists(nctUser.getNctEmail())) {
                nctRedirectAttributes.addFlashAttribute("nctError", "Email đã tồn tại");
                return "redirect:/auth/register";
            }

            nctUserService.nctRegisterUser(nctUser);
            nctRedirectAttributes.addFlashAttribute("nctSuccess", "Đăng ký thành công! Vui lòng đăng nhập.");
            return "redirect:/auth/login";

        } catch (Exception e) {
            nctRedirectAttributes.addFlashAttribute("nctError", "Đã xảy ra lỗi: " + e.getMessage());
            return "redirect:/auth/register";
        }
    }
}