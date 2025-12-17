package k23cnt3.nguyencongtung.project3.service;

import k23cnt3.nguyencongtung.project3.entity.NctOrder;
import k23cnt3.nguyencongtung.project3.entity.NctUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface NctOrderServiceInterface {
    List<NctOrder> nctGetAllOrders();
    List<NctOrder> nctGetUserOrders(NctUser nctUser);
    Optional<NctOrder> nctGetOrderById(Long nctOrderId);
    NctOrder nctCreateOrder(NctUser nctUser, String nctShippingAddress, String nctPhone, NctOrder.NctPaymentMethod nctPaymentMethod);
    NctOrder nctCreateOrderFromCart(NctUser nctUser, NctOrder nctOrderDetails);
    NctOrder nctCreateOrderFromSingleProduct(NctUser nctUser, Long nctProductId, Integer nctQuantity, String nctShippingAddress, String nctPhone, NctOrder.NctPaymentMethod nctPaymentMethod);
    NctOrder nctUpdateOrderStatus(Long nctOrderId, NctOrder.NctOrderStatus nctStatus);
    void nctCancelOrder(Long nctOrderId);
    Long nctGetOrderCountByStatus(NctOrder.NctOrderStatus nctStatus);
    Double nctGetTotalRevenue();
    List<Object[]> nctGetBestSellingProducts();
    NctOrder nctSaveOrder(NctOrder order);
    void nctDeleteOrder(Long orderId);
    List<NctOrder> nctGetOrdersByStatus(NctOrder.NctOrderStatus status);
    Page<NctOrder> nctFindPaginated(NctOrder.NctOrderStatus status, String keyword, Pageable pageable);
}
