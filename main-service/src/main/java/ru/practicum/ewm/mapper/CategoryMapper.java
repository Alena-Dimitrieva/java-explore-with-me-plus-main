package ru.practicum.ewm.mapper;

import lombok.experimental.UtilityClass;
import org.springframework.lang.NonNull;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.dto.category.NewCategoryDto;
import ru.practicum.ewm.model.Category;

@UtilityClass
public class CategoryMapper {
	public CategoryDto toDto(@NonNull Category category) {
		return CategoryDto.builder()
				.id(category.getId())
				.name(category.getName())
				.build();
	}

	public Category toEntity(@NonNull CategoryDto categoryDto) {
		return Category.builder()
				.name(categoryDto.name())
				.build();
	}

	public Category toEntity(@NonNull NewCategoryDto newCategoryDto) {
		return Category.builder()
				.name(newCategoryDto.name())
				.build();
	}
}
