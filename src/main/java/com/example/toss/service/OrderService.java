package com.example.toss.service;

import com.example.toss.dto.ItemOrderDto;
import com.example.toss.dto.PaymentCancelDto;
import com.example.toss.dto.PaymentConfirmDto;
import com.example.toss.entity.Item;
import com.example.toss.entity.ItemOrder;
import com.example.toss.repo.ItemRepository;
import com.example.toss.repo.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
// TossHttpService을 실제로 사용하는 OrderService + OrderRepository의 Service
public class OrderService {
  private final TossHttpService tossService;
  // 어떤 아이템을 구매했는지 정보를 받기 위해 DI
  private final ItemRepository itemRepository;
  // 주문을 저장하기 위해 DI
  private final OrderRepository orderRepository;

  // 결제 승인을 위한 메서드
  public Object confirmPayment(PaymentConfirmDto dto) {
    // HTTP 요청이 toss 서버에게 보내진다.
    Object tossPaymentObj = tossService.confirmPayment(dto);
    log.info(tossPaymentObj.toString());
    // TODO 사용자가 결제한 물품 + 결제 정보에 대한 내용을 DB에 저장한다.
    // 1. 결제한 물품 정보를 응답 Body에서 찾는다 (orderName)
    String orderName = ((LinkedHashMap<String, Object>) tossPaymentObj)
            .get("orderName").toString();
    // 2. orderName에서 itemId를 회수하고, 그에 해당하는 Item 엔티티를 조회한다.
    // Long으로 변환: Long.parseLong()
    Long itemId = Long.parseLong(orderName.split("-")[0]);
    Item item = itemRepository.findById(itemId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));

    // 3. Item 엔티티를 바탕으로 ItemOrder를 만들자.
    return ItemOrderDto.fromEntity(orderRepository.save(ItemOrder.builder()
            .item(item)
            .tossPaymentKey(dto.getPaymentKey())
            .tossOrderId(dto.getOrderId())
            .status("DONE")
            .build()));
  }

  // readAll
  public List<ItemOrderDto> readAll() {
    return orderRepository.findAll().stream()
            .map(ItemOrderDto::fromEntity)
            .toList();
  }

  // readOne
  public ItemOrderDto readOne(Long id) {
    return orderRepository.findById(id)
            .map(ItemOrderDto::fromEntity)
            .orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND));
  }

  // readTossPayment
  // : tossPayment 객체를 읽기 위한 메서드, 어떤 카드 or 간편 결제로 했는지 확인시켜주는 메서드
  public Object readTossPayment(Long id) {
    // 1. id를 가지고 주문정보를 조회한다.
    ItemOrder order = orderRepository.findById(id)
            .orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND));
    // 2. 주문정보에 포함된 결제 정보키(paymentKey)를 바탕으로
    // Toss에 요청을 보내 결제 정보를 받는다.
    Object response = tossService.getPayment(order.getTossPaymentKey());
    log.info(response.toString());
    // 3. 해당 결제 정보를 반환한다.
    return response;
  }

  // cancelPayment
  @Transactional
  public Object cancelPayment(
        Long id,
        PaymentCancelDto dto
  ) {
      // 1. 취소할 주문을 찾는다.
      ItemOrder order = orderRepository.findById(id)
              .orElseThrow(()
                      -> new ResponseStatusException(HttpStatus.NOT_FOUND));
      // 2. 주문정보를 갱신한다.
      order.setStatus("CANCEL");
      // 3. 취소후 결과를 응답한다.
      return tossService.cancelPayment(order.getTossPaymentKey(), dto);
  }
}
// Http 요청을 보내는 Spring Boot의 방식들의 결과는 모두 JSON 객체면 LinkedHashMap<>()으로 번역
// 배열이면 ArrayList<>()로 데이터가 해석이 된다.