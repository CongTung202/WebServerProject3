package k23cnt3.nguyencongtung.project3.repository;

import k23cnt3.nguyencongtung.project3.entity.NctOrder;
import k23cnt3.nguyencongtung.project3.entity.NctOrderItem;
import k23cnt3.nguyencongtung.project3.entity.NctProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NctOrderItemRepository extends JpaRepository<NctOrderItem, Long> {
    List<NctOrderItem> findByNctOrder(NctOrder nctOrder);
    List<NctOrderItem> findByNctProduct(NctProduct nctProduct);

    // SỬA: Sử dụng enum class thay vì string
    @Query("SELECT oi.nctProduct, SUM(oi.nctQuantity) as totalSold " +
            "FROM NctOrderItem oi " +
            "JOIN oi.nctOrder o " +
            "WHERE o.nctStatus = k23cnt3.nguyencongtung.project3.entity.NctOrder.NctOrderStatus.DELIVERED " +
            "GROUP BY oi.nctProduct " +
            "ORDER BY totalSold DESC")
    List<Object[]> findBestSellingProducts();
}