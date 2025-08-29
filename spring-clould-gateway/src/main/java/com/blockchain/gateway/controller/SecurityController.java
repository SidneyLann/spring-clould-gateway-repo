package com.blockchain.gateway.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.blockchain.common.base.BizzException;
import com.blockchain.common.base.OpResult;
import com.blockchain.common.util.MemberPaths;
import com.blockchain.gateway.common.SecurityUser;
import com.blockchain.gateway.handler.JwtHelper;
import com.blockchain.gateway.service.SecurityService;

import jakarta.annotation.Resource;
import reactor.core.publisher.Mono;

@RestController
public class SecurityController {
	private static final Logger LOG = LoggerFactory.getLogger(SecurityController.class);
	@Value("${jwt.secret}")
	private String secret;
	@Value("${jwt.expires_in}")
	private Integer expiresIn;
	@Value("${jwt.issuer}")
	private String issuer;
	@Resource
	private SecurityService securityService;

	//@PostMapping(value = MemberPaths.SECURITY_LOGIN, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	@PostMapping(value = MemberPaths.SECURITY_LOGIN)
	public Mono<OpResult> login(@RequestParam("loginName") String loginName, @RequestParam("weId") String weId,
			@RequestParam("password") String password, @RequestParam("clientVersion") String clientVersion) {
		OpResult opResult = new OpResult();
		final Mono<SecurityUser> authentication = securityService.authenticate(loginName, Long.valueOf(weId), password);

		return authentication.map(user -> {
			if (user.getId() == null) {
				LOG.info("user.getId()= {}", user.getId());
				LOG.info("user.getPassword()= {}", user.getPassword());
				opResult.setCode(OpResult.CODE_COMM_GRANT_INVALID_USER);
			} else {
				LOG.debug("SecurityUser: {}", user);
				JwtHelper jwtHelper = new JwtHelper(secret);
				jwtHelper.generateToken(user, expiresIn, issuer);

				user.setPassword(null);
				opResult.setBody(user);
				opResult.setCode(OpResult.CODE_COMM_0_SUCCESS);
			}

			return opResult;
		}).onErrorResume(e -> {
			LOG.error("onErrorResume e: ", e);
			if (e instanceof BizzException) {
				OpResult opResult2 = ((BizzException) e).getOpResult();
				opResult.setCode(opResult2.getCode());
				opResult.setMessage(opResult2.getMessage());
				opResult.setBody(opResult2.getBody());
				opResult.setTotalRecords(opResult2.getTotalRecords());
			} else {
				opResult.setCode(OpResult.CODE_COMM_EXCEPTION);
				opResult.setBody(e.getMessage());
			}
			return Mono.just(opResult);
		});
	}
}
