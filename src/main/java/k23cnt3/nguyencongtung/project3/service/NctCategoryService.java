package k23cnt3.nguyencongtung.project3.service;

import k23cnt3.nguyencongtung.project3.entity.NctCategory;
import k23cnt3.nguyencongtung.project3.repository.NctCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NctCategoryService {
    private final NctCategoryRepository nctCategoryRepository;

    @Autowired
    public NctCategoryService(NctCategoryRepository nctCategoryRepository) {
        this.nctCategoryRepository = nctCategoryRepository;
    }

    // Lấy tất cả danh mục
    public List<NctCategory> nctGetAllCategories() {
        return nctCategoryRepository.findAll();
    }

    // Lấy tất cả danh mục và sắp xếp theo tên
    public List<NctCategory> nctGetAllCategoriesSorted() {
        return nctCategoryRepository.findAllByOrderByNctCategoryNameAsc();
    }

    // Lấy danh mục theo ID
    public Optional<NctCategory> nctGetCategoryById(Long nctId) {
        return nctCategoryRepository.findById(nctId);
    }

    // Lấy danh mục theo ID với sản phẩm
    public Optional<NctCategory> nctGetCategoryByIdWithProducts(Long nctId) {
        return nctCategoryRepository.findByIdWithProducts(nctId);
    }

    // Lấy danh mục theo tên
    public Optional<NctCategory> nctGetCategoryByName(String categoryName) {
        return nctCategoryRepository.findByNctCategoryName(categoryName);
    }

    // Lưu danh mục
    public NctCategory nctSaveCategory(NctCategory nctCategory) {
        return nctCategoryRepository.save(nctCategory);
    }

    // Xóa danh mục
    public void nctDeleteCategory(Long nctId) {
        nctCategoryRepository.deleteById(nctId);
    }

    // Kiểm tra danh mục tồn tại theo tên
    public boolean nctCategoryExistsByName(String nctCategoryName) {
        return nctCategoryRepository.existsByNctCategoryName(nctCategoryName);
    }

    // Tìm kiếm danh mục
    public List<NctCategory> nctSearchCategories(String nctKeyword) {
        return nctCategoryRepository.findByNctCategoryNameContainingIgnoreCase(nctKeyword);
    }

    // Lấy danh mục có sản phẩm
    public List<NctCategory> nctGetCategoriesWithProducts() {
        return nctCategoryRepository.findCategoriesWithProducts();
    }

    // Lấy danh mục có sản phẩm active
    public List<NctCategory> nctGetCategoriesWithActiveProducts() {
        return nctCategoryRepository.findCategoriesWithActiveProducts();
    }

    // Đếm số danh mục
    public long nctCountCategories() {
        return nctCategoryRepository.count();
    }

    // Kiểm tra danh mục có thể xóa (không có sản phẩm)
    public boolean nctCanDeleteCategory(Long categoryId) {
        // Trong thực tế, bạn có thể kiểm tra xem danh mục có sản phẩm không
        // Ở đây tôi giả sử sử dụng ProductService để đếm
        return true; // Tạm thời trả về true, bạn có thể implement logic thực tế
    }

    // Cập nhật danh mục
    public NctCategory nctUpdateCategory(Long categoryId, NctCategory updatedCategory) {
        Optional<NctCategory> existingCategory = nctCategoryRepository.findById(categoryId);
        if (existingCategory.isPresent()) {
            NctCategory category = existingCategory.get();
            category.setNctCategoryName(updatedCategory.getNctCategoryName());
            category.setNctDescription(updatedCategory.getNctDescription());
            category.setNctImageUrl(updatedCategory.getNctImageUrl());
            return nctCategoryRepository.save(category);
        }
        return null;
    }
    public long countCategoriesCreatedBetween(LocalDateTime start, LocalDateTime end) {
        return nctGetAllCategories().stream()
                .filter(c -> c.getNctCreatedAt() != null && !c.getNctCreatedAt().isBefore(start) && c.getNctCreatedAt().isBefore(end))
                .count();
    }
}