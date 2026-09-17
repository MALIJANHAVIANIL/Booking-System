package com.booking.dto;

import com.booking.entity.ReservationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationStatusUpdateRequest {

    @NotNull(message = "Reservation status is required")
    private ReservationStatus status;
}
