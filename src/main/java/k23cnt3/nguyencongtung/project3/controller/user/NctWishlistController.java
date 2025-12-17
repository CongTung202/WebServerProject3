package k23cnt3.nguyencongtung.project3.controller.user;

import k23cnt3.nguyencongtung.project3.entity.NctUser;
import k23cnt3.nguyencongtung.project3.entity.NctWishlist;
import k23cnt3.nguyencongtung.project3.service.NctUserService;
import k23cnt3.nguyencongtung.project3.service.NctWishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/wishlist")
public class NctWishlistController {

    private final NctWishlistService nctWishlistService;
    private final NctUserService nctUserService;

    @Autowired
    public NctWishlistController(NctWishlistService nctWishlistService, NctUserService nctUserService) {
        this.nctWishlistService = nctWishlistService;
        this.nctUserService = nctUserService;
    }

    private NctUser getCurrentUser(UserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }
        return nctUserService.nctFindByUsername(userDetails.getUsername()).orElse(null);
    }

    @GetMapping
    public String viewWishlist(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        NctUser currentUser = getCurrentUser(userDetails);
        if (currentUser == null) {
            return "redirect:/login";
        }
        List<NctWishlist> wishlistItems = nctWishlistService.nctGetWishlist(currentUser);
        model.addAttribute("wishlistItems", wishlistItems);
        model.addAttribute("nctPageTitle", "Danh sách yêu thích");
        return "user/nct-wishlist";
    }

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addToWishlist(@RequestParam("productId") Long productId, @AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        NctUser currentUser = getCurrentUser(userDetails);

        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập để thêm sản phẩm.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        boolean success = nctWishlistService.nctAddToWishlist(currentUser, productId);

        if (success) {
            response.put("success", true);
            response.put("message", "Sản phẩm đã được thêm vào danh sách yêu thích.");
        } else {
            response.put("success", false);
            response.put("message", "Sản phẩm đã có trong danh sách yêu thích.");
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/remove/{productId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeFromWishlist(@PathVariable("productId") Long productId, @AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        NctUser currentUser = getCurrentUser(userDetails);

        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập để thực hiện hành động này.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            nctWishlistService.nctRemoveFromWishlist(currentUser, productId);
            response.put("success", true);
            response.put("message", "Sản phẩm đã được xóa khỏi danh sách yêu thích.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Đã xảy ra lỗi khi xóa sản phẩm.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
