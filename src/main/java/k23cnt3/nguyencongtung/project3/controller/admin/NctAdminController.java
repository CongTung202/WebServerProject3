package k23cnt3.nguyencongtung.project3.controller.admin;

import k23cnt3.nguyencongtung.project3.service.NctProductService;
import k23cnt3.nguyencongtung.project3.service.NctCategoryService;
import k23cnt3.nguyencongtung.project3.service.NctOrderService;
import k23cnt3.nguyencongtung.project3.service.NctUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin")
public class NctAdminController {

    private final NctProductService nctProductService;
    private final NctCategoryService nctCategoryService;
    private final NctOrderService nctOrderService;
    private final NctUserService nctUserService;

    @Autowired
    public NctAdminController(NctProductService nctProductService,
                              NctCategoryService nctCategoryService,
                              NctOrderService nctOrderService,
                              NctUserService nctUserService) {
        this.nctProductService = nctProductService;
        this.nctCategoryService = nctCategoryService;
        this.nctOrderService = nctOrderService;
        this.nctUserService = nctUserService;
    }

    private double calculatePercentageChange(double current, double previous) {
        if (previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        return ((current - previous) / previous) * 100.0;
    }

    @GetMapping("/dashboard")
    public String nctDashboard(Model model) {
        // Total counts
        model.addAttribute("nctTotalProducts", (long) nctProductService.nctGetAllProducts().size());
        model.addAttribute("nctTotalCategories", (long) nctCategoryService.nctGetAllCategories().size());
        model.addAttribute("nctTotalOrders", nctOrderService.nctGetAllOrders().size());
        model.addAttribute("nctTotalRevenue", nctOrderService.nctGetTotalRevenue());
        model.addAttribute("nctTotalUsers", nctUserService.nctCountTotalUsers());

        // Date ranges for statistics
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime last30DaysStart = now.minusDays(30);
        LocalDateTime previous30DaysStart = now.minusDays(60);

        // Product change calculation
        long newProducts = nctProductService.countProductsCreatedBetween(last30DaysStart, now);
        long previousProducts = nctProductService.countProductsCreatedBetween(previous30DaysStart, last30DaysStart);
        model.addAttribute("productChange", calculatePercentageChange(newProducts, previousProducts));

        // Category change calculation
        long newCategories = nctCategoryService.countCategoriesCreatedBetween(last30DaysStart, now);
        long previousCategories = nctCategoryService.countCategoriesCreatedBetween(previous30DaysStart, last30DaysStart);
        model.addAttribute("categoryChange", calculatePercentageChange(newCategories, previousCategories));

        // Order change calculation
        long newOrders = nctOrderService.countOrdersCreatedBetween(last30DaysStart, now);
        long previousOrders = nctOrderService.countOrdersCreatedBetween(previous30DaysStart, last30DaysStart);
        model.addAttribute("orderChange", calculatePercentageChange(newOrders, previousOrders));

        // Revenue change calculation
        double newRevenue = nctOrderService.getRevenueBetween(last30DaysStart, now);
        double previousRevenue = nctOrderService.getRevenueBetween(previous30DaysStart, last30DaysStart);
        model.addAttribute("revenueChange", calculatePercentageChange(newRevenue, previousRevenue));

        // User change calculation
        long newUsers = nctUserService.countUsersCreatedBetween(last30DaysStart, now);
        long previousUsers = nctUserService.countUsersCreatedBetween(previous30DaysStart, last30DaysStart);
        model.addAttribute("userChange", calculatePercentageChange(newUsers, previousUsers));

        return "admin/nct-dashboard";
    }
}
