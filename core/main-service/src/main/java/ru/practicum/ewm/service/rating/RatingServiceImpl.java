package ru.practicum.ewm.service.rating;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dao.EventRepository;
import ru.practicum.ewm.dao.RatingRepository;
import ru.practicum.ewm.dao.UserRepository;
import ru.practicum.ewm.dto.rating.RatingRequest;
import ru.practicum.ewm.dto.rating.RatingResponse;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.Rating;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.model.enums.EventState;
import ru.practicum.ewm.model.enums.Reaction;
import ru.practicum.ewm.util.error.exception.ConflictException;
import ru.practicum.ewm.util.error.exception.NotFoundException;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class RatingServiceImpl implements RatingService {

	private final RatingRepository ratingRepository;
	private final UserRepository userRepository;
	private final EventRepository eventRepository;

	@Override
	public RatingResponse addOrUpdateReaction(Long userId, Long eventId, RatingRequest request) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
		Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
				.orElseThrow(() ->
						new NotFoundException("Событие с id=" + eventId + " не существует или не опубликовано.")
				);
		User initiator = event.getInitiator();
		if (Objects.equals(user.getId(), initiator.getId())) {
			throw new ValidationException("Нельзя ставить реакции своим событиям");
		}

		Rating rating = ratingRepository.findByUserIdAndEventId(userId, eventId).orElse(null);

		if (rating != null) {
			if (rating.getReaction() == request.getReaction()) {
				ratingRepository.delete(rating);
				updateEventRate(event);
				throw new ConflictException("Reaction removed");
			} else {
				rating.setReaction(request.getReaction());
				ratingRepository.save(rating);
				updateEventRate(event);
				return mapToResponse(rating);
			}
		} else {
			rating = Rating.builder()
					.user(user)
					.event(event)
					.reaction(request.getReaction())
					.build();
			ratingRepository.save(rating);
			updateEventRate(event);
			return mapToResponse(rating);
		}
	}

	@Override
	public void removeReaction(Long userId, Long eventId) {
		Rating rating = ratingRepository.findByUserIdAndEventId(userId, eventId)
				.orElseThrow(() -> new NotFoundException("Реакция не найдена"));
		ratingRepository.delete(rating);
		Event event = rating.getEvent();
		updateEventRate(event);
	}

	private void updateEventRate(@NonNull Event event) {
		long likes = ratingRepository.countByEventIdAndReaction(event.getId(), Reaction.LIKE);
		long dislikes = ratingRepository.countByEventIdAndReaction(event.getId(), Reaction.DISLIKE);
		event.setRate(likes - dislikes);
		eventRepository.save(event);
	}

	private RatingResponse mapToResponse(@NonNull Rating rating) {
		return RatingResponse.builder()
				.id(rating.getId())
				.userId(rating.getUser().getId())
				.eventId(rating.getEvent().getId())
				.reaction(rating.getReaction())
				.build();
	}
}
