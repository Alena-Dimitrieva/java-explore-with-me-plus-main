package ru.practicum.ewm.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.dto.category.NewCategoryDto;
import ru.practicum.ewm.service.category.CategoryService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminCategoryControllerTest {

	@Mock
	private CategoryService categoryService;

	@InjectMocks
	private AdminCategoryController controller;

	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
	}

	@Test
	void createCategory_blankName_shouldReturn400() throws Exception {
		NewCategoryDto newCat = new NewCategoryDto("");

		mockMvc.perform(post("/admin/categories")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(newCat)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void updateCategory_validData_shouldReturn200() throws Exception {
		CategoryDto update = CategoryDto.builder().id(1L).name("Выставки").build();
		CategoryDto updated = CategoryDto.builder().id(1L).name("Выставки").build();
		when(categoryService.updateCategory(1L, update)).thenReturn(updated);

		mockMvc.perform(patch("/admin/categories/1")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(update)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Выставки"));
	}

	@Test
	void deleteCategory_existing_shouldReturn204() throws Exception {
		mockMvc.perform(delete("/admin/categories/1"))
				.andExpect(status().isNoContent());
	}
}