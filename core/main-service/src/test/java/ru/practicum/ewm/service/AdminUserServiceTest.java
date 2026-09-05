package ru.practicum.ewm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.ewm.dao.UserRepository;
import ru.practicum.ewm.dto.user.NewUserRequest;
import ru.practicum.ewm.dto.user.UserDto;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.service.user.UserServiceImpl;
import ru.practicum.ewm.util.error.exception.NotFoundException;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.eq;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserServiceImpl userService;

	private User user;
	private NewUserRequest newUserRequest;

	@BeforeEach
	void setUp() {
		user = User.builder()
				.id(1L)
				.name("Test User")
				.email("user@example.com")
				.build();
		newUserRequest = NewUserRequest.builder()
				.name("Test User")
				.email("user@example.com")
				.build();
	}

	@Test
	void createUser_shouldReturnUserDto() {
		when(userRepository.save(any(User.class))).thenReturn(user);

		UserDto result = userService.adminAddNewUser(newUserRequest);

		assertThat(result.id()).isEqualTo(1L);
		assertThat(result.name()).isEqualTo("Test User");
		assertThat(result.email()).isEqualTo("user@example.com");
		verify(userRepository).save(any(User.class));
	}

	@Test
	void getUsers_withoutIds_shouldReturnAll() {
		when(userRepository.findAll(any(PageRequest.class)))
				.thenReturn(org.springframework.data.domain.Page.empty());

		List<UserDto> result = userService.getUsers(null, 0, 10);

		assertThat(result).isEmpty();
		verify(userRepository).findAll(any(PageRequest.class));
	}

	@Test
	void getUsers_withIds_shouldFilterByIds() {
		when(userRepository.findAllByIdIn(eq(List.of(1L)), any(PageRequest.class)))
				.thenReturn(List.of(user));

		List<UserDto> result = userService.getUsers(List.of(1L), 0, 10);

		assertThat(result).hasSize(1);
		assertThat(result.getFirst().id()).isEqualTo(1L);
	}

	@Test
	void deleteUser_existingUser_shouldSucceed() {
		when(userRepository.existsById(1L)).thenReturn(true);
		doNothing().when(userRepository).deleteById(1L);

		assertThatCode(() -> userService.deleteUser(1L)).doesNotThrowAnyException();
		verify(userRepository).deleteById(1L);
	}

	@Test
	void deleteUser_notFound_shouldThrowNotFoundException() {
		when(userRepository.existsById(999L)).thenReturn(false);

		assertThatThrownBy(() -> userService.deleteUser(999L))
				.isInstanceOf(NotFoundException.class)
				.hasMessageContaining("Пользователь с id=999 не найден");
		verify(userRepository, never()).deleteById(any());
	}
}