package k23cnt3.nguyencongtung.project3.controller.admin;

import k23cnt3.nguyencongtung.project3.entity.NctProduct;
import k23cnt3.nguyencongtung.project3.entity.NctCategory;
import k23cnt3.nguyencongtung.project3.service.NctProductService;
import k23cnt3.nguyencongtung.project3.service.NctCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/admin/products")
public class NctAdminProductController {

    private final NctProductService nctProductService;
    private final NctCategoryService nctCategoryService;

    @Value("${nct.upload.dir:./uploads}")
    private String nctUploadDirectory;

    @Autowired
    public NctAdminProductController(NctProductService nctProductService,
                                     NctCategoryService nctCategoryService) {
        this.nctProductService = nctProductService;
        this.nctCategoryService = nctCategoryService;
    }

    // GET: Hiển thị danh sách sản phẩm
    @GetMapping
    public String nctProductList(Model model,
                                 @RequestParam(name = "keyword", required = false) String keyword,
                                 @RequestParam(name = "page", defaultValue = "0") int page,
                                 @RequestParam(name = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NctProduct> nctProductPage = nctProductService.nctFindPaginated(keyword, pageable);

        model.addAttribute("nctProductPage", nctProductPage);
        model.addAttribute("nctKeyword", keyword);
        model.addAttribute("nctPageTitle", "Danh sách Sản phẩm - Admin");
        return "admin/products/nct-product-list";
    }

    // GET: Hiển thị form thêm sản phẩm
    @GetMapping("/create")
    public String nctShowCreateForm(Model model) {
        List<NctCategory> nctCategories = nctCategoryService.nctGetAllCategories();
        model.addAttribute("nctProduct", new NctProduct());
        model.addAttribute("nctCategories", nctCategories); // QUAN TRỌNG: Phải có dòng này
        model.addAttribute("nctPageTitle", "Thêm Sản phẩm Mới - Admin");
        return "admin/products/nct-product-create"; // KIỂM TRA ĐƯỜNG DẪN
    }

    // POST: Xử lý thêm sản phẩm
    @PostMapping("/create")
    public String nctCreateProduct(
            @ModelAttribute NctProduct nctProduct,
            @RequestParam("nctImageFile") MultipartFile nctImageFile,
            @RequestParam("nctCategoryId") Long nctCategoryId,
            RedirectAttributes nctRedirectAttributes) {

        try {
            // Xử lý upload ảnh
            if (!nctImageFile.isEmpty()) {
                String nctImageUrl = nctHandleFileUpload(nctImageFile);
                nctProduct.setNctImageUrl(nctImageUrl);
            }

            // Set category
            Optional<NctCategory> nctCategory = nctCategoryService.nctGetCategoryById(nctCategoryId);
            nctCategory.ifPresent(nctProduct::setNctCategory);

            nctProductService.nctSaveProduct(nctProduct);
            nctRedirectAttributes.addFlashAttribute("nctSuccess", "Thêm sản phẩm thành công!");

        } catch (Exception e) {
            e.printStackTrace();
            nctRedirectAttributes.addFlashAttribute("nctError", "Lỗi khi thêm sản phẩm: " + e.getMessage());
        }

        return "redirect:/admin/products";
    }

    // GET: Hiển thị form chỉnh sửa sản phẩm
    @GetMapping("/edit/{id}")
    public String nctShowEditForm(@PathVariable Long id, Model model) {
        Optional<NctProduct> nctProductOpt = nctProductService.nctGetProductById(id);
        if (nctProductOpt.isEmpty()) {
            return "redirect:/admin/products";
        }

        List<NctCategory> nctCategories = nctCategoryService.nctGetAllCategories();
        model.addAttribute("nctProduct", nctProductOpt.get());
        model.addAttribute("nctCategories", nctCategories);
        model.addAttribute("nctPageTitle", "Chỉnh sửa Sản phẩm - Admin");
        return "admin/products/nct-product-edit";
    }

    // POST: Xử lý cập nhật sản phẩm
    @PostMapping("/edit/{id}")
    public String nctUpdateProduct(
            @PathVariable Long id,
            @ModelAttribute NctProduct nctProduct,
            @RequestParam(value = "nctImageFile", required = false) MultipartFile nctImageFile,
            @RequestParam("nctCategoryId") Long nctCategoryId,
            RedirectAttributes nctRedirectAttributes) {

        try {
            // Lấy sản phẩm hiện tại
            Optional<NctProduct> nctExistingProductOpt = nctProductService.nctGetProductById(id);
            if (nctExistingProductOpt.isEmpty()) {
                nctRedirectAttributes.addFlashAttribute("nctError", "Sản phẩm không tồn tại!");
                return "redirect:/admin/products";
            }

            NctProduct nctExistingProduct = nctExistingProductOpt.get();

            // Cập nhật thông tin cơ bản
            nctExistingProduct.setNctProductName(nctProduct.getNctProductName());
            nctExistingProduct.setNctDescription(nctProduct.getNctDescription());
            nctExistingProduct.setNctPrice(nctProduct.getNctPrice());
            nctExistingProduct.setNctOriginalPrice(nctProduct.getNctOriginalPrice());
            nctExistingProduct.setNctStockQuantity(nctProduct.getNctStockQuantity());
            nctExistingProduct.setNctStatus(nctProduct.getNctStatus());

            // Xử lý upload ảnh mới
            if (nctImageFile != null && !nctImageFile.isEmpty()) {
                // Xóa ảnh cũ nếu có
                if (nctExistingProduct.getNctImageUrl() != null && nctExistingProduct.getNctImageUrl().startsWith("/uploads/")) {
                    nctDeleteImageFile(nctExistingProduct.getNctImageUrl());
                }

                String nctImageUrl = nctHandleFileUpload(nctImageFile);
                nctExistingProduct.setNctImageUrl(nctImageUrl);
            }

            // Set category
            Optional<NctCategory> nctCategory = nctCategoryService.nctGetCategoryById(nctCategoryId);
            nctCategory.ifPresent(nctExistingProduct::setNctCategory);

            nctProductService.nctSaveProduct(nctExistingProduct);
            nctRedirectAttributes.addFlashAttribute("nctSuccess", "Cập nhật sản phẩm thành công!");

        } catch (Exception e) {
            e.printStackTrace();
            nctRedirectAttributes.addFlashAttribute("nctError", "Lỗi khi cập nhật sản phẩm: " + e.getMessage());
        }

        return "redirect:/admin/products";
    }

    // GET: Hiển thị trang xác nhận xóa
    @GetMapping("/delete/{id}")
    public String nctShowDeleteForm(@PathVariable Long id, Model model) {
        Optional<NctProduct> nctProductOpt = nctProductService.nctGetProductById(id);
        if (nctProductOpt.isEmpty()) {
            return "redirect:/admin/products";
        }

        model.addAttribute("nctProduct", nctProductOpt.get());
        model.addAttribute("nctPageTitle", "Xóa Sản phẩm - Admin");
        return "admin/products/nct-product-delete";
    }

    // POST: Xử lý xóa sản phẩm
    @PostMapping("/delete/{id}")
    public String nctDeleteProduct(@PathVariable Long id, RedirectAttributes nctRedirectAttributes) {
        try {
            Optional<NctProduct> nctProductOpt = nctProductService.nctGetProductById(id);
            if (nctProductOpt.isPresent()) {
                NctProduct nctProduct = nctProductOpt.get();

                // Xóa file ảnh nếu có
                if (nctProduct.getNctImageUrl() != null && nctProduct.getNctImageUrl().startsWith("/uploads/")) {
                    nctDeleteImageFile(nctProduct.getNctImageUrl());
                }

                nctProductService.nctDeleteProduct(id);
                nctRedirectAttributes.addFlashAttribute("nctSuccess", "Xóa sản phẩm thành công!");
            } else {
                nctRedirectAttributes.addFlashAttribute("nctError", "Sản phẩm không tồn tại!");
            }
        } catch (Exception e) {
            nctRedirectAttributes.addFlashAttribute("nctError", "Lỗi khi xóa sản phẩm: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    // GET: Xem chi tiết sản phẩm
    @GetMapping("/view/{id}")
    public String nctViewProduct(@PathVariable Long id, Model model) {
        Optional<NctProduct> nctProductOpt = nctProductService.nctGetProductById(id);
        if (nctProductOpt.isEmpty()) {
            return "redirect:/admin/products";
        }

        model.addAttribute("nctProduct", nctProductOpt.get());
        model.addAttribute("nctPageTitle", "Chi tiết Sản phẩm - Admin");
        return "admin/products/nct-product-view";
    }

    // Utility method: Xử lý upload file
    private String nctHandleFileUpload(MultipartFile nctImageFile) throws IOException {
        String nctOriginalFileName = nctImageFile.getOriginalFilename();
        String nctFileExtension = "";
        if (nctOriginalFileName != null && nctOriginalFileName.contains(".")) {
            nctFileExtension = nctOriginalFileName.substring(nctOriginalFileName.lastIndexOf("."));
        }
        String nctFileName = UUID.randomUUID().toString() + nctFileExtension;

        Path nctUploadPath = Paths.get(nctUploadDirectory);
        if (!Files.exists(nctUploadPath)) {
            Files.createDirectories(nctUploadPath);
        }

        Path nctFilePath = nctUploadPath.resolve(nctFileName);
        Files.write(nctFilePath, nctImageFile.getBytes());

        return "/uploads/" + nctFileName;
    }
    // Add this method inside your NctAdminProductController class
    @PostMapping("/update-status/{id}")
    public String nctUpdateProductStatus(@PathVariable("id") Long productId,
                                         @RequestParam("status") NctProduct.NctStatus status,
                                         RedirectAttributes redirectAttributes) {
        try {
            nctProductService.nctUpdateProductStatus(productId, status);
            redirectAttributes.addFlashAttribute("nctSuccess", "Trạng thái sản phẩm đã được cập nhật thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("nctError", "Lỗi khi cập nhật trạng thái: " + e.getMessage());
        }
        return "redirect:/admin/products/view/" + productId;
    }


    // Utility method: Xóa file ảnh
    private void nctDeleteImageFile(String nctImageUrl) throws IOException {
        if (nctImageUrl.startsWith("/uploads/")) {
            String nctFileName = nctImageUrl.substring("/uploads/".length());
            Path nctFilePath = Paths.get(nctUploadDirectory, nctFileName);
            Files.deleteIfExists(nctFilePath);
        }
    }
}
