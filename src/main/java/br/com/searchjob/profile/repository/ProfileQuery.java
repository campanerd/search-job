package br.com.searchjob.profile.repository;

import br.com.searchjob.profile.model.Profile;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProfileQuery {

	private final ProfileRepository repository;

	@Transactional(readOnly = true)
	public Profile getProfile(Long id) {
		return this.repository.findById(id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile " + id + " not found"));
	}

	@Transactional(readOnly = true)
	public List<Profile> listProfiles() {
		return this.repository.findAll(Sort.by("id"));
	}

}
