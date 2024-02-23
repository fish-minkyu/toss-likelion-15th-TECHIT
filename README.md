# Toss
- 2024.02.20 ~ 02.21 `15주차`
- 02.20 - 결제 승인 요청 API에 결제 정보 전(`confirmPayment`)

`likelion-backend-8-toss`에서 스켈레톤 코드를 받아  
Server가 토스 페이먼츠의 결제 승인 API에게 요청을 보내는 부분을 구현하는 연습 프로젝트다.

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

`2월 21일`
<details>
<summary><strong>2월 21일 사용 Page</strong></summary>

<div>OrderRepository</div>
<div>ItemOrderDto</div>
<div>PaymentCancelDto</div>
<div>OrderService</div>
<div>OrderController</div>

</details>

// Http 요청을 보내는 Spring Boot의 방식들의 결과는 모두 JSON 객체면 LinkedHashMap<>()으로 번역
// 배열이면 ArrayList<>()로 데이터가 해석이 된다.
dirty checking(Transactional <- 알아보기)

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


## GitHUb

- 강사님 GitHub  
[likelion-backend-8-toss](https://github.com/edujeeho0/likelion-backend-8-toss)