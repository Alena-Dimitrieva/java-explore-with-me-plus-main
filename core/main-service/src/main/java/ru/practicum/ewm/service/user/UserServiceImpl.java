package ru.practicum.ewm.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dao.UserRepository;
import ru.practicum.ewm.dto.user.NewUserRequest;
import ru.practicum.ewm.dto.user.UserDto;
import ru.practicum.ewm.mapper.UserMapper;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.util.error.exception.ConflictException;
import ru.practicum.ewm.util.error.exception.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;

	@Override
	public UserDto adminAddNewUser(@NonNull NewUserRequest newUserRequest) {
		if (userRepository.existsByEmail(newUserRequest.email())) {
			throw new ConflictException("Пользователь с такой почтой " + newUserRequest.email() + " уже существует");
		}
		User user = UserMapper.toEntity(newUserRequest);
		return UserMapper.toUserDto(userRepository.save(user));
	}

	@Override
	public List<UserDto> getUsers(List<Long> ids, int from, int size) {
		PageRequest page = PageRequest.of(from / size, size);
		List<User> users;
		if (ids == null || ids.isEmpty()) {
			users = userRepository.findAll(page).getContent();
		} else {
			users = userRepository.findAllByIdIn(ids, page);
		}
		return users.stream()
				.map(UserMapper::toUserDto)
				.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public void deleteUser(Long userId) {
		checkUser(userId);
		userRepository.deleteById(userId);
	}

	private void checkUser(Long userId) {
		if (!userRepository.existsById(userId)) {
			throw new NotFoundException("Пользователь с id=" + userId + " не найден");
		}
	}
}
