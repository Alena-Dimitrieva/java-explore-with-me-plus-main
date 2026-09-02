package ru.practicum.ewm.service.user;

import jakarta.transaction.Transactional;
import ru.practicum.ewm.dto.user.NewUserRequest;
import ru.practicum.ewm.dto.user.UserDto;

import java.util.List;

public interface UserService {

	@Transactional
	UserDto adminAddNewUser(NewUserRequest newUserRequest);

	List<UserDto> getUsers(List<Long> ids, int from, int size);

	@Transactional
	void deleteUser(Long userId);
}
