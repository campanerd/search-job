package br.com.searchjob.profile.repository;

import br.com.searchjob.profile.model.Profile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ProfileCommand {

	private final ProfileRepository repository;

	@Transactional
	public Profile create(String name) {
		Profile profile = new Profile(name.trim());
		return this.repository.save(profile);
	}
}
