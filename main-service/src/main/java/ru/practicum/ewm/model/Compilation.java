package ru.practicum.ewm.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import ru.practicum.ewm.util.entity.BaseEntity;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "compilations")
@SuperBuilder
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Compilation extends BaseEntity {

	@Column(nullable = false, length = 50)
	String title;

	@ManyToMany
	@JoinTable(name = "compilation_events",
			joinColumns = @JoinColumn(name = "compilations_id"),
			inverseJoinColumns = @JoinColumn(name = "events_id"))
	@Builder.Default
	Set<Event> events = new HashSet<>();

	@Column(nullable = false)
	boolean pinned;
}
