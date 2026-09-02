package ru.practicum.ewm.service.request;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dao.EventRepository;
import ru.practicum.ewm.dao.RequestRepository;
import ru.practicum.ewm.dao.UserRepository;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.mapper.RequestMapper;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.ParticipationRequest;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.model.enums.EventState;
import ru.practicum.ewm.model.enums.ParticipationStatus;
import ru.practicum.ewm.util.error.exception.ConflictException;
import ru.practicum.ewm.util.error.exception.NotFoundException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RequestServiceImpl implements RequestService {

	RequestRepository requestRepository;
	UserRepository userRepository;
	EventRepository eventRepository;

	public List<ParticipationRequestDto> findByEventId(Long userId, Long eventId) {
		Event event = getEventById(eventId);
		if (!event.getInitiator().getId().equals(userId)) {
			throw new NotFoundException("Событие не найдено");
		}

		return requestRepository.findByEventId(eventId)
				.stream()
				.map(RequestMapper::toParticipationRequestDto)
				.toList();
	}

	public EventRequestStatusUpdateResult updateStatusRequest(Long userId, Long eventId,
	                                                          EventRequestStatusUpdateRequest request) {
		Event event = getEventById(eventId);
		if (!event.getInitiator().getId().equals(userId)) {
			throw new NotFoundException("Событие не найдено");
		}

		int limit = event.getParticipantLimit();
		List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
		List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();

		boolean isModerationOff = !event.isRequestModeration() || limit == 0;
		boolean idsEmpty = request.requestIds() == null || request.requestIds().isEmpty();

		if (isModerationOff || idsEmpty) {
			return EventRequestStatusUpdateResult.builder()
					.confirmedRequests(Collections.emptyList())
					.rejectedRequests(Collections.emptyList())
					.build();
		}

		int countConfirmed = requestRepository.countByEventIdAndStatus(eventId, ParticipationStatus.CONFIRMED);
		List<ParticipationRequest> requests = requestRepository.findAllByIdIn(request.requestIds());

		if (request.status().name().equals(ParticipationStatus.CONFIRMED.name()) && countConfirmed >= limit) {
			throw new ConflictException("Достигнут лимит подтвержденных заявок");
		}

		for (ParticipationRequest pr : requests) {
			if (!pr.getStatus().equals(ParticipationStatus.PENDING)) {
				throw new ConflictException("Статус можно изменить только у заявок в состоянии рассмотрения");
			}

			if (request.status().name().equals(ParticipationStatus.CONFIRMED.name()) && countConfirmed < limit) {
				pr.setStatus(ParticipationStatus.CONFIRMED);
				countConfirmed++;
				confirmedRequests.add(RequestMapper.toParticipationRequestDto(pr));
			} else {
				pr.setStatus(ParticipationStatus.REJECTED);
				rejectedRequests.add(RequestMapper.toParticipationRequestDto(pr));
			}
		}

		requestRepository.saveAll(requests);

		// если в процессе лимит превышен - отклоняем все оставшиеся заявки
		if (request.status().name().equals(ParticipationStatus.CONFIRMED.name()) && countConfirmed >= limit) {
			if (requestRepository.rejectPendingRequests(eventId, ParticipationStatus.PENDING) < 0) {
				throw new RuntimeException("Не удалось отклонить заявку");
			}
		}

		return EventRequestStatusUpdateResult.builder()
				.confirmedRequests(confirmedRequests)
				.rejectedRequests(rejectedRequests)
				.build();
	}

	public List<ParticipationRequestDto> findByRequesterId(Long userId) {
		return requestRepository.findByRequesterId(userId)
				.stream()
				.map(RequestMapper::toParticipationRequestDto)
				.toList();
	}

	@Transactional
	public ParticipationRequestDto addParticipationRequest(Long userId, Long eventId) {
		User requester = getUserById(userId);
		Event event = getEventById(eventId);

		if (!EventState.PUBLISHED.equals(event.getState())) {
			throw new ConflictException("Нельзя участвовать в неопубликованном событии");
		}

		if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
			throw new ConflictException("Запрос уже существует");
		}

		if (event.getInitiator().getId().equals(userId)) {
			throw new ConflictException("Инициатор события не может добавить запрос на участие в своём событии");
		}

		int limit = event.getParticipantLimit();
		if (limit != 0) {
			long confirmedCount = requestRepository.countByEventIdAndStatus(eventId, ParticipationStatus.CONFIRMED);

			if (event.isRequestModeration()) {
				long pendingCount = requestRepository.countByEventIdAndStatus(eventId, ParticipationStatus.PENDING);
				if (confirmedCount + pendingCount >= limit) {
					throw new ConflictException("Достигнут лимит запросов на участие");
				}
			} else {
				if (confirmedCount >= limit) {
					throw new ConflictException("Достигнут лимит запросов на участие");
				}
			}
		}

		ParticipationStatus status;

		if (!event.isRequestModeration() || limit == 0) {
			status = ParticipationStatus.CONFIRMED;
		} else {
			status = ParticipationStatus.PENDING;
		}

		ParticipationRequest request = ParticipationRequest.builder()
				.requester(requester)
				.event(event)
				.status(status)
				.created(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS))
				.build();

		return RequestMapper.toParticipationRequestDto(requestRepository.save(request));
	}

	public ParticipationRequestDto cancelParticipationRequest(Long userId, Long requestId) {
		ParticipationRequest request = getRequestById(requestId);

		if (!request.getRequester().getId().equals(userId)) {
			throw new ConflictException("Нельзя отменить чужую заявку");
		}
		request.setStatus(ParticipationStatus.CANCELED);
		return RequestMapper.toParticipationRequestDto(requestRepository.save(request));
	}

	@NonNull
	private User getUserById(long userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
	}

	@NonNull
	private Event getEventById(long eventId) {
		return eventRepository.findById(eventId).orElseThrow(
				() -> new NotFoundException("Событие с id=" + eventId + " не найдено")
		);
	}

	@NonNull
	private ParticipationRequest getRequestById(Long requestId) {
		return requestRepository.findById(requestId).orElseThrow(
				() -> new NotFoundException("Заявка с id=" + requestId + " не найдена")
		);
	}
}
