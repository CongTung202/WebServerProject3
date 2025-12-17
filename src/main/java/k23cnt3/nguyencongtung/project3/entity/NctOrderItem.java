package k23cnt3.nguyencongtung.project3.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "nct_order_items")
@Data
public class NctOrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nct_order_item_id")
    private Long nctOrderItemId;

    @ManyToOne
    @JoinColumn(name = "nct_order_id", nullable = false)
    private NctOrder nctOrder;

    @ManyToOne
    @JoinColumn(name = "nct_product_id", nullable = false)
    private NctProduct nctProduct;

    @Column(name = "nct_quantity", nullable = false)
    private Integer nctQuantity;

    @Column(name = "nct_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal nctPrice;

    @Column(name = "nct_created_at")
    private LocalDateTime nctCreatedAt = LocalDateTime.now();

    public NctOrderItem() {
    }

    public Long getNctOrderItemId() {
        return nctOrderItemId;
    }

    public void setNctOrderItemId(Long nctOrderItemId) {
        this.nctOrderItemId = nctOrderItemId;
    }

    public NctOrder getNctOrder() {
        return nctOrder;
    }

    public void setNctOrder(NctOrder nctOrder) {
        this.nctOrder = nctOrder;
    }

    public NctProduct getNctProduct() {
        return nctProduct;
    }

    public void setNctProduct(NctProduct nctProduct) {
        this.nctProduct = nctProduct;
    }

    public Integer getNctQuantity() {
        return nctQuantity;
    }

    public void setNctQuantity(Integer nctQuantity) {
        this.nctQuantity = nctQuantity;
    }

    public BigDecimal getNctPrice() {
        return nctPrice;
    }

    public void setNctPrice(BigDecimal nctPrice) {
        this.nctPrice = nctPrice;
    }

    public LocalDateTime getNctCreatedAt() {
        return nctCreatedAt;
    }

    public void setNctCreatedAt(LocalDateTime nctCreatedAt) {
        this.nctCreatedAt = nctCreatedAt;
    }

    public NctOrderItem(Long nctOrderItemId, NctOrder nctOrder, NctProduct nctProduct, Integer nctQuantity, BigDecimal nctPrice, LocalDateTime nctCreatedAt) {
        this.nctOrderItemId = nctOrderItemId;
        this.nctOrder = nctOrder;
        this.nctProduct = nctProduct;
        this.nctQuantity = nctQuantity;
        this.nctPrice = nctPrice;
        this.nctCreatedAt = nctCreatedAt;
    }
}