package ru.practicum.ewm.dto.event;

import jakarta.validation.constraints.Size;
import ru.practicum.ewm.model.Location;
import ru.practicum.ewm.model.enums.UserStateAction;

import java.time.LocalDateTime;

public record UpdateEventUserRequest(

		@Size(min = 20, max = 2000)
		String annotation,

		Long category,

		@Size(min = 20, max = 7000)
		String description,

		LocalDateTime eventDate,

		Location location,

		Boolean paid,

		Integer participantLimit,

		Boolean requestModeration,

		@Size(min = 3, max = 120)
		String title,

		UserStateAction stateAction
) {
}
