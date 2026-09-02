package ru.practicum.ewm.service.rating;

import ru.practicum.ewm.dto.rating.RatingRequest;
import ru.practicum.ewm.dto.rating.RatingResponse;

public interface RatingService {

	RatingResponse addOrUpdateReaction(Long userId, Long eventId, RatingRequest request);

	void removeReaction(Long userId, Long eventId);
}
