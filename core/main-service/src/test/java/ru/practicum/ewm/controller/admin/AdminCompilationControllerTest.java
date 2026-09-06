package ru.practicum.ewm.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.compilation.CompilationUpdateDto;
import ru.practicum.ewm.dto.compilation.NewCompilationDto;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminCompilationControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void createCompilation_WithoutEvents_ShouldReturn201() throws Exception {
		NewCompilationDto dto = NewCompilationDto.builder()
				.title("Подборка без событий")
				.pinned(true)
				.events(null)
				.build();

		mockMvc.perform(post("/admin/compilations")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.title").value("Подборка без событий"))
				.andExpect(jsonPath("$.pinned").value(true))
				.andExpect(jsonPath("$.events").isEmpty());
	}

	@Test
	void updateCompilation_onlyTitle_shouldUpdateTitleAndKeepOtherFields() throws Exception {
		NewCompilationDto createDto = NewCompilationDto.builder()
				.title("Старое название")
				.pinned(true)
				.build();

		String response = mockMvc.perform(post("/admin/compilations")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(createDto)))
				.andReturn().getResponse().getContentAsString();

		Long id = objectMapper.readTree(response).get("id").asLong();


		CompilationUpdateDto updateDto = CompilationUpdateDto.builder()
				.title("Новое название")
				.build();

		mockMvc.perform(patch("/admin/compilations/{compilationId}", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateDto)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Новое название"))
				.andExpect(jsonPath("$.pinned").value(true));
	}

	@Test
	void deleteCompilation_whenExists_shouldReturnNoContentAndThen404() throws Exception {
		NewCompilationDto createDto = NewCompilationDto.builder()
				.title("На удаление")
				.build();

		String response = mockMvc.perform(post("/admin/compilations")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(createDto)))
				.andReturn().getResponse().getContentAsString();

		Long id = objectMapper.readTree(response).get("id").asLong();

		mockMvc.perform(delete("/admin/compilations/{compilationId}", id))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/compilations/{compilationId}", id))
				.andExpect(status().isNotFound());
	}

	@Test
	void updateCompilation_whenNotFound_shouldReturn404() throws Exception {
		CompilationUpdateDto updateDto = CompilationUpdateDto.builder()
				.title("Никому не нужное название")
				.build();

		mockMvc.perform(patch("/admin/compilations/{compilationId}", 999L)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateDto)))
				.andExpect(status().isNotFound());
	}
}