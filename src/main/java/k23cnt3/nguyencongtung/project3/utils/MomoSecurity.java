package k23cnt3.nguyencongtung.project3.utils;

import k23cnt3.nguyencongtung.project3.config.MomoConfig;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class MomoSecurity {

    private final MomoConfig momoConfig;

    public MomoSecurity(MomoConfig momoConfig) {
        this.momoConfig = momoConfig;
    }

    /**
     * Hàm ký tên (HmacSHA256)
     * @param message Chuỗi dữ liệu cần ký (phải đúng thứ tự a-z của key)
     * @param secretKey Key bí mật lấy từ MoMo
     * @return Chuỗi chữ ký đã mã hóa
     */
    public String signSHA256(String message, String secretKey) throws Exception {
        try {
            // 1. Tạo key spec từ secret key
            SecretKeySpec signingKey = new SecretKeySpec(
                    secretKey.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );

            // 2. Khởi tạo thuật toán
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);

            // 3. Mã hóa message và chuyển sang chuỗi Hex
            byte[] rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Hex.encodeHexString(rawHmac);

        } catch (Exception e) {
            throw new Exception("Lỗi tạo chữ ký MoMo: " + e.getMessage());
        }
    }
}