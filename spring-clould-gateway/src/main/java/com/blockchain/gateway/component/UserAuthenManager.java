package com.blockchain.gateway.component;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class UserAuthenManager implements ReactiveAuthenticationManager {
//  @Resource
//  private SecurityService securityService;

  @Override
  public Mono<Authentication> authenticate(Authentication authentication) {
//    SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
 //   if (securityUser.getLoginName() == null || !securityUser.getLoginName().contains(StrUtil.SEPARATOR)) {
      return Mono.just(authentication);
 //   }

//    System.out.println("securityUser.getLoginName(): " + securityUser.getLoginName());
//    return securityService.authenticate(securityUser.getLoginName()).filter(SecurityUser::isEnabled).switchIfEmpty(Mono.error(new BizzException("User disabled"))).map(user -> authentication);
  }
}