package ru.practicum.ewm.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.practicum.ewm.MainServiceApp;
import ru.practicum.ewm.TestUtils;
import ru.practicum.ewm.dto.event.AdminGetDto;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.dto.event.FreeGetDto;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.enums.EventState;
import ru.practicum.ewm.service.event.EventService;
import ru.practicum.ewm.util.statistic.StatRepository;
import ru.practicum.stat.dto.ViewStatsDto;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@Transactional
@SpringBootTest
@EnableAutoConfiguration(exclude = {
		WebMvcAutoConfiguration.class
})
@FieldDefaults(level = AccessLevel.PRIVATE)
@ContextConfiguration(classes = MainServiceApp.class)
class EventServiceIntegrationTest {

	@MockitoBean
	StatRepository statRepository;

	@Autowired
	EventService eventService;

	@Autowired
	TestUtils testUtils;

	@PersistenceContext
	EntityManager em;

	public Event mergeEvent(@NonNull Event event) {
		event.setInitiator(em.merge(event.getInitiator()));
		event.setCategory(em.merge(event.getCategory()));
		return em.merge(event);
	}

	@Nested
	@DisplayName("Получение публичного события по поиску")
	class GetFreeEvents {

		@Test
		void whenBaseFlow_thenReturnOnlyPublishedEvents() {
			// region setup
			Event event = mergeEvent(
					testUtils.generateEvent(testUtils.futureDate, EventState.PENDING)
			);
			mergeEvent(testUtils.getNewCopyOfEvent(event, EventState.CANCELED));
			mergeEvent(testUtils.getNewCopyOfEvent(event, EventState.PUBLISHED)
					.toBuilder().title("test title").build());

			FreeGetDto dto = FreeGetDto.builder().from(0).size(10).build();
			// endregion setup

			doNothing().when(statRepository).sendHitRequest(any(HttpServletRequest.class));

			List<EventShortDto> result =
					eventService.getFreeEvents(dto, mock(HttpServletRequest.class));

			assertThat(result).hasSize(1);
			assertThat(result.getFirst().title()).isEqualTo("test title");
			verify(statRepository).sendHitRequest(any(HttpServletRequest.class));
		}
	}

	@Nested
	@DisplayName("Получение админского события по поиску")
	class GetAdminEvents {

		@Test
		void whenBaseFlow_thenReturnOnlyPendingEvents() {
			// region setup
			Event event = mergeEvent(
					testUtils.generateEvent(testUtils.futureDate, EventState.PUBLISHED)
			);

			mergeEvent(testUtils.getNewCopyOfEvent(event, EventState.CANCELED));
			mergeEvent(testUtils.getNewCopyOfEvent(event, EventState.PENDING)
					.toBuilder().title("test title").build());

			AdminGetDto dto = AdminGetDto.builder()
					.users(List.of(1))
					.categories(List.of(1))
					.states(List.of(EventState.PENDING))
					.from(0)
					.size(10)
					.build();

			List<ViewStatsDto> stats = Collections.emptyList();
			// endregion setup

			when(statRepository.getStat(List.of("/events/2"), false))
					.thenReturn(stats);

			List<EventFullDto> result = eventService.adminGetEvents(dto);

			assertThat(result).hasSize(1);
			assertThat(result.getFirst().title()).isEqualTo("test title");
			verify(statRepository, never()).sendHitRequest(any(HttpServletRequest.class));
		}
	}
}