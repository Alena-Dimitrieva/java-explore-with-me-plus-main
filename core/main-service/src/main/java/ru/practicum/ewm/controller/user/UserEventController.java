package ru.practicum.ewm.controller.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.dto.event.NewEventDto;
import ru.practicum.ewm.dto.event.UpdateEventUserRequest;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.service.event.EventService;
import ru.practicum.ewm.service.request.RequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/users/{userId}/events")
@RequiredArgsConstructor
public class UserEventController {

	private final EventService eventService;
	private final RequestService requestService;

	/**
	 * Получение событий, добавленных текущим пользователем
	 *
	 * @param userId id текущего пользователя
	 * @param from   количество элементов, которые нужно пропустить для формирования текущего набора
	 *               Default value : 0
	 * @param size   количество элементов в наборе
	 *               Default value : 10
	 * @return List<{@link EventShortDto}>
	 */
	@GetMapping
	public List<EventShortDto> findEventsByUserId(@PathVariable @Positive Long userId,
	                                              @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
	                                              @RequestParam(defaultValue = "10") @Positive Integer size) {
		return eventService.findByUserId(userId, from, size);
	}

	/**
	 * Получение полной информации о событии добавленном текущим пользователем
	 *
	 * @param userId  id текущего пользователя
	 * @param eventId id события
	 * @return {@link EventFullDto}
	 */
	@GetMapping("/{eventId}")
	public EventFullDto findEventById(@PathVariable @Positive Long userId,
	                                  @PathVariable @Positive Long eventId) {
		return eventService.findEventById(userId, eventId);
	}

	/**
	 * Добавление нового события
	 * <p>
	 * Обратите внимание: дата и время на которые намечено событие не может быть раньше,
	 * чем через два часа от текущего момента
	 *
	 * @param userId      id текущего пользователя
	 * @param newEventDto данные добавляемого события
	 * @return {@link EventFullDto}
	 */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public EventFullDto addEvent(@PathVariable @Positive Long userId,
	                             @RequestBody @Valid NewEventDto newEventDto) {

		return eventService.userAddNewEvent(userId, newEventDto);
	}

	/**
	 * Изменение события добавленного текущим пользователем
	 * <p>
	 * Обратите внимание:
	 * <p>
	 * - изменить можно только отмененные события или события в состоянии ожидания модерации (Ожидается код ошибки 409)
	 * <p>
	 * - дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента
	 * (Ожидается код ошибки 409)
	 *
	 * @param userId  id текущего пользователя
	 * @param eventId id редактируемого события
	 * @param request Данные HTTP-запроса
	 * @return {@link EventFullDto}
	 */
	@PatchMapping("/{eventId}")
	public EventFullDto patchEvent(@PathVariable @Positive Long userId,
	                               @PathVariable @Positive Long eventId,
	                               @RequestBody @Valid UpdateEventUserRequest request) {
		return eventService.patchEvent(userId, eventId, request);
	}

	/**
	 * Получение информации о запросах на участие в событии текущего пользователя
	 * <p>
	 * В случае, если по заданным фильтрам не найдено ни одной заявки, возвращает пустой список
	 *
	 * @param userId  id текущего пользователя
	 * @param eventId id события
	 * @return List<{@link ParticipationRequestDto}>
	 */
	@GetMapping("/{eventId}/requests")
	public List<ParticipationRequestDto> getRequests(@PathVariable @Positive Long userId,
	                                                 @PathVariable @Positive Long eventId) {
		return requestService.findByEventId(userId, eventId);
	}

	/**
	 * Изменение статуса (подтверждена, отменена) заявок на участие в событии текущего пользователя
	 * <p>
	 * Обратите внимание:
	 * <p>
	 * - если для события лимит заявок равен 0 или отключена пре-модерация заявок, то подтверждение заявок не требуется
	 * <p>
	 * - нельзя подтвердить заявку, если уже достигнут лимит по заявкам на данное событие (Ожидается код ошибки 409)
	 * <p>
	 * - статус можно изменить только у заявок, находящихся в состоянии ожидания (Ожидается код ошибки 409)
	 * <p>
	 * - если при подтверждении данной заявки, лимит заявок для события исчерпан, то все неподтверждённые заявки
	 * необходимо отклонить
	 *
	 * @param userId  id текущего пользователя
	 * @param eventId id события текущего пользователя
	 * @param status  Новый статус для заявок на участие в событии текущего пользователя
	 * @return {@link EventRequestStatusUpdateResult}
	 */
	@PatchMapping("/{eventId}/requests")
	public EventRequestStatusUpdateResult patchRequests(@PathVariable @Positive Long userId,
	                                                    @PathVariable @Positive Long eventId,
	                                                    @RequestBody @Valid EventRequestStatusUpdateRequest status) {
		return requestService.updateStatusRequest(userId, eventId, status);
	}
}
