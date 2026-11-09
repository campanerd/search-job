package br.com.searchjob.technology.repository;

import br.com.searchjob.technology.model.Technology;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "spring.datasource.url=jdbc:sqlite::memory:")
class TechnologyRepositoryTests {

	@Autowired
	private TechnologyRepository repository;

	@BeforeEach
	void cleanTable() {
		this.repository.deleteAll();
	}

	@Test
	void savesATechnologyAndFindsItByName() {
		Technology saved = this.repository.saveAndFlush(new Technology("Java"));

		Technology found = this.repository.findByNameIgnoreCase("Java").orElseThrow();

		assertThat(saved.getId()).isNotNull();
		assertThat(found.getId()).isEqualTo(saved.getId());
		assertThat(found.getName()).isEqualTo("Java");
	}

	@Test
	void findsByNameIgnoringCase() {
		this.repository.saveAndFlush(new Technology("Spring Boot"));

		assertThat(this.repository.findByNameIgnoreCase("spring boot")).isPresent();
		assertThat(this.repository.findByNameIgnoreCase("SPRING BOOT")).isPresent();
	}

	@Test
	void findsNothingForAnUnknownName() {
		this.repository.saveAndFlush(new Technology("Java"));

		assertThat(this.repository.findByNameIgnoreCase("Kotlin")).isEmpty();
	}

	@Test
	void rejectsTheSameNameIgnoringCase() {
		this.repository.saveAndFlush(new Technology("Java"));

		// O dialeto do SQLite não traduz a violação para DataIntegrityViolationException
		assertThatThrownBy(() -> this.repository.saveAndFlush(new Technology("java")))
			.isInstanceOf(DataAccessException.class)
			.hasMessageContaining("UNIQUE constraint failed");
	}

}
