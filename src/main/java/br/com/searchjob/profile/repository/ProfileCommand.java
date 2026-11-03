package br.com.searchjob.profile.repository;

import br.com.searchjob.profile.dto.ProfileRequest;
import br.com.searchjob.profile.model.Profile;
import br.com.searchjob.user.repository.UserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class ProfileCommand {

	private final ProfileRepository repository;

	private final ProfileQuery query;

	private final UserQuery userQuery;

	@Transactional
	public Profile createProfile(ProfileRequest request) {
		this.userQuery.getUser(request.userId());
		String name = request.name().trim();
		if (this.repository.existsByUserIdAndNameIgnoreCase(request.userId(), name)) {
			throw nameAlreadyExists(name);
		}
		return this.repository.save(new Profile(request.userId(), name, request.level(), request.area(),
				request.workMode(), request.contractType(), trimToNull(request.city()), trimToNull(request.state()),
				request.minSalary()));
	}

	@Transactional
	public Profile updateProfile(Long id, ProfileRequest request) {
		Profile profile = this.query.getProfile(id);
		if (!profile.getUserId().equals(request.userId())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A profile cannot change its user");
		}
		String name = request.name().trim();
		if (this.repository.existsByUserIdAndNameIgnoreCaseAndIdNot(request.userId(), name, id)) {
			throw nameAlreadyExists(name);
		}
		profile.update(name, request.level(), request.area(), request.workMode(), request.contractType(),
				trimToNull(request.city()), trimToNull(request.state()), request.minSalary());
		return profile;
	}

	@Transactional
	public void deleteProfile(Long id) {
		this.repository.delete(this.query.getProfile(id));
	}

	private static ResponseStatusException nameAlreadyExists(String name) {
		return new ResponseStatusException(HttpStatus.CONFLICT, "This user already has a profile named " + name);
	}

	private static String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

}
