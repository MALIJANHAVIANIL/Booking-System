package com.booking.service;

import com.booking.dto.PageResponse;
import com.booking.dto.ReservationRequest;
import com.booking.dto.ReservationResponse;
import com.booking.dto.ReservationStatusUpdateRequest;
import com.booking.entity.Reservation;
import com.booking.entity.ReservationStatus;
import com.booking.entity.Resource;
import com.booking.entity.Role;
import com.booking.entity.User;
import com.booking.exception.BadRequestException;
import com.booking.exception.ResourceNotFoundException;
import com.booking.repository.ReservationRepository;
import com.booking.repository.ReservationSpecification;
import com.booking.repository.ResourceRepository;
import com.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    public ReservationResponse createReservation(ReservationRequest request) {
        User loggedInUser = getAuthenticatedUser();

        Resource resource = resourceRepository.findById(request.getResourceId())
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + request.getResourceId()));

        if (Boolean.FALSE.equals(resource.getAvailable())) {
            throw new BadRequestException("Resource is currently not available for reservation");
        }

        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new BadRequestException("Start time and end time are required");
        }

        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new BadRequestException("End time must be after start time");
        }

        Reservation reservation = Reservation.builder()
                .user(loggedInUser)
                .resource(resource)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .price(resource.getPrice())
                .status(ReservationStatus.PENDING)
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);
        return mapToResponse(savedReservation);
    }

    public PageResponse<ReservationResponse> getAllReservations(
            ReservationStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable
    ) {
        User loggedInUser = getAuthenticatedUser();
        Long filterUserId = null;

        if (loggedInUser.getRole() == Role.USER) {
            filterUserId = loggedInUser.getId();
        }

        Page<Reservation> page = reservationRepository.findAll(
                ReservationSpecification.filterReservations(filterUserId, status, minPrice, maxPrice),
                pageable
        );

        Page<ReservationResponse> responsePage = page.map(this::mapToResponse);
        return PageResponse.from(responsePage);
    }

    public ReservationResponse getReservationById(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));

        User loggedInUser = getAuthenticatedUser();
        checkOwnershipOrAdmin(reservation, loggedInUser, "view");

        return mapToResponse(reservation);
    }

    public ReservationResponse updateReservationStatus(Long id, ReservationStatusUpdateRequest request) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));

        User loggedInUser = getAuthenticatedUser();
        checkOwnershipOrAdmin(reservation, loggedInUser, "update");

        reservation.setStatus(request.getStatus());
        Reservation updatedReservation = reservationRepository.save(reservation);
        return mapToResponse(updatedReservation);
    }

    public void deleteReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));

        User loggedInUser = getAuthenticatedUser();
        checkOwnershipOrAdmin(reservation, loggedInUser, "delete");

        reservationRepository.delete(reservation);
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new BadRequestException("User is not authenticated");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private void checkOwnershipOrAdmin(Reservation reservation, User user, String action) {
        if (user.getRole() != Role.ADMIN && !reservation.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to " + action + " another user's reservation");
        }
    }

    private ReservationResponse mapToResponse(Reservation reservation) {
        return ReservationResponse.builder()
                .id(reservation.getId())
                .userId(reservation.getUser().getId())
                .username(reservation.getUser().getUsername())
                .userEmail(reservation.getUser().getEmail())
                .resourceId(reservation.getResource().getId())
                .resourceName(reservation.getResource().getName())
                .startTime(reservation.getStartTime())
                .endTime(reservation.getEndTime())
                .price(reservation.getPrice())
                .status(reservation.getStatus())
                .build();
    }
}
