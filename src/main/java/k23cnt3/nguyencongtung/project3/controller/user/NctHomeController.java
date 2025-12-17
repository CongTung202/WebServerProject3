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
    import org.springframework.web.bind.annotation.RequestMapping;

    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;

    @Controller
    @RequestMapping("/")
    public class NctHomeController {

        private final NctProductService nctProductService;
        private final NctCategoryService nctCategoryService;
        private final NctCartService nctCartService;
        private final NctUserService nctUserService;

        @Autowired
        public NctHomeController(NctProductService nctProductService, NctCategoryService nctCategoryService, NctCartService nctCartService, NctUserService nctUserService) {
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
        public String nctHomePage(Model model, @AuthenticationPrincipal UserDetails userDetails) {
            addCommonAttributes(model, userDetails);
            List<NctCategory> nctCategories = nctCategoryService.nctGetAllCategories();
            Map<String, List<NctProduct>> nctProductsByCategory = new HashMap<>();
            Map<String, String> nctCategoryUrls = new HashMap<>();

            for (NctCategory category : nctCategories) {
                List<NctProduct> products = nctProductService.nctGetActiveProductsByCategoryId(category.getNctCategoryId());
                if (products.size() > 12) {
                    products = products.subList(0, 12);
                }
                nctProductsByCategory.put(category.getNctCategoryName(), products);
                nctCategoryUrls.put(category.getNctCategoryName(), "/products?category=" + category.getNctCategoryId());
            }

            List<NctProduct> nctFeaturedProducts = nctProductService.nctGetActiveFeaturedProducts();

            model.addAttribute("nctCategories", nctCategories);
            model.addAttribute("nctProductsByCategory", nctProductsByCategory);
            model.addAttribute("nctCategoryUrls", nctCategoryUrls);
            model.addAttribute("nctFeaturedProducts", nctFeaturedProducts);
            model.addAttribute("nctPageTitle", "UmaCT - Ngựa thật việc thật");

            return "user/nct-home";
        }

        @GetMapping("/about")
        public String nctAboutPage(Model model, @AuthenticationPrincipal UserDetails userDetails) {
            addCommonAttributes(model, userDetails);
            model.addAttribute("nctPageTitle", "Giới thiệu - UmaCT");
            return "user/nct-about";
        }

        @GetMapping("/contact")
        public String nctContactPage(Model model, @AuthenticationPrincipal UserDetails userDetails) {
            addCommonAttributes(model, userDetails);
            model.addAttribute("nctPageTitle", "Liên hệ - UmaCT");
            return "user/nct-contact";
        }
        @GetMapping("/policy")
        public String nctPolicyPage(Model model, @AuthenticationPrincipal UserDetails userDetails) {
            addCommonAttributes(model, userDetails);
            model.addAttribute("nctPageTitle", "Chính sách Bảo hành & Đổi trả - UmaCT");
            return "user/nct-policy";
        }
    }