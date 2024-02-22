package com.example.toss;


import com.example.toss.dto.PaymentConfirmDto;
import com.example.toss.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("/toss")
@RequiredArgsConstructor
// Front에서 보내는 결제 승인 요청을 받아주기 위한 Controller
public class TossController {
  private final OrderService service;

  @PostMapping("/confirm-payment")
  // Object로 하는 이유는 모양새가 복잡하기 때문에
  // "임시로" Object로 잡아주는 것이다.
  public Object confirmPayment(
    @RequestBody
    PaymentConfirmDto dto,
    @RequestHeader("Authorization")
    String authorization
  ) {
    log.info(authorization);
    log.info("received: {}", dto.toString());
    return service.confirmPayment(dto);
  }
}
