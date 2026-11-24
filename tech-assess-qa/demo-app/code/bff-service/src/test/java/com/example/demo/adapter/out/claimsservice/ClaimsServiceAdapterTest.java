
package com.example.demo.adapter.out.claimsservice;

import com.example.demo.application.auth.ClaimsServicePort;
import com.example.demo.application.auth.ExternalServiceException;
import com.example.demo.application.auth.ServiceAccountTokenProvider;
import com.example.demo.application.auth.UserNotFoundException;
import com.example.bff.adapter.out.claims.ApiClient;
import com.example.bff.adapter.out.claims.ApiException;
import com.example.bff.adapter.out.claims.generated.UsersApi;
import com.example.bff.adapter.out.claims.generated.model.UserResponseDTO;
import com.example.bff.adapter.out.claims.generated.model.UserRoleDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClaimsServiceAdapterTest {

    private final UUID userId =
            UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    private final ServiceAccountTokenProvider tokenProvider =
            mock(ServiceAccountTokenProvider.class);

    private ClaimsServiceAdapter createAdapter() {
        return new ClaimsServiceAdapter(
                new ApiClient(),
                tokenProvider,
                new ObjectMapper()
        );
    }

    private UserResponseDTO createUserResponse() {
        UserResponseDTO response = new UserResponseDTO();
        response.setUserId(userId);
        response.setEmail("claimant@test.com");
        response.setName("Test Claimant");
        response.setRole(UserRoleDTO.fromValue("CLAIMANT"));
        response.setCreatedAt(OffsetDateTime.parse("2026-11-23T10:00:00Z"));
        return response;
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenApiReturns404() throws Exception {

        when(tokenProvider.getServiceAccountToken())
                .thenReturn("test-token");

        try (MockedConstruction<UsersApi> mocked =
                     mockConstruction(UsersApi.class, (mock, context) ->
                             when(mock.getUser(userId))
                                     .thenThrow(new ApiException(404, "Not Found")))) {

            ClaimsServiceAdapter adapter = createAdapter();

            assertThrows(
                    UserNotFoundException.class,
                    () -> adapter.getUser(userId)
            );

            verify(tokenProvider, never()).forceRefreshToken();
        }
    }

    @Test
    void shouldRefreshTokenAndRetryWhenApiReturns401() throws Exception {

        when(tokenProvider.getServiceAccountToken())
                .thenReturn("old-token");

        when(tokenProvider.forceRefreshToken())
                .thenReturn("fresh-token");

        AtomicInteger attempts = new AtomicInteger(0);

        try (MockedConstruction<UsersApi> mocked =
                     mockConstruction(UsersApi.class, (mock, context) ->
                             when(mock.getUser(userId)).thenAnswer(invocation -> {
                                 if (attempts.getAndIncrement() == 0) {
                                     throw new ApiException(401, "Unauthorized");
                                 }
                                 return createUserResponse();
                             }))) {

            ClaimsServiceAdapter adapter = createAdapter();

            ClaimsServicePort.UserRecord result = adapter.getUser(userId);

            assertNotNull(result);
            assertEquals(userId, result.userId());
            assertEquals("claimant@test.com", result.email());
            assertEquals("Test Claimant", result.name());
            assertEquals("CLAIMANT", result.role());

            assertEquals(2, attempts.get());
            verify(tokenProvider).forceRefreshToken();
        }
    }

    @Test
    void shouldThrowExternalServiceExceptionWhenApiReturns500() throws Exception {

        when(tokenProvider.getServiceAccountToken())
                .thenReturn("test-token");

        try (MockedConstruction<UsersApi> mocked =
                     mockConstruction(UsersApi.class, (mock, context) ->
                             when(mock.getUser(userId))
                                     .thenThrow(new ApiException(500, "Internal Server Error")))) {

            ClaimsServiceAdapter adapter = createAdapter();

            ExternalServiceException exception = assertThrows(
                    ExternalServiceException.class,
                    () -> adapter.getUser(userId)
            );

            assertTrue(exception.getMessage().contains("HTTP 500"));
            verify(tokenProvider, never()).forceRefreshToken();
        }
    }

    @Test
    void shouldThrowExternalServiceExceptionWhenApiReturns503() throws Exception {

        when(tokenProvider.getServiceAccountToken())
                .thenReturn("test-token");

        try (MockedConstruction<UsersApi> mocked =
                     mockConstruction(UsersApi.class, (mock, context) ->
                             when(mock.getUser(userId))
                                     .thenThrow(new ApiException(503, "Service Unavailable")))) {

            ClaimsServiceAdapter adapter = createAdapter();

            ExternalServiceException exception = assertThrows(
                    ExternalServiceException.class,
                    () -> adapter.getUser(userId)
            );

            assertTrue(exception.getMessage().contains("HTTP 503"));
            verify(tokenProvider, never()).forceRefreshToken();
        }
    }
}
