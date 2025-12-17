package k23cnt3.nguyencongtung.project3.repository;

import k23cnt3.nguyencongtung.project3.entity.NctCategory;
import k23cnt3.nguyencongtung.project3.entity.NctProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NctProductRepository extends JpaRepository<NctProduct, Long> {

    // Find by name for pagination
    Page<NctProduct> findByNctProductNameContainingIgnoreCase(String nctProductName, Pageable pageable);

    // Tìm sản phẩm theo danh mục
    List<NctProduct> findByNctCategory(NctCategory nctCategory);

    // Tìm sản phẩm theo danh mục ID
    List<NctProduct> findByNctCategory_NctCategoryId(Long nctCategoryId);

    // Tìm sản phẩm theo danh mục và trạng thái
    List<NctProduct> findByNctCategoryAndNctStatus(NctCategory nctCategory, NctProduct.NctStatus nctStatus);

    // Tìm sản phẩm theo danh mục ID và trạng thái
    List<NctProduct> findByNctCategory_NctCategoryIdAndNctStatus(Long nctCategoryId, NctProduct.NctStatus nctStatus);

    // Tìm kiếm sản phẩm theo tên (không phân biệt hoa thường)
    List<NctProduct> findByNctProductNameContainingIgnoreCase(String nctProductName);

    // Tìm kiếm sản phẩm theo tên và trạng thái
    List<NctProduct> findByNctProductNameContainingIgnoreCaseAndNctStatus(String nctProductName, NctProduct.NctStatus nctStatus);

    // Tìm sản phẩm theo trạng thái
    List<NctProduct> findByNctStatus(NctProduct.NctStatus nctStatus);

    // Tìm sản phẩm theo khoảng giá
    @Query("SELECT p FROM NctProduct p WHERE p.nctPrice BETWEEN :minPrice AND :maxPrice")
    List<NctProduct> findByPriceRange(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);

    // Tìm sản phẩm theo khoảng giá và trạng thái
    @Query("SELECT p FROM NctProduct p WHERE p.nctPrice BETWEEN :minPrice AND :maxPrice AND p.nctStatus = :status")
    List<NctProduct> findByNctPriceBetweenAndNctStatus(@Param("minPrice") Double minPrice,
                                                       @Param("maxPrice") Double maxPrice,
                                                       @Param("status") NctProduct.NctStatus status);

    // Tìm 8 sản phẩm mới nhất
    @Query("SELECT p FROM NctProduct p ORDER BY p.nctCreatedAt DESC LIMIT 8")
    List<NctProduct> findTop8ByOrderByNctCreatedAtDesc();

    // Tìm 8 sản phẩm mới nhất theo trạng thái
    @Query("SELECT p FROM NctProduct p WHERE p.nctStatus = :status ORDER BY p.nctCreatedAt DESC LIMIT 8")
    List<NctProduct> findTop8ByNctStatusOrderByNctCreatedAtDesc(@Param("status") NctProduct.NctStatus status);

    // Tìm sản phẩm còn hàng
    List<NctProduct> findByNctStockQuantityGreaterThan(Integer nctStockQuantity);

    // Tìm sản phẩm còn hàng và có trạng thái active
    List<NctProduct> findByNctStockQuantityGreaterThanAndNctStatus(Integer nctStockQuantity, NctProduct.NctStatus nctStatus);

    // Đếm số sản phẩm theo danh mục
    @Query("SELECT COUNT(p) FROM NctProduct p WHERE p.nctCategory.nctCategoryId = :categoryId")
    Long countByNctCategoryId(@Param("categoryId") Long categoryId);

    // Tìm sản phẩm theo nhiều tiêu chí (tìm kiếm nâng cao)
    @Query("SELECT p FROM NctProduct p WHERE " +
            "(:categoryId IS NULL OR p.nctCategory.nctCategoryId = :categoryId) AND " +
            "(:minPrice IS NULL OR p.nctPrice >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.nctPrice <= :maxPrice) AND " +
            "(:status IS NULL OR p.nctStatus = :status) AND " +
            "(:keyword IS NULL OR LOWER(p.nctProductName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<NctProduct> findByAdvancedSearch(@Param("categoryId") Long categoryId,
                                          @Param("minPrice") Double minPrice,
                                          @Param("maxPrice") Double maxPrice,
                                          @Param("status") NctProduct.NctStatus status,
                                          @Param("keyword") String keyword);
}
