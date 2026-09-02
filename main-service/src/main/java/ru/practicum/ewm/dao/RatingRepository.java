package ru.practicum.ewm.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.model.Rating;
import ru.practicum.ewm.model.enums.Reaction;

import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {

	Optional<Rating> findByUserIdAndEventId(Long userId, Long eventId);

	boolean existsByUserIdAndEventId(Long userId, Long eventId);

	long countByEventIdAndReaction(Long eventId, Reaction reaction);
}
