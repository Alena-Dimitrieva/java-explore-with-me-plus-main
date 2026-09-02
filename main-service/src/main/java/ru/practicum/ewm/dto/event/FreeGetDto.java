package ru.practicum.ewm.dto.event;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@NotNull
@Builder
public record FreeGetDto(
		String text,
		List<Integer> categories,
		Boolean paid,
		LocalDateTime rangeStart,
		LocalDateTime rangeEnd,
		Boolean onlyAvailable,
		FreeEventSort sort,
		Integer from,
		Integer size
) {

	public enum FreeEventSort {
		EVENT_DATE, VIEWS
	}
}
