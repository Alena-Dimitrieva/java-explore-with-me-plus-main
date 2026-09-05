package ru.practicum.ewm.service.compilation;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dao.CompilationRepository;
import ru.practicum.ewm.dao.EventRepository;
import ru.practicum.ewm.dao.RequestRepository;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.CompilationSearchFilter;
import ru.practicum.ewm.dto.compilation.CompilationUpdateDto;
import ru.practicum.ewm.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.mapper.CompilationMapper;
import ru.practicum.ewm.model.Compilation;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.enums.ParticipationStatus;
import ru.practicum.ewm.service.request.EventRequestCount;
import ru.practicum.ewm.util.error.exception.NotFoundException;
import ru.practicum.ewm.util.statistic.StatRepository;
import ru.practicum.stat.dto.ViewStatsDto;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CompilationServiceImpl implements CompilationService {

	CompilationRepository compilationRepository;
	EventRepository eventRepository;
	StatRepository statRepository;
	RequestRepository requestRepository;

	@Override
	public CompilationDto getById(Long compilationId, HttpServletRequest request) {
		statRepository.sendHitRequest(request);

		Compilation compilation = getCompilationById(compilationId);

		return CompilationMapper.toCompilationDto(
				compilation,
				getConfirmedRequests(List.of(compilation)),
				getViews(List.of(compilation))
		);
	}

	@Override
	@Transactional
	public void delById(Long compilationId) {
		getCompilationById(compilationId);
		compilationRepository.deleteById(compilationId);
	}

	@Override
	@Transactional
	public CompilationDto addCompilation(@NonNull NewCompilationDto compilationDto) {
		Set<Event> events = new HashSet<>();

		if (compilationDto.getEvents() != null && !compilationDto.getEvents().isEmpty()) {
			events = new HashSet<>(eventRepository.findAllById(compilationDto.getEvents()));

			if (events.size() < compilationDto.getEvents().size()) {
				throw new NotFoundException("Одно или несколько событий не найдены");
			}
		}

		Compilation compilation = CompilationMapper.toEntity(compilationDto, events);
		Compilation savedCompilation = compilationRepository.save(compilation);

		Map<Long, Long> confirmedRequests = new HashMap<>();
		savedCompilation.getEvents().forEach(event -> confirmedRequests.put(event.getId(), 0L));

		Map<Long, Long> views = new HashMap<>();
		savedCompilation.getEvents().forEach(event -> views.put(event.getId(), 0L));

		return CompilationMapper.toCompilationDto(
				savedCompilation,
				confirmedRequests,
				views
		);
	}

	@Override
	@Transactional
	public CompilationDto updateCompilation(Long compilationId, @NonNull CompilationUpdateDto compilationUpdateDto) {
		Compilation compilationInDb = getCompilationById(compilationId);

		if (compilationUpdateDto.getEvents() != null) {
			List<Event> eventsUpdate = new ArrayList<>();

			if (!compilationUpdateDto.getEvents().isEmpty()) {
				eventsUpdate = eventRepository.findAllById(compilationUpdateDto.getEvents());
				if (eventsUpdate.size() < compilationUpdateDto.getEvents().size()) {
					throw new NotFoundException("Одно или несколько событий не найдены");
				}
			}
			compilationInDb.setEvents(new HashSet<>(eventsUpdate));

		}

		if (compilationUpdateDto.getTitle() != null && !compilationUpdateDto.getTitle().isBlank()) {
			compilationInDb.setTitle(compilationUpdateDto.getTitle());
		}

		if (compilationUpdateDto.getPinned() != null) {
			compilationInDb.setPinned(compilationUpdateDto.getPinned());
		}

		return CompilationMapper.toCompilationDto(
				compilationInDb, getConfirmedRequests(List.of(compilationInDb)), getViews(List.of(compilationInDb)));
	}

	@Override
	public List<CompilationDto> getByFilter(@NonNull CompilationSearchFilter filter, HttpServletRequest request) {
		Pageable pageable = PageRequest.of(filter.getFrom() / filter.getSize(), filter.getSize());
		Page<Compilation> compilationsPage;

		if (filter.getPinned() != null) {
			compilationsPage = compilationRepository.findAllByPinned(filter.getPinned(), pageable);
		} else {
			compilationsPage = compilationRepository.findAll(pageable);
		}

		List<Compilation> compilations = compilationsPage.getContent();

		if (compilations.isEmpty()) {
			return Collections.emptyList();
		}

		Map<Long, Long> allConfirmedRequests = getConfirmedRequests(compilations);
		Map<Long, Long> allViews = getViews(compilations);

		// Теперь маппим, просто подставляя готовые Map
		return compilations.stream()
				.map(compilation -> CompilationMapper.toCompilationDto(
						compilation,
						allConfirmedRequests,
						allViews
				))
				.toList();
	}

	@NonNull
	private Compilation getCompilationById(long compilationId) {
		return compilationRepository.findById(compilationId).orElseThrow(
				() -> new NotFoundException("Подборка с id=" + compilationId + " не найдена")
		);
	}

	/// Map<eventId, confirmedRequests>
	@NonNull
	private Map<Long, Long> getConfirmedRequests(Collection<Compilation> compilations) {
		List<Event> events = compilations.stream()
				.flatMap(c -> c.getEvents().stream())
				.toList();

		if (events.isEmpty()) return Collections.emptyMap();

		List<EventRequestCount> eventRequestCountList = requestRepository.countConfirmedRequestsByEventIds(
				events.stream().map(Event::getId).toList(),
				ParticipationStatus.CONFIRMED);

		return eventRequestCountList.stream()
				.collect(Collectors.toMap(
						EventRequestCount::getEventId,
						EventRequestCount::getCount,
						(existing, replacement) -> existing
				));
	}

	/// Map<eventId, views>
	@NonNull
	private Map<Long, Long> getViews(Collection<Compilation> compilations) {
		// Все уникальные события
		List<Event> allEvents = compilations.stream()
				.flatMap(c -> c.getEvents().stream())
				.distinct()
				.toList();

		if (allEvents.isEmpty()) return Collections.emptyMap();

		// Все URIs
		List<String> uris = allEvents.stream()
				.map(e -> "/events/" + e.getId())
				.toList();

		List<ViewStatsDto> stats = statRepository.getStat(uris, false);

		//  Map<eventId, hits>
		return stats.stream()
				.collect(Collectors.toMap(
						s -> Long.parseLong(s.getUri().replace("/events/", "")),
						ViewStatsDto::getHits,
						(a, b) -> a
				));
	}
}
