package k23cnt3.nguyencongtung.project3.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "nct_categories")
@Data
public class NctCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nct_category_id")
    private Long nctCategoryId;

    @Column(name = "nct_category_name", nullable = false)
    private String nctCategoryName;

    @Column(name = "nct_description", columnDefinition = "TEXT")
    private String nctDescription;

    @Column(name = "nct_image_url")
    private String nctImageUrl;

    @Column(name = "nct_created_at")
    private LocalDateTime nctCreatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "nctCategory", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<NctProduct> nctProducts = new ArrayList<>();

    public Long getNctCategoryId() {
        return nctCategoryId;
    }

    public String getNctCategoryName() {
        return nctCategoryName;
    }

    public String getNctDescription() {
        return nctDescription;
    }

    public LocalDateTime getNctCreatedAt() {
        return nctCreatedAt;
    }

    public String getNctImageUrl() {
        return nctImageUrl;
    }

    public void setNctCategoryId(Long nctCategoryId) {
        this.nctCategoryId = nctCategoryId;
    }

    public void setNctProducts(List<NctProduct> nctProducts) {
        this.nctProducts = nctProducts;
    }

    public void setNctCreatedAt(LocalDateTime nctCreatedAt) {
        this.nctCreatedAt = nctCreatedAt;
    }

    public void setNctImageUrl(String nctImageUrl) {
        this.nctImageUrl = nctImageUrl;
    }

    public void setNctDescription(String nctDescription) {
        this.nctDescription = nctDescription;
    }

    public void setNctCategoryName(String nctCategoryName) {
        this.nctCategoryName = nctCategoryName;
    }

    public List<NctProduct> getNctProducts() {
        return nctProducts;
    }

    public NctCategory(Long nctCategoryId, List<NctProduct> nctProducts, LocalDateTime nctCreatedAt, String nctImageUrl, String nctDescription, String nctCategoryName) {
        this.nctCategoryId = nctCategoryId;
        this.nctProducts = nctProducts;
        this.nctCreatedAt = nctCreatedAt;
        this.nctImageUrl = nctImageUrl;
        this.nctDescription = nctDescription;
        this.nctCategoryName = nctCategoryName;
    }

    public NctCategory() {
    }
}