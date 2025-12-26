package k23cnt3.nguyencongtung.project3.service;

import k23cnt3.nguyencongtung.project3.entity.NctCartItem;
import k23cnt3.nguyencongtung.project3.entity.NctUser;
import k23cnt3.nguyencongtung.project3.entity.NctProduct;
import k23cnt3.nguyencongtung.project3.repository.NctCartItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class NctCartService {
    private final NctCartItemRepository nctCartItemRepository;
    private final NctProductService nctProductService;

    @Autowired
    public NctCartService(NctCartItemRepository nctCartItemRepository, NctProductService nctProductService) {
        this.nctCartItemRepository = nctCartItemRepository;
        this.nctProductService = nctProductService;
    }
    public List<NctCartItem> nctGetCartItems(NctUser nctUser) {
        return nctCartItemRepository.findByNctUser(nctUser);
    }

    public NctCartItem nctAddToCart(NctUser nctUser, Long nctProductId, Integer nctQuantity) {
        NctProduct nctProduct = nctProductService.nctGetProductById(nctProductId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        Optional<NctCartItem> nctExistingCartItem = nctCartItemRepository.findByNctUserAndNctProduct(nctUser, nctProduct);

        int totalQuantityRequired = nctQuantity;
        if (nctExistingCartItem.isPresent()) {
            totalQuantityRequired += nctExistingCartItem.get().getNctQuantity();
        }

        if (!nctProductService.nctIsProductInStock(nctProductId, totalQuantityRequired)) {
            throw new RuntimeException("Sản phẩm không đủ số lượng trong kho.");
        }

        if (nctExistingCartItem.isPresent()) {
            NctCartItem nctCartItem = nctExistingCartItem.get();
            nctCartItem.setNctQuantity(nctCartItem.getNctQuantity() + nctQuantity);
            return nctCartItemRepository.save(nctCartItem);
        } else {
            NctCartItem nctNewCartItem = new NctCartItem();
            nctNewCartItem.setNctUser(nctUser);
            nctNewCartItem.setNctProduct(nctProduct);
            nctNewCartItem.setNctQuantity(nctQuantity);
            return nctCartItemRepository.save(nctNewCartItem);
        }
    }

    public NctCartItem nctUpdateCartItem(NctUser nctUser, Long nctCartItemId, Integer nctQuantity) {
        NctCartItem nctCartItem = nctCartItemRepository.findByNctCartItemIdAndNctUser(nctCartItemId, nctUser)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mục giỏ hàng hoặc bạn không có quyền truy cập."));

        if (nctQuantity <= 0) {
            nctCartItemRepository.delete(nctCartItem);
            return null;
        } else {
            if (!nctProductService.nctIsProductInStock(nctCartItem.getNctProduct().getNctProductId(), nctQuantity)) {
                throw new RuntimeException("Sản phẩm không đủ số lượng trong kho.");
            }
            nctCartItem.setNctQuantity(nctQuantity);
            return nctCartItemRepository.save(nctCartItem);
        }
    }

    public void nctRemoveFromCart(NctUser nctUser, Long nctCartItemId) {
        NctCartItem nctCartItem = nctCartItemRepository.findByNctCartItemIdAndNctUser(nctCartItemId, nctUser)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mục giỏ hàng hoặc bạn không có quyền truy cập."));
        nctCartItemRepository.delete(nctCartItem);
    }

    public void nctClearCart(NctUser nctUser) {
        nctCartItemRepository.deleteByNctUserId(nctUser.getNctUserId());
    }

    public Integer nctGetCartItemCount(NctUser nctUser) {
        if (nctUser == null) return 0;
        Integer count = nctCartItemRepository.countNctCartItemsByNctUser(nctUser.getNctUserId());
        return count != null ? count : 0;
    }

    public Double nctCalculateCartTotal(NctUser nctUser) {
        List<NctCartItem> nctCartItems = nctGetCartItems(nctUser);
        return nctCartItems.stream()
                .mapToDouble(item -> item.getNctProduct().getNctPrice().doubleValue() * item.getNctQuantity())
                .sum();
    }
    // Trong NctCartService.java
    public void nctAddProductToCart(NctUser user, Long productId, Integer quantity) {
        // Gọi lại hàm nctAddToCart đã được viết đầy đủ logic ở phía trên
        nctAddToCart(user, productId, quantity);
    }
}
