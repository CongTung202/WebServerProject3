package k23cnt3.nguyencongtung.project3.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "nct_orders")
@Data
public class NctOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nct_order_id")
    private Long nctOrderId;

    @ManyToOne
    @JoinColumn(name = "nct_user_id", nullable = false)
    private NctUser nctUser;

    @Column(name = "nct_total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal nctTotalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "nct_status")
    private NctOrderStatus nctStatus = NctOrderStatus.PENDING;

    @Column(name = "nct_shipping_address", nullable = false, columnDefinition = "TEXT")
    private String nctShippingAddress;

    @Column(name = "nct_phone", nullable = false)
    private String nctPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "nct_payment_method")
    private NctPaymentMethod nctPaymentMethod = NctPaymentMethod.COD;

    @Column(name = "nct_created_at")
    private LocalDateTime nctCreatedAt = LocalDateTime.now();

    @Column(name = "nct_updated_at")
    private LocalDateTime nctUpdatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "nctOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<NctOrderItem> nctOrderItems = new ArrayList<>();

    public enum NctOrderStatus {
        PENDING("Chờ xác nhận"),
        CONFIRMED("Đã xác nhận"),
        SHIPPING("Đang giao hàng"),
        DELIVERED("Đã giao hàng"),
        CANCELLED("Đã hủy");

        private final String displayName;

        NctOrderStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum NctPaymentMethod {
        COD, BANKING
    }

    // Constructors
    public NctOrder() {
    }

    public NctOrder(Long nctOrderId, NctUser nctUser, NctOrderStatus nctStatus, BigDecimal nctTotalAmount,
                    String nctShippingAddress, NctPaymentMethod nctPaymentMethod, String nctPhone,
                    LocalDateTime nctCreatedAt, LocalDateTime nctUpdatedAt, List<NctOrderItem> nctOrderItems) {
        this.nctOrderId = nctOrderId;
        this.nctUser = nctUser;
        this.nctStatus = nctStatus;
        this.nctTotalAmount = nctTotalAmount;
        this.nctShippingAddress = nctShippingAddress;
        this.nctPaymentMethod = nctPaymentMethod;
        this.nctPhone = nctPhone;
        this.nctCreatedAt = nctCreatedAt;
        this.nctUpdatedAt = nctUpdatedAt;
        this.nctOrderItems = nctOrderItems;
    }

    // Getter và Setter
    public Long getNctOrderId() {
        return nctOrderId;
    }

    public void setNctOrderId(Long nctOrderId) {
        this.nctOrderId = nctOrderId;
    }

    public NctUser getNctUser() {
        return nctUser;
    }

    public void setNctUser(NctUser nctUser) {
        this.nctUser = nctUser;
    }

    public BigDecimal getNctTotalAmount() {
        return nctTotalAmount;
    }

    public void setNctTotalAmount(BigDecimal nctTotalAmount) {
        this.nctTotalAmount = nctTotalAmount;
    }

    public NctOrderStatus getNctStatus() {
        return nctStatus;
    }

    public void setNctStatus(NctOrderStatus nctStatus) {
        this.nctStatus = nctStatus;
    }

    public String getNctShippingAddress() {
        return nctShippingAddress;
    }

    public void setNctShippingAddress(String nctShippingAddress) {
        this.nctShippingAddress = nctShippingAddress;
    }

    public String getNctPhone() {
        return nctPhone;
    }

    public void setNctPhone(String nctPhone) {
        this.nctPhone = nctPhone;
    }

    public NctPaymentMethod getNctPaymentMethod() {
        return nctPaymentMethod;
    }

    public void setNctPaymentMethod(NctPaymentMethod nctPaymentMethod) {
        this.nctPaymentMethod = nctPaymentMethod;
    }

    public LocalDateTime getNctCreatedAt() {
        return nctCreatedAt;
    }

    public void setNctCreatedAt(LocalDateTime nctCreatedAt) {
        this.nctCreatedAt = nctCreatedAt;
    }

    public LocalDateTime getNctUpdatedAt() {
        return nctUpdatedAt;
    }

    public void setNctUpdatedAt(LocalDateTime nctUpdatedAt) {
        this.nctUpdatedAt = nctUpdatedAt;
    }

    public List<NctOrderItem> getNctOrderItems() {
        return nctOrderItems;
    }

    public void setNctOrderItems(List<NctOrderItem> nctOrderItems) {
        this.nctOrderItems = nctOrderItems;
    }

    // Thêm method tiện ích
    @Transient
    public int getNctItemCount() {
        return nctOrderItems != null ? nctOrderItems.stream()
                .mapToInt(NctOrderItem::getNctQuantity)
                .sum() : 0;
    }

    @Transient
    public String getNctFormattedTotal() {
        return "₫" + String.format("%,.0f", nctTotalAmount.doubleValue());
    }

    // Method hiển thị tên trạng thái
    @Transient
    public String getNctStatusDisplayName() {
        return this.nctStatus != null ? this.nctStatus.getDisplayName() : "Không xác định";
    }

    // Method hiển thị tên phương thức thanh toán
    @Transient
    public String getNctPaymentMethodDisplayName() {
        switch (this.nctPaymentMethod) {
            case COD: return "Thanh toán khi nhận hàng";
            case BANKING: return "Chuyển khoản ngân hàng";
            default: return "Không xác định";
        }
    }

    // Method kiểm tra trạng thái
    @Transient
    public boolean isNctCancellable() {
        return this.nctStatus == NctOrderStatus.PENDING || this.nctStatus == NctOrderStatus.CONFIRMED;
    }

    @Transient
    public boolean isNctEditable() {
        return this.nctStatus == NctOrderStatus.PENDING;
    }

    @Transient
    public boolean isNctStatusUpdatable() {
        return this.nctStatus != NctOrderStatus.DELIVERED && this.nctStatus != NctOrderStatus.CANCELLED;
    }

    // Method lấy màu cho trạng thái
    @Transient
    public String getNctStatusColor() {
        switch (this.nctStatus) {
            case PENDING: return "warning";
            case CONFIRMED: return "info";
            case SHIPPING: return "primary";
            case DELIVERED: return "success";
            case CANCELLED: return "danger";
            default: return "secondary";
        }
    }
}
