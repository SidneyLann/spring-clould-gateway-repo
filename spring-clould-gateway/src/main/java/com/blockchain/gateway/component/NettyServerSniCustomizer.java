package com.blockchain.gateway.component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.Http2;
import org.springframework.boot.web.server.Ssl;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.server.WebServerSslBundle;
import org.springframework.stereotype.Component;

import com.blockchain.gateway.handler.SniSslServerCustomizer;

@Component
public class NettyServerSniCustomizer implements WebServerFactoryCustomizer<NettyReactiveWebServerFactory> {
  private static final String KEY_STORE = ".pc168.com.p12";
  @Value("${server.ssl.folder}")
  private String sslCerts;  
  @Value("${server.http2.enabled}")
  private boolean enableHttp2;

  @Override
  public void customize(NettyReactiveWebServerFactory serverFactory) {
    String domain2s = System.getProperty("domain2s");
    String[] domain2sArr = domain2s.split(",");

    String[] hostNames = new String[domain2sArr.length];
    Ssl.ClientAuth[] clientAuths = new Ssl.ClientAuth[domain2sArr.length];
    Http2[] http2s = new Http2[domain2sArr.length];
    SslBundle[] sslBundles = new SslBundle[domain2sArr.length];

    for (int i = 0; i < domain2sArr.length; i++) {
      hostNames[i] = domain2sArr[i] + ".pc168.com";

      Ssl ssl = new Ssl();
      ssl.setKeyStore(sslCerts + domain2sArr[i] + KEY_STORE);
      ssl.setKeyStorePassword("Fnsh1t18");
      ssl.setKeyStoreType("PKCS12");
      ssl.setClientAuth(Ssl.ClientAuth.NONE);

      clientAuths[i] = ssl.getClientAuth();

      Http2 http2 = new Http2();
      http2.setEnabled(enableHttp2);
      http2s[i] = http2;

      sslBundles[i] = WebServerSslBundle.get(ssl);
    }

    try {
      serverFactory.addServerCustomizers(new SniSslServerCustomizer(hostNames, http2s, clientAuths, sslBundles));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
