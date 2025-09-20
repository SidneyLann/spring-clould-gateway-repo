package com.blockchain.gateway.conf;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.util.UriComponentsBuilder;

import com.blockchain.gateway.handler.JwtHelper;
import com.blockchain.gateway.handler.Token2AuthenConverter;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfiguration {
	private static Logger LOG = LoggerFactory.getLogger(SecurityConfiguration.class);
	@Value("${jwt.secret}")
	private String secret;
	// Add all Swagger endpoints to public routes
	private final String[] publicRoutes = { "/", "/swagger-ui.html", "/webjars/**", "/v3/api-docs/**", "/swagger-ui/**",
			"/swagger-ui/index.html", "/swagger-resources/**", "/swagger-config", "/sec/login", "/member/pub/init/search"};

	// List of allowed origins for CORS (add your production domains here)
	private final List<String> allowedOrigins = Arrays.asList("https://ec2-13-229-223-170.ap-southeast-1.compute.amazonaws.com",
			"https://www.amazonaws.com");

	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
			ReactiveAuthenticationManager userAuthenticationManager) {
		return http.csrf(csrf -> csrf.disable()).formLogin(form -> form.disable())
				.httpBasic(httpBasic -> httpBasic.disable()).cors(cors -> cors.disable())
				.authorizeExchange((exchanges) -> exchanges.pathMatchers(HttpMethod.OPTIONS).permitAll()
						.pathMatchers(publicRoutes).permitAll().anyExchange().authenticated())
				.addFilterAt(authenticationFilter(userAuthenticationManager), SecurityWebFiltersOrder.AUTHENTICATION)
				.exceptionHandling(e -> {
					e.authenticationEntryPoint((sxe, ex) -> {
						LOG.warn("{} In SecurityWebFilterChain - unauthorized error: {}", sxe.getRequest().getPath(),
								ex.getMessage());
						return Mono.fromRunnable(() -> sxe.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED));
					});
					e.accessDeniedHandler((sxc, ex) -> {
						LOG.warn("{} In SecurityWebFilterChain - access denied: {}", sxc.getRequest().getPath(),
								ex.getMessage());
						return Mono.fromRunnable(() -> sxc.getResponse().setStatusCode(HttpStatus.FORBIDDEN));
					});
				}).build();
	}

	private AuthenticationWebFilter authenticationFilter(ReactiveAuthenticationManager userAuthenticationManager) {
		AuthenticationWebFilter authenticationWebFilter = new AuthenticationWebFilter(userAuthenticationManager);
		authenticationWebFilter.setServerAuthenticationConverter(new Token2AuthenConverter(new JwtHelper(secret)));

		// Only apply authentication to non-public paths
		authenticationWebFilter.setRequiresAuthenticationMatcher(exchange -> ServerWebExchangeMatchers
				.pathMatchers(publicRoutes).matches(exchange).flatMap(matchResult -> {
					if (matchResult.isMatch()) {
						// Public route - skip authentication
						return ServerWebExchangeMatcher.MatchResult.notMatch();
					} else {
						// Protected route - require authentication
						return ServerWebExchangeMatcher.MatchResult.match();
					}
				}));

		return authenticationWebFilter;
	}

	@Bean
	public CorsWebFilter corsFilter() {
		CorsConfiguration config = new CorsConfiguration();
		config.addAllowedOriginPattern("*"); // Allow both HTTP and HTTPs
		// config.setAllowedOrigins(allowedOrigins);
		config.setAllowCredentials(true);
		config.addAllowedMethod("*");
		config.addAllowedHeader("*");
		config.setMaxAge(3600L); // 1 hour

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);

		return new CorsWebFilter(source);
	}

	@Bean
	public RouteLocator routeLocator(RouteLocatorBuilder builder) {
		return builder.routes().route("www",
				r -> r.host("azure.com").filters(f -> f.filter((serverWebExchange, gatewayFilterChain) -> {
					ServerHttpRequest request = serverWebExchange.getRequest();
					ServerHttpResponse response = serverWebExchange.getResponse();
					URI uri = request.getURI();
					URI httpsUri;
					try {
						String host = uri.getHost();
						String path = uri.getPath();
						LOG.debug("user info: {}, routeLocator redirect host: {}, uri: {}", uri.getUserInfo(), host,
								path);
						if ("azure.com".equals(host)) {
							host = "www." + host;
							httpsUri = new URI("https", uri.getUserInfo(), host, 443, path, uri.getQuery(),
									uri.getFragment());
							response.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
							response.getHeaders().setLocation(httpsUri);

							return response.setComplete();
						}
					} catch (URISyntaxException e) {
					}

					return response.setComplete();
				})).uri("never:///reach-here-because-the-request-is-redirect-to-another-url")).build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(11);
	}

	//@Bean
	//@Order(Ordered.LOWEST_PRECEDENCE)
	WebFilter firtWebFilter() {
		return (exchange, chain) -> {
			// 只处理 POST 请求且 Content-Type 为 application/x-www-form-urlencoded
			boolean a = exchange.getRequest().getMethod() == HttpMethod.POST;
			boolean b = MediaType.APPLICATION_FORM_URLENCODED
					.equals(exchange.getRequest().getHeaders().getContentType());
			// if (exchange.getRequest().getMethod() == HttpMethod.POST &&
			// MediaType.APPLICATION_FORM_URLENCODED_VALUE.equals(exchange.getRequest().getHeaders().getContentType()))
			// {
			if (a && b) {
				return DataBufferUtils.join(exchange.getRequest().getBody()).flatMap(dataBuffer -> {
					try {
						// 1. 读取 FormData 内容
						byte[] bytes = new byte[dataBuffer.readableByteCount()];
						dataBuffer.read(bytes);
						String body = new String(bytes, StandardCharsets.UTF_8);

						// 2. 将 FormData 转换为查询参数格式
						// String formDataQuery = sanitizeFormData(body);

						// 3. 获取原始 URI
						URI originalUri = exchange.getRequest().getURI();

						// 4. 构建新的 URI（包含原始查询参数 + FormData）
						// URI newUri = buildNewUri(originalUri, body);

						// 获取原始查询参数
						String originalQuery = originalUri.getQuery();

						// 合并查询参数
						String newQuery = (originalQuery == null || originalQuery.isEmpty()) ? body
								: originalQuery + "&" + body;

						// 使用 UriComponentsBuilder 安全构建新 URI
						URI newUri = UriComponentsBuilder.fromUri(originalUri).replaceQuery(newQuery).build(true)
								.toUri();
						// 5. 创建新的请求对象
						ServerHttpRequest newRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {
							private final URI uri = newUri;

							@Override
							public URI getURI() {
								return this.uri;
							}
						};

						// 6. 替换交换对象中的请求
						ServerWebExchange newExchange = exchange.mutate().request(newRequest).build();

						// 7. 继续过滤器链
						return chain.filter(newExchange);
					} finally {
						DataBufferUtils.release(dataBuffer);
					}
				});
			}

			return chain.filter(exchange);
		};
	}

	@Bean
	@Order(Ordered.LOWEST_PRECEDENCE)
	WebFilter lastWebFilter() {
		return (exchange, chain) -> {
			String memberId = (String) exchange.getAttributes().get("memberId");
			String memberIdHeader = exchange.getRequest().getHeaders().getFirst("memberId");
			if (memberId == null) {
				LOG.trace("mutate memberId: {}", memberId);
				return chain.filter(exchange);
			} else if (memberIdHeader != null) {// never reach here
				LOG.debug("mutate memberIdHeader: {}", memberIdHeader);
				return chain.filter(exchange);
			}

			ServerHttpRequestDecorator writeableRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {
				@Override
				public HttpHeaders getHeaders() {
					HttpHeaders headers = super.getHeaders();
					headers.set("memberId", memberId);
					headers.set("orgId", (String) exchange.getAttributes().get("orgId"));
					headers.set("orgType", (String) exchange.getAttributes().get("orgType"));
					headers.set("authorities", (String) exchange.getAttributes().get("authorities"));
					LOG.trace("mutate memberId3: {}", memberId);
					return headers;
				}
			};

			ServerWebExchange writeableExchange = exchange.mutate().request(writeableRequest).build();

			return chain.filter(writeableExchange);
		};
	}
}
