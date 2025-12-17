package k23cnt3.nguyencongtung.project3.controller.user;

import k23cnt3.nguyencongtung.project3.entity.NctCartItem;
import k23cnt3.nguyencongtung.project3.entity.NctOrder;
import k23cnt3.nguyencongtung.project3.entity.NctProduct;
import k23cnt3.nguyencongtung.project3.entity.NctUser;
import k23cnt3.nguyencongtung.project3.service.NctCartService;
import k23cnt3.nguyencongtung.project3.service.NctOrderService;
import k23cnt3.nguyencongtung.project3.service.NctProductService;
import k23cnt3.nguyencongtung.project3.service.NctUserService;
import k23cnt3.nguyencongtung.project3.service.ZaloPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    private final ZaloPayService zaloPayService;

    @Autowired
    public NctCheckoutController(NctCartService nctCartService,
                                 NctUserService nctUserService,
                                 NctOrderService nctOrderService,
                                 NctProductService nctProductService,
                                 ZaloPayService zaloPayService) {
        this.nctCartService = nctCartService;
        this.nctUserService = nctUserService;
        this.nctOrderService = nctOrderService;
        this.nctProductService = nctProductService;
        this.zaloPayService = zaloPayService;
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
        if (currentUser == null) {
            return "redirect:/login";
        }

        String viewName = "user/nct-checkout";

        try {
            // 1. Tính toán tổng tiền
            double amount = 0;
            if (isBuyNow) {
                if (productId == null || quantity == null) throw new RuntimeException("Thiếu thông tin sản phẩm mua ngay.");
                NctProduct product = nctProductService.nctGetProductById(productId).orElseThrow();
                amount = product.getNctPrice().doubleValue() * quantity;
            } else {
                amount = nctCartService.nctCalculateCartTotal(currentUser);
            }

            // 2. KIỂM TRA PHƯƠNG THỨC THANH TOÁN
            if (nctOrderDetails.getNctPaymentMethod() == NctOrder.NctPaymentMethod.BANKING) {

                // B1: Lưu đơn hàng trước để có ID
                NctOrder savedOrder; // Khai báo biến hứng đơn hàng đã lưu
                if (isBuyNow) {
                    savedOrder = nctOrderService.nctCreateOrderFromSingleProduct(currentUser, productId, quantity,
                            nctOrderDetails.getNctShippingAddress(), nctOrderDetails.getNctPhone(), NctOrder.NctPaymentMethod.BANKING);
                } else {
                    savedOrder = nctOrderService.nctCreateOrderFromCart(currentUser, nctOrderDetails);
                }

                // B2: Tạo đường dẫn quay về (Trang chi tiết đơn hàng)
                // Lưu ý: Thay "localhost:8080" bằng domain thật khi deploy
                String returnUrl = "http://localhost:8080/payment/callback?nctOrderId=" + savedOrder.getNctOrderId();

                // B3: Gọi ZaloPay Service kèm returnUrl
                Map<String, Object> zaloPayResult = zaloPayService.createOrder((int) amount, returnUrl);

                if (zaloPayResult.containsKey("orderUrl")) {
                    return "redirect:" + zaloPayResult.get("orderUrl").toString();
                } else {
                    throw new RuntimeException("Lỗi tạo link thanh toán ZaloPay: " + zaloPayResult.get("error"));
                }
            }

            // 3. XỬ LÝ THANH TOÁN THƯỜNG (COD)
            if (isBuyNow) {
                nctOrderService.nctCreateOrderFromSingleProduct(currentUser, productId, quantity,
                        nctOrderDetails.getNctShippingAddress(), nctOrderDetails.getNctPhone(), nctOrderDetails.getNctPaymentMethod());
            } else {
                nctOrderService.nctCreateOrderFromCart(currentUser, nctOrderDetails);
            }

            model.addAttribute("notificationStatus", "success");
            model.addAttribute("notificationMessage", "Đặt hàng thành công!");
            model.addAttribute("redirectUrl", "/orders"); // Chuyển hướng về danh sách đơn hàng

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("notificationStatus", "failure");
            model.addAttribute("notificationMessage", "LỖI: " + e.getMessage());
            model.addAttribute("redirectUrl", null);
        }

        // --- NẠP LẠI DỮ LIỆU ĐỂ HIỂN THỊ GIAO DIỆN KHI CÓ LỖI HOẶC LOAD LẠI ---
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
            } catch (Exception ex) {
                model.addAttribute("nctCartItems", new ArrayList<>());
                model.addAttribute("nctTotal", 0);
            }
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
}