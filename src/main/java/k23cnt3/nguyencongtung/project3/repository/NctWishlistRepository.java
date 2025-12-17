package k23cnt3.nguyencongtung.project3.repository;

import k23cnt3.nguyencongtung.project3.entity.NctProduct;
import k23cnt3.nguyencongtung.project3.entity.NctUser;
import k23cnt3.nguyencongtung.project3.entity.NctWishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NctWishlistRepository extends JpaRepository<NctWishlist, Long> {
    List<NctWishlist> findByNctUser(NctUser nctUser);

    boolean existsByNctUserAndNctProduct(NctUser nctUser, NctProduct nctProduct);

    @Transactional
    void deleteByNctUserAndNctProduct_NctProductId(NctUser nctUser, Long nctProductId);
}