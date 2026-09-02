package ru.practicum.ewm.model;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;
import ru.practicum.ewm.model.enums.EventState;
import ru.practicum.ewm.util.entity.BaseEntity;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "events")
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Event extends BaseEntity {

	/// Краткое описание
	@Column(nullable = false, length = 2000)
	String annotation;

	/// Категория
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id")
	Category category;

	/// Дата и время создания события (в формате "yyyy-MM-dd HH:mm:ss")
	@Column(nullable = false)
	LocalDateTime createdOn;

	/// Полное описание события
	@Column(nullable = false, length = 7000)
	String description;

	/// Дата и время на которые намечено событие (в формате "yyyy-MM-dd HH:mm:ss")
	@Column(nullable = false)
	LocalDateTime eventDate;

	/// Пользователь инициатор события
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "initiator_id")
	User initiator;

	/// Широта и долгота места проведения события
	@Type(JsonBinaryType.class)
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	Location location;

	/// Нужно ли оплачивать участие
	@Column(nullable = false)
	boolean paid;

	/// Ограничение на количество участников. Значение 0 - означает отсутствие ограничения
	@Column(nullable = false)
	int participantLimit;

	/// Дата и время публикации события (в формате "yyyy-MM-dd HH:mm:ss")
	LocalDateTime publishedOn;

	/// Нужна ли пре-модерация заявок на участие
	@Column(nullable = false)
	boolean requestModeration;

	/// Список состояний жизненного цикла события ( PENDING, PUBLISHED, CANCELED )
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	EventState state;

	/// Заголовок
	@Column(nullable = false, length = 120)
	String title;

	/// В каких компиляциях состоит событие
	@ManyToMany(mappedBy = "events")
	List<Compilation> compilations;

	/// Рейтинг события, вычисляемый как количество лайков и дизлайков
	@Column(nullable = false)
	private long rate;
}
