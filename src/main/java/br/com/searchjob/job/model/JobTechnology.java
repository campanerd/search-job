package br.com.searchjob.job.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "job_technology")
@IdClass(JobTechnologyId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JobTechnology {

	@Id
	@Column(columnDefinition = "INTEGER")
	private Long jobId;

	@Id
	@Column(columnDefinition = "INTEGER")
	private Long technologyId;

	public JobTechnology(Long jobId, Long technologyId) {
		this.jobId = jobId;
		this.technologyId = technologyId;
	}

}
