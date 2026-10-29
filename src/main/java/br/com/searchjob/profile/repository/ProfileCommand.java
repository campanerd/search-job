package br.com.searchjob.profile.repository;

import br.com.searchjob.profile.model.Profile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ProfileCommand {

	private final ProfileRepository repository;

	private final ProfileQuery query;

	@Transactional
	public Profile createProfile(String name) {
		Profile profile = new Profile(name.trim());
		return this.repository.save(profile);
	}

	@Transactional
	public Profile updateProfile(Long id, String name) {
		Profile profile = this.query.getProfile(id);
		profile.rename(name.trim());
		return profile;
	}

	@Transactional
	public void deleteProfile(Long id) {
		this.repository.delete(this.query.getProfile(id));
	}

}
