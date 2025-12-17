package k23cnt3.nguyencongtung.project3.controller.user;

import k23cnt3.nguyencongtung.project3.entity.NctCartItem;
import k23cnt3.nguyencongtung.project3.entity.NctUser;
import k23cnt3.nguyencongtung.project3.service.NctCartService;
import k23cnt3.nguyencongtung.project3.service.NctUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cart")
public class NctCartController {

    private final NctCartService nctCartService;
    private final NctUserService nctUserService;

    @Autowired
    public NctCartController(NctCartService nctCartService, NctUserService nctUserService) {
        this.nctCartService = nctCartService;
        this.nctUserService = nctUserService;
    }

    private NctUser getCurrentUser(UserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }
        return nctUserService.nctFindByUsername(userDetails.getUsername()).orElse(null);
    }

    @GetMapping
    public String nctShowCart(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        NctUser currentUser = getCurrentUser(userDetails);
        if (currentUser == null) {
            return "redirect:/login";
        }
        List<NctCartItem> cartItems = nctCartService.nctGetCartItems(currentUser);
        double total = nctCartService.nctCalculateCartTotal(currentUser);

        model.addAttribute("nctCartItems", cartItems);
        model.addAttribute("nctTotal", total);
        model.addAttribute("nctPageTitle", "Giỏ hàng - UMACT Store");
        // You will need to create a 'user/nct-cart.html' view for this
        return "user/nct-cart";
    }

    @PostMapping("/add")
    public ResponseEntity<?> nctAddToCart(@RequestParam("productId") Long productId,
                                          @RequestParam(value = "quantity", defaultValue = "1") Integer quantity,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        NctUser currentUser = getCurrentUser(userDetails);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Vui lòng đăng nhập để thực hiện chức năng này."));
        }

        try {
            nctCartService.nctAddToCart(currentUser, productId, quantity);
            Integer cartCount = nctCartService.nctGetCartItemCount(currentUser);
            return ResponseEntity.ok(Map.of("success", true, "message", "Thêm vào giỏ hàng thành công!", "cartCount", cartCount));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/update")
    public String nctUpdateCart(@RequestParam("cartItemId") Long cartItemId,
                                @RequestParam("quantity") Integer quantity,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        NctUser currentUser = getCurrentUser(userDetails);
        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            nctCartService.nctUpdateCartItem(currentUser, cartItemId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Số lượng sản phẩm đã được cập nhật.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/remove/{cartItemId}")
    public String nctRemoveFromCart(@PathVariable("cartItemId") Long cartItemId,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    RedirectAttributes redirectAttributes) {
        NctUser currentUser = getCurrentUser(userDetails);
        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            nctCartService.nctRemoveFromCart(currentUser, cartItemId);
            redirectAttributes.addFlashAttribute("successMessage", "Sản phẩm đã được xóa khỏi giỏ hàng.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/cart";
    }
}
