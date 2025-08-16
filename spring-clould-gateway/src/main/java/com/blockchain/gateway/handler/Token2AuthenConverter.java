package com.blockchain.gateway.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

public class Token2AuthenConverter implements ServerAuthenticationConverter {
  private static final Logger LOG = LoggerFactory.getLogger(Token2AuthenConverter.class);
  private final JwtHelper jwtHandler;
  
  public Token2AuthenConverter(JwtHelper jwtHandler) {
    this.jwtHandler=jwtHandler;
  }
  
  @Override
  public Mono<Authentication> convert(ServerWebExchange exchange) {
    return extractHeader(exchange).flatMap(jwtHandler::checkAuthen).flatMap(UserAuthenCreation::createAuthen).onErrorResume(e -> {
      LOG.debug("convert exception: {}", e.getMessage());
//      SecurityUser principal = new SecurityUser();
//      UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal, null, null);
//
//      return Mono.just(authentication);

      return Mono.empty();
    });
  }

  private Mono<ServerWebExchange> extractHeader(ServerWebExchange exchange) {
    return Mono.justOrEmpty(exchange);
  }
}
