package com.blockchain.gateway.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslOptions;
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer;
import org.springframework.boot.web.server.Http2;
import org.springframework.boot.web.server.Ssl;

import io.netty.handler.ssl.ClientAuth;
import reactor.netty.http.Http11SslContextSpec;
import reactor.netty.http.Http2SslContextSpec;
import reactor.netty.http.server.HttpServer;
import reactor.netty.tcp.AbstractProtocolSslContextSpec;
import reactor.netty.tcp.SslProvider;

public class SniSslServerCustomizer implements NettyServerCustomizer {
  private final String[] hostNames;
  private final Http2[] http2;
  private final Ssl.ClientAuth[] clientAuth;
  private final SslBundle[] sslBundle;

  public SniSslServerCustomizer(String[] hostNames, Http2[] http2, Ssl.ClientAuth[] clientAuth, SslBundle[] sslBundle) {
    this.hostNames = hostNames;
    this.http2 = http2;
    this.clientAuth = clientAuth;
    this.sslBundle = sslBundle;
  }

  @Override
  public HttpServer apply(HttpServer server) {
    try {
      AbstractProtocolSslContextSpec<?> sslContextSpec = null;
      Map<String, Consumer<? super SslProvider.SslContextSpec>> domainMap = new HashMap<>();

      for (int i = 1; i < sslBundle.length; i++) {
        sslContextSpec = createSslContextSpec(i);
        final AbstractProtocolSslContextSpec<?> sslContextSpec2 = sslContextSpec;
        domainMap.put(this.hostNames[i], spec -> spec.sslContext(sslContextSpec2));
      }

      return server.secure(spec -> spec.sslContext(createSslContextSpec(0)).addSniMappings(domainMap));
    } catch (Exception e) {
      return null;
    }
  }

  protected AbstractProtocolSslContextSpec<?> createSslContextSpec(int i) {
    AbstractProtocolSslContextSpec<?> sslContextSpec = (this.http2[i] != null && this.http2[i].isEnabled()) ? Http2SslContextSpec.forServer(this.sslBundle[i].getManagers().getKeyManagerFactory())
        : Http11SslContextSpec.forServer(this.sslBundle[i].getManagers().getKeyManagerFactory());
    sslContextSpec.configure((builder) -> {
      builder.trustManager(this.sslBundle[i].getManagers().getTrustManagerFactory());
      SslOptions options = this.sslBundle[i].getOptions();
      builder.protocols(options.getEnabledProtocols());
      builder.ciphers(SslOptions.asSet(options.getCiphers()));
      builder.clientAuth(org.springframework.boot.web.server.Ssl.ClientAuth.map(this.clientAuth[i], ClientAuth.NONE, ClientAuth.OPTIONAL, ClientAuth.REQUIRE));
    });
    
    return sslContextSpec;
  }
}
