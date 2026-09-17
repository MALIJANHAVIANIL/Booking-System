package com.booking.service;

import com.booking.dto.ReservationRequest;
import com.booking.entity.Reservation;
import com.booking.entity.ReservationStatus;
import com.booking.entity.Resource;
import com.booking.entity.Role;
import com.booking.entity.User;
import com.booking.exception.BadRequestException;
import com.booking.exception.ResourceNotFoundException;
import com.booking.repository.ReservationRepository;
import com.booking.repository.ResourceRepository;
import com.booking.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReservationService reservationService;

    private User normalUser;
    private User otherUser;
    private Resource availableResource;

    @BeforeEach
    void setUp() {
        normalUser = User.builder()
                .id(1L)
                .username("Normal User")
                .email("user@example.com")
                .role(Role.USER)
                .build();

        otherUser = User.builder()
                .id(2L)
                .username("Other User")
                .email("other@example.com")
                .role(Role.USER)
                .build();

        availableResource = Resource.builder()
                .id(10L)
                .name("Test Room")
                .type("Room")
                .price(new BigDecimal("100.00"))
                .available(true)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockSecurityContext(User user) {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user.getEmail(), null, java.util.Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void createReservation_EndTimeBeforeStartTime_ThrowsBadRequestException() {
        mockSecurityContext(normalUser);
        when(userRepository.findByEmail(normalUser.getEmail())).thenReturn(Optional.of(normalUser));
        when(resourceRepository.findById(10L)).thenReturn(Optional.of(availableResource));

        ReservationRequest request = ReservationRequest.builder()
                .resourceId(10L)
                .startTime(LocalDateTime.now().plusHours(2))
                .endTime(LocalDateTime.now().plusHours(1))
                .build();

        assertThrows(BadRequestException.class, () -> reservationService.createReservation(request));
    }

    @Test
    void createReservation_ResourceNotAvailable_ThrowsBadRequestException() {
        mockSecurityContext(normalUser);
        Resource unavailableResource = Resource.builder()
                .id(10L)
                .available(false)
                .build();

        when(userRepository.findByEmail(normalUser.getEmail())).thenReturn(Optional.of(normalUser));
        when(resourceRepository.findById(10L)).thenReturn(Optional.of(unavailableResource));

        ReservationRequest request = ReservationRequest.builder()
                .resourceId(10L)
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusHours(2))
                .build();

        assertThrows(BadRequestException.class, () -> reservationService.createReservation(request));
    }

    @Test
    void getReservationById_UserAccessingOtherUserReservation_ThrowsAccessDeniedException() {
        mockSecurityContext(normalUser);
        when(userRepository.findByEmail(normalUser.getEmail())).thenReturn(Optional.of(normalUser));

        Reservation reservationOfOtherUser = Reservation.builder()
                .id(50L)
                .user(otherUser)
                .resource(availableResource)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(1))
                .price(new BigDecimal("100.00"))
                .status(ReservationStatus.PENDING)
                .build();

        when(reservationRepository.findById(50L)).thenReturn(Optional.of(reservationOfOtherUser));

        assertThrows(AccessDeniedException.class, () -> reservationService.getReservationById(50L));
    }
}
