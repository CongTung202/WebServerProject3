package k23cnt3.nguyencongtung.project3.entity;

        import jakarta.persistence.*;
        import lombok.Data;

        import java.math.BigDecimal;
        import java.time.LocalDateTime;

        @Entity
        @Table(name = "nct_cart_items")
        @Data
        public class NctCartItem {
            @Id
            @GeneratedValue(strategy = GenerationType.IDENTITY)
            @Column(name = "nct_cart_item_id")
            private Long nctCartItemId;

            @ManyToOne
            @JoinColumn(name = "nct_user_id", nullable = false)
            private NctUser nctUser;

            @ManyToOne
            @JoinColumn(name = "nct_product_id", nullable = false)
            private NctProduct nctProduct;

            @Column(name = "nct_quantity", nullable = false)
            private Integer nctQuantity = 1;

            @Column(name = "nct_created_at")
            private LocalDateTime nctCreatedAt = LocalDateTime.now();

            public NctCartItem(Long nctCartItemId, LocalDateTime nctCreatedAt, Integer nctQuantity, NctProduct nctProduct, NctUser nctUser) {
                this.nctCartItemId = nctCartItemId;
                this.nctCreatedAt = nctCreatedAt;
                this.nctQuantity = nctQuantity;
                this.nctProduct = nctProduct;
                this.nctUser = nctUser;
            }

            public Long getNctCartItemId() {
                return nctCartItemId;
            }

            public NctUser getNctUser() {
                return nctUser;
            }

            public NctProduct getNctProduct() {
                return nctProduct;
            }

            public void setNctCartItemId(Long nctCartItemId) {
                this.nctCartItemId = nctCartItemId;
            }

            public void setNctUser(NctUser nctUser) {
                this.nctUser = nctUser;
            }

            public void setNctProduct(NctProduct nctProduct) {
                this.nctProduct = nctProduct;
            }

            public void setNctQuantity(Integer nctQuantity) {
                this.nctQuantity = nctQuantity;
            }

            public void setNctCreatedAt(LocalDateTime nctCreatedAt) {
                this.nctCreatedAt = nctCreatedAt;
            }

            public LocalDateTime getNctCreatedAt() {
                return nctCreatedAt;
            }

            public Integer getNctQuantity() {
                return nctQuantity;
            }

            public NctCartItem() {
            }

            @Transient
            public BigDecimal getNctSubtotal() {
                if (nctProduct != null && nctProduct.getNctPrice() != null && nctQuantity != null) {
                    return nctProduct.getNctPrice().multiply(BigDecimal.valueOf(nctQuantity));
                }
                return BigDecimal.ZERO;
            }
        }