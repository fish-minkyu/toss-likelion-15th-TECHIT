package com.example.toss.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
// 결제 취소 시, 필수값인 취소 사유를 받기 위해 만든 DTO
public class PaymentCancelDto {
  private String cancelReason;
}
