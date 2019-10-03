package com.woowacourse.tecobrary.user.infra.util;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class GithubApiRequestClient {

    private static final String GITHUB_API_BASE_URL = "https://api.github.com/user";
    private static final String EMAIL_API_ROUTE = "/emails";
    private static final String AUTHORIZATION = "Authorization";
    private static final String USER_AGENT = "User-Agent";
    private static final String AUTHORIZATION_PREFIX = "token ";
    private static final String LOGIN_APP = "Login-App";

    public String userInfo(String githubApiAccessToken) {
        return buildUserInfoClient(githubApiAccessToken)
                .get()
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private WebClient buildUserInfoClient(String githubApiAccessToken) {
        return WebClient
                .builder()
                .baseUrl(GITHUB_API_BASE_URL)
                .defaultHeader(AUTHORIZATION, AUTHORIZATION_PREFIX + githubApiAccessToken)
                .defaultHeader(USER_AGENT, LOGIN_APP)
                .build();
    }

    public String userEmail(String githubApiAccessToken) {
        return buildUserEmailClient(githubApiAccessToken)
                .get()
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private WebClient buildUserEmailClient(String githubApiAccessToken) {
        return WebClient
                .builder()
                .baseUrl(GITHUB_API_BASE_URL + EMAIL_API_ROUTE)
                .defaultHeader(AUTHORIZATION, AUTHORIZATION_PREFIX + githubApiAccessToken)
                .defaultHeader(USER_AGENT, LOGIN_APP)
                .build();
    }
}
