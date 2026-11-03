package br.com.searchjob.profile.repository;

import br.com.searchjob.profile.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

	List<Profile> findByUserIdOrderById(Long userId);

	boolean existsByUserId(Long userId);

	boolean existsByUserIdAndNameIgnoreCase(Long userId, String name);

	boolean existsByUserIdAndNameIgnoreCaseAndIdNot(Long userId, String name, Long id);

}
