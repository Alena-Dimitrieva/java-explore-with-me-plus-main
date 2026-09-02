package ru.practicum.ewm.dto.request;

import lombok.Builder;

import java.util.List;

@Builder
public record EventRequestStatusUpdateResult(
		List<ParticipationRequestDto> confirmedRequests,
		List<ParticipationRequestDto> rejectedRequests
) {
}
