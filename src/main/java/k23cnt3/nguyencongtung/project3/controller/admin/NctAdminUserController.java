package k23cnt3.nguyencongtung.project3.controller.admin;

import k23cnt3.nguyencongtung.project3.entity.NctUser;
import k23cnt3.nguyencongtung.project3.service.NctUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/users")
public class NctAdminUserController {

    private final NctUserService nctUserService;

    @Autowired
    public NctAdminUserController(NctUserService nctUserService) {
        this.nctUserService = nctUserService;
    }

    @GetMapping
    public String nctUserList(Model model) {
        List<NctUser> nctUsers = nctUserService.nctGetAllUsers();

        // Thống kê
        long nctAdminCount = nctUsers.stream()
                .filter(user -> user.getNctRole() == NctUser.NctRole.ADMIN)
                .count();
        long nctUserCount = nctUsers.stream()
                .filter(user -> user.getNctRole() == NctUser.NctRole.USER)
                .count();

        model.addAttribute("nctUsers", nctUsers);
        model.addAttribute("nctAdminCount", nctAdminCount);
        model.addAttribute("nctUserCount", nctUserCount);
        model.addAttribute("nctPageTitle", "Quản lý Người dùng - Admin");
        return "admin/users/nct-user-list";
    }

    @GetMapping("/view/{id}")
    public String nctViewUser(@PathVariable Long id, Model model) {
        Optional<NctUser> nctUserOpt = nctUserService.nctGetUserById(id);
        if (nctUserOpt.isEmpty()) {
            return "redirect:/admin/users";
        }

        NctUser nctUser = nctUserOpt.get();
        model.addAttribute("nctUser", nctUser);
        model.addAttribute("nctOrderCount", nctUser.getNctOrderCount());
        model.addAttribute("nctPageTitle", "Chi tiết Người dùng - " + nctUser.getNctFullName());
        return "admin/users/nct-user-view";
    }

    @GetMapping("/edit/{id}")
    public String nctShowEditForm(@PathVariable Long id, Model model) {
        Optional<NctUser> nctUserOpt = nctUserService.nctGetUserById(id);
        if (nctUserOpt.isEmpty()) {
            return "redirect:/admin/users";
        }

        model.addAttribute("nctUser", nctUserOpt.get());
        model.addAttribute("nctRoles", NctUser.NctRole.values());
        model.addAttribute("nctPageTitle", "Chỉnh sửa Người dùng - Admin");
        return "admin/users/nct-user-edit";
    }

    @PostMapping("/edit/{id}")
    public String nctUpdateUser(
            @PathVariable Long id,
            @ModelAttribute NctUser nctUser,
            @RequestParam("nctRole") String nctRole,
            RedirectAttributes nctRedirectAttributes) {

        try {
            Optional<NctUser> nctExistingUserOpt = nctUserService.nctGetUserById(id);
            if (nctExistingUserOpt.isEmpty()) {
                nctRedirectAttributes.addFlashAttribute("nctError", "Người dùng không tồn tại!");
                return "redirect:/admin/users";
            }

            NctUser nctExistingUser = nctExistingUserOpt.get();

            // Cập nhật thông tin cơ bản
            nctExistingUser.setNctFullName(nctUser.getNctFullName());
            nctExistingUser.setNctEmail(nctUser.getNctEmail());
            nctExistingUser.setNctPhone(nctUser.getNctPhone());
            nctExistingUser.setNctAddress(nctUser.getNctAddress());

            // Cập nhật role - xử lý enum đúng cách
            try {
                NctUser.NctRole role = NctUser.NctRole.valueOf(nctRole);
                nctExistingUser.setNctRole(role);
            } catch (IllegalArgumentException e) {
                nctRedirectAttributes.addFlashAttribute("nctError", "Vai trò không hợp lệ!");
                return "redirect:/admin/users/edit/" + id;
            }

            nctExistingUser.setNctUpdatedAt(java.time.LocalDateTime.now());

            nctUserService.nctSaveUser(nctExistingUser);
            nctRedirectAttributes.addFlashAttribute("nctSuccess", "Cập nhật người dùng thành công!");

        } catch (Exception e) {
            nctRedirectAttributes.addFlashAttribute("nctError", "Lỗi khi cập nhật người dùng: " + e.getMessage());
        }

        return "redirect:/admin/users";
    }

    @GetMapping("/delete/{id}")
    public String nctShowDeleteForm(@PathVariable Long id, Model model) {
        Optional<NctUser> nctUserOpt = nctUserService.nctGetUserById(id);
        if (nctUserOpt.isEmpty()) {
            return "redirect:/admin/users";
        }

        NctUser nctUser = nctUserOpt.get();
        model.addAttribute("nctUser", nctUser);
        model.addAttribute("nctOrderCount", nctUser.getNctOrderCount());
        model.addAttribute("nctPageTitle", "Xóa Người dùng - " + nctUser.getNctFullName());
        return "admin/users/nct-user-delete";
    }

    @PostMapping("/delete/{id}")
    public String nctDeleteUser(@PathVariable Long id, RedirectAttributes nctRedirectAttributes) {
        try {
            Optional<NctUser> nctUserOpt = nctUserService.nctGetUserById(id);
            if (nctUserOpt.isPresent()) {
                NctUser nctUser = nctUserOpt.get();
                if (nctUser.getNctOrders() != null && !nctUser.getNctOrders().isEmpty()) {
                    nctRedirectAttributes.addFlashAttribute("nctError",
                            "Không thể xóa người dùng vì có " + nctUser.getNctOrders().size() + " đơn hàng liên quan!");
                    return "redirect:/admin/users";
                }

                nctUserService.nctDeleteUser(id);
                nctRedirectAttributes.addFlashAttribute("nctSuccess", "Xóa người dùng " + nctUser.getNctFullName() + " thành công!");
            }
        } catch (Exception e) {
            nctRedirectAttributes.addFlashAttribute("nctError", "Lỗi khi xóa người dùng: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
}