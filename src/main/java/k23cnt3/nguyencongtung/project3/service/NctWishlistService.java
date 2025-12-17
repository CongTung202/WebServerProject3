package k23cnt3.nguyencongtung.project3.service;

    import k23cnt3.nguyencongtung.project3.entity.NctProduct;
    import k23cnt3.nguyencongtung.project3.entity.NctUser;
    import k23cnt3.nguyencongtung.project3.entity.NctWishlist;
    import k23cnt3.nguyencongtung.project3.repository.NctProductRepository;
    import k23cnt3.nguyencongtung.project3.repository.NctWishlistRepository;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    import java.util.List;
    import java.util.Optional;

    @Service
    public class NctWishlistService {

        private final NctWishlistRepository nctWishlistRepository;
        private final NctProductRepository nctProductRepository;

        @Autowired
        public NctWishlistService(NctWishlistRepository nctWishlistRepository, NctProductRepository nctProductRepository) {
            this.nctWishlistRepository = nctWishlistRepository;
            this.nctProductRepository = nctProductRepository;
        }

        public List<NctWishlist> nctGetWishlist(NctUser nctUser) {
            return nctWishlistRepository.findByNctUser(nctUser);
        }

        @Transactional
        public boolean nctAddToWishlist(NctUser nctUser, Long nctProductId) {
            Optional<NctProduct> productOpt = nctProductRepository.findById(nctProductId);
            if (productOpt.isPresent()) {
                NctProduct product = productOpt.get();
                if (!nctWishlistRepository.existsByNctUserAndNctProduct(nctUser, product)) {
                    NctWishlist wishlistItem = new NctWishlist();
                    wishlistItem.setNctUser(nctUser);
                    wishlistItem.setNctProduct(product);
                    nctWishlistRepository.save(wishlistItem);
                    return true;
                }
            }
            return false;
        }

        @Transactional
        public void nctRemoveFromWishlist(NctUser nctUser, Long nctProductId) {
            nctWishlistRepository.deleteByNctUserAndNctProduct_NctProductId(nctUser, nctProductId);
        }
    }