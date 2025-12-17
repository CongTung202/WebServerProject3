package k23cnt3.nguyencongtung.project3.controller.user;

import k23cnt3.nguyencongtung.project3.entity.NctOrder;
import k23cnt3.nguyencongtung.project3.entity.NctOrderItem;
import k23cnt3.nguyencongtung.project3.service.NctOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class PaymentController {

    @Autowired
    private NctOrderService nctOrderService;

    @GetMapping("/payment/callback")
    public String handleZaloPayCallback(
            @RequestParam(name = "nctOrderId") Long orderId,
            @RequestParam(name = "status") int status,
            @RequestParam(name = "amount") long amount,
            @RequestParam(name = "apptransid", required = false) String appTransId
    ) {
        // Lấy đơn hàng từ DB
        NctOrder order = nctOrderService.nctGetOrderById(orderId).orElse(null);

        if (order != null) {
            // TRƯỜNG HỢP 1: THANH TOÁN THÀNH CÔNG
            if (status == 1) {
                order.setNctStatus(NctOrder.NctOrderStatus.CONFIRMED);
                nctOrderService.nctSaveOrder(order);

                // Chuyển về trang chi tiết đơn hàng
                // Đảm bảo bạn có Controller hứng URL này: /orders/detail/{id}
                return "redirect:/orders/" + orderId;
            }

            // TRƯỜNG HỢP 2: THANH TOÁN THẤT BẠI / HỦY
            else {
                // (Tùy chọn) Cập nhật trạng thái đơn hàng thành Hủy
                order.setNctStatus(NctOrder.NctOrderStatus.CANCELLED);
                nctOrderService.nctSaveOrder(order);

                // Tìm ID sản phẩm để quay về
                // Logic: Lấy sản phẩm đầu tiên trong đơn hàng để redirect
                List<NctOrderItem> items = order.getNctOrderItems();
                if (items != null && !items.isEmpty()) {
                    Long productId = items.get(0).getNctProduct().getNctProductId();

                    // Chuyển về trang chi tiết sản phẩm
                    // LƯU Ý: Kiểm tra lại URL chi tiết sản phẩm của bạn là "/productdetails/" hay "/products/"
                    return "redirect:/products/" + productId;
                }
            }
        }

        // Fallback: Nếu không tìm thấy đơn hoặc lỗi, quay về trang chủ
        return "redirect:/";
    }
}