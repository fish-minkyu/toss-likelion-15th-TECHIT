# Toss
- 2024.02.20 ~ 02.21 `15주차`

- 02.20 - 요구사항 1번(`confirmPayment`)
- 02.21 - 요구사항 2 ~ 5번

`likelion-backend-8-toss`에서 스켈레톤 코드를 받아  
Server가 토스 페이먼츠의 결제 승인 API에게 요청을 보내는 부분을 구현하는 연습 프로젝트다.

## 요구사항

1. 전달받은 결제 정보를 바탕으로 결제 승인 요청을 Toss로 보냅니다.
2. 어떤 물품이 결제되었는지, 해당 물품의 Toss 결제 정보는 무엇인지가 포함된 주문 정보를 데이터베이스에 저장합니다.
3. 결제가 진행되었던 주문 정보들을 전체, 또는 단일 조회가 가능합니다.
4. 결제가 진행되었던 주문 정보들의 Toss 결제 정보에 대한 개별 조회가 가능합니다.
5. 결제가 진행되었던 주문 정보들의 결제에 대하여, 개별 취소가 가능합니다.

<hr>

`2월 20일`
<details>
<summary><strong>2월 20일 사용 Page</strong></summary>

- Http Interface
<div>RestClientConfig: HttpClient 객체</div>
<div>PaymentConfirmDto</div>
<div>TossHttpService: 요청을 보내기 위한 방식을 정의(요청을 보내줄 Http Client 객체 필요)</div>
<div>OrderService: TossHttpService을 실제로 사용하는 OrderService</div>
<div>TossController: Front에서 보내는 결제 승인 요청을 받아주기 위한 Controller</div>

</details>

<hr>

`2월 21일`
<details>
<summary><strong>2월 21일 사용 Page</strong></summary>

<div>OrderRepository: 주문 정보를 저장하기 위한 Repository</div>
<div>ItemOrderDto: 응답 객체로 반환하기 위해 사용</div>
<div>PaymentCancelDto: 결제 취소 시, 필수값인 취소 사유를 받기 위해 만든 DTO</div>
<div>OrderService: TossHttpService을 실제로 사용하는 OrderService + OrderRepository의 Service</div>
<div>OrderController: OrderRepository - OrderService - OrderController</div>

</details>

Http 요청을 보내는 Spring Boot의 방식들의 결과는 모두 JSON이고  
객체면 `LinkedHashMap<>()`으로 번역, 배열이면 `ArrayList<>()`로 데이터가 해석이 된다.

Transactional의 dirty checking이 사용되었다.


## 스팩

- Sprnig Boot 3.2.2
- Spring Web
- Lombok
- Spring Data Jpa
- SQLite
- H2

## Key Point

`02/20`
<details>
<summary><strong> 02/20 - Page </strong></summary>

- Http Interface  
[RestClientConfig](/src/main/java/com/example/toss/config/RestClientConfig.java)  
=> Http Interface의 객체, 실제로 요청을 보내줄 역할을 담당하고 있다.
```java
@Configuration
// Http Client 객체, RestClient 설정
public class RestClientConfig {
  @Value("${toss.secret}")
  private String tossSecret;

  @Bean
  public RestClient tossClient() {
    // Base64 방식으로 tossSecrest + :을 인코딩
    String basicAuth = Base64.getEncoder().encodeToString((tossSecret + ":").getBytes());
    return RestClient
            .builder()
            .baseUrl("https://api.tosspayments.com/v1")
            // "Authorization" 헤더 설정
            .defaultHeader("Authorization", String.format("Basic %s", basicAuth))
            .build();
  }

  @Bean
  // Spring Boot가 httpService의 의존성이 tossClient에 있다는 걸 알아서
  // tossClient가 먼저 Bean으로 생성이 되어진다.
  public TossHttpService httpService() {
    return HttpServiceProxyFactory
            .builderFor(RestClientAdapter.create(tossClient()))
            .build()
            .createClient(TossHttpService.class);
  }
}
```

[TossHttpService](/src/main/java/com/example/toss/service/TossHttpService.java)  
=> 요청을 보내기 위한 방식을 정의만 하고 있다.
```java
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
```

[OrderService](/src/main/java/com/example/toss/service/OrderService.java)  
- confirmPayment  
  => 결제 승인을 위한 메서드, HTTP 요청이 toss 서버에 보내진다.
```java
@Slf4j
@Service
@RequiredArgsConstructor
// TossHttpService을 실제로 사용하는 OrderService
public class OrderService {
  private final TossHttpService tossService;
  private final ItemRepository itemRepository;
  private final OrderRepository orderRepository;

  // 결제 승인을 위한 메서드
  public Object confirmPayment(PaymentConfirmDto dto) {
    // HTTP 요청이 toss 서버에게 보내진다.
    Object tossPaymentObj = tossService.confirmPayment(dto);
    log.info(tossPaymentObj.toString());
    
    return  tossPaymentObj;
  }
}
```

OrderService.confirmPayment() -> tossService.confirmPayment()로 흐름이 이어진다.
```java
@HttpExchange("/payments")
public interface TossHttpService {

  // 결제 승인 요청 API
  @PostExchange("/confirm")
  Object confirmPayment(@RequestBody PaymentConfirmDto dto);
}
```
</details>

<hr>

`02/21`

<details>
<summary><strong> 02/21 - page</strong></summary>

[OrderService](/src/main/java/com/example/toss/service/OrderService.java)

- confirmPayment (== Create)
```java
  // 결제 승인을 위한 메서드 + 주문 생성
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
```

- readTossPayment  
=> 요구사항 4번
```java
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
```

- cancelPayment  
=> 요구사항 5번
```java
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
  // 3. 취소후 결과를 응답한다. (결제가 확실히 취소되었다는 걸 알 수 있다.)
  return tossService.cancelPayment(order.getTossPaymentKey(), dto);
  }
```

</details>

반환타입을 Dto로 할 수 있으나, 현재는 간단한 작업을 구현하는 것이어서 Object를 사용했다. 

## GitHUb

- 강사님 GitHub  
[likelion-backend-8-toss](https://github.com/edujeeho0/likelion-backend-8-toss)