package com.booking.controller;

import com.booking.dto.ReservationRequest;

import com.booking.entity.Reservation;
import com.booking.entity.ReservationStatus;
import com.booking.entity.Resource;
import com.booking.entity.Role;
import com.booking.entity.User;
import com.booking.repository.ReservationRepository;
import com.booking.repository.ResourceRepository;
import com.booking.repository.UserRepository;
import com.booking.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ReservationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User user1;
    private User user2;
    private User admin;
    private Resource resource1;
    private Resource resource2;
    private String user1Token;
    private String user2Token;
    private String adminToken;

    @BeforeEach
    void setUp() {
        user1 = userRepository.save(User.builder()
                .username("User One")
                .email("testuser1@example.com")
                .password(passwordEncoder.encode("User@123"))
                .role(Role.USER)
                .build());

        user2 = userRepository.save(User.builder()
                .username("User Two")
                .email("testuser2@example.com")
                .password(passwordEncoder.encode("User@123"))
                .role(Role.USER)
                .build());

        admin = userRepository.save(User.builder()
                .username("Admin User")
                .email("testadmin@example.com")
                .password(passwordEncoder.encode("Admin@123"))
                .role(Role.ADMIN)
                .build());

        resource1 = resourceRepository.save(Resource.builder()
                .name("Resource Low Price")
                .type("Room")
                .price(new BigDecimal("100.00"))
                .available(true)
                .build());

        resource2 = resourceRepository.save(Resource.builder()
                .name("Resource High Price")
                .type("Equipment")
                .price(new BigDecimal("500.00"))
                .available(true)
                .build());

        user1Token = "Bearer " + jwtTokenProvider.generateToken(user1.getEmail(), user1.getRole().name());
        user2Token = "Bearer " + jwtTokenProvider.generateToken(user2.getEmail(), user2.getRole().name());
        adminToken = "Bearer " + jwtTokenProvider.generateToken(admin.getEmail(), admin.getRole().name());
    }

    @Test
    void user_CanCreateReservation_ExtractsUserIdFromJWT() throws Exception {
        ReservationRequest request = ReservationRequest.builder()
                .resourceId(resource1.getId())
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .build();

        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(user1.getId()))
                .andExpect(jsonPath("$.resourceId").value(resource1.getId()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.price").value(100.00));
    }

    @Test
    void user_SeesOnlyOwnReservations_AdminSeesAll() throws Exception {
        // User 1 creates reservation
        reservationRepository.save(Reservation.builder()
                .user(user1)
                .resource(resource1)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .price(resource1.getPrice())
                .status(ReservationStatus.CONFIRMED)
                .build());

        // User 2 creates reservation
        reservationRepository.save(Reservation.builder()
                .user(user2)
                .resource(resource2)
                .startTime(LocalDateTime.now().plusDays(2))
                .endTime(LocalDateTime.now().plusDays(2).plusHours(3))
                .price(resource2.getPrice())
                .status(ReservationStatus.PENDING)
                .build());

        // User 1 queries GET /api/reservations -> should get only 1
        mockMvc.perform(get("/api/reservations")
                        .header("Authorization", user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].userId").value(user1.getId()));

        // Admin queries GET /api/reservations -> should get both 2
        mockMvc.perform(get("/api/reservations")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void user_CannotAccessAnotherUsersReservation_ReturnsForbidden() throws Exception {
        Reservation res2 = reservationRepository.save(Reservation.builder()
                .user(user2)
                .resource(resource2)
                .startTime(LocalDateTime.now().plusDays(2))
                .endTime(LocalDateTime.now().plusDays(2).plusHours(3))
                .price(resource2.getPrice())
                .status(ReservationStatus.PENDING)
                .build());

        mockMvc.perform(get("/api/reservations/" + res2.getId())
                        .header("Authorization", user1Token))
                .andExpect(status().isForbidden());
    }

    @Test
    void filtering_Pagination_Sorting() throws Exception {
        reservationRepository.save(Reservation.builder()
                .user(user1)
                .resource(resource1)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .price(new BigDecimal("100.00"))
                .status(ReservationStatus.CONFIRMED)
                .build());

        reservationRepository.save(Reservation.builder()
                .user(user1)
                .resource(resource2)
                .startTime(LocalDateTime.now().plusDays(2))
                .endTime(LocalDateTime.now().plusDays(2).plusHours(2))
                .price(new BigDecimal("500.00"))
                .status(ReservationStatus.PENDING)
                .build());

        // Filter by status=CONFIRMED
        mockMvc.perform(get("/api/reservations?status=CONFIRMED")
                        .header("Authorization", user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].status").value("CONFIRMED"));

        // Filter by minPrice & maxPrice
        mockMvc.perform(get("/api/reservations?minPrice=200&maxPrice=600")
                        .header("Authorization", user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].price").value(500.00));

        // Pagination and Sorting (descending price)
        mockMvc.perform(get("/api/reservations?page=0&size=10&sort=price,desc")
                        .header("Authorization", user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].price").value(500.00))
                .andExpect(jsonPath("$.content[1].price").value(100.00));
    }
}
