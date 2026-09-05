package ru.practicum.ewm.controller.free;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import ru.practicum.ewm.MainServiceApp;
import ru.practicum.ewm.dao.*;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.dto.event.FreeGetDto;
import ru.practicum.ewm.mapper.EventMapper;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.Location;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.model.enums.EventState;
import ru.practicum.ewm.service.category.CategoryService;
import ru.practicum.ewm.service.event.EventService;
import ru.practicum.ewm.service.rating.RatingService;
import ru.practicum.ewm.service.user.UserService;
import ru.practicum.ewm.util.statistic.StatRepository;
import ru.practicum.stat.client.StatClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = FreeEventController.class)
@ContextConfiguration(classes = MainServiceApp.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class FreeEventControllerTest {

	@Autowired
	ObjectMapper mapper;

	@Autowired
	MockMvc mvc;

	@MockitoBean
	EventService eventService;

	@MockitoBean
	StatRepository statRepository;

	@MockitoBean
	CategoryService categoryService;

	@MockitoBean
	UserService userService;

	@MockitoBean
	StatClient statClient;

	@MockitoBean
	CompilationRepository compilationRepository;

	@MockitoBean
	UserRepository userRepository;

	@MockitoBean
	RequestRepository requestRepository;

	@MockitoBean
	EventRepository eventRepository;

	@MockitoBean
	RatingService ratingService;

	@MockitoBean
	RatingRepository ratingRepository;

	final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	static Event event;

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

	@Test
	@SneakyThrows
	void getFreeEvents() {
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

		List<EventShortDto> shortEvents = List.of(EventMapper.toEventShortDto(event, 0L, 0L));

		doNothing().when(statRepository).sendHitRequest(any(HttpServletRequest.class));
		when(eventService.getFreeEvents(eq(getDto), any(HttpServletRequest.class)))
				.thenReturn(shortEvents);

		String result = mvc.perform(get("/events")
						.param("text", getDto.text())
						.param("categories", getDto.categories()
								.toString().replace("[", "")
								.replace("]", "")
						)
						.param("paid", getDto.paid().toString())
						.param("rangeStart", getDto.rangeStart().format(formatter))
						.param("rangeEnd", getDto.rangeEnd().format(formatter))
						.param("onlyAvailable", getDto.onlyAvailable().toString())
						.param("sort", getDto.sort().name())
						.param("from", getDto.from().toString())
						.param("size", getDto.size().toString())
				)
				.andDo(MockMvcResultHandlers.print())
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		assertThat(result, is(mapper.writeValueAsString(shortEvents)));
	}

	@Test
	@SneakyThrows
	void getFreeEventById() {
		EventFullDto dto = EventMapper.toEventFullDto(event, 0L, 0L);

		doNothing().when(statRepository).sendHitRequest(any(HttpServletRequest.class));
		when(eventService.getFreeEventById(eq(1L), any(HttpServletRequest.class)))
				.thenReturn(dto);

		String result = mvc.perform(get("/events/{eventId}", 1L))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		assertThat(result, is(mapper.writeValueAsString(dto)));
	}
}