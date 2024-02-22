package com.example.toss.config;

import com.example.toss.service.TossHttpService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.util.Base64;

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
