package br.com.searchjob.profile.dto;

import java.time.LocalDateTime;

// @formatter:off
public record ProfileResponse(

		Long id,
		String name,
		LocalDateTime createdAt,
		LocalDateTime updatedAt

) {
}
// @formatter:on
