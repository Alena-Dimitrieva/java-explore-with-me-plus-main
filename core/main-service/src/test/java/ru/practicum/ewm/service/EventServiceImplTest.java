package ru.practicum.ewm.service;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.ewm.dao.EventRepository;
import ru.practicum.ewm.dao.RequestRepository;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.dto.event.FreeGetDto;
import ru.practicum.ewm.mapper.EventMapper;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.Location;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.model.enums.EventState;
import ru.practicum.ewm.model.enums.ParticipationStatus;
import ru.practicum.ewm.service.event.EventServiceImpl;
import ru.practicum.ewm.util.statistic.StatRepository;
import ru.practicum.stat.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

	@Mock
	StatRepository statRepository;

	@Mock
	EventRepository eventRepository;

	@Mock
	RequestRepository requestRepository;

	@Mock
	HttpServletRequest httpServletRequest;

	@InjectMocks
	EventServiceImpl eventService;

	private static Event event;

	@BeforeAll
	static void setup() {
		event = Event.builder()
				.id(1L)
				.annotation("annotation".repeat(10))
				.category(Category.builder().id(1L).name("category").build())
				.createdOn(LocalDateTime.parse("2026-11-10T19:00:00"))
				.description("description".repeat(10))
				.eventDate(LocalDateTime.parse("2027-01-01T00:00:01"))
				.initiator(User.builder().id(1L).name("User").email("user@email.ry").build())
				.location(Location.builder().lat(25.55F).lon(35.77F).build())
				.paid(true)
				.participantLimit(30)
				.publishedOn(LocalDateTime.parse("2026-12-10T21:00:00"))
				.requestModeration(true)
				.state(EventState.PUBLISHED)
				.title("title")
				.rate(0)
				.build();
	}

	@Nested
	@DisplayName("Получение списка публичных событий")
	class GetFreeEvents {

		@Test
		void basicFlow() {
			// region setup
			FreeGetDto getDto = FreeGetDto.builder()
					.text("0")
					.categories(List.of(0))
					.paid(true)
					.onlyAvailable(true)
					.rangeStart(LocalDateTime.parse("2026-12-31T23:59:00"))
					.rangeEnd(LocalDateTime.parse("2027-01-01T02:00:00"))
					.sort(FreeGetDto.FreeEventSort.EVENT_DATE)
					.from(0)
					.size(1)
					.build();

			Page<Event> page = new PageImpl<>(List.of(event));
			// endregion setup

			doNothing().when(statRepository).sendHitRequest(httpServletRequest);
			when(eventRepository.findAll(
					ArgumentMatchers.<Specification<Event>>any(),
					ArgumentMatchers.any(Pageable.class)
			)).thenReturn(page);

			List<EventShortDto> result = eventService.getFreeEvents(getDto, httpServletRequest);

			assertThat(result, contains(EventMapper.toEventShortDto(event, 0L, 0L)));
			verify(statRepository).sendHitRequest(httpServletRequest);
		}

		@Test
		void whenNoDateRangeProvided() {
			FreeGetDto dto = FreeGetDto.builder()
					.from(0)
					.size(10)
					.build();

			when(eventRepository.findAll(
					ArgumentMatchers.<Specification<Event>>any(),
					ArgumentMatchers.any(Pageable.class)
			)).thenReturn(Page.empty());

			eventService.getFreeEvents(dto, httpServletRequest);

			verify(eventRepository).findAll(
					ArgumentMatchers.<Specification<Event>>any(),
					ArgumentMatchers.any(Pageable.class)
			);
		}
	}

	@Nested
	@DisplayName("Получение публичного события по id")
	class GetFreeEventById {

		@Test
		void basicFlow() {
			doNothing().when(statRepository).sendHitRequest(httpServletRequest);
			when(eventRepository.existsByIdAndState(event.getId(), EventState.PUBLISHED)).thenReturn(Boolean.TRUE);
			when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
			when(httpServletRequest.getRequestURI()).thenReturn("/events");
			when(statRepository.getStat(List.of("/events"), true))
					.thenReturn(List.of(ViewStatsDto.builder()
							.hits(0L)
							.build()
					));
			when(requestRepository.countByEventIdAndStatus(event.getId(), ParticipationStatus.CONFIRMED))
					.thenReturn(0);

			EventFullDto result = eventService.getFreeEventById(event.getId(), httpServletRequest);

			assertThat(result, is(EventMapper.toEventFullDto(event, 0L, 0L)));
			verify(statRepository).sendHitRequest(httpServletRequest);
		}
	}
}