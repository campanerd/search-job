package br.com.searchjob.job.dto;

import br.com.searchjob.job.enums.Source;
import br.com.searchjob.profile.enums.Area;
import br.com.searchjob.profile.enums.ContractType;
import br.com.searchjob.profile.enums.Level;
import br.com.searchjob.profile.enums.WorkMode;

import java.time.LocalDateTime;

// @formatter:off
public record JobResponse(

		Long id,
		Source source,
		String externalId,
		String slug,
		String title,
		String company,
		String platform,
		String url,
		String description,
		Level level,
		Area area,
		WorkMode workMode,
		ContractType contractType,
		String city,
		String state,
		Integer salaryMin,
		Integer salaryMax,
		LocalDateTime publishedAt,
		boolean active,
		LocalDateTime syncedAt

) {
}
// @formatter:on
