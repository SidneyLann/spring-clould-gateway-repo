package com.blockchain.common.base;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.WebFluxConfigurer;

//@Configuration
public class WebConfig implements WebFluxConfigurer {

    @Bean
    public FormDataToQueryParamWebFilter formDataToQueryParamWebFilter() {
        return new FormDataToQueryParamWebFilter();
    }
}