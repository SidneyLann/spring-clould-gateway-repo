package com.blockchain.gateway.handler;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;

import reactor.core.publisher.Mono;

public class UserAuthenWebFilter{ //extends AuthenticationWebFilter {
//  public UserAuthenWebFilter(ReactiveAuthenticationManager authenticationManager) {
//    super(authenticationManager);
//  }
//
//  @Override
//  protected Mono<Void> onAuthenticationSuccess(Authentication authentication, WebFilterExchange webFilterExchange) {
//    return super.onAuthenticationSuccess(authentication, webFilterExchange);
//  }
}
