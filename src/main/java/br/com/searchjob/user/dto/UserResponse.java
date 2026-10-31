package br.com.searchjob.user.dto;

import br.com.searchjob.user.model.User;

import java.time.LocalDateTime;

// @formatter:off
public record UserResponse(

		Long id,
		String name,
		LocalDateTime createdAt,
		LocalDateTime updatedAt

) {

	public static UserResponse from(User user) {
		return new UserResponse(user.getId(), user.getName(), user.getCreatedAt(), user.getUpdatedAt());
	}

}
// @formatter:on
