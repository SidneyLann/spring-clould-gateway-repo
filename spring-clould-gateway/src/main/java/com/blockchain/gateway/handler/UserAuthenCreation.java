package com.blockchain.gateway.handler;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.blockchain.common.base.BizzException;
import com.blockchain.common.base.OpResult;
import com.blockchain.gateway.common.SecurityUser;

import io.jsonwebtoken.Claims;
import reactor.core.publisher.Mono;

public class UserAuthenCreation {
  private static final Logger LOG = LoggerFactory.getLogger(UserAuthenCreation.class);

  public static Mono<Authentication> createAuthen(Claims claims) {
    String loginName = claims.get("loginName", String.class);
    String authoritiesStr = claims.get("authorities", String.class);
    String[] authoritiesArr = null;
    if (StringUtils.isBlank(authoritiesStr)) {
      LOG.debug("grand not enough for loginName: {}", loginName);
      return Mono.error(new BizzException(OpResult.CODE_COMM_GRANT_NOT_ENOUGH));
    }

    authoritiesArr = authoritiesStr.split(",");

    Set<SimpleGrantedAuthority> authoritiesSet = null;// 401
    authoritiesSet = new HashSet<SimpleGrantedAuthority>(authoritiesArr.length);
    for (String authority : authoritiesArr)
      authoritiesSet.add(new SimpleGrantedAuthority(authority));

    SecurityUser principal = new SecurityUser();
    principal.setLoginName(loginName);
    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal, null, authoritiesSet);
    authentication.setDetails(claims);

    return Mono.just(authentication);
  }
}
