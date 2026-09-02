package ru.practicum.ewm.util.specification;

import jakarta.persistence.criteria.Join;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.model.enums.EventState;

import java.time.LocalDateTime;
import java.util.List;

@UtilityClass
public class EventSpecifications {

	/// Поиск по тексту
	public Specification<Event> textContains(String text) {
		return (root, query, cb) -> {
			if (text == null || text.isBlank()) {
				return null;
			}

			String pattern = "%" + text.toLowerCase() + "%";

			return cb.or(
					cb.like(cb.lower(root.get("annotation")), pattern),
					cb.like(cb.lower(root.get("description")), pattern)
			);
		};
	}

	/// Категории
	public Specification<Event> hasCategories(List<Integer> categories) {
		return (root, query, cb) -> {
			if (categories == null || categories.isEmpty()) {
				return null;
			}

			Join<Event, Category> categoryJoin = root.join("category");

			return categoryJoin.get("id").in(categories);
		};
	}

	/// Платное / бесплатное
	public Specification<Event> isPaid(Boolean paid) {
		return (root, query, cb) -> {
			if (paid == null) {
				return null;
			}

			return cb.equal(root.get("paid"), paid);
		};
	}

	/// Дата начала
	public Specification<Event> dateAfter(LocalDateTime rangeStart) {
		return (root, query, cb) -> {
			if (rangeStart == null) {
				return null;
			}

			return cb.greaterThanOrEqualTo(root.get("eventDate"), rangeStart);
		};
	}

	/// Дата окончания
	public Specification<Event> dateBefore(LocalDateTime rangeEnd) {
		return (root, query, cb) -> {
			if (rangeEnd == null) {
				return null;
			}

			return cb.lessThanOrEqualTo(root.get("eventDate"), rangeEnd);
		};
	}

	/// Только доступные события
	public Specification<Event> onlyAvailable(Boolean onlyAvailable) {
		return (root, query, cb) -> {
			if (!Boolean.TRUE.equals(onlyAvailable)) {
				return null;
			}

			return cb.or(
					cb.equal(root.get("participantLimit"), 0),
					cb.lessThan(
							root.get("confirmedRequests"),
							root.get("participantLimit")
					)
			);
		};
	}

	/// Статус = PUBLISHED
	public Specification<Event> isPublished() {
		return (root, query, cb) ->
				cb.equal(root.get("state"), EventState.PUBLISHED);
	}

	/// Дата в будущем
	public Specification<Event> eventDateAfterNow(LocalDateTime now) {
		return (root, query, cb) ->
				cb.greaterThan(root.get("eventDate"), now);
	}

	/// initiator
	public static Specification<Event> hasUsers(List<Integer> userIds) {
		return (root, query, cb) -> {
			if (userIds == null || userIds.isEmpty()) {
				return null;
			}

			Join<Event, User> userJoin = root.join("initiator");
			return userJoin.get("id").in(userIds);
		};
	}

	/// По статусам
	public static Specification<Event> hasStates(List<EventState> states) {
		return (root, query, cb) -> {
			if (states == null || states.isEmpty()) {
				return null;
			}

			return root.get("state").in(states);
		};
	}
}