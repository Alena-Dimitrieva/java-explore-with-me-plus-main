package ru.practicum.ewm.controller.user;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.service.request.RequestService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/users/{userId}/requests")
@RequiredArgsConstructor
public class UserRequestController {
	private final RequestService service;

	/**
	 * Получение информации о заявках текущего пользователя на участие в чужих событиях
	 * <p>
	 * В случае, если по заданным фильтрам не найдено ни одной заявки, возвращает пустой список
	 *
	 * @param userId id текущего пользователя
	 * @return List<{@link ParticipationRequestDto}>
	 */
	@GetMapping
	public List<ParticipationRequestDto> findByRequesterId(@PathVariable @Positive Long userId) {
		return service.findByRequesterId(userId);
	}

	/**
	 * Добавление запроса от текущего пользователя на участие в событии
	 * <p>
	 * Обратите внимание:
	 * <p>
	 * нельзя добавить повторный запрос (Ожидается код ошибки 409)
	 * <p>
	 * инициатор события не может добавить запрос на участие в своём событии (Ожидается код ошибки 409)
	 * <p>
	 * нельзя участвовать в неопубликованном событии (Ожидается код ошибки 409)
	 * <p>
	 * если у события достигнут лимит запросов на участие - необходимо вернуть ошибку (Ожидается код ошибки 409)
	 * <p>
	 * если для события отключена пре-модерация запросов на участие, то запрос должен автоматически перейти в
	 * состояние подтвержденного
	 *
	 * @param userId  id текущего пользователя
	 * @param eventId id события
	 * @return {@link ParticipationRequestDto}
	 */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ParticipationRequestDto addParticipationRequest(@PathVariable @Positive Long userId,
	                                                       @RequestParam @Positive Long eventId) {
		return service.addParticipationRequest(userId, eventId);
	}

	/**
	 * Отмена своего запроса на участие в событии
	 *
	 * @param userId    id текущего пользователя
	 * @param requestId id запроса на участие
	 * @return {@link ParticipationRequestDto}
	 */
	@PatchMapping("/{requestId}/cancel")
	public ParticipationRequestDto cancelParticipationRequest(@PathVariable @Positive Long userId,
	                                                          @PathVariable @Positive Long requestId) {
		return service.cancelParticipationRequest(userId, requestId);
	}
}
