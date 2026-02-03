package com.bumil.audio_fall_care.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class InternalApiKeyFilterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private InternalApiKeyFilter createFilter(String apiKey) throws Exception {
        InternalApiKeyFilter filter = new InternalApiKeyFilter(objectMapper);
        var field = InternalApiKeyFilter.class.getDeclaredField("internalApiKey");
        field.setAccessible(true);
        field.set(filter, apiKey);
        return filter;
    }

    @Test
    @DisplayName("internal 경로가 아닌 요청은 그대로 통과")
    void nonInternalPathPassesThrough() throws Exception {
        InternalApiKeyFilter filter = createFilter("secret-key");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/recorders");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("올바른 API 키로 internal 요청 시 통과")
    void validApiKeyPassesThrough() throws Exception {
        InternalApiKeyFilter filter = createFilter("secret-key");
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/internal/fall");
        request.addHeader("X-API-Key", "secret-key");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("잘못된 API 키로 internal 요청 시 401")
    void invalidApiKeyReturns401() throws Exception {
        InternalApiKeyFilter filter = createFilter("secret-key");
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/internal/fall");
        request.addHeader("X-API-Key", "wrong-key");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(chain, never()).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("INVALID_API_KEY");
    }

    @Test
    @DisplayName("API 키 헤더 없이 internal 요청 시 401")
    void missingApiKeyReturns401() throws Exception {
        InternalApiKeyFilter filter = createFilter("secret-key");
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/internal/fall");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(chain, never()).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    @DisplayName("INTERNAL_API_KEY 미설정 시 보안 비활성화 (통과)")
    void emptyApiKeyDisablesSecurity() throws Exception {
        InternalApiKeyFilter filter = createFilter("");
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/internal/fall");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }
}
