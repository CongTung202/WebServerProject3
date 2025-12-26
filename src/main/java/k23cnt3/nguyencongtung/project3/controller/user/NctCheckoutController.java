package k23cnt3.nguyencongtung.project3.controller.user;

import k23cnt3.nguyencongtung.project3.entity.NctCartItem;
import k23cnt3.nguyencongtung.project3.entity.NctOrder;
import k23cnt3.nguyencongtung.project3.entity.NctProduct;
import k23cnt3.nguyencongtung.project3.entity.NctUser;
import k23cnt3.nguyencongtung.project3.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
public class NctCheckoutController {

    private final NctCartService nctCartService;
    private final NctUserService nctUserService;
    private final NctOrderService nctOrderService;
    private final NctProductService nctProductService;
    private final NctPayOSService nctPayOSService;

    @Autowired
    public NctCheckoutController(NctCartService nctCartService,
                                 NctUserService nctUserService,
                                 NctOrderService nctOrderService,
                                 NctProductService nctProductService,
                                 NctPayOSService nctPayOSService) {
        this.nctCartService = nctCartService;
        this.nctUserService = nctUserService;
        this.nctOrderService = nctOrderService;
        this.nctProductService = nctProductService;
        this.nctPayOSService = nctPayOSService;
    }

    private NctUser getCurrentUser(UserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }
        return nctUserService.nctFindByUsername(userDetails.getUsername()).orElse(null);
    }

    @GetMapping("/checkout")
    public String nctShowCheckoutForm(@AuthenticationPrincipal UserDetails userDetails, Model model, RedirectAttributes redirectAttributes) {
        NctUser currentUser = getCurrentUser(userDetails);
        if (currentUser == null) {
            return "redirect:/login";
        }

        List<NctCartItem> cartItems = nctCartService.nctGetCartItems(currentUser);
        if (cartItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("nctError", "Giỏ hàng của bạn đang trống.");
            return "redirect:/cart";
        }

        double total = nctCartService.nctCalculateCartTotal(currentUser);
        NctOrder order = new NctOrder();
        order.setNctShippingAddress(currentUser.getNctAddress());
        order.setNctPhone(currentUser.getNctPhone());

        // SỬA LỖI 1: Gán bằng Enum, không phải String
        order.setNctPaymentMethod(NctOrder.NctPaymentMethod.COD);

        model.addAttribute("nctCartItems", cartItems);
        model.addAttribute("nctTotal", total);
        model.addAttribute("nctOrder", order);
        model.addAttribute("isBuyNow", false);
        model.addAttribute("nctPageTitle", "Thanh toán");
        return "user/nct-checkout";
    }

    @GetMapping("/checkout/buy-now")
    public String nctShowBuyNowForm(@RequestParam("productId") Long productId,
                                    @RequestParam(value = "quantity", defaultValue = "1") Integer quantity,
                                    @AuthenticationPrincipal UserDetails userDetails, Model model, RedirectAttributes redirectAttributes) {
        NctUser currentUser = getCurrentUser(userDetails);
        if (currentUser == null) {
            return "redirect:/login";
        }

        NctProduct product = nctProductService.nctGetProductById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        if (!nctProductService.nctIsProductInStock(productId, quantity)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Sản phẩm không đủ số lượng trong kho.");
            return "redirect:/products/" + productId;
        }

        NctCartItem tempCartItem = new NctCartItem();
        tempCartItem.setNctProduct(product);
        tempCartItem.setNctQuantity(quantity);

        double total = product.getNctPrice().doubleValue() * quantity;

        NctOrder order = new NctOrder();
        order.setNctShippingAddress(currentUser.getNctAddress());
        order.setNctPhone(currentUser.getNctPhone());

        // SỬA LỖI 2: Gán bằng Enum
        order.setNctPaymentMethod(NctOrder.NctPaymentMethod.COD);

        model.addAttribute("nctCartItems", Collections.singletonList(tempCartItem));
        model.addAttribute("nctTotal", total);
        model.addAttribute("nctOrder", order);
        model.addAttribute("isBuyNow", true);
        model.addAttribute("buyNowProductId", productId);
        model.addAttribute("buyNowQuantity", quantity);
        model.addAttribute("nctPageTitle", "Thanh toán Mua ngay");

        return "user/nct-checkout";
    }

    @PostMapping("/checkout")
    public String nctProcessCheckout(@ModelAttribute("nctOrder") NctOrder nctOrderDetails,
                                     @RequestParam(name = "isBuyNow", defaultValue = "false") boolean isBuyNow,
                                     @RequestParam(name = "productId", required = false) Long productId,
                                     @RequestParam(name = "quantity", required = false) Integer quantity,
                                     @AuthenticationPrincipal UserDetails userDetails,
                                     Model model) {

        NctUser currentUser = getCurrentUser(userDetails);
        if (currentUser == null) return "redirect:/login";

        String viewName = "user/nct-checkout";

        try {
            // 1. Tính toán tổng tiền (như cũ)
            double amount = 0;
            if (isBuyNow) {
                if (productId == null || quantity == null) throw new RuntimeException("Thiếu thông tin sản phẩm mua ngay.");
                NctProduct product = nctProductService.nctGetProductById(productId).orElseThrow();
                amount = product.getNctPrice().doubleValue() * quantity;
            } else {
                amount = nctCartService.nctCalculateCartTotal(currentUser);
            }

            // 2. LƯU ĐƠN HÀNG VÀO DATABASE TRƯỚC (Trạng thái mặc định là PENDING/CHỜ DUYỆT)
            NctOrder savedOrder;
            if (isBuyNow) {
                savedOrder = nctOrderService.nctCreateOrderFromSingleProduct(currentUser, productId, quantity,
                        nctOrderDetails.getNctShippingAddress(), nctOrderDetails.getNctPhone(), nctOrderDetails.getNctPaymentMethod());
            } else {
                savedOrder = nctOrderService.nctCreateOrderFromCart(currentUser, nctOrderDetails);
            }

            // Cập nhật lại tổng tiền vào object order để chắc chắn Service PayOS lấy đúng giá
            //savedOrder.setNctTotalAmount(amount);
            // Nếu entity của bạn dùng BigDecimal cho amount thì set:
            savedOrder.setNctTotalAmount(BigDecimal.valueOf(amount));

            // --- BẮT ĐẦU THAY ĐỔI TẠI ĐÂY ---

            // 3. KIỂM TRA PHƯƠNG THỨC THANH TOÁN
            // Nếu user chọn BANKING -> Gọi PayOS (Thay thế ZaloPay)
            if (nctOrderDetails.getNctPaymentMethod() == NctOrder.NctPaymentMethod.BANKING) {

                // Gọi Service PayOS lấy link thanh toán
                String checkoutUrl = nctPayOSService.createPaymentLink(savedOrder);

                // Chuyển hướng người dùng sang trang của PayOS
                return "redirect:" + checkoutUrl;
            }

            // 4. NẾU LÀ COD (Thanh toán khi nhận hàng)
            // Đơn hàng đã được lưu ở bước 2 rồi, chỉ cần báo thành công
            model.addAttribute("notificationStatus", "success");
            model.addAttribute("notificationMessage", "Đặt hàng thành công!");
            model.addAttribute("redirectUrl", "/orders");

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("notificationStatus", "failure");
            model.addAttribute("notificationMessage", "LỖI: " + e.getMessage());
            model.addAttribute("redirectUrl", null);

            // Logic load lại dữ liệu cart khi lỗi (như cũ của bạn)
            if (isBuyNow && productId != null) {
                try {
                    NctProduct product = nctProductService.nctGetProductById(productId).orElse(null);
                    if (product != null) {
                        NctCartItem tempCartItem = new NctCartItem();
                        tempCartItem.setNctProduct(product);
                        tempCartItem.setNctQuantity(quantity != null ? quantity : 1);
                        double total = product.getNctPrice().doubleValue() * tempCartItem.getNctQuantity();
                        model.addAttribute("nctCartItems", Collections.singletonList(tempCartItem));
                        model.addAttribute("nctTotal", total);
                        model.addAttribute("buyNowProductId", productId);
                        model.addAttribute("buyNowQuantity", quantity);
                    }
                } catch (Exception ex) {}
            } else {
                List<NctCartItem> cartItems = nctCartService.nctGetCartItems(currentUser);
                double total = nctCartService.nctCalculateCartTotal(currentUser);
                model.addAttribute("nctCartItems", cartItems);
                model.addAttribute("nctTotal", total);
            }
            model.addAttribute("nctPageTitle", isBuyNow ? "Thanh toán Mua ngay" : "Thanh toán");
            model.addAttribute("isBuyNow", isBuyNow);
            model.addAttribute("nctOrder", nctOrderDetails);
            return viewName;
        }
        return viewName;
    }
}