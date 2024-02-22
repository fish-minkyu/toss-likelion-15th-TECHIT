package com.example.toss.service;

import com.example.toss.dto.PaymentCancelDto;
import com.example.toss.dto.PaymentConfirmDto;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

// Http Interface는 그냥 보낼 수 없다.
// 요청을 보내기 위한 방식이 정의가 되어있다면 누가 보낼지는 아직 정의가 되지 않은 형식이다. (노트에 옮겨적기)
// 따라서 실제로 보낼 Http Client 객체가 필요하다.
@HttpExchange("/payments")
public interface TossHttpService {

  // 결제 승인 요청 API
  @PostExchange("/confirm")
  Object confirmPayment(@RequestBody PaymentConfirmDto dto);

  @GetExchange("/{paymentKey}")
  Object getPayment(
    @PathVariable("paymentKey")
    String paymentKey
  );

  @PostExchange("/{paymentKey}/cancel")
  Object cancelPayment(
    @PathVariable("paymentKey")
    String paymentKey,
    @RequestBody
    PaymentCancelDto dto
  );
}
