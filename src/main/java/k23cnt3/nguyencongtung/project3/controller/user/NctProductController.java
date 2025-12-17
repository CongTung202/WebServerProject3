package k23cnt3.nguyencongtung.project3.controller.user;

import k23cnt3.nguyencongtung.project3.entity.NctCategory;
import k23cnt3.nguyencongtung.project3.entity.NctProduct;
import k23cnt3.nguyencongtung.project3.entity.NctUser;
import k23cnt3.nguyencongtung.project3.service.NctCartService;
import k23cnt3.nguyencongtung.project3.service.NctCategoryService;
import k23cnt3.nguyencongtung.project3.service.NctProductService;
import k23cnt3.nguyencongtung.project3.service.NctUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/products")
public class NctProductController {

    private final NctProductService nctProductService;
    private final NctCategoryService nctCategoryService;
    private final NctCartService nctCartService;
    private final NctUserService nctUserService;

    @Autowired
    public NctProductController(NctProductService nctProductService, NctCategoryService nctCategoryService, NctCartService nctCartService, NctUserService nctUserService) {
        this.nctProductService = nctProductService;
        this.nctCategoryService = nctCategoryService;
        this.nctCartService = nctCartService;
        this.nctUserService = nctUserService;
    }

    private void addCommonAttributes(Model model, UserDetails userDetails) {
        if (userDetails != null) {
            NctUser currentUser = nctUserService.nctFindByUsername(userDetails.getUsername()).orElse(null);
            if (currentUser != null) {
                Integer cartItemCount = nctCartService.nctGetCartItemCount(currentUser);
                model.addAttribute("nctCartItemCount", cartItemCount != null ? cartItemCount : 0);
            }
        }
    }

    @GetMapping
    public String nctProductsPage(
            @RequestParam(value = "category", required = false) Long nctCategoryId,
            @RequestParam(value = "search", required = false) String nctSearchKeyword,
            @RequestParam(value = "sort", required = false) String sort,
            Model model, @AuthenticationPrincipal UserDetails userDetails) {

        addCommonAttributes(model, userDetails);
        List<NctProduct> nctProducts;
        String nctHeaderTitle = "Tất cả sản phẩm";

        if (nctSearchKeyword != null && !nctSearchKeyword.isEmpty()) {
            nctProducts = nctProductService.nctSearchActiveProducts(nctSearchKeyword);
            model.addAttribute("nctSearchKeyword", nctSearchKeyword);
            nctHeaderTitle = "Kết quả cho '" + nctSearchKeyword + "'";
        } else if (nctCategoryId != null) {
            nctProducts = nctProductService.nctGetActiveProductsByCategoryId(nctCategoryId);
            Optional<NctCategory> categoryOpt = nctCategoryService.nctGetCategoryById(nctCategoryId);
            if (categoryOpt.isPresent()) {
                nctHeaderTitle = categoryOpt.get().getNctCategoryName();
            }
            model.addAttribute("nctSelectedCategoryId", nctCategoryId);
        } else {
            nctProducts = nctProductService.nctGetActiveProducts();
        }

        // Apply sorting if requested
        nctProducts = nctProductService.nctSortProducts(nctProducts, sort);

        model.addAttribute("nctProducts", nctProducts);
        model.addAttribute("nctCategories", nctCategoryService.nctGetCategoriesWithActiveProducts());
        model.addAttribute("nctHeaderTitle", nctHeaderTitle);
        model.addAttribute("nctPageTitle", nctHeaderTitle + " - UMACT Store");
        model.addAttribute("nctSort", sort);

        return "user/nct-products";
    }

    @GetMapping("/{id}")
    public String nctProductDetail(@PathVariable("id") Long nctProductId, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        addCommonAttributes(model, userDetails);
        Optional<NctProduct> nctProductOpt = nctProductService.nctGetProductById(nctProductId);

        if (nctProductOpt.isPresent()) {
            NctProduct nctProduct = nctProductOpt.get();
            model.addAttribute("nctProduct", nctProduct);
            if (nctProduct.getNctCategory() != null) {
                model.addAttribute("nctRelatedProducts", nctProductService.nctGetActiveProductsByCategory(nctProduct.getNctCategory()));
            }
            model.addAttribute("nctPageTitle", nctProduct.getNctProductName() + " - UMACT Store");
            return "user/nct-product-detail";
        }

        return "redirect:/products";
    }
}
