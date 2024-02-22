package com.example.toss.dto;

import lombok.Data;

@Data
// 결제 승인을 하기 위한 DTO
public class PaymentConfirmDto {
    private String paymentKey;
    private String orderId;
    private Integer amount;
}
