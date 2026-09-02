package ru.practicum.ewm.mapper;

import lombok.experimental.UtilityClass;
import org.springframework.lang.NonNull;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.model.enums.EventState;

import java.time.LocalDateTime;
import java.util.Optional;

@UtilityClass
public class EventMapper {

	public EventShortDto toEventShortDto(@NonNull Event event, long confirmedRequests, long views) {
		return EventShortDto.builder()
				.annotation(event.getAnnotation())
				.category(CategoryMapper.toDto(event.getCategory()))
				.confirmedRequests(confirmedRequests)
				.eventDate(event.getEventDate())
				.id(event.getId())
				.initiator(UserMapper.toUserShortDto(event.getInitiator()))
				.paid(event.isPaid())
				.title(event.getTitle())
				.views(views)
				.rate(event.getRate())
				.build();
	}

	public EventFullDto toEventFullDto(@NonNull Event event, long confirmedRequests, long views) {
		return EventFullDto.builder()
				.id(event.getId())
				.annotation(event.getAnnotation())
				.category(CategoryMapper.toDto(event.getCategory()))
				.confirmedRequests(confirmedRequests)
				.createdOn(event.getCreatedOn())
				.description(event.getDescription())
				.eventDate(event.getEventDate())
				.initiator(UserMapper.toUserShortDto(event.getInitiator()))
				.location(event.getLocation())
				.paid(event.isPaid())
				.participantLimit(event.getParticipantLimit())
				.publishedOn(event.getPublishedOn())
				.requestModeration(event.isRequestModeration())
				.state(event.getState())
				.title(event.getTitle())
				.views(views)
				.rate(event.getRate())
				.build();
	}

	public Event toEntity(@NonNull NewEventDto newEventDto,
	                      Category category,
	                      LocalDateTime createdOn,
	                      User initiator,
	                      LocalDateTime publishedOn,
	                      EventState state) {
		return Event.builder()
				.annotation(newEventDto.annotation())
				.category(category)
				.createdOn(createdOn)
				.description(newEventDto.description())
				.eventDate(newEventDto.eventDate())
				.initiator(initiator)
				.location(newEventDto.location())
				.paid(newEventDto.paid() != null && newEventDto.paid())
				.participantLimit(newEventDto.participantLimit() == null ? 0 : newEventDto.participantLimit())
				.publishedOn(publishedOn)
				.requestModeration(newEventDto.requestModeration() == null || newEventDto.requestModeration())
				.state(state)
				.title(newEventDto.title())
				.rate(0)
				.build();
	}

	public static Event update(@NonNull Event oldEvent,
	                           @NonNull UpdateEventAdminRequest request,
	                           EventState state,
	                           LocalDateTime publishedOn,
	                           @NonNull Optional<Category> category) {

		return oldEvent.toBuilder()
				.annotation(request.annotation() != null ?
						request.annotation() : oldEvent.getAnnotation()
				)
				.category(category.orElse(oldEvent.getCategory()))
				.description(request.description() != null ?
						request.description() : oldEvent.getDescription()
				)
				.eventDate(request.eventDate() != null ?
						request.eventDate() : oldEvent.getEventDate()
				)
				.location(request.location() != null ?
						request.location() : oldEvent.getLocation()
				)
				.paid(request.paid() != null ?
						request.paid() : oldEvent.isPaid()
				)
				.participantLimit(request.participantLimit() != null ?
						request.participantLimit() : oldEvent.getParticipantLimit()
				)
				.requestModeration(request.requestModeration() != null ?
						request.requestModeration() : oldEvent.isRequestModeration()
				)
				.state(state)
				.title(request.title() != null ?
						request.title() : oldEvent.getTitle()
				)
				.publishedOn(publishedOn)
				.build();
	}

	public void merge(Event event, UpdateEventUserRequest dto) {
		if (dto == null) return;

		if (dto.annotation() != null) {
			event.setAnnotation(dto.annotation());
		}

		if (dto.description() != null) {
			event.setDescription(dto.description());
		}

		if (dto.title() != null) {
			event.setTitle(dto.title());
		}

		if (dto.eventDate() != null) {
			event.setEventDate(dto.eventDate());
		}

		if (dto.paid() != null) {
			event.setPaid(dto.paid());
		}

		if (dto.participantLimit() != null) {
			event.setParticipantLimit(dto.participantLimit());
		}

		if (dto.requestModeration() != null) {
			event.setRequestModeration(dto.requestModeration());
		}
	}
}
