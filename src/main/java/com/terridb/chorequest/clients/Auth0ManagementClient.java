package com.terridb.chorequest.clients;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Component
public class Auth0ManagementClient {
    private final RestClient restClient;
    private final String audience;
    private final String clientId;
    private final String clientSecret;

    public Auth0ManagementClient(
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuer,
            @Value("${auth0.management.client-id}") String clientId,
            @Value("${auth0.management.client-secret}") String clientSecret) {
        this.restClient = RestClient.builder().baseUrl(issuer).build();
        this.audience = issuer + "api/v2/";
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public void deleteUser(String sub) {
        if (clientId.isBlank() || clientSecret.isBlank()) {
            log.warn("Auth0 management credentials are not set, so {} was not deleted from Auth0", sub);
            return;
        }

        restClient.delete()
                .uri("api/v2/users/{id}", sub)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + fetchAccessToken())
                .retrieve()
                .toBodilessEntity();
    }

    private String fetchAccessToken() {
        TokenResponse response = restClient.post()
                .uri("oauth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "grant_type", "client_credentials",
                        "client_id", clientId,
                        "client_secret", clientSecret,
                        "audience", audience
                ))
                .retrieve()
                .body(TokenResponse.class);

        if (response == null || response.accessToken() == null) {
            throw new IllegalStateException("Auth0 did not return a management access token");
        }
        return response.accessToken();
    }

    record TokenResponse(@JsonProperty("access_token") String accessToken) {
    }
}
