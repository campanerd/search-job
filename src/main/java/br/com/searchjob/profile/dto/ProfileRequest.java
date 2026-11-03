package br.com.searchjob.profile.dto;

import br.com.searchjob.profile.enums.Area;
import br.com.searchjob.profile.enums.ContractType;
import br.com.searchjob.profile.enums.Level;
import br.com.searchjob.profile.enums.WorkMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

// @formatter:off
public record ProfileRequest(

		@NotNull
		Long userId,

		@NotBlank @Size(max = 100)
		String name,

		Level level,
		Area area,
		WorkMode workMode,
		ContractType contractType,

		@Size(max = 100)
		String city,

		@Size(max = 100)
		String state,

		@PositiveOrZero
		Integer minSalary

) {
}
// @formatter:on
