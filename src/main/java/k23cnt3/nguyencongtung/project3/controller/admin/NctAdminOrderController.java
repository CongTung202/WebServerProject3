package k23cnt3.nguyencongtung.project3.controller.admin;

import k23cnt3.nguyencongtung.project3.entity.NctOrder;
import k23cnt3.nguyencongtung.project3.service.NctOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/orders")
public class NctAdminOrderController {

    private final NctOrderService nctOrderService;

    @Autowired
    public NctAdminOrderController(NctOrderService nctOrderService) {
        this.nctOrderService = nctOrderService;
    }

    @GetMapping
    public String listOrders(Model model,
                             @RequestParam(name = "status", required = false) String statusStr,
                             @RequestParam(name = "keyword", required = false) String keyword,
                             @RequestParam(name = "page", defaultValue = "1") int page,
                             @RequestParam(name = "size", defaultValue = "10") int size) {

        NctOrder.NctOrderStatus status = null;
        if (statusStr != null && !statusStr.isEmpty()) {
            try {
                status = NctOrder.NctOrderStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid status, ignore it
            }
        }

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("nctCreatedAt").descending());
        Page<NctOrder> orderPage = nctOrderService.nctFindPaginated(status, keyword, pageable);

        model.addAttribute("nctOrderPage", orderPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", statusStr);

        // Status counts for the cards
        model.addAttribute("nctPendingCount", nctOrderService.nctGetOrderCountByStatus(NctOrder.NctOrderStatus.PENDING));
        model.addAttribute("nctConfirmedCount", nctOrderService.nctGetOrderCountByStatus(NctOrder.NctOrderStatus.CONFIRMED));
        model.addAttribute("nctShippingCount", nctOrderService.nctGetOrderCountByStatus(NctOrder.NctOrderStatus.SHIPPING));
        model.addAttribute("nctDeliveredCount", nctOrderService.nctGetOrderCountByStatus(NctOrder.NctOrderStatus.DELIVERED));
        model.addAttribute("nctCancelledCount", nctOrderService.nctGetOrderCountByStatus(NctOrder.NctOrderStatus.CANCELLED));

        return "admin/orders/nct-order-list";
    }

    @GetMapping("/view/{id}")
    public String viewOrder(@PathVariable("id") Long orderId, Model model) {
        Optional<NctOrder> orderOpt = nctOrderService.nctGetOrderById(orderId);
        if (orderOpt.isPresent()) {
            model.addAttribute("nctOrder", orderOpt.get());
            model.addAttribute("nctPageTitle", "Chi tiết Đơn hàng #" + orderId);
            return "admin/orders/nct-order-view";
        }
        return "redirect:/admin/orders";
    }

    @GetMapping("/edit/{id}")
    public String editOrderForm(@PathVariable("id") Long orderId, Model model) {
        Optional<NctOrder> orderOpt = nctOrderService.nctGetOrderById(orderId);
        if (orderOpt.isPresent()) {
            NctOrder order = orderOpt.get();
            if (!order.isNctEditable()) {
                return "redirect:/admin/orders/view/" + orderId;
            }
            model.addAttribute("nctOrder", order);
            model.addAttribute("nctPageTitle", "Chỉnh sửa Đơn hàng #" + orderId);
            return "admin/orders/nct-order-edit";
        }
        return "redirect:/admin/orders";
    }

    @PostMapping("/edit/{id}")
    public String updateOrder(@PathVariable("id") Long orderId, @ModelAttribute("nctOrder") NctOrder orderDetails, RedirectAttributes redirectAttributes) {
        try {
            nctOrderService.nctUpdateOrder(orderId, orderDetails);
            redirectAttributes.addFlashAttribute("nctSuccess", "Đơn hàng đã được cập nhật thành công.");
            return "redirect:/admin/orders/view/" + orderId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("nctError", "Lỗi: " + e.getMessage());
            return "redirect:/admin/orders/edit/" + orderId;
        }
    }

    @PostMapping("/update-status/{id}")
    public String updateOrderStatus(@PathVariable("id") Long orderId,
                                    @RequestParam("status") NctOrder.NctOrderStatus status,
                                    RedirectAttributes redirectAttributes) {
        try {
            nctOrderService.nctUpdateOrderStatus(orderId, status);
            redirectAttributes.addFlashAttribute("nctSuccess", "Cập nhật trạng thái đơn hàng thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("nctError", "Lỗi khi cập nhật trạng thái: " + e.getMessage());
        }
        return "redirect:/admin/orders/view/" + orderId;
    }

    @GetMapping("/cancel/{id}")
    public String cancelOrderForm(@PathVariable("id") Long orderId, Model model, RedirectAttributes redirectAttributes) {
        Optional<NctOrder> orderOpt = nctOrderService.nctGetOrderById(orderId);
        if (orderOpt.isPresent()) {
            NctOrder order = orderOpt.get();
            if (!order.isNctCancellable()) {
                redirectAttributes.addFlashAttribute("nctError", "Không thể hủy đơn hàng ở trạng thái này.");
                return "redirect:/admin/orders/view/" + orderId;
            }
            model.addAttribute("nctOrder", order);
            model.addAttribute("nctPageTitle", "Hủy Đơn hàng #" + orderId);
            return "admin/orders/nct-order-cancel";
        }
        return "redirect:/admin/orders";
    }

    @PostMapping("/cancel/{id}")
    public String cancelOrder(@PathVariable("id") Long orderId, RedirectAttributes redirectAttributes) {
        try {
            nctOrderService.nctCancelOrder(orderId);
            redirectAttributes.addFlashAttribute("nctSuccess", "Đơn hàng đã được hủy thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("nctError", "Lỗi khi hủy đơn hàng: " + e.getMessage());
        }
        return "redirect:/admin/orders/view/" + orderId;
    }

    @GetMapping("/delete/{id}")
    public String deleteOrderForm(@PathVariable("id") Long orderId, Model model) {
        Optional<NctOrder> orderOpt = nctOrderService.nctGetOrderById(orderId);
        if (orderOpt.isPresent()) {
            model.addAttribute("nctOrder", orderOpt.get());
            model.addAttribute("nctPageTitle", "Xóa Đơn hàng #" + orderId);
            return "admin/orders/nct-order-delete";
        }
        return "redirect:/admin/orders";
    }

    @PostMapping("/delete/{id}")
    public String deleteOrder(@PathVariable("id") Long orderId, RedirectAttributes redirectAttributes) {
        try {
            nctOrderService.nctDeleteOrder(orderId);
            redirectAttributes.addFlashAttribute("nctSuccess", "Đơn hàng đã được xóa vĩnh viễn.");
            return "redirect:/admin/orders";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("nctError", "Lỗi khi xóa đơn hàng: " + e.getMessage());
            return "redirect:/admin/orders/view/" + orderId;
        }
    }
}
