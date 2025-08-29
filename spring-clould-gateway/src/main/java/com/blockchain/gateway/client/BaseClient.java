package com.blockchain.gateway.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.annotation.PostConstruct;

@Service
public class BaseClient {

  @Value("${pcng.folder.root}")
  private String rootFolder;
  @Value("${pcng.system.member.port}")
  private Integer memberSysPort;

  protected WebClient webClient;

  @PostConstruct
  private void createClient() {
    String memberSysHost =  "localhost";
    webClient = WebClient.create("http://" + memberSysHost + ":" + memberSysPort);
  }

}
