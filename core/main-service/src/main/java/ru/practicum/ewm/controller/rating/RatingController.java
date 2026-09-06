package ru.practicum.ewm.controller.rating;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.rating.RatingRequest;
import ru.practicum.ewm.dto.rating.RatingResponse;
import ru.practicum.ewm.service.rating.RatingService;

@RestController
@RequestMapping("/users/{userId}/events/{eventId}/likes")
@RequiredArgsConstructor
public class RatingController {

	private final RatingService ratingService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public RatingResponse addReaction(@PathVariable Long userId,
	                                  @PathVariable Long eventId,
	                                  @Valid @RequestBody RatingRequest request) {
		return ratingService.addOrUpdateReaction(userId, eventId, request);
	}

	@DeleteMapping
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void removeReaction(@PathVariable Long userId,
	                           @PathVariable Long eventId) {
		ratingService.removeReaction(userId, eventId);
	}
}