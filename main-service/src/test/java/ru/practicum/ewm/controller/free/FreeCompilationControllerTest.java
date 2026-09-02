package ru.practicum.ewm.controller.free;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dao.CompilationRepository;
import ru.practicum.ewm.model.Compilation;

import java.util.HashSet;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FreeCompilationControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CompilationRepository compilationRepository;

	private Compilation pinnedComp;
	private Compilation unpinnedComp;

	@BeforeEach
	void setUp() {
		compilationRepository.deleteAll();

		pinnedComp = Compilation.builder()
				.title("Pinned Compilation")
				.pinned(true)
				.events(new HashSet<>())
				.build();

		unpinnedComp = Compilation.builder()
				.title("Unpinned Compilation")
				.pinned(false)
				.events(new HashSet<>())
				.build();

		pinnedComp = compilationRepository.save(pinnedComp);
		unpinnedComp = compilationRepository.save(unpinnedComp);
	}

	@Test
	void getCompilationById_whenExists_shouldReturnDto() throws Exception {
		mockMvc.perform(get("/compilations/{compilationId}", pinnedComp.getId()))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.id").value(pinnedComp.getId()))
				.andExpect(jsonPath("$.title").value("Pinned Compilation"))
				.andExpect(jsonPath("$.pinned").value(true));
	}

	@Test
	void getCompilationById_whenNotFound_shouldReturn404() throws Exception {
		mockMvc.perform(get("/compilations/{compilationId}", 999L))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value("NOT_FOUND"));
	}

	@Test
	void getCompilations_whenPinnedTrue_shouldReturnOnlyPinned() throws Exception {
		mockMvc.perform(get("/compilations")
						.param("pinned", "true")
						.param("from", "0")
						.param("size", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].id").value(pinnedComp.getId()))
				.andExpect(jsonPath("$[0].pinned").value(true));
	}

	@Test
	void getCompilations_withoutParams_shouldReturnAll() throws Exception {
		mockMvc.perform(get("/compilations"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)));
	}

	@Test
	void getCompilations_withPagination_shouldReturnCorrectSize() throws Exception {
		mockMvc.perform(get("/compilations")
						.param("from", "0")
						.param("size", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)));
	}

}