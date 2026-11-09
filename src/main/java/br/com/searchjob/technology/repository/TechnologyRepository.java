package br.com.searchjob.technology.repository;

import br.com.searchjob.technology.model.Technology;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TechnologyRepository extends JpaRepository<Technology, Long> {

	Optional<Technology> findByNameIgnoreCase(String name);

}
