package ru.practicum.ewm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.ewm.dao.CategoryRepository;
import ru.practicum.ewm.dao.EventRepository;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.dto.category.NewCategoryDto;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.service.category.CategoryServiceImpl;
import ru.practicum.ewm.util.error.exception.ConflictException;
import ru.practicum.ewm.util.error.exception.NotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminCategoryServiceTest {

	@Mock
	private CategoryRepository categoryRepository;
	@Mock
	private EventRepository eventRepository;

	@InjectMocks
	private CategoryServiceImpl categoryService;

	private Category category;
	private NewCategoryDto newCategoryDto;
	private CategoryDto updateDto;

	@BeforeEach
	void setUp() {
		category = Category.builder()
				.id(1L)
				.name("Концерты")
				.build();
		newCategoryDto = new NewCategoryDto("Концерты");
		updateDto = CategoryDto.builder()
				.id(1L)
				.name("Выставки")
				.build();
	}

	@Test
	void createCategory_success() {
		when(categoryRepository.existsByName("Концерты")).thenReturn(false);
		when(categoryRepository.save(any(Category.class))).thenReturn(category);

		CategoryDto result = categoryService.adminAddNewCategory(newCategoryDto);

		assertThat(result.id()).isEqualTo(1L);
		assertThat(result.name()).isEqualTo("Концерты");
	}

	@Test
	void createCategory_duplicateName_shouldThrowConflict() {
		when(categoryRepository.existsByName("Концерты")).thenReturn(true);

		assertThatThrownBy(() -> categoryService.adminAddNewCategory(newCategoryDto))
				.isInstanceOf(ConflictException.class)
				.hasMessageContaining("уже существует");
	}

	@Test
	void updateCategory_success() {
		when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
		when(categoryRepository.existsByName("Выставки")).thenReturn(false);
		when(categoryRepository.save(any(Category.class))).thenReturn(category);

		CategoryDto result = categoryService.updateCategory(1L, updateDto);

		assertThat(result.name()).isEqualTo("Выставки");
		verify(categoryRepository).save(category);
	}

	@Test
	void updateCategory_notFound_shouldThrowNotFound() {
		when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> categoryService.updateCategory(999L, updateDto))
				.isInstanceOf(NotFoundException.class);
	}

	@Test
	void deleteCategory_noEvents_success() {
		when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
		when(eventRepository.existsByCategoryId(1L)).thenReturn(false);

		categoryService.deleteCategory(1L);

		verify(categoryRepository).delete(category);
	}

	@Test
	void deleteCategory_withEvents_shouldThrowConflict() {
		when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
		when(eventRepository.existsByCategoryId(1L)).thenReturn(true);

		assertThatThrownBy(() -> categoryService.deleteCategory(1L))
				.isInstanceOf(ConflictException.class)
				.hasMessageContaining("The category is not empty");
		verify(categoryRepository, never()).delete(any());
	}
}