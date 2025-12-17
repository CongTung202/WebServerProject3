package k23cnt3.nguyencongtung.project3.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MomoPaymentResponse {
    private String partnerCode;
    private String orderId;
    private String requestId;
    private String amount;
    private String responseTime;
    private String message;
    private int resultCode;       // 0 = Thành công, Khác 0 = Lỗi
    private String payUrl;        // Link thanh toán (Redirect user vào đây)
    private String shortLink;     // Link rút gọn (cho Mobile)
}