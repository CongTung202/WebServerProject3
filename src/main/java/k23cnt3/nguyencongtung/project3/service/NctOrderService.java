package k23cnt3.nguyencongtung.project3.service;

import k23cnt3.nguyencongtung.project3.entity.*;
import k23cnt3.nguyencongtung.project3.repository.NctOrderRepository;
import k23cnt3.nguyencongtung.project3.repository.NctOrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class NctOrderService implements NctOrderServiceInterface {
    private final NctOrderRepository nctOrderRepository;
    private final NctOrderItemRepository nctOrderItemRepository;
    private final NctCartService nctCartService;
    private final NctProductService nctProductService;

    @Autowired
    public NctOrderService(NctOrderRepository nctOrderRepository,
                           NctOrderItemRepository nctOrderItemRepository,
                           NctCartService nctCartService,
                           NctProductService nctProductService) {
        this.nctOrderRepository = nctOrderRepository;
        this.nctOrderItemRepository = nctOrderItemRepository;
        this.nctCartService = nctCartService;
        this.nctProductService = nctProductService;
    }

    @Override
    public List<NctOrder> nctGetAllOrders() {
        return nctOrderRepository.findAllByOrderByNctCreatedAtDesc();
    }

    @Override
    public List<NctOrder> nctGetUserOrders(NctUser nctUser) {
        return nctOrderRepository.findByNctUserOrderByNctCreatedAtDesc(nctUser);
    }

    @Override
    public Optional<NctOrder> nctGetOrderById(Long nctOrderId) {
        return nctOrderRepository.findById(nctOrderId);
    }

    @Override
    @Transactional
    public NctOrder nctCreateOrder(NctUser nctUser, String nctShippingAddress, String nctPhone,
                                   NctOrder.NctPaymentMethod nctPaymentMethod) {
        List<NctCartItem> nctCartItems = nctCartService.nctGetCartItems(nctUser);
        if (nctCartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống");
        }

        BigDecimal nctTotalAmount = BigDecimal.valueOf(nctCartService.nctCalculateCartTotal(nctUser));

        NctOrder nctOrder = new NctOrder();
        nctOrder.setNctUser(nctUser);
        nctOrder.setNctTotalAmount(nctTotalAmount);
        nctOrder.setNctShippingAddress(nctShippingAddress);
        nctOrder.setNctPhone(nctPhone);
        nctOrder.setNctPaymentMethod(nctPaymentMethod);
        nctOrder.setNctStatus(NctOrder.NctOrderStatus.PENDING);

        NctOrder nctSavedOrder = nctOrderRepository.save(nctOrder);

        for (NctCartItem nctCartItem : nctCartItems) {
            NctProduct nctProduct = nctCartItem.getNctProduct();

            if (nctProduct.getNctStockQuantity() < nctCartItem.getNctQuantity()) {
                throw new RuntimeException("Sản phẩm " + nctProduct.getNctProductName() + " không đủ số lượng tồn kho");
            }

            nctProduct.setNctStockQuantity(nctProduct.getNctStockQuantity() - nctCartItem.getNctQuantity());
            nctProductService.nctSaveProduct(nctProduct);

            NctOrderItem nctOrderItem = new NctOrderItem();
            nctOrderItem.setNctOrder(nctSavedOrder);
            nctOrderItem.setNctProduct(nctProduct);
            nctOrderItem.setNctQuantity(nctCartItem.getNctQuantity());
            nctOrderItem.setNctPrice(nctProduct.getNctPrice());
            nctOrderItemRepository.save(nctOrderItem);
        }

        nctCartService.nctClearCart(nctUser);

        return nctSavedOrder;
    }

    @Override
    @Transactional
    public NctOrder nctCreateOrderFromCart(NctUser nctUser, NctOrder nctOrderDetails) {
        List<NctCartItem> nctCartItems = nctCartService.nctGetCartItems(nctUser);
        if (nctCartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống, không thể đặt hàng.");
        }

        BigDecimal nctTotalAmount = BigDecimal.valueOf(nctCartService.nctCalculateCartTotal(nctUser));

        NctOrder nctOrder = new NctOrder();
        nctOrder.setNctUser(nctUser);
        nctOrder.setNctTotalAmount(nctTotalAmount);
        nctOrder.setNctShippingAddress(nctOrderDetails.getNctShippingAddress());
        nctOrder.setNctPhone(nctOrderDetails.getNctPhone());
        nctOrder.setNctPaymentMethod(nctOrderDetails.getNctPaymentMethod());
        nctOrder.setNctStatus(NctOrder.NctOrderStatus.PENDING);

        NctOrder nctSavedOrder = nctOrderRepository.save(nctOrder);

        for (NctCartItem nctCartItem : nctCartItems) {
            NctProduct nctProduct = nctCartItem.getNctProduct();

            if (nctProduct.getNctStockQuantity() < nctCartItem.getNctQuantity()) {
                throw new RuntimeException("Sản phẩm '" + nctProduct.getNctProductName() + "' không đủ số lượng tồn kho.");
            }

            nctProduct.setNctStockQuantity(nctProduct.getNctStockQuantity() - nctCartItem.getNctQuantity());
            nctProductService.nctSaveProduct(nctProduct);

            NctOrderItem nctOrderItem = new NctOrderItem();
            nctOrderItem.setNctOrder(nctSavedOrder);
            nctOrderItem.setNctProduct(nctProduct);
            nctOrderItem.setNctQuantity(nctCartItem.getNctQuantity());
            nctOrderItem.setNctPrice(nctProduct.getNctPrice());
            nctOrderItemRepository.save(nctOrderItem);
        }

        nctCartService.nctClearCart(nctUser);

        return nctSavedOrder;
    }

    @Override
    @Transactional
    public NctOrder nctCreateOrderFromSingleProduct(NctUser nctUser, Long nctProductId, Integer nctQuantity, String nctShippingAddress, String nctPhone, NctOrder.NctPaymentMethod nctPaymentMethod) {
        NctProduct nctProduct = nctProductService.nctGetProductById(nctProductId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        if (nctProduct.getNctStockQuantity() < nctQuantity) {
            throw new RuntimeException("Sản phẩm " + nctProduct.getNctProductName() + " không đủ số lượng tồn kho");
        }

        BigDecimal nctTotalAmount = nctProduct.getNctPrice().multiply(BigDecimal.valueOf(nctQuantity));

        NctOrder nctOrder = new NctOrder();
        nctOrder.setNctUser(nctUser);
        nctOrder.setNctTotalAmount(nctTotalAmount);
        nctOrder.setNctShippingAddress(nctShippingAddress);
        nctOrder.setNctPhone(nctPhone);
        nctOrder.setNctPaymentMethod(nctPaymentMethod);
        nctOrder.setNctStatus(NctOrder.NctOrderStatus.PENDING);

        NctOrder nctSavedOrder = nctOrderRepository.save(nctOrder);

        nctProduct.setNctStockQuantity(nctProduct.getNctStockQuantity() - nctQuantity);
        nctProductService.nctSaveProduct(nctProduct);

        NctOrderItem nctOrderItem = new NctOrderItem();
        nctOrderItem.setNctOrder(nctSavedOrder);
        nctOrderItem.setNctProduct(nctProduct);
        nctOrderItem.setNctQuantity(nctQuantity);
        nctOrderItem.setNctPrice(nctProduct.getNctPrice());
        nctOrderItemRepository.save(nctOrderItem);

        return nctSavedOrder;
    }


    @Override
    public NctOrder nctUpdateOrderStatus(Long nctOrderId, NctOrder.NctOrderStatus nctStatus) {
        Optional<NctOrder> nctOrderOpt = nctOrderRepository.findById(nctOrderId);
        if (nctOrderOpt.isPresent()) {
            NctOrder nctOrder = nctOrderOpt.get();
            nctOrder.setNctStatus(nctStatus);
            nctOrder.setNctUpdatedAt(LocalDateTime.now());
            return nctOrderRepository.save(nctOrder);
        }
        throw new RuntimeException("Đơn hàng không tồn tại");
    }

    @Override
    public void nctCancelOrder(Long nctOrderId) {
        Optional<NctOrder> nctOrderOpt = nctOrderRepository.findById(nctOrderId);
        if (nctOrderOpt.isPresent()) {
            NctOrder nctOrder = nctOrderOpt.get();

            if (nctOrder.getNctStatus() != NctOrder.NctOrderStatus.PENDING &&
                    nctOrder.getNctStatus() != NctOrder.NctOrderStatus.CONFIRMED) {
                throw new RuntimeException("Không thể hủy đơn hàng ở trạng thái hiện tại");
            }

            for (NctOrderItem nctOrderItem : nctOrder.getNctOrderItems()) {
                NctProduct nctProduct = nctOrderItem.getNctProduct();
                nctProduct.setNctStockQuantity(nctProduct.getNctStockQuantity() + nctOrderItem.getNctQuantity());
                nctProductService.nctSaveProduct(nctProduct);
            }

            nctOrder.setNctStatus(NctOrder.NctOrderStatus.CANCELLED);
            nctOrder.setNctUpdatedAt(LocalDateTime.now());
            nctOrderRepository.save(nctOrder);
        } else {
            throw new RuntimeException("Đơn hàng không tồn tại");
        }
    }

    @Override
    public Long nctGetOrderCountByStatus(NctOrder.NctOrderStatus nctStatus) {
        return nctOrderRepository.countByNctStatus(nctStatus);
    }

    @Override
    public Double nctGetTotalRevenue() {
        Double revenue = nctOrderRepository.getTotalRevenue();
        return revenue != null ? revenue : 0.0;
    }

    @Override
    public List<Object[]> nctGetBestSellingProducts() {
        return nctOrderItemRepository.findBestSellingProducts();
    }

    @Override
    public NctOrder nctSaveOrder(NctOrder order) {
        order.setNctUpdatedAt(LocalDateTime.now());
        return nctOrderRepository.save(order);
    }

    @Override
    public void nctDeleteOrder(Long orderId) {
        Optional<NctOrder> nctOrderOpt = nctOrderRepository.findById(orderId);
        if (nctOrderOpt.isPresent()) {
            NctOrder nctOrder = nctOrderOpt.get();

            if (nctOrder.getNctStatus() == NctOrder.NctOrderStatus.DELIVERED ||
                    nctOrder.getNctStatus() == NctOrder.NctOrderStatus.SHIPPING) {
                throw new RuntimeException("Không thể xóa đơn hàng đã giao hoặc đang giao");
            }

            if (nctOrder.getNctStatus() != NctOrder.NctOrderStatus.CANCELLED) {
                for (NctOrderItem nctOrderItem : nctOrder.getNctOrderItems()) {
                    NctProduct nctProduct = nctOrderItem.getNctProduct();
                    nctProduct.setNctStockQuantity(nctProduct.getNctStockQuantity() + nctOrderItem.getNctQuantity());
                    nctProductService.nctSaveProduct(nctProduct);
                }
            }

            nctOrderRepository.deleteById(orderId);
        } else {
            throw new RuntimeException("Đơn hàng không tồn tại");
        }
    }

    @Override
    public List<NctOrder> nctGetOrdersByStatus(NctOrder.NctOrderStatus status) {
        return nctOrderRepository.findByNctStatusOrderByNctCreatedAtDesc(status);
    }

    @Override
    public Page<NctOrder> nctFindPaginated(NctOrder.NctOrderStatus status, String keyword, Pageable pageable) {
        return nctOrderRepository.findWithFiltersAndPagination(status, keyword, pageable);
    }

    public List<NctOrder> nctGetOrdersByUserId(Long userId) {
        return nctOrderRepository.findByNctUser_NctUserIdOrderByNctCreatedAtDesc(userId);
    }

    public List<Object[]> nctGetMonthlyOrderStats() {
        return nctOrderRepository.getMonthlyOrderStats();
    }

    public NctOrder nctUpdateOrder(Long orderId, NctOrder orderDetails) {
        NctOrder existingOrder = nctOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        // Only allow address and phone to be updated if the order is still pending
        if (existingOrder.getNctStatus() == NctOrder.NctOrderStatus.PENDING) {
            existingOrder.setNctShippingAddress(orderDetails.getNctShippingAddress());
            existingOrder.setNctPhone(orderDetails.getNctPhone());
        }

        // Always allow the status to be updated
        existingOrder.setNctStatus(orderDetails.getNctStatus());
        existingOrder.setNctUpdatedAt(LocalDateTime.now());

        return nctOrderRepository.save(existingOrder);
    }


// ... inside NctOrderService class

    public long countOrdersCreatedBetween(LocalDateTime start, LocalDateTime end) {
        return nctGetAllOrders().stream()
                .filter(o -> o.getNctCreatedAt() != null && !o.getNctCreatedAt().isBefore(start) && o.getNctCreatedAt().isBefore(end))
                .count();
    }

    public double getRevenueBetween(LocalDateTime start, LocalDateTime end) {
        return nctGetAllOrders().stream()
                .filter(o -> o.getNctStatus() == NctOrder.NctOrderStatus.DELIVERED && o.getNctCreatedAt() != null && !o.getNctCreatedAt().isBefore(start) && o.getNctCreatedAt().isBefore(end))
                .mapToDouble(o -> o.getNctTotalAmount().doubleValue())
                .sum();
    }

}
