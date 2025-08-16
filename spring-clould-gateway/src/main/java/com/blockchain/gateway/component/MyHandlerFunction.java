package com.blockchain.gateway.component;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;

@Component
public class MyHandlerFunction implements HandlerFunction<ServerResponse> {

  @Override
  public Mono<ServerResponse> handle(ServerRequest request) {
    URI uri = request.uri();

    try {
      String host = uri.getHost();
      String path = uri.getPath();
      if ("pc168.com".equals(host)) {
        host = "www." + host;

        String userAgent = request.headers().firstHeader("User-Agent");
        if (userAgent != null) {
          if ("/".equals(path) && userAgent.indexOf("Mobile") != -1) {
            path = "/h5";
          }
        }

        uri = new URI("https", uri.getUserInfo(), host, 443, path, uri.getQuery(), uri.getFragment());

        return ServerResponse.permanentRedirect(uri).build();
      }
    } catch (URISyntaxException e) {
      // return Mono.error(e);
    }

    return ServerResponse.created(uri).build();
  }
}
