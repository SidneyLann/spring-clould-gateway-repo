package com.blockchain.gateway.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.blockchain.common.base.BizzException;
import com.blockchain.common.base.OpResult;
import com.blockchain.common.util.DefaultPermissionDto;
import com.blockchain.common.util.JsonUtil;
import com.blockchain.gateway.client.MemberClient;
import com.blockchain.gateway.common.SecurityUser;

import jakarta.annotation.Resource;
import reactor.core.publisher.Mono;

@Service
public class SecurityService {
	private static Logger LOG = LoggerFactory.getLogger(SecurityService.class);
	@Resource
	private MemberClient memberClient;

	public Mono<SecurityUser> authenticate(String loginName, Long weId, String password) {
		final Set<String> authorities = new HashSet<String>();
		Mono<OpResult> flxResult = null;

		LOG.debug("loginName 1: {}", loginName);
		LOG.debug("weId 1: {}", weId);
		flxResult = memberClient.login(loginName, weId, password);

		return flxResult.flatMap(opResult -> {
			if (opResult.getBody() == null || opResult.getCode() == OpResult.CODE_COMM_NULL_EXCEPTION) {
				LOG.info("can't find member, code {} msg: {}", opResult.getCode(), opResult.getMessage());
				return Mono.error(new BizzException(OpResult.CODE_COMM_GRANT_INVALID_USER));
			} else if (opResult.getCode() != OpResult.CODE_COMM_0_SUCCESS) {
				LOG.info("exception find member, code {} msg: {}", opResult.getCode(), opResult.getMessage());
				return Mono.error(new BizzException(OpResult.CODE_COMM_EXCEPTION));
			}

			LOG.debug("member: {}", opResult.getBody());
			SecurityUser securityUser = JsonUtil.object2Object(opResult.getBody(), SecurityUser.class);

			return Mono.just(securityUser);
//		}).flatMap(securityUser -> {
//			LOG.debug("securityUser.getId() 2: {}", securityUser.getId());
//			Mono<OpResult> memberPermissionMono = memberClient.searchPermission4Member(securityUser.getId());
//			return memberPermissionMono.map((memberPermissionResult) -> {
//				List<DefaultPermissionDto> defaultPermissionDtos = JsonUtil
//						.json2List(memberPermissionResult.getBody().toString(), DefaultPermissionDto.class);
//				for (DefaultPermissionDto dto : defaultPermissionDtos)
//					authorities.add(dto.getPermissionCode());
//
//				return securityUser;
//			});
//		}).flatMap(securityUser -> {
//			String userId = securityUser.getId();
//			String orgTypeStr = securityUser.getOrgType();
//			short orgType = Short.valueOf(orgTypeStr);
//			LOG.debug("userId 3: {}", userId);
//			LOG.debug("orgType 3: {}", orgType);
//
//			return Mono.just(securityUser);
		}).flatMap(securityUser -> {
			LOG.debug("securityUser.getNickName() 5: {}", securityUser.getNickName());

			if (StringUtils.isBlank(securityUser.getNickName()))
				securityUser.setNickName(securityUser.getWxNickName());
			if (StringUtils.isBlank(securityUser.getNickName()))
				securityUser.setNickName(securityUser.getLoginName());
			if (StringUtils.isBlank(securityUser.getAvatar()))
				securityUser.setAvatar(securityUser.getWxAvatar());

			securityUser.setAuthorities(authorities);

			return Mono.just(securityUser);
		});
	}
}
