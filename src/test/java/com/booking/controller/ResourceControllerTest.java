package com.booking.controller;

import com.booking.dto.ResourceRequest;
import com.booking.dto.ResourceResponse;
import com.booking.service.ResourceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ResourceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ResourceService resourceService;

    @Test
    @WithMockUser(roles = "USER")
    void user_CannotCreateResource_ReturnsForbidden() throws Exception {
        ResourceRequest request = ResourceRequest.builder()
                .name("Conference Room")
                .type("Room")
                .price(new BigDecimal("100.00"))
                .build();

        mockMvc.perform(post("/api/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void user_CannotUpdateResource_ReturnsForbidden() throws Exception {
        ResourceRequest request = ResourceRequest.builder()
                .name("Updated Room")
                .type("Room")
                .price(new BigDecimal("120.00"))
                .build();

        mockMvc.perform(put("/api/resources/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void user_CannotDeleteResource_ReturnsForbidden() throws Exception {
        mockMvc.perform(delete("/api/resources/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_CanCreateResource_Success() throws Exception {
        ResourceRequest request = ResourceRequest.builder()
                .name("Conference Room")
                .type("Room")
                .price(new BigDecimal("100.00"))
                .available(true)
                .build();

        ResourceResponse response = ResourceResponse.builder()
                .id(1L)
                .name("Conference Room")
                .type("Room")
                .price(new BigDecimal("100.00"))
                .available(true)
                .build();

        when(resourceService.createResource(any(ResourceRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "USER")
    void user_CanReadResources_Success() throws Exception {
        when(resourceService.getAllResources()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/resources"))
                .andExpect(status().isOk());
    }
}
