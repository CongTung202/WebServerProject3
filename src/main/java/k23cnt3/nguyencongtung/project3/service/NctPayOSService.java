package k23cnt3.nguyencongtung.project3.service;

import k23cnt3.nguyencongtung.project3.entity.NctOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;

@Service
public class NctPayOSService {

    @Autowired
    private PayOS payOS;

    public String createPaymentLink(NctOrder order) throws Exception {
        // 1. Tạo mã đơn hàng cho PayOS (Phải là số unique và là Long)
        // Kết hợp thời gian + ID đơn hàng để tránh trùng lặp
        long orderCode = Long.parseLong(String.valueOf(System.currentTimeMillis()).substring(0, 10) + order.getNctOrderId());

        // 2. Tạo Item (Mô tả đơn hàng)
        // LƯU Ý: Dùng .longValue() để chuyển BigDecimal sang long tránh lỗi
        PaymentLinkItem item = PaymentLinkItem.builder()
                .name("Thanh toán đơn hàng #" + order.getNctOrderId())
                .quantity(1)
                .price(order.getNctTotalAmount().longValue())
                .build();

        // 3. Tạo URL Callback
        // Quan trọng: Truyền ?nctOrderId=... để PaymentController biết đơn nào vừa thanh toán xong
        String domain = "http://localhost:8080";
        String returnUrl = domain + "/payment/payos-callback?nctOrderId=" + order.getNctOrderId();
        String cancelUrl = domain + "/payment/payos-cancel?nctOrderId=" + order.getNctOrderId();

        // 4. Tạo Request gửi sang PayOS
        CreatePaymentLinkRequest request = CreatePaymentLinkRequest.builder()
                .orderCode(orderCode)
                .amount(order.getNctTotalAmount().longValue())
                .description("Thanh toan don #" + order.getNctOrderId())
                .returnUrl(returnUrl)
                .cancelUrl(cancelUrl)
                .item(item)
                .build();

        // 5. Gọi API
        CreatePaymentLinkResponse response = payOS.paymentRequests().create(request);

        return response.getCheckoutUrl();
    }
}