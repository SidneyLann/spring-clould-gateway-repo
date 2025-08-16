package com.blockchain.gateway.conf;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;

import com.blockchain.gateway.handler.JwtHelper;
import com.blockchain.gateway.handler.Token2AuthenConverter;

import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfiguration {
  private static Logger LOG = LoggerFactory.getLogger(SecurityConfiguration.class);
  @Value("${jwt.secret}")
  private String secret;
  private final String[] publicRoutes = { "/", "/member/pub/**" };

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, ReactiveAuthenticationManager userAuthenticationManager) {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOriginPatterns(Arrays.asList("https://www.pc168.com:8080", "https://*.pc168.com"));
    config.setAllowCredentials(true);
    config.addAllowedMethod("*");
    config.addAllowedHeader("*");
    UrlBasedCorsConfigurationSource corsConfigurationSource = new UrlBasedCorsConfigurationSource();
    corsConfigurationSource.registerCorsConfiguration("/**", config);

    return http.csrf(csrf -> csrf.disable()).cors(cors -> cors.configurationSource(corsConfigurationSource))
        // return http.csrf(csrf -> csrf.disable())
        .authorizeExchange((exchanges) -> exchanges.pathMatchers(HttpMethod.OPTIONS).permitAll().pathMatchers(publicRoutes).permitAll().anyExchange().authenticated())
        .addFilterAt(authenticationFilter(userAuthenticationManager), SecurityWebFiltersOrder.AUTHENTICATION).exceptionHandling(e -> {
          e.authenticationEntryPoint((sxe, ex) -> {
            LOG.warn("{} In SecurityWebFilterChain - unauthorized error: {}", sxe.getRequest().getPath(), ex.getMessage());
            return Mono.fromRunnable(() -> sxe.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED));
          }).accessDeniedHandler((sxc, ex) -> {
            LOG.warn("{} In SecurityWebFilterChain - access denied: {}", sxc.getRequest().getPath(), ex.getMessage());
            return Mono.fromRunnable(() -> sxc.getResponse().setStatusCode(HttpStatus.FORBIDDEN));
          });
        }).build();
  }

  private AuthenticationWebFilter authenticationFilter(ReactiveAuthenticationManager userAuthenticationManager) {
    AuthenticationWebFilter authenticationWebFilter = new AuthenticationWebFilter(userAuthenticationManager);
    authenticationWebFilter.setServerAuthenticationConverter(new Token2AuthenConverter(new JwtHelper(secret)));
    authenticationWebFilter.setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers("/member/pub/**"));

    return authenticationWebFilter;
  }

  @Bean
  public RouteLocator routeLocator(RouteLocatorBuilder builder) {
    return builder.routes().route("www", r -> r.host("pc168.com").filters(f -> f.filter((serverWebExchange, gatewayFilterChain) -> {
      ServerHttpRequest request = serverWebExchange.getRequest();
      ServerHttpResponse response = serverWebExchange.getResponse();
      URI uri = request.getURI();
      URI httpsUri;
      try {
        String host = uri.getHost();
        String path = uri.getPath();
        LOG.debug("user info: {}, routeLocator redirect host: {}, uri: {}", uri.getUserInfo(), host, path);
        if ("pc168.com".equals(host)) {
          host = "www." + host;
          httpsUri = new URI("https", uri.getUserInfo(), host, 443, path, uri.getQuery(), uri.getFragment());
          response.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
          response.getHeaders().setLocation(httpsUri);

          return response.setComplete();
        }
      } catch (URISyntaxException e) {
      }

      return response.setComplete();
    })).uri("never:///reach-here-because-the-request-is-redirect-to-another-url")).build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(11);
  }

  @Bean
  @Order(Ordered.LOWEST_PRECEDENCE)
  WebFilter newHeaders() {
    return (exchange, chain) -> {
      String memberId = (String) exchange.getAttributes().get("memberId");
      String memberIdHeader = exchange.getRequest().getHeaders().getFirst("memberId");
      if (memberId == null) {
        LOG.trace("mutate memberId2: {}", memberId);
        return chain.filter(exchange);
      } else if (memberIdHeader != null) {// never reach here
        LOG.debug("mutate memberIdHeader: {}", memberIdHeader);
        return chain.filter(exchange);
      }

      ServerHttpRequestDecorator writeableRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {
        @Override
        public HttpHeaders getHeaders() {
          HttpHeaders headers = super.getHeaders();
          headers.set("memberId", memberId);
          headers.set("orgId", (String) exchange.getAttributes().get("orgId"));
          headers.set("orgType", (String) exchange.getAttributes().get("orgType"));
          headers.set("authorities", (String) exchange.getAttributes().get("authorities"));
          LOG.trace("mutate memberId3: {}", memberId);
          return headers;
        }
      };
      
      ServerWebExchange writeableExchange = exchange.mutate().request(writeableRequest).build();
      
      return chain.filter(writeableExchange);
    };
  }
}
