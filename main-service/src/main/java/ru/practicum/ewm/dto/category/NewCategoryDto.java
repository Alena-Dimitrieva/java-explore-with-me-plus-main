package ru.practicum.ewm.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NewCategoryDto(

		@NotBlank
		@Size(min = 1, max = 50)
		String name
) {
}
