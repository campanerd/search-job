package br.com.searchjob.job.repository;

import br.com.searchjob.job.dto.JobResponse;
import br.com.searchjob.job.enums.Source;
import br.com.searchjob.job.model.Job;
import br.com.searchjob.profile.enums.Area;
import br.com.searchjob.profile.enums.ContractType;
import br.com.searchjob.profile.enums.Level;
import br.com.searchjob.profile.enums.WorkMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "spring.datasource.url=jdbc:sqlite::memory:")
class JobRepositoryTests {

	private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 6, 10, 30);

	@Autowired
	private JobRepository repository;

	@BeforeEach
	void cleanTable() {
		this.repository.deleteAll();
	}

	@Test
	void savesAJobWithAllFieldsAndFindsItBySourceAndExternalId() {
		this.repository.saveAndFlush(Job.builder()
			.source(Source.MEU_PADRINHO)
			.externalId("CsnqRBQ6")
			.slug("desenvolvedor-a-mobile-flutter-pleno-remoto")
			.title("Desenvolvedor(a) Mobile Flutter")
			.company("Acme")
			.platform("LinkedIn")
			.url("https://example.com/apply")
			.description("A long description")
			.level(Level.MID)
			.area(Area.MOBILE)
			.workMode(WorkMode.REMOTE)
			.contractType(ContractType.CLT)
			.city("Sao Paulo")
			.state("SP")
			.salaryMin(8000)
			.salaryMax(12000)
			.publishedAt(NOW.minusDays(2))
			.active(true)
			.syncedAt(NOW)
			.build());

		Job found = this.repository.findBySourceAndExternalId(Source.MEU_PADRINHO, "CsnqRBQ6").orElseThrow();

		assertThat(found.getId()).isNotNull();
		assertThat(found.getSource()).isEqualTo(Source.MEU_PADRINHO);
		assertThat(found.getSlug()).isEqualTo("desenvolvedor-a-mobile-flutter-pleno-remoto");
		assertThat(found.getTitle()).isEqualTo("Desenvolvedor(a) Mobile Flutter");
		assertThat(found.getCompany()).isEqualTo("Acme");
		assertThat(found.getPlatform()).isEqualTo("LinkedIn");
		assertThat(found.getUrl()).isEqualTo("https://example.com/apply");
		assertThat(found.getDescription()).isEqualTo("A long description");
		assertThat(found.getLevel()).isEqualTo(Level.MID);
		assertThat(found.getArea()).isEqualTo(Area.MOBILE);
		assertThat(found.getWorkMode()).isEqualTo(WorkMode.REMOTE);
		assertThat(found.getContractType()).isEqualTo(ContractType.CLT);
		assertThat(found.getCity()).isEqualTo("Sao Paulo");
		assertThat(found.getState()).isEqualTo("SP");
		assertThat(found.getSalaryMin()).isEqualTo(8000);
		assertThat(found.getSalaryMax()).isEqualTo(12000);
		assertThat(found.getPublishedAt()).isEqualTo(NOW.minusDays(2));
		assertThat(found.isActive()).isTrue();
		assertThat(found.getSyncedAt()).isEqualTo(NOW);
	}

	@Test
	void savesAJobWithOnlyTheRequiredFields() {
		this.repository.saveAndFlush(minimalJob("abc12345"));

		Job found = this.repository.findBySourceAndExternalId(Source.MEU_PADRINHO, "abc12345").orElseThrow();

		assertThat(found.getTitle()).isEqualTo("A job");
		assertThat(found.getSlug()).isNull();
		assertThat(found.getCompany()).isNull();
		assertThat(found.getUrl()).isNull();
		assertThat(found.getLevel()).isNull();
		assertThat(found.getSalaryMin()).isNull();
		assertThat(found.getPublishedAt()).isNull();
	}

	@Test
	void savesAClosedJob() {
		this.repository.saveAndFlush(Job.builder()
			.source(Source.MEU_PADRINHO)
			.externalId("closed001")
			.title("A closed job")
			.active(false)
			.syncedAt(NOW)
			.build());

		assertThat(this.repository.findBySourceAndExternalId(Source.MEU_PADRINHO, "closed001").orElseThrow().isActive())
			.isFalse();
	}

	@Test
	void findsNothingForAnUnknownExternalId() {
		this.repository.saveAndFlush(minimalJob("abc12345"));

		assertThat(this.repository.findBySourceAndExternalId(Source.MEU_PADRINHO, "other")).isEmpty();
	}

	@Test
	void rejectsTheSameExternalIdForTheSameSource() {
		this.repository.saveAndFlush(minimalJob("abc12345"));

		// O dialeto do SQLite não traduz a violação para DataIntegrityViolationException
		assertThatThrownBy(() -> this.repository.saveAndFlush(minimalJob("abc12345")))
			.isInstanceOf(DataAccessException.class)
			.hasMessageContaining("UNIQUE constraint failed");
	}

	@Test
	void responseCopiesTheFieldsOfTheJob() {
		Job job = this.repository.saveAndFlush(Job.builder()
			.source(Source.MEU_PADRINHO)
			.externalId("abc12345")
			.slug("a-job")
			.title("A job")
			.level(Level.JUNIOR)
			.area(Area.BACKEND)
			.salaryMin(5000)
			.active(true)
			.syncedAt(NOW)
			.build());

		JobResponse response = JobResponse.from(job);

		assertThat(response.id()).isEqualTo(job.getId());
		assertThat(response.source()).isEqualTo(Source.MEU_PADRINHO);
		assertThat(response.externalId()).isEqualTo("abc12345");
		assertThat(response.slug()).isEqualTo("a-job");
		assertThat(response.title()).isEqualTo("A job");
		assertThat(response.level()).isEqualTo(Level.JUNIOR);
		assertThat(response.area()).isEqualTo(Area.BACKEND);
		assertThat(response.salaryMin()).isEqualTo(5000);
		assertThat(response.salaryMax()).isNull();
		assertThat(response.active()).isTrue();
		assertThat(response.syncedAt()).isEqualTo(NOW);
	}

	private Job minimalJob(String externalId) {
		return Job.builder()
			.source(Source.MEU_PADRINHO)
			.externalId(externalId)
			.title("A job")
			.active(true)
			.syncedAt(NOW)
			.build();
	}

}
