package k23cnt3.nguyencongtung.project3.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "nct_products")
@Data
public class NctProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nct_product_id")
    private Long nctProductId;

    @Column(name = "nct_product_name", nullable = false)
    private String nctProductName;

    @Column(name = "nct_description", columnDefinition = "TEXT")
    private String nctDescription;

    @Column(name = "nct_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal nctPrice;

    @Column(name = "nct_original_price", precision = 10, scale = 2)
    private BigDecimal nctOriginalPrice;

    @Column(name = "nct_stock_quantity")
    private Integer nctStockQuantity = 0;

    @Column(name = "nct_image_url")
    private String nctImageUrl;

    @ManyToOne
    @JoinColumn(name = "nct_category_id")
    private NctCategory nctCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "nct_status")
    private NctStatus nctStatus = NctStatus.ACTIVE;

    @Column(name = "nct_created_at")
    private LocalDateTime nctCreatedAt = LocalDateTime.now();

    @Column(name = "nct_updated_at")
    private LocalDateTime nctUpdatedAt = LocalDateTime.now();

    public enum NctStatus {
        ACTIVE, INACTIVE
    }

    public NctProduct(String nctDescription, Long nctProductId, String nctProductName, BigDecimal nctPrice, BigDecimal nctOriginalPrice, Integer nctStockQuantity, String nctImageUrl, NctCategory nctCategory, NctStatus nctStatus, LocalDateTime nctCreatedAt, LocalDateTime nctUpdatedAt) {
        this.nctDescription = nctDescription;
        this.nctProductId = nctProductId;
        this.nctProductName = nctProductName;
        this.nctPrice = nctPrice;
        this.nctOriginalPrice = nctOriginalPrice;
        this.nctStockQuantity = nctStockQuantity;
        this.nctImageUrl = nctImageUrl;
        this.nctCategory = nctCategory;
        this.nctStatus = nctStatus;
        this.nctCreatedAt = nctCreatedAt;
        this.nctUpdatedAt = nctUpdatedAt;
    }

    public Long getNctProductId() {
        return nctProductId;
    }

    public void setNctProductId(Long nctProductId) {
        this.nctProductId = nctProductId;
    }

    public String getNctProductName() {
        return nctProductName;
    }

    public void setNctProductName(String nctProductName) {
        this.nctProductName = nctProductName;
    }

    public String getNctDescription() {
        return nctDescription;
    }

    public void setNctDescription(String nctDescription) {
        this.nctDescription = nctDescription;
    }

    public BigDecimal getNctPrice() {
        return nctPrice;
    }

    public void setNctPrice(BigDecimal nctPrice) {
        this.nctPrice = nctPrice;
    }

    public BigDecimal getNctOriginalPrice() {
        return nctOriginalPrice;
    }

    public void setNctOriginalPrice(BigDecimal nctOriginalPrice) {
        this.nctOriginalPrice = nctOriginalPrice;
    }

    public Integer getNctStockQuantity() {
        return nctStockQuantity;
    }

    public void setNctStockQuantity(Integer nctStockQuantity) {
        this.nctStockQuantity = nctStockQuantity;
    }

    public String getNctImageUrl() {
        return nctImageUrl;
    }

    public void setNctImageUrl(String nctImageUrl) {
        this.nctImageUrl = nctImageUrl;
    }

    public NctCategory getNctCategory() {
        return nctCategory;
    }

    public void setNctCategory(NctCategory nctCategory) {
        this.nctCategory = nctCategory;
    }

    public NctStatus getNctStatus() {
        return nctStatus;
    }

    public void setNctStatus(NctStatus nctStatus) {
        this.nctStatus = nctStatus;
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

    public NctProduct() {
    }
}