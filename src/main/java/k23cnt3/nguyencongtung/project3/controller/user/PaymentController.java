package k23cnt3.nguyencongtung.project3.controller.user;

import k23cnt3.nguyencongtung.project3.entity.NctOrder;
import k23cnt3.nguyencongtung.project3.service.NctOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PaymentController {

    @Autowired
    private NctOrderService nctOrderService;

    // --- XỬ LÝ KHI THANH TOÁN THÀNH CÔNG (PAYOS) ---
    @GetMapping("/payment/payos-callback")
    public String handlePayOSCallback(
            @RequestParam(name = "nctOrderId") Long nctOrderId, // Lấy từ URL do Service tạo
            @RequestParam(name = "code") String code,
            @RequestParam(name = "id") String id,
            @RequestParam(name = "cancel") boolean cancel,
            @RequestParam(name = "status") String status,
            @RequestParam(name = "orderCode") long orderCode) {

        // 1. Tìm đơn hàng
        NctOrder order = nctOrderService.nctGetOrderById(nctOrderId).orElse(null);

        if (order != null) {
            // PayOS trả về code "00" là thành công
            if ("00".equals(code) && "PAID".equals(status) && !cancel) {
                // Cập nhật trạng thái đơn hàng thành ĐÃ XÁC NHẬN (CONFIRMED) hoặc ĐÃ THANH TOÁN
                order.setNctStatus(NctOrder.NctOrderStatus.CONFIRMED);
                nctOrderService.nctSaveOrder(order);

                // Chuyển hướng về trang chi tiết đơn hàng
                return "redirect:/orders/" + nctOrderId;
            }
        }

        // Nếu lỗi hoặc không tìm thấy đơn, về trang danh sách
        return "redirect:/orders?error=payment_failed";
    }

    // --- XỬ LÝ KHI NGƯỜI DÙNG HỦY (PAYOS) ---
    @GetMapping("/payment/payos-cancel")
    public String handlePayOSCancel(@RequestParam(name = "nctOrderId") Long nctOrderId) {
        NctOrder order = nctOrderService.nctGetOrderById(nctOrderId).orElse(null);
        if (order != null) {
            // Cập nhật trạng thái là ĐÃ HỦY
            order.setNctStatus(NctOrder.NctOrderStatus.CANCELLED);
            nctOrderService.nctSaveOrder(order);

            // Có thể redirect về trang chi tiết để báo hủy
            return "redirect:/orders/" + nctOrderId;
        }
        return "redirect:/orders";
    }
}