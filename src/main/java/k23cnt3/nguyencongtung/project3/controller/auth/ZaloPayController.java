package k23cnt3.nguyencongtung.project3.controller.auth; // Hoặc package controller của bạn

import k23cnt3.nguyencongtung.project3.service.ZaloPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/zalopay")
public class ZaloPayController {

    @Autowired
    private ZaloPayService zaloPayService;

    @PostMapping("/create")
    public ResponseEntity<?> createPayment(@RequestParam(defaultValue = "50000") int amount) {
        // [SỬA] Thêm returnUrl vào hàm createOrder
        // Đây là API test nên ta để returnUrl về trang chủ hoặc trang nào tùy ý
        String returnUrl = "http://localhost:8080/";

        Map<String, Object> result = zaloPayService.createOrder(amount, returnUrl);
        return ResponseEntity.ok(result);
    }
}