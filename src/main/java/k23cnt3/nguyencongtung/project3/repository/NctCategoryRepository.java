package k23cnt3.nguyencongtung.project3.repository;

import k23cnt3.nguyencongtung.project3.entity.NctCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NctCategoryRepository extends JpaRepository<NctCategory, Long> {

    // Tìm danh mục theo tên
    Optional<NctCategory> findByNctCategoryName(String nctCategoryName);

    // Kiểm tra tồn tại danh mục theo tên
    boolean existsByNctCategoryName(String nctCategoryName);

    // Tìm kiếm danh mục theo từ khóa (không phân biệt hoa thường)
    List<NctCategory> findByNctCategoryNameContainingIgnoreCase(String nctKeyword);

    // Tìm danh mục có sản phẩm
    @Query("SELECT DISTINCT c FROM NctCategory c WHERE c.nctProducts IS NOT EMPTY")
    List<NctCategory> findCategoriesWithProducts();

    // Tìm danh mục có sản phẩm active
    @Query("SELECT DISTINCT c FROM NctCategory c JOIN c.nctProducts p WHERE p.nctStatus = 'ACTIVE'")
    List<NctCategory> findCategoriesWithActiveProducts();

    // Đếm số danh mục
    long count();

    // Tìm danh mục và sắp xếp theo tên
    List<NctCategory> findAllByOrderByNctCategoryNameAsc();

    // Tìm danh mục theo ID với sản phẩm
    @Query("SELECT c FROM NctCategory c LEFT JOIN FETCH c.nctProducts WHERE c.nctCategoryId = :categoryId")
    Optional<NctCategory> findByIdWithProducts(@Param("categoryId") Long categoryId);
}