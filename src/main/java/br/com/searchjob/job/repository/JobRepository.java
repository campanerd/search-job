package br.com.searchjob.job.repository;

import br.com.searchjob.job.enums.Source;
import br.com.searchjob.job.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long> {

	Optional<Job> findBySourceAndExternalId(Source source, String externalId);

}
