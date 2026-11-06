package br.com.searchjob.job.model;

import br.com.searchjob.job.enums.Source;
import br.com.searchjob.profile.enums.Area;
import br.com.searchjob.profile.enums.ContractType;
import br.com.searchjob.profile.enums.Level;
import br.com.searchjob.profile.enums.WorkMode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "job")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Job {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(columnDefinition = "INTEGER")
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20, updatable = false)
	private Source source;

	@Column(nullable = false, length = 50, updatable = false)
	private String externalId;

	@Column(length = 255)
	private String slug;

	@Column(nullable = false, length = 255)
	private String title;

	@Column(length = 255)
	private String company;

	@Column(length = 50)
	private String platform;

	@Column(length = 1000)
	private String url;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private Level level;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private Area area;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private WorkMode workMode;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private ContractType contractType;

	@Column(length = 100)
	private String city;

	@Column(length = 100)
	private String state;

	private Integer salaryMin;

	private Integer salaryMax;

	private LocalDateTime publishedAt;

	@Column(nullable = false, columnDefinition = "INTEGER")
	private boolean active;

	@Column(nullable = false)
	private LocalDateTime syncedAt;

	@Builder
	public Job(Source source, String externalId, String slug, String title, String company, String platform, String url,
			String description, Level level, Area area, WorkMode workMode, ContractType contractType, String city,
			String state, Integer salaryMin, Integer salaryMax, LocalDateTime publishedAt, boolean active,
			LocalDateTime syncedAt) {
		this.source = source;
		this.externalId = externalId;
		this.slug = slug;
		this.title = title;
		this.company = company;
		this.platform = platform;
		this.url = url;
		this.description = description;
		this.level = level;
		this.area = area;
		this.workMode = workMode;
		this.contractType = contractType;
		this.city = city;
		this.state = state;
		this.salaryMin = salaryMin;
		this.salaryMax = salaryMax;
		this.publishedAt = publishedAt;
		this.active = active;
		this.syncedAt = syncedAt;
	}

}
