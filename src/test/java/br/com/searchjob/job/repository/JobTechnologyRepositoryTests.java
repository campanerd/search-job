package br.com.searchjob.job.repository;

import br.com.searchjob.job.enums.Source;
import br.com.searchjob.job.model.Job;
import br.com.searchjob.job.model.JobTechnology;
import br.com.searchjob.job.model.JobTechnologyId;
import br.com.searchjob.technology.model.Technology;
import br.com.searchjob.technology.repository.TechnologyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "spring.datasource.url=jdbc:sqlite::memory:")
class JobTechnologyRepositoryTests {

	private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 9, 10, 30);

	@Autowired
	private JobTechnologyRepository repository;

	@Autowired
	private JobRepository jobRepository;

	@Autowired
	private TechnologyRepository technologyRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	void cleanTables() {
		this.repository.deleteAll();
		this.technologyRepository.deleteAll();
		this.jobRepository.deleteAll();
	}

	@Test
	void linksATechnologyToAJobAndFindsItByJob() {
		Job job = saveJob("job00001");
		Technology java = this.technologyRepository.saveAndFlush(new Technology("Java"));
		Technology spring = this.technologyRepository.saveAndFlush(new Technology("Spring"));
		this.repository.saveAndFlush(new JobTechnology(job.getId(), java.getId()));
		this.repository.saveAndFlush(new JobTechnology(job.getId(), spring.getId()));

		assertThat(this.repository.findByJobId(job.getId())).extracting(JobTechnology::getTechnologyId)
			.containsExactlyInAnyOrder(java.getId(), spring.getId());
	}

	@Test
	void findsTheJobsOfATechnology() {
		Job first = saveJob("job00001");
		Job second = saveJob("job00002");
		Job third = saveJob("job00003");
		Technology java = this.technologyRepository.saveAndFlush(new Technology("Java"));
		Technology go = this.technologyRepository.saveAndFlush(new Technology("Go"));
		this.repository.saveAndFlush(new JobTechnology(first.getId(), java.getId()));
		this.repository.saveAndFlush(new JobTechnology(second.getId(), java.getId()));
		this.repository.saveAndFlush(new JobTechnology(third.getId(), go.getId()));

		assertThat(this.repository.findByTechnologyId(java.getId())).extracting(JobTechnology::getJobId)
			.containsExactlyInAnyOrder(first.getId(), second.getId());
	}

	@Test
	void findsNothingForAJobWithoutTechnologies() {
		Job job = saveJob("job00001");

		assertThat(this.repository.findByJobId(job.getId())).isEmpty();
	}

	@Test
	void savingTheSameLinkTwiceKeepsASingleRow() {
		Job job = saveJob("job00001");
		Technology java = this.technologyRepository.saveAndFlush(new Technology("Java"));

		this.repository.saveAndFlush(new JobTechnology(job.getId(), java.getId()));
		this.repository.saveAndFlush(new JobTechnology(job.getId(), java.getId()));

		assertThat(this.repository.count()).isEqualTo(1);
		assertThat(this.repository.findById(new JobTechnologyId(job.getId(), java.getId()))).isPresent();
	}

	@Test
	void theDatabaseRejectsTheSameLinkTwice() {
		Job job = saveJob("job00001");
		Technology java = this.technologyRepository.saveAndFlush(new Technology("Java"));
		this.repository.saveAndFlush(new JobTechnology(job.getId(), java.getId()));

		// Insert direto, sem passar pelo JPA (o save() de uma chave já existente só
		// atualiza)
		assertThatThrownBy(() -> this.jdbcTemplate
			.update("INSERT INTO job_technology (job_id, technology_id) VALUES (?, ?)", job.getId(), java.getId()))
			.isInstanceOf(DataAccessException.class)
			.hasMessageContaining("UNIQUE constraint failed");
	}

	private Job saveJob(String externalId) {
		return this.jobRepository.saveAndFlush(Job.builder()
			.source(Source.MEU_PADRINHO)
			.externalId(externalId)
			.title("A job")
			.active(true)
			.syncedAt(NOW)
			.build());
	}

}
