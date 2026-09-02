package ru.practicum.ewm.dto.user;

import lombok.Builder;

@Builder
public record UserDto(
		Long id,
		String name,
		String email
) {
}
