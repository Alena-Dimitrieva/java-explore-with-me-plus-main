package ru.practicum.ewm.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.model.ParticipationRequest;
import ru.practicum.ewm.model.enums.ParticipationStatus;
import ru.practicum.ewm.service.request.EventRequestCount;

import java.util.List;

public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {

	List<ParticipationRequest> findByRequesterId(Long requesterId);

	List<ParticipationRequest> findAllByIdIn(List<Long> requestIds);

	List<ParticipationRequest> findByEventId(Long eventId);

	int countByEventIdAndStatus(Long eventId, ParticipationStatus status);

	@Query("""
			SELECT r.event.id as eventId,
			COUNT(r) as count
			FROM ParticipationRequest r
			WHERE r.event.id IN :eventIds
			AND r.status = :status
			GROUP BY r.event.id
			""")
	List<EventRequestCount> countConfirmedRequestsByEventIds(List<Long> eventIds, ParticipationStatus status);

	@Modifying
	@Transactional
	@Query("""
			UPDATE ParticipationRequest pr
			SET pr.status = 'REJECTED'
			WHERE pr.event.id = :eventId
			AND pr.status=:status
			""")
	int rejectPendingRequests(Long eventId, ParticipationStatus status);

	boolean existsByRequesterIdAndEventId(Long requesterId, Long eventId);
}
