package com.leviathan.bidpass.ai;

import io.netty.channel.ChannelOption;
import io.netty.resolver.DefaultAddressResolverGroup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class AiConfig {
    @Value("${gemini.api.key}")
    private String geminiKey;

    @Value("${gemini.api.url}")
    private String geminiUrl;

    @Value("${ai.chat.base-url}")
    private String aiChatBaseUrl;

    @Value("${ai.chat.api-key}")
    private String aiChatApiKey;

    @Bean
    public WebClient geminiWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofSeconds(15))
                .resolver(DefaultAddressResolverGroup.INSTANCE);

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(geminiUrl)
                .defaultHeader("x-goog-api-key", geminiKey)
                .build();
    }

    // OpenAI-compatible chat client (DeepSeek / Qwen / GLM / Kimi / ...)
    @Bean
    public WebClient aiChatWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofSeconds(10))
                .resolver(DefaultAddressResolverGroup.INSTANCE);

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(aiChatBaseUrl)
                .defaultHeader("Authorization", "Bearer " + aiChatApiKey)
                .build();
    }
}
