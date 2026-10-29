package br.com.searchjob.profile.dto;

import br.com.searchjob.profile.model.Profile;

import java.time.LocalDateTime;

// @formatter:off
public record ProfileResponse(

		Long id,
		String name,
		LocalDateTime createdAt,
		LocalDateTime updatedAt

) {

	public static ProfileResponse from(Profile profile) {
		return new ProfileResponse(profile.getId(), profile.getName(), profile.getCreatedAt(), profile.getUpdatedAt());
	}

}
// @formatter:on
