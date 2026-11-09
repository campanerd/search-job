package br.com.searchjob.job.repository;

import br.com.searchjob.job.model.JobTechnology;
import br.com.searchjob.job.model.JobTechnologyId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobTechnologyRepository extends JpaRepository<JobTechnology, JobTechnologyId> {

	List<JobTechnology> findByJobId(Long jobId);

	List<JobTechnology> findByTechnologyId(Long technologyId);

}
