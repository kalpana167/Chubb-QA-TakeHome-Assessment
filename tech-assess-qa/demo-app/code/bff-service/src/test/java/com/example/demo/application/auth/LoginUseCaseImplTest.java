
package com.example.demo.application.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class LoginUseCaseImplTest {

    @Test
    void shouldLoginSuccessfullyWhenUserAlreadyExists() {

        // Arrange: Create mock dependencies
        KeycloakTokenPort keycloakTokenPort =
                mock(KeycloakTokenPort.class);

        ClaimsServicePort claimsServicePort =
                mock(ClaimsServicePort.class);

        ObjectMapper objectMapper = new ObjectMapper();

        UUID userId = UUID.randomUUID();

        String payload = """
                {
                  "sub": "%s",
                  "email": "admin@example.com",
                  "preferred_username": "admin",
                  "name": "Admin User",
                  "realm_access": {
                    "roles": ["admin"]
                  }
                }
                """.formatted(userId);

        String encodedPayload = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));

        String accessToken = "header." + encodedPayload + ".signature";
        String refreshToken = "test-refresh-token";

        KeycloakTokenPort.TokenResponse tokenResponse =
                new KeycloakTokenPort.TokenResponse(
                        accessToken,
                        refreshToken,
                        300,
                        1800
                );

        when(keycloakTokenPort.getTokens(
                "admin@example.com", "Password123"
        )).thenReturn(tokenResponse);

        ClaimsServicePort.UserRecord existingUser =
                new ClaimsServicePort.UserRecord(
                        userId,
                        "admin@example.com",
                        "Admin User",
                        "ADMIN",
                        OffsetDateTime.now()
                );

        when(claimsServicePort.getUser(userId))
                .thenReturn(existingUser);

        LoginUseCaseImpl loginUseCase =
                new LoginUseCaseImpl(
                        keycloakTokenPort,
                        claimsServicePort,
                        objectMapper
                );

        // Act: Execute login
        LoginResult result = loginUseCase.login(
                new LoginCommand("admin@example.com", "Password123")
        );

        // Assert: Verify login response
        assertEquals(accessToken, result.accessToken());
        assertEquals(refreshToken, result.refreshToken());
        assertEquals(userId, result.userId());
        assertEquals("admin@example.com", result.email());
        assertEquals("Admin User", result.name());
        assertEquals("ADMIN", result.role());

        // Verify that existing user was not created again
        verify(claimsServicePort).getUser(userId);
        verify(claimsServicePort, never())
                .createUser(any(), anyString(), anyString(), anyString());
    }
@Test
void shouldCreateUserWhenUserDoesNotExist() {

    KeycloakTokenPort keycloakTokenPort =
            mock(KeycloakTokenPort.class);

    ClaimsServicePort claimsServicePort =
            mock(ClaimsServicePort.class);

    ObjectMapper objectMapper = new ObjectMapper();

    UUID userId = UUID.randomUUID();

    String payload = """
            {
              "sub": "%s",
              "email": "newuser@example.com",
              "preferred_username": "newuser",
              "name": "New User",
              "realm_access": {
                "roles": ["claimant"]
              }
            }
            """.formatted(userId);

    String encodedPayload = Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(payload.getBytes(StandardCharsets.UTF_8));

    String accessToken = "header." + encodedPayload + ".signature";

    when(keycloakTokenPort.getTokens(
            "newuser@example.com", "Password123"
    )).thenReturn(new KeycloakTokenPort.TokenResponse(
            accessToken, "refresh-token", 300, 1800
    ));

    when(claimsServicePort.getUser(userId))
            .thenThrow(new UserNotFoundException(userId));

    ClaimsServicePort.UserRecord newUser =
            new ClaimsServicePort.UserRecord(
                    userId,
                    "newuser@example.com",
                    "New User",
                    "CLAIMANT",
                    OffsetDateTime.now()
            );

    when(claimsServicePort.createUser(
            userId, "newuser@example.com",
            "New User", "CLAIMANT"
    )).thenReturn(newUser);

    LoginUseCaseImpl loginUseCase =
            new LoginUseCaseImpl(
                    keycloakTokenPort,
                    claimsServicePort,
                    objectMapper
            );

    LoginResult result = loginUseCase.login(
            new LoginCommand("newuser@example.com", "Password123")
    );

    assertEquals(userId, result.userId());
    assertEquals("newuser@example.com", result.email());
    assertEquals("CLAIMANT", result.role());

    verify(claimsServicePort).createUser(
            userId, "newuser@example.com",
            "New User", "CLAIMANT"
    );
}

@Test
void shouldRejectLoginWhenCredentialsAreInvalid() {

    KeycloakTokenPort keycloakTokenPort =
            mock(KeycloakTokenPort.class);

    ClaimsServicePort claimsServicePort =
            mock(ClaimsServicePort.class);

    ObjectMapper objectMapper = new ObjectMapper();

    when(keycloakTokenPort.getTokens(
            "wrong@example.com", "WrongPassword"
    )).thenThrow(new InvalidCredentialsException());

    LoginUseCaseImpl loginUseCase =
            new LoginUseCaseImpl(
                    keycloakTokenPort,
                    claimsServicePort,
                    objectMapper
            );

    org.junit.jupiter.api.Assertions.assertThrows(
            InvalidCredentialsException.class,
            () -> loginUseCase.login(
                    new LoginCommand(
                            "wrong@example.com",
                            "WrongPassword"
                    )
            )
    );

    verifyNoInteractions(claimsServicePort);
}

@Test
void shouldNotCreateUserWhenClaimsServiceIsUnavailable() {

    KeycloakTokenPort keycloakTokenPort =
            mock(KeycloakTokenPort.class);

    ClaimsServicePort claimsServicePort =
            mock(ClaimsServicePort.class);

    ObjectMapper objectMapper = new ObjectMapper();

    UUID userId = UUID.randomUUID();

    String payload = """
            {
                "sub": "%s",
                "email": "newuser@example.com",
                "preferred_username": "newuser",
                "name": "New User",
                "realm_access": {
                    "roles": ["claimant"]
                }
            }
            """.formatted(userId);

    String encodedPayload = Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(payload.getBytes(StandardCharsets.UTF_8));

    String accessToken = "header." + encodedPayload + ".signature";

    when(keycloakTokenPort.getTokens(
            "newuser@example.com", "Password123"
    )).thenReturn(new KeycloakTokenPort.TokenResponse(
            accessToken, "refresh-token", 300, 1800
    ));

    when(claimsServicePort.getUser(userId))
            .thenThrow(new ExternalServiceException(
                    "Claims Service",
                    "Service unavailable: HTTP 503"
            ));

    LoginUseCaseImpl loginUseCase = new LoginUseCaseImpl(
            keycloakTokenPort,
            claimsServicePort,
            objectMapper
    );

    org.junit.jupiter.api.Assertions.assertThrows(
            ExternalServiceException.class,
            () -> loginUseCase.login(
                    new LoginCommand("newuser@example.com", "Password123")
            )
    );

    verify(claimsServicePort, never())
            .createUser(any(), anyString(), anyString(), anyString());
}

}
