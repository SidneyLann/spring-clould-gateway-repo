package com.blockchain.gateway.handler;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import com.blockchain.common.base.BizzException;
import com.blockchain.common.base.OpResult;
import com.blockchain.gateway.common.SecurityUser;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import reactor.core.publisher.Mono;

public class JwtHelper {
  private static final Logger LOG = LoggerFactory.getLogger(JwtHelper.class);
  private static final String BEARER_PREFIX = "Bearer ";
  private final String secret;

  public JwtHelper(String secret) {
    this.secret = secret;
  }

  public SecurityUser generateToken(SecurityUser user, Integer expiresIn, String issuer) {
    final String authorities = user.getAuthorities().toString().replace("[", "").replace("]", "");
    Map<String, String> claims = new HashMap<>() {
      {
        put("id", user.getId());
        put("orgId", user.getOrgId());
        put("orgType", user.getOrgType());
        put("loginName", user.getLoginName());
        put("authorities", authorities);
      }
    };

    Long expirationTimeInMillis = expiresIn * 1000L;
    Date expirationDate = new Date(new Date().getTime() + expirationTimeInMillis);

    Date createdDate = new Date();
    String token = Jwts.builder().setClaims(claims).setIssuer(issuer).setSubject(user.getId()).setIssuedAt(createdDate).setId(UUID.randomUUID().toString()).setExpiration(expirationDate)
        .signWith(SignatureAlgorithm.HS256, Base64.getEncoder().encodeToString(secret.getBytes())).compact();

    user.setToken(token);
    user.setIssuedAt(createdDate);
    user.setExpiresAt(expirationDate);

    return user;
  }

  public Mono<Claims> checkAuthen(ServerWebExchange exchange) {
    try {
      ServerHttpRequest request = exchange.getRequest();
      HttpHeaders httpHeaders = request.getHeaders();
      String accessUrl = request.getURI().toString();
      LOG.debug("access url: {}", accessUrl);
      LOG.debug("access allIps: {}", httpHeaders.getFirst("allIps"));
      String clientIp = String.valueOf(request.getRemoteAddress().getAddress().getHostAddress());
      String userAgent = httpHeaders.getFirst("User-Agent");
      LOG.debug("access clientIp: {}", clientIp);
      LOG.debug("access User-Agent: {}", userAgent);

      String accessToken = httpHeaders.getFirst(HttpHeaders.AUTHORIZATION);
      LOG.debug("accessToken: {}", accessToken);
      if (StringUtils.isBlank(accessToken) || accessToken.length() < 256 || accessToken.indexOf(".") < 0) {
        LOG.debug("wrong accessToken: {}", accessToken);
        return Mono.error(new BizzException(OpResult.CODE_COMM_GRANT_NULL));
      }

      Claims claims = getClaimsFromToken(accessToken);
      final Date expirationDate = claims.getExpiration();
      if (expirationDate.before(new Date())) {
        LOG.debug("grand expired: {}", expirationDate);
        return Mono.error(new BizzException(OpResult.CODE_COMM_GRANT_EXPIRED));
      }

      String id = claims.get("id", String.class);
      String orgId = claims.get("orgId", String.class);
      String orgType = claims.get("orgType", String.class);
      String authorities = claims.get("authorities", String.class);
      String tokenIp = claims.get("clientIp", String.class);
      String tokenIpOff = claims.get("clientIpOff", String.class);
      String tokenUa = claims.get("userAgent", String.class);
      LOG.debug("token clientIp: {}", tokenIp);
      LOG.debug("token clientIpOff: {}", tokenIpOff);
      LOG.debug("token User-Agent: {}", tokenUa);
      boolean isSameHalf = false;
      if (tokenIpOff != null) {
        String[] clientIpv6s = tokenIpOff.split(":");
        if (clientIpv6s.length > 4) {
          if (clientIp.replaceAll(":", "").startsWith(clientIpv6s[0] + clientIpv6s[1] + clientIpv6s[2] + clientIpv6s[3]))
            isSameHalf = true;
        }
      }

      if (clientIp == null || userAgent == null || !(clientIp.equals(tokenIp) || isSameHalf) || !userAgent.equals(tokenUa)) {
        LOG.debug("clientIp == null || userAgent == null || !(clientIp.equals(tokenIp) || isSameHalf) || !userAgent.equals(tokenUa)");
        return Mono.error(new BizzException(OpResult.CODE_COMM_GRANT_EXPIRED));
      }

      exchange.getAttributes().put("memberId", id);
      exchange.getAttributes().put("orgId", orgId);
      exchange.getAttributes().put("orgType", orgType);
      exchange.getAttributes().put("authorities", authorities);
      LOG.debug("gateway memberId: {}", id);
      LOG.debug("gateway orgId: {}", orgId);
      LOG.debug("gateway orgType: {}", orgType);
      LOG.debug("gateway member has authorities: {}", authorities);
      return Mono.just(claims);
    } catch (ExpiredJwtException e) {
      LOG.error("Expired Jwt Exception:", e);
      return Mono.error(new BizzException(OpResult.CODE_COMM_GRANT_EXPIRED));
    } catch (Exception e) {
      LOG.error("check Exception:", e);
      return Mono.error(new BizzException(OpResult.CODE_COMM_EXCEPTION));
    }
  }

  private Claims getClaimsFromToken(String token) {
    JwtParser p = Jwts.parser().setSigningKey(Base64.getEncoder().encodeToString(secret.getBytes())).build();

    return p.parseClaimsJws(token.substring(BEARER_PREFIX.length())).getPayload();
  }

}
