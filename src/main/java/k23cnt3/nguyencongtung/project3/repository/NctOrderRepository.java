package k23cnt3.nguyencongtung.project3.repository;

import k23cnt3.nguyencongtung.project3.entity.NctOrder;
import k23cnt3.nguyencongtung.project3.entity.NctUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NctOrderRepository extends JpaRepository<NctOrder, Long> {

    List<NctOrder> findByNctUserOrderByNctCreatedAtDesc(NctUser nctUser);

    List<NctOrder> findByNctStatusOrderByNctCreatedAtDesc(NctOrder.NctOrderStatus status);

    List<NctOrder> findByNctUser_NctUserIdOrderByNctCreatedAtDesc(Long userId);

    Long countByNctStatus(NctOrder.NctOrderStatus nctStatus);

    List<NctOrder> findAllByOrderByNctCreatedAtDesc();

    @Query("SELECT MONTH(o.nctCreatedAt) as month, COUNT(o) as orderCount, SUM(o.nctTotalAmount) as revenue " +
            "FROM NctOrder o WHERE YEAR(o.nctCreatedAt) = YEAR(CURRENT_DATE) " +
            "GROUP BY MONTH(o.nctCreatedAt) ORDER BY month")
    List<Object[]> getMonthlyOrderStats();

    @Query("SELECT SUM(o.nctTotalAmount) FROM NctOrder o WHERE o.nctStatus = k23cnt3.nguyencongtung.project3.entity.NctOrder.NctOrderStatus.DELIVERED")
    Double getTotalRevenue();

    @Query("SELECT o FROM NctOrder o WHERE " +
            "(:status IS NULL OR o.nctStatus = :status) AND " +
            "(:keyword IS NULL OR LOWER(o.nctUser.nctFullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR CAST(o.nctOrderId AS string) LIKE CONCAT('%', :keyword, '%'))")
    Page<NctOrder> findWithFiltersAndPagination(@Param("status") NctOrder.NctOrderStatus status,
                                                @Param("keyword") String keyword,
                                                Pageable pageable);
}
