package k23cnt3.nguyencongtung.project3.controller.user;

import k23cnt3.nguyencongtung.project3.entity.NctOrder;
import k23cnt3.nguyencongtung.project3.entity.NctOrderItem;
import k23cnt3.nguyencongtung.project3.entity.NctProduct;
import k23cnt3.nguyencongtung.project3.entity.NctUser;
import k23cnt3.nguyencongtung.project3.service.NctCartService;
import k23cnt3.nguyencongtung.project3.service.NctOrderService;
import k23cnt3.nguyencongtung.project3.service.NctProductService;
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
    private final NctCartService nctCartService;
    private final NctProductService nctProductService;

    @Autowired
    public NctOrderController(NctOrderService nctOrderService, NctUserService nctUserService, NctCartService nctCartService,       // Inject thêm
                              NctProductService nctProductService) {
        this.nctOrderService = nctOrderService;
        this.nctUserService = nctUserService;
        this.nctCartService = nctCartService;
        this.nctProductService = nctProductService;
    }

    @GetMapping("/orders")
    public String viewOrders(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/login";
        NctUser nctUser = nctUserService.findByUsername(userDetails.getUsername());
        List<NctOrder> orders = nctOrderService.nctGetUserOrders(nctUser);
        model.addAttribute("nctOrders", orders);
        model.addAttribute("nctPageTitle", "Lịch sử đơn hàng");
        return "user/nct-order-list";
    }

    @GetMapping("/orders/{orderId}")
    public String viewOrderDetail(@PathVariable Long orderId, Model model,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  RedirectAttributes redirectAttributes){
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

    // Dưới đây là logic Hủy đơn (giữ lại record để có thể Mua lại)
    @PostMapping("/orders/cancel/{orderId}")
    public String cancelOrder(@PathVariable Long orderId,
                              RedirectAttributes redirectAttributes,
                              @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/login";
        try {
            NctUser nctUser = nctUserService.findByUsername(userDetails.getUsername());
            NctOrder order = nctOrderService.nctGetOrderById(orderId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng."));

            if (!order.getNctUser().getNctUserId().equals(nctUser.getNctUserId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không có quyền.");
                return "redirect:/orders";
            }

            // Chỉ thực hiện HỦY, KHÔNG XÓA RECORD
            nctOrderService.nctCancelOrder(orderId);

            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đơn hàng thành công.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/orders/" + orderId; // Load lại trang chi tiết
    }

    // --- LOGIC MỚI: MUA LẠI ĐƠN HÀNG ---
    @PostMapping("/orders/buy-again/{orderId}")
    public String buyAgain(@PathVariable Long orderId,
                           RedirectAttributes redirectAttributes,
                           @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        try {
            NctUser currentUser = nctUserService.findByUsername(userDetails.getUsername());
            NctOrder oldOrder = nctOrderService.nctGetOrderById(orderId)
                    .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại."));

            if (!oldOrder.getNctUser().getNctUserId().equals(currentUser.getNctUserId())) {
                throw new RuntimeException("Bạn không có quyền thao tác.");
            }

            // Duyệt qua từng sản phẩm trong đơn cũ
            int itemsAdded = 0;
            for (NctOrderItem item : oldOrder.getNctOrderItems()) {
                NctProduct product = item.getNctProduct();

                // Kiểm tra xem sản phẩm còn kinh doanh và còn hàng không
                if (product.getNctStockQuantity() >= 1) { // Hoặc kiểm tra >= item.getNctQuantity() nếu muốn mua đúng số lượng cũ
                    // Gọi hàm thêm vào giỏ hàng (Giả sử bạn đã có hàm này trong NctCartService)
                    // Nếu chưa có, bạn cần viết thêm hàm nctAddProductToCart(user, product, quantity)
                    nctCartService.nctAddProductToCart(currentUser, product.getNctProductId(), 1);
                    itemsAdded++;
                }
            }

            if (itemsAdded > 0) {
                redirectAttributes.addFlashAttribute("nctSuccess", "Đã thêm các sản phẩm vào giỏ hàng. Vui lòng kiểm tra lại.");
                return "redirect:/cart";
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Các sản phẩm trong đơn hàng này hiện đã hết hàng.");
                return "redirect:/orders/" + orderId;
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/orders";
        }
    }

}
