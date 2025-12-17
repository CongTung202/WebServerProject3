package k23cnt3.nguyencongtung.project3.controller.admin;

import k23cnt3.nguyencongtung.project3.entity.NctCategory;
import k23cnt3.nguyencongtung.project3.service.NctCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
@RequestMapping("/admin/categories")
public class NctAdminCategoryController {

    private final NctCategoryService nctCategoryService;

    @Value("${nct.upload.dir:./uploads}")
    private String nctUploadDirectory;

    @Autowired
    public NctAdminCategoryController(NctCategoryService nctCategoryService) {
        this.nctCategoryService = nctCategoryService;
    }

    // GET: Hiển thị danh sách danh mục
    @GetMapping
    public String nctCategoryList(Model model) {
        List<NctCategory> nctCategories = nctCategoryService.nctGetAllCategories();

        // Tính toán thống kê
        int nctTotalProducts = nctCategories.stream()
                .mapToInt(category -> category.getNctProducts() != null ? category.getNctProducts().size() : 0)
                .sum();

        model.addAttribute("nctCategories", nctCategories);
        model.addAttribute("nctTotalProducts", nctTotalProducts);
        model.addAttribute("nctActiveCategories", nctCategories.size());
        model.addAttribute("nctPageTitle", "Danh sách Danh mục - Admin");

        return "admin/categories/nct-category-list";
    }

    // GET: Hiển thị form thêm danh mục
    @GetMapping("/create")
    public String nctShowCreateForm(Model model) {
        model.addAttribute("nctCategory", new NctCategory());
        model.addAttribute("nctPageTitle", "Thêm Danh mục Mới - Admin");
        return "admin/categories/nct-category-create";
    }

    // POST: Xử lý thêm danh mục
    @PostMapping("/create")
    public String nctCreateCategory(
            @ModelAttribute NctCategory nctCategory,
            @RequestParam(value = "nctImageFile", required = false) MultipartFile nctImageFile,
            RedirectAttributes nctRedirectAttributes) {

        try {
            // Xử lý upload ảnh
            if (nctImageFile != null && !nctImageFile.isEmpty()) {
                String nctImageUrl = nctHandleFileUpload(nctImageFile);
                nctCategory.setNctImageUrl(nctImageUrl);
            }

            nctCategoryService.nctSaveCategory(nctCategory);
            nctRedirectAttributes.addFlashAttribute("nctSuccess", "Thêm danh mục thành công!");

        } catch (Exception e) {
            e.printStackTrace();
            nctRedirectAttributes.addFlashAttribute("nctError", "Lỗi khi thêm danh mục: " + e.getMessage());
        }

        return "redirect:/admin/categories";
    }

    // GET: Hiển thị form chỉnh sửa danh mục
    @GetMapping("/edit/{id}")
    public String nctShowEditForm(@PathVariable Long id, Model model) {
        Optional<NctCategory> nctCategoryOpt = nctCategoryService.nctGetCategoryById(id);
        if (nctCategoryOpt.isEmpty()) {
            return "redirect:/admin/categories";
        }

        model.addAttribute("nctCategory", nctCategoryOpt.get());
        model.addAttribute("nctPageTitle", "Chỉnh sửa Danh mục - Admin");
        return "admin/categories/nct-category-edit";
    }

    // POST: Xử lý cập nhật danh mục
    @PostMapping("/edit/{id}")
    public String nctUpdateCategory(
            @PathVariable Long id,
            @ModelAttribute NctCategory nctCategory,
            @RequestParam(value = "nctImageFile", required = false) MultipartFile nctImageFile,
            RedirectAttributes nctRedirectAttributes) {

        try {
            // Lấy danh mục hiện tại
            Optional<NctCategory> nctExistingCategoryOpt = nctCategoryService.nctGetCategoryById(id);
            if (nctExistingCategoryOpt.isEmpty()) {
                nctRedirectAttributes.addFlashAttribute("nctError", "Danh mục không tồn tại!");
                return "redirect:/admin/categories";
            }

            NctCategory nctExistingCategory = nctExistingCategoryOpt.get();

            // Cập nhật thông tin cơ bản
            nctExistingCategory.setNctCategoryName(nctCategory.getNctCategoryName());
            nctExistingCategory.setNctDescription(nctCategory.getNctDescription());

            // Xử lý upload ảnh mới
            if (nctImageFile != null && !nctImageFile.isEmpty()) {
                // Xóa ảnh cũ nếu có
                if (nctExistingCategory.getNctImageUrl() != null && nctExistingCategory.getNctImageUrl().startsWith("/uploads/")) {
                    nctDeleteImageFile(nctExistingCategory.getNctImageUrl());
                }

                String nctImageUrl = nctHandleFileUpload(nctImageFile);
                nctExistingCategory.setNctImageUrl(nctImageUrl);
            }

            nctCategoryService.nctSaveCategory(nctExistingCategory);
            nctRedirectAttributes.addFlashAttribute("nctSuccess", "Cập nhật danh mục thành công!");

        } catch (Exception e) {
            e.printStackTrace();
            nctRedirectAttributes.addFlashAttribute("nctError", "Lỗi khi cập nhật danh mục: " + e.getMessage());
        }

        return "redirect:/admin/categories";
    }

    // GET: Hiển thị trang xác nhận xóa
    @GetMapping("/delete/{id}")
    public String nctShowDeleteForm(@PathVariable Long id, Model model) {
        Optional<NctCategory> nctCategoryOpt = nctCategoryService.nctGetCategoryById(id);
        if (nctCategoryOpt.isEmpty()) {
            return "redirect:/admin/categories";
        }

        NctCategory nctCategory = nctCategoryOpt.get();
        // Kiểm tra xem danh mục có sản phẩm không
        boolean nctHasProducts = nctCategory.getNctProducts() != null && !nctCategory.getNctProducts().isEmpty();

        model.addAttribute("nctCategory", nctCategory);
        model.addAttribute("nctHasProducts", nctHasProducts);
        model.addAttribute("nctPageTitle", "Xóa Danh mục - Admin");
        return "admin/categories/nct-category-delete";
    }

    // POST: Xử lý xóa danh mục
    @PostMapping("/delete/{id}")
    public String nctDeleteCategory(@PathVariable Long id, RedirectAttributes nctRedirectAttributes) {
        try {
            Optional<NctCategory> nctCategoryOpt = nctCategoryService.nctGetCategoryById(id);
            if (nctCategoryOpt.isPresent()) {
                NctCategory nctCategory = nctCategoryOpt.get();

                // Kiểm tra xem danh mục có sản phẩm không
                if (nctCategory.getNctProducts() != null && !nctCategory.getNctProducts().isEmpty()) {
                    nctRedirectAttributes.addFlashAttribute("nctError", "Không thể xóa danh mục vì còn sản phẩm!");
                    return "redirect:/admin/categories";
                }

                // Xóa file ảnh nếu có
                if (nctCategory.getNctImageUrl() != null && nctCategory.getNctImageUrl().startsWith("/uploads/")) {
                    nctDeleteImageFile(nctCategory.getNctImageUrl());
                }

                nctCategoryService.nctDeleteCategory(id);
                nctRedirectAttributes.addFlashAttribute("nctSuccess", "Xóa danh mục thành công!");
            } else {
                nctRedirectAttributes.addFlashAttribute("nctError", "Danh mục không tồn tại!");
            }
        } catch (Exception e) {
            nctRedirectAttributes.addFlashAttribute("nctError", "Lỗi khi xóa danh mục: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    // GET: Xem chi tiết danh mục
    @GetMapping("/view/{id}")
    public String nctViewCategory(@PathVariable Long id, Model model) {
        Optional<NctCategory> nctCategoryOpt = nctCategoryService.nctGetCategoryById(id);
        if (nctCategoryOpt.isEmpty()) {
            return "redirect:/admin/categories";
        }

        model.addAttribute("nctCategory", nctCategoryOpt.get());
        model.addAttribute("nctPageTitle", "Chi tiết Danh mục - Admin");
        return "admin/categories/nct-category-view";
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

    // Utility method: Xóa file ảnh
    private void nctDeleteImageFile(String nctImageUrl) throws IOException {
        if (nctImageUrl.startsWith("/uploads/")) {
            String nctFileName = nctImageUrl.substring("/uploads/".length());
            Path nctFilePath = Paths.get(nctUploadDirectory, nctFileName);
            Files.deleteIfExists(nctFilePath);
        }
    }
}