package k23cnt3.nguyencongtung.project3.controller.user;

import k23cnt3.nguyencongtung.project3.entity.NctUser;
import k23cnt3.nguyencongtung.project3.service.NctUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class NctProfileController {

    private final NctUserService nctUserService;

    @Autowired
    public NctProfileController(NctUserService nctUserService) {
        this.nctUserService = nctUserService;
    }

    private NctUser getAuthenticatedUser(Authentication authentication) {
        String username = authentication.getName();
        return nctUserService.nctFindByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @GetMapping
    public String nctProfilePage(Model model, Authentication authentication) {
        NctUser nctCurrentUser = getAuthenticatedUser(authentication);
        model.addAttribute("nctUser", nctCurrentUser);
        model.addAttribute("nctPageTitle", "Thông tin tài khoản - UMACT Store");
        return "user/nct-profile";
    }

    @PostMapping("/update")
    public String nctUpdateProfile(@ModelAttribute NctUser nctUpdatedUser, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            NctUser nctCurrentUser = getAuthenticatedUser(authentication);
            nctCurrentUser.setNctFullName(nctUpdatedUser.getNctFullName());
            nctCurrentUser.setNctPhone(nctUpdatedUser.getNctPhone());
            nctCurrentUser.setNctAddress(nctUpdatedUser.getNctAddress());
            nctUserService.nctSaveUser(nctCurrentUser);
            redirectAttributes.addFlashAttribute("nctSuccess", "Cập nhật thông tin thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("nctError", "Đã xảy ra lỗi khi cập nhật.");
        }
        return "redirect:/profile";
    }

    @GetMapping("/change-password")
    public String nctChangePasswordPage(Model model) {
        model.addAttribute("nctPageTitle", "Đổi mật khẩu - UMACT Store");
        return "auth/nct-change-password";
    }

    @PostMapping("/change-password")
    public String nctChangePassword(
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("nctError", "Mật khẩu mới không khớp.");
            return "redirect:/profile/change-password";
        }

        try {
            NctUser nctCurrentUser = getAuthenticatedUser(authentication);
            nctUserService.nctChangePassword(nctCurrentUser, currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("nctSuccess", "Đổi mật khẩu thành công!");
            return "redirect:/profile";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("nctError", e.getMessage());
            return "redirect:/profile/change-password";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("nctError", "Đã xảy ra lỗi không mong muốn.");
            return "redirect:/profile/change-password";
        }
    }
}