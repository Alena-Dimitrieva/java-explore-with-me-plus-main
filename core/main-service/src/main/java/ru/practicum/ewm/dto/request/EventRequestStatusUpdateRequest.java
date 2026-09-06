package ru.practicum.ewm.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import ru.practicum.ewm.model.enums.RequestUpdateStatus;

import java.util.List;

@Builder
public record EventRequestStatusUpdateRequest(

		List<Long> requestIds,

		@NotNull
		RequestUpdateStatus status
) {
}
