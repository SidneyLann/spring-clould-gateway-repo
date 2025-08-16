package com.blockchain.gateway.client;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.blockchain.common.base.OpResult;
import com.blockchain.common.util.MemberPaths;
import com.blockchain.common.util.ModulePaths;

import reactor.core.publisher.Mono;

@Service
public class MemberClient extends BaseClient {
  public Mono<OpResult> login(String loginName, Long weId, String password) {
    final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("loginName", loginName);
    if (weId == null) {
      params.add("weId", null);
    } else {
      params.add("weId", String.valueOf(weId));
    }
    params.add("password", password);

    Mono<OpResult> flxResult = webClient.post().uri(ModulePaths.MEMBER + MemberPaths.MEMBER_SEARCH_LOGIN).contentType(MediaType.APPLICATION_FORM_URLENCODED).bodyValue(params).retrieve()
        .bodyToMono(OpResult.class);

    return flxResult;
  }

  public Mono<OpResult> searchPermission4Member(String userId) {
    final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("userId", userId);

    Mono<OpResult> flxResult = webClient.post().uri(ModulePaths.MEMBER + MemberPaths.PERMISSIN_MB_SEARCH).contentType(MediaType.APPLICATION_FORM_URLENCODED).bodyValue(params).retrieve()
        .bodyToMono(OpResult.class);

    return flxResult;
  }
}
