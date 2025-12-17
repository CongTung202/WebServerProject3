package k23cnt3.nguyencongtung.project3.service;

import k23cnt3.nguyencongtung.project3.config.MomoConfig;
import k23cnt3.nguyencongtung.project3.model.MomoPaymentRequest;
import k23cnt3.nguyencongtung.project3.model.MomoPaymentResponse;
import k23cnt3.nguyencongtung.project3.utils.MomoSecurity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class MomoService {

    @Autowired
    private MomoConfig momoConfig;

    /**
     * Tạo link thanh toán MoMo
     * @param orderId Mã đơn hàng của bạn (duy nhất)
     * @param fullName Tên khách hàng (để hiển thị chơi thôi)
     * @param amountStr Số tiền (dạng chuỗi, ví dụ "50000")
     * @return Đối tượng chứa link thanh toán (payUrl)
     */
    public MomoPaymentResponse createPayment(String orderId, String fullName, String amountStr) {
        try {
            // 1. Các tham số cơ bản
            String requestId = String.valueOf(System.currentTimeMillis()); // Mã request ngẫu nhiên
            String requestType = "captureWallet";
            String extraData = ""; // Có thể lưu tên KH vào đây nếu cần (encode Base64)
            String orderInfo = "Thanh toan don hang " + orderId + " boi " + fullName;

            // 2. Tạo chuỗi dữ liệu RAW để ký (QUAN TRỌNG: Phải đúng thứ tự Alphabet)
            // accessKey -> amount -> extraData -> ipnUrl -> orderId -> orderInfo
            // -> partnerCode -> redirectUrl -> requestId -> requestType
            String rawSignature = "accessKey=" + momoConfig.getAccessKey()
                    + "&amount=" + amountStr
                    + "&extraData=" + extraData
                    + "&ipnUrl=" + momoConfig.getIpnUrl()
                    + "&orderId=" + orderId
                    + "&orderInfo=" + orderInfo
                    + "&partnerCode=" + momoConfig.getPartnerCode()
                    + "&redirectUrl=" + momoConfig.getRedirectUrl()
                    + "&requestId=" + requestId
                    + "&requestType=" + requestType;

            // 3. Tạo chữ ký (Signature)
            MomoSecurity crypto = new MomoSecurity(momoConfig);
            String signature = crypto.signSHA256(rawSignature, momoConfig.getSecretKey());

            // 4. Tạo đối tượng Request để gửi đi
            MomoPaymentRequest requestDTO = new MomoPaymentRequest();
            requestDTO.setPartnerCode(momoConfig.getPartnerCode());
            requestDTO.setOrderId(orderId);
            requestDTO.setRequestId(requestId);
            requestDTO.setAmount(amountStr);
            requestDTO.setOrderInfo(orderInfo);
            requestDTO.setRedirectUrl(momoConfig.getRedirectUrl());
            requestDTO.setIpnUrl(momoConfig.getIpnUrl());
            requestDTO.setRequestType(requestType);
            requestDTO.setExtraData(extraData);
            requestDTO.setLang("vi");
            requestDTO.setSignature(signature);

            // 5. Gửi Request sang MoMo bằng RestTemplate
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<MomoPaymentRequest> entity = new HttpEntity<>(requestDTO, headers);

            // Gọi API
            ResponseEntity<MomoPaymentResponse> response = restTemplate.postForEntity(
                    momoConfig.getApiUrl(),
                    entity,
                    MomoPaymentResponse.class
            );

            // 6. Trả về kết quả
            return response.getBody();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Lỗi tạo thanh toán MoMo: " + e.getMessage());
            return null;
        }
    }
}