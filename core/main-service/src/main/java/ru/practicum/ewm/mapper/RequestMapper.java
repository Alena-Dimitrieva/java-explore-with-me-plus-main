package ru.practicum.ewm.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.model.ParticipationRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Component
public class RequestMapper {

	public static ParticipationRequestDto toParticipationRequestDto(ParticipationRequest participationRequest) {
		if (participationRequest == null) return null;
		return ParticipationRequestDto.builder()
				.event(participationRequest.getEvent().getId())
				.requester(participationRequest.getRequester().getId())
				.status(participationRequest.getStatus())
				.created(formatDateTime(participationRequest.getCreated()))
				.id(participationRequest.getId())
				.build();
	}

	private static String formatDateTime(LocalDateTime dateTime) {
		if (dateTime == null) return null;

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
		return dateTime.format(formatter);
	}
}
