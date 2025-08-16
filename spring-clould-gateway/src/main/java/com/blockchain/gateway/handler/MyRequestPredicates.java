package com.blockchain.gateway.handler;

import org.springframework.util.Assert;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.ServerRequest;

public class MyRequestPredicates extends RequestPredicates {

  public static RequestPredicate host(String pattern) {
    return new HostPatternPredicate(pattern);
  }

  private static class HostPatternPredicate implements RequestPredicate {

    private String pattern;

    public HostPatternPredicate(String pattern) {
      Assert.notNull(pattern, "'pattern' must not be null");
      this.pattern = pattern;
    }

    @Override
    public boolean test(ServerRequest request) {
      String host = request.uri().getHost();
      return host.equals(this.pattern);
    }
  }
}
