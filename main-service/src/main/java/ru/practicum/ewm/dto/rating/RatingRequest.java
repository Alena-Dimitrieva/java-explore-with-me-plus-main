package ru.practicum.ewm.dto.rating;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.model.enums.Reaction;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingRequest {

	@NotNull
	private Reaction reaction;
}
