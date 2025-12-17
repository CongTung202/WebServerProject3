package k23cnt3.nguyencongtung.project3.controller.auth;

import k23cnt3.nguyencongtung.project3.model.MomoPaymentResponse;
import k23cnt3.nguyencongtung.project3.service.MomoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
public class PaymentControllererror {

    @Autowired
    private MomoService momoService;

    // API 1: Tạo giao dịch thanh toán (Gửi yêu cầu sang MoMo)
    // Frontend gọi vào đây kèm số tiền và mã đơn
    @PostMapping("/create")
    public ResponseEntity<?> createPayment(
            @RequestParam String orderId,
            @RequestParam String fullName,
            @RequestParam String amount
    ) {
        MomoPaymentResponse response = momoService.createPayment(orderId, fullName, amount);

        if (response != null && response.getPayUrl() != null) {
            // Trả về đối tượng JSON chứa link thanh toán (payUrl)
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Không thể tạo link thanh toán. Vui lòng kiểm tra lại cấu hình.");
        }
    }

    // API 2: Xử lý kết quả trả về từ MoMo (Redirect URL)
    // Sau khi thanh toán xong, MoMo sẽ chuyển hướng user về link này
    // Cấu hình trong file properties: momo.redirect-url=http://localhost:8080/api/payment/result
    @GetMapping("/result")
    public ResponseEntity<?> paymentResult(
            @RequestParam(required = false) String partnerCode,
            @RequestParam(required = false) String orderId,
            @RequestParam(required = false) String requestId,
            @RequestParam(required = false) String amount,
            @RequestParam(required = false) String orderInfo,
            @RequestParam(required = false) String orderType,
            @RequestParam(required = false) String transId,
            @RequestParam(required = false) String resultCode,
            @RequestParam(required = false) String message,
            @RequestParam(required = false) String payType,
            @RequestParam(required = false) String responseTime,
            @RequestParam(required = false) String extraData,
            @RequestParam(required = false) String signature
    ) {
        // Kiểm tra kết quả (resultCode = 0 là thành công)
        if ("0".equals(resultCode)) {
            return ResponseEntity.ok("THANH TOÁN THÀNH CÔNG! Đơn hàng: " + orderId);
            // Ở đây bạn có thể update trạng thái đơn hàng trong DB thành 'PAID'
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("THANH TOÁN THẤT BẠI. Lỗi: " + message);
        }
    }

    // API 3: IPN (Instant Payment Notification)
    // MoMo gọi ngầm vào đây để báo kết quả (dành cho server-to-server)
    // Cấu hình: momo.ipn-url=http://.../api/payment/ipn-momo
    @PostMapping("/ipn-momo")
    public void ipnMomo(@RequestBody Object data) {
        // Vì đang chạy localhost nên MoMo không gọi vào đây được (cần deploy lên host thật)
        // Nhưng vẫn để sẵn hàm để sau này xử lý
        System.out.println("MoMo IPN received: " + data);
    }
}
