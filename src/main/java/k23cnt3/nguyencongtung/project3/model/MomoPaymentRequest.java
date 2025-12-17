package k23cnt3.nguyencongtung.project3.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data                // Tự sinh Getter/Setter
@AllArgsConstructor  // Tự sinh Constructor có tham số
@NoArgsConstructor   // Tự sinh Constructor không tham số
public class MomoPaymentRequest {
    private String partnerCode;   // Mã đối tác
    private String orderId;       // Mã đơn hàng bên bạn
    private String requestId;     // Mã yêu cầu (thường giống orderId)
    private String amount;        // Số tiền (String, không phải int/long)
    private String orderInfo;     // Mô tả đơn hàng
    private String redirectUrl;   // Quay về web bạn khi thanh toán xong
    private String ipnUrl;        // MoMo gọi vào đây để báo kết quả
    private String requestType;   // Mặc định: captureWallet
    private String extraData;     // Dữ liệu thêm (email, sđt...)
    private String lang = "vi";   // Ngôn ngữ: vi hoặc en
    private String signature;     // Chữ ký bảo mật (quan trọng nhất)
}