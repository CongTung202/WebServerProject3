package k23cnt3.nguyencongtung.project3.controller.user;

import k23cnt3.nguyencongtung.project3.entity.NctOrder;
import k23cnt3.nguyencongtung.project3.entity.NctUser;
import k23cnt3.nguyencongtung.project3.service.NctOrderService;
import k23cnt3.nguyencongtung.project3.service.NctUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class NctOrderController {

    private final NctOrderService nctOrderService;
    private final NctUserService nctUserService;

    @Autowired
    public NctOrderController(NctOrderService nctOrderService, NctUserService nctUserService) {
        this.nctOrderService = nctOrderService;
        this.nctUserService = nctUserService;
    }

    @GetMapping("/orders")
    public String viewOrders(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        NctUser nctUser = nctUserService.findByUsername(userDetails.getUsername());
        List<NctOrder> orders = nctOrderService.nctGetUserOrders(nctUser);
        model.addAttribute("nctOrders", orders);
        model.addAttribute("nctPageTitle", "Lịch sử đơn hàng");
        return "user/nct-order-list"; // Changed from "user/nct-order-history"
    }

    @GetMapping("/orders/{orderId}")
    public String viewOrderDetail(@PathVariable Long orderId, Model model,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        NctUser currentUser = nctUserService.findByUsername(userDetails.getUsername());
        Optional<NctOrder> orderOpt = nctOrderService.nctGetOrderById(orderId);

        if (orderOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng.");
            return "redirect:/orders";
        }

        NctOrder order = orderOpt.get();
        if (!order.getNctUser().getNctUserId().equals(currentUser.getNctUserId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền xem đơn hàng này.");
            return "redirect:/orders";
        }

        model.addAttribute("nctOrder", order);
        model.addAttribute("nctPageTitle", "Chi tiết đơn hàng #" + order.getNctOrderId());
        return "user/nct-order-detail";
    }

    @PostMapping("/orders/cancel/{orderId}")
    public String cancelOrder(@PathVariable Long orderId, RedirectAttributes redirectAttributes, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        try {
            NctUser nctUser = nctUserService.findByUsername(userDetails.getUsername());
            NctOrder order = nctOrderService.nctGetOrderById(orderId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng."));

            if (!order.getNctUser().getNctUserId().equals(nctUser.getNctUserId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền hủy đơn hàng này.");
                return "redirect:/orders";
            }

            nctOrderService.nctCancelOrder(orderId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đơn hàng #" + orderId + " thành công.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi hủy đơn hàng: " + e.getMessage());
        }
        return "redirect:/orders";
    }
}
