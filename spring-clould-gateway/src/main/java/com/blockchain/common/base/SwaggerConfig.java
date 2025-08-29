//package com.blockchain.common.base;
//
//import org.springdoc.core.customizers.OpenApiCustomizer;
//import org.springdoc.core.models.GroupedOpenApi;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import io.swagger.v3.oas.models.OpenAPI;
//import io.swagger.v3.oas.models.info.Info;
//import io.swagger.v3.oas.models.servers.Server;
//
////@Configuration
//public class SwaggerConfig {
//	@Bean
//	public OpenApiCustomizer serverCustomizer() {
//		return openApi -> {
//			openApi.getServers().clear();
//			openApi.addServersItem(new Server().url("http://sh1.koreacentral.cloudapp.azure.com:8443")
//					.description("Development Server"));
//		};
//	}
//
//	@Bean
//	public GroupedOpenApi userApi() {
//		return GroupedOpenApi.builder().group("spring-biz-member").pathsToMatch("/member/pub/**").build();
//	}
//
//	@Bean
//	public OpenAPI customOpenAPI() {
//		return new OpenAPI().info(new Info().title("API Gateway").version("1.0").description("Aggregated APIs"));
//	}
//}
