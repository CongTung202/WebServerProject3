package k23cnt3.nguyencongtung.project3.service;

import k23cnt3.nguyencongtung.project3.config.ZaloPayConfig;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ZaloPayService {

    @Autowired
    private ZaloPayConfig zalopayConfig;

    public Map<String, Object> createOrder(int amount, String returnUrl) {
        Map<String, Object> result = new HashMap<>();
        try {
            String randomUUID = UUID.randomUUID().toString();
            String appTransId = getCurrentDateString() + "_" + randomUUID.substring(0, 7);
            long appTime = System.currentTimeMillis();
            String appUser = "DemoUser";
            String item = "[]";

            // [QUAN TRỌNG] Tạo embed_data chứa redirecturl
            // Cấu trúc JSON: {"redirecturl": "http://..."}
            // Lưu ý: redirecturl phải là chữ thường, viết liền
            String embedData = "{\"redirecturl\": \"" + returnUrl + "\"}";

            // 1. Tạo Map dữ liệu
            MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
            requestBody.add("app_id", zalopayConfig.getAppId());
            requestBody.add("app_user", appUser);
            requestBody.add("app_time", String.valueOf(appTime));
            requestBody.add("amount", String.valueOf(amount));
            requestBody.add("app_trans_id", appTransId);
            requestBody.add("bank_code", "");
            requestBody.add("description", "Thanh toan don hang #" + appTransId);

            // [SỬA] Gửi embedData mới đã có link redirect
            requestBody.add("embed_data", embedData);
            requestBody.add("item", item);

            // 2. Tạo chữ ký (Mac) - embedData thay đổi thì chữ ký cũng đổi theo
            String dataToSign = zalopayConfig.getAppId() + "|" + appTransId + "|" + appUser + "|" + amount + "|" + appTime + "|" + embedData + "|" + item;
            String mac = calculateHmacSHA256(dataToSign, zalopayConfig.getKey1());
            requestBody.add("mac", mac);

            // ... (Phần gửi RestTemplate phía dưới giữ nguyên) ...

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    zalopayConfig.getEndpoint(),
                    entity,
                    Map.class
            );

            Map<String, Object> body = response.getBody();
            if (body != null && "1".equals(String.valueOf(body.get("return_code")))) {
                result.put("orderUrl", body.get("order_url"));
                result.put("returnCode", body.get("return_code"));
                result.put("returnMessage", body.get("return_message"));
            } else {
                result.put("error", "ZaloPay Error: " + body);
            }
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            result.put("error", e.getMessage());
            return result;
        }
    }

    private String calculateHmacSHA256(String data, String key) throws Exception {
        Mac hmac256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmac256.init(secretKey);
        return Hex.encodeHexString(hmac256.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }

    private String getCurrentDateString() {
        return new SimpleDateFormat("yyMMdd").format(new Date());
    }
}