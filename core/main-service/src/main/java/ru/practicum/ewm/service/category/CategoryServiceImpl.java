package ru.practicum.ewm.service.category;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dao.CategoryRepository;
import ru.practicum.ewm.dao.EventRepository;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.dto.category.NewCategoryDto;
import ru.practicum.ewm.mapper.CategoryMapper;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.util.error.exception.ConflictException;
import ru.practicum.ewm.util.error.exception.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryServiceImpl implements CategoryService {

	CategoryRepository categoryRepository;
	EventRepository eventRepository;

	@Override
	public CategoryDto adminAddNewCategory(@NonNull NewCategoryDto newCategoryDto) {
		if (categoryRepository.existsByName(newCategoryDto.name())) {
			throw new ConflictException(
					"Категория с именем '" + newCategoryDto.name() + "' уже существует");
		}
		Category category = CategoryMapper.toEntity(newCategoryDto);
		return CategoryMapper.toDto(categoryRepository.save(category));
	}

	@Override
	public List<CategoryDto> findAll(Integer from, Integer size) {
		int page = from / size;

		return categoryRepository.findAll(PageRequest.of(page, size)).stream()
				.map(CategoryMapper::toDto)
				.collect(Collectors.toList());
	}

	@Override
	public CategoryDto findById(Long catId) {
		Category category = getCategoryById(catId);

		return CategoryMapper.toDto(category);
	}

	@Override
	@Transactional
	public CategoryDto updateCategory(Long catId, @NonNull CategoryDto categoryDto) {
		Category category = getCategoryById(catId);
		if (!category.getName().equals(categoryDto.name()) &&
				categoryRepository.existsByName(categoryDto.name())) {
			throw new ConflictException(
					"Категория с именем '" + categoryDto.name() + "' уже существует");
		}

		category.setName(categoryDto.name());
		return CategoryMapper.toDto(categoryRepository.save(category));
	}

	@Override
	@Transactional
	public void deleteCategory(Long catId) {
		Category category = getCategoryById(catId);
		if (eventRepository.existsByCategoryId(catId)) {
			throw new ConflictException("The category is not empty");
		}

		categoryRepository.delete(category);
	}

	@NonNull
	private Category getCategoryById(long categoryId) {
		return categoryRepository.findById(categoryId).orElseThrow(
				() -> new NotFoundException("Категория с id=" + categoryId + " не найдена")
		);
	}
}
