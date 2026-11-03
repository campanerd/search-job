package br.com.searchjob.profile.dto;

import br.com.searchjob.profile.enums.Area;
import br.com.searchjob.profile.enums.ContractType;
import br.com.searchjob.profile.enums.Level;
import br.com.searchjob.profile.enums.WorkMode;
import br.com.searchjob.profile.model.Profile;

import java.time.LocalDateTime;

// @formatter:off
public record ProfileResponse(

		Long id,
		Long userId,
		String name,
		Level level,
		Area area,
		WorkMode workMode,
		ContractType contractType,
		String city,
		String state,
		Integer minSalary,
		LocalDateTime createdAt,
		LocalDateTime updatedAt

) {

	public static ProfileResponse from(Profile profile) {
		return new ProfileResponse(profile.getId(), profile.getUserId(), profile.getName(), profile.getLevel(),
				profile.getArea(), profile.getWorkMode(), profile.getContractType(), profile.getCity(),
				profile.getState(), profile.getMinSalary(), profile.getCreatedAt(), profile.getUpdatedAt());
	}

}
// @formatter:on
