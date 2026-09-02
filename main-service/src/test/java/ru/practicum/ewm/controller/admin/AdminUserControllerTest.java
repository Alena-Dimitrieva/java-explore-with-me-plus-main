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
import ru.practicum.ewm.dto.user.NewUserRequest;
import ru.practicum.ewm.dto.user.UserDto;
import ru.practicum.ewm.service.user.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminUserControllerTest {

	@Mock
	private UserService userService;

	@InjectMocks
	private AdminUserController controller;

	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
	}

	@Test
	void createUser_validRequest_shouldReturn201() throws Exception {
		NewUserRequest request = new NewUserRequest("Test User", "user@example.com");
		UserDto userDto = new UserDto(1L, "Test User", "user@example.com");
		when(userService.adminAddNewUser(any(NewUserRequest.class))).thenReturn(userDto);

		mockMvc.perform(post("/admin/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1L))
				.andExpect(jsonPath("$.name").value("Test User"))
				.andExpect(jsonPath("$.email").value("user@example.com"));
	}

	@Test
	void createUser_blankName_shouldReturn400() throws Exception {
		NewUserRequest request = new NewUserRequest("", "user@example.com");

		mockMvc.perform(post("/admin/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void getUsers_shouldReturnList() throws Exception {
		UserDto user = new UserDto(1L, "Test User", "user@example.com");
		when(userService.getUsers(any(), anyInt(), anyInt())).thenReturn(List.of(user));

		mockMvc.perform(get("/admin/users")
						.param("from", "0")
						.param("size", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(1L))
				.andExpect(jsonPath("$[0].name").value("Test User"));
	}

	@Test
	void deleteUser_existingUser_shouldReturn204() throws Exception {
		mockMvc.perform(delete("/admin/users/1"))
				.andExpect(status().isNoContent());
	}
}