package ru.practicum.ewm;

import org.springframework.boot.test.context.TestComponent;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.Location;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.model.enums.EventState;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@SuppressWarnings("unused")
@TestComponent
public class TestUtils {

	public final LocalDateTime pastDate = LocalDateTime.parse("2023-01-01T00:00:01");
	public final LocalDateTime futureDate = LocalDateTime.parse("2028-01-01T00:00:01");

	private final Random random = new Random();

	public String generateRandomText(int length, String addAfterStr, Character... addSynbols) {
		java.util.List<Character> symbols = new ArrayList<>(List.of(
				'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
				'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
				'u', 'v', 'w', 'x', 'y', 'z'
		));
		symbols.addAll(Arrays.asList(addSynbols));
		StringBuilder sb = new StringBuilder(length);

		for (int i = 0; i < length; i++) {
			int index = random.nextInt(symbols.size());
			sb.append(symbols.get(index));
		}

		return sb.append(addAfterStr).toString();
	}

	public Location generateLocation() {
		return Location.builder()
				.lat(random.nextFloat())
				.lon(random.nextFloat())
				.build();
	}

	public User generateUser() {
		return User.builder()
				.name(generateRandomText(15, "", ' '))
				.email(generateRandomText(15, "@mail.ru"))
				.build();
	}

	public Category generateCategory() {
		return Category.builder()
				.name(generateRandomText(10, ""))
				.build();
	}

	public Event generateEvent(User initiator,
	                           Category category,
	                           LocalDateTime eventDate,
	                           EventState eventState) {
		return Event.builder()
				.annotation(generateRandomText(100, "", ' '))
				.category(category)
				.createdOn(eventDate.minusDays(1))
				.description(generateRandomText(200, "", ' '))
				.eventDate(eventDate)
				.initiator(initiator)
				.location(generateLocation())
				.paid(random.nextBoolean())
				.participantLimit(random.nextInt(100))
				.publishedOn(eventDate.plusHours(3))
				.requestModeration(random.nextBoolean())
				.state(eventState)
				.title(generateRandomText(15, "", ' '))
				.rate(0)
				.build();
	}

	public Event generateEvent(LocalDateTime eventDate, EventState eventState) {
		return generateEvent(generateUser(), generateCategory(), eventDate, eventState);
	}

	public Event generateEvent(EventState eventState) {
		return generateEvent(pastDate, eventState);
	}

	public Event generateEvent() {
		return generateEvent(pastDate, EventState.PUBLISHED);
	}

	public Event getNewCopyOfEvent(Event event) {
		return event.toBuilder().id(null).build();
	}

	public Event getNewCopyOfEvent(Event event, EventState eventState) {
		return event.toBuilder().id(null).state(eventState).build();
	}
}