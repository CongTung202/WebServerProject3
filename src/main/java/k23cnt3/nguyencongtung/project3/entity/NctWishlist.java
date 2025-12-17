package k23cnt3.nguyencongtung.project3.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "nct_wishlist")
@Data
public class NctWishlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nct_wishlist_id")
    private Long nctWishlistId;

    @ManyToOne
    @JoinColumn(name = "nct_user_id", nullable = false)
    private NctUser nctUser;

    public Long getNctWishlistId() {
        return nctWishlistId;
    }

    public void setNctWishlistId(Long nctWishlistId) {
        this.nctWishlistId = nctWishlistId;
    }

    public NctWishlist(Long nctWishlistId, NctUser nctUser, NctProduct nctProduct, LocalDateTime nctCreatedAt) {
        this.nctWishlistId = nctWishlistId;
        this.nctUser = nctUser;
        this.nctProduct = nctProduct;
        this.nctCreatedAt = nctCreatedAt;
    }

    public void setNctUser(NctUser nctUser) {
        this.nctUser = nctUser;
    }

    public void setNctProduct(NctProduct nctProduct) {
        this.nctProduct = nctProduct;
    }

    public void setNctCreatedAt(LocalDateTime nctCreatedAt) {
        this.nctCreatedAt = nctCreatedAt;
    }

    public NctUser getNctUser() {
        return nctUser;
    }

    public NctProduct getNctProduct() {
        return nctProduct;
    }

    public LocalDateTime getNctCreatedAt() {
        return nctCreatedAt;
    }

    public NctWishlist() {
    }

    @ManyToOne
    @JoinColumn(name = "nct_product_id", nullable = false)
    private NctProduct nctProduct;

    @Column(name = "nct_created_at")
    private LocalDateTime nctCreatedAt = LocalDateTime.now();
}