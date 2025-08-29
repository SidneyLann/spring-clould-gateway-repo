package com.blockchain.common.base;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;

//@Component
public class FormDataToQueryParamWebFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // 只处理 POST 请求且 Content-Type 为 application/x-www-form-urlencoded
    	boolean a=exchange.getRequest().getMethod() == HttpMethod.POST;
    	boolean b=MediaType.APPLICATION_FORM_URLENCODED.equals(exchange.getRequest().getHeaders().getContentType());
       // if (exchange.getRequest().getMethod() == HttpMethod.POST &&
        //    MediaType.APPLICATION_FORM_URLENCODED_VALUE.equals(exchange.getRequest().getHeaders().getContentType())) {
           if(a&&b) {
            return DataBufferUtils.join(exchange.getRequest().getBody())
                .flatMap(dataBuffer -> {
                    try {
                        // 1. 读取 FormData 内容
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        String body = new String(bytes, StandardCharsets.UTF_8);
                        
                        // 2. 将 FormData 转换为查询参数格式
                        String formDataQuery = sanitizeFormData(body);
                        
                        // 3. 获取原始 URI
                        URI originalUri = exchange.getRequest().getURI();
                        
                        // 4. 构建新的 URI（包含原始查询参数 + FormData）
                        URI newUri = buildNewUri(originalUri, formDataQuery);
                        
                        // 5. 创建新的请求对象
                        ServerHttpRequest newRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {
                            @Override
                            public URI getURI() {
                                return newUri;
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
    }

    private String sanitizeFormData(String formData) {
        // 这里可以添加安全过滤逻辑
        return formData;
    }

    private URI buildNewUri(URI originalUri, String formDataQuery) {
        // 获取原始查询参数
        String originalQuery = originalUri.getQuery();
        
        // 合并查询参数
        String newQuery = (originalQuery == null || originalQuery.isEmpty()) 
            ? formDataQuery 
            : originalQuery + "&" + formDataQuery;
        
        // 使用 UriComponentsBuilder 安全构建新 URI
        return UriComponentsBuilder.fromUri(originalUri)
            .replaceQuery(newQuery)
            .build(true) // true 表示自动编码特殊字符
            .toUri();
    }
}