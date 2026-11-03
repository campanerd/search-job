package br.com.searchjob.user.repository;

import br.com.searchjob.profile.repository.ProfileRepository;
import br.com.searchjob.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class UserCommand {

	private final UserRepository repository;

	private final UserQuery query;

	private final ProfileRepository profileRepository;

	@Transactional
	public User createUser(String name) {
		String trimmed = name.trim();
		if (this.repository.existsByNameIgnoreCase(trimmed)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "User " + trimmed + " already exists");
		}
		return this.repository.save(new User(trimmed));
	}

	@Transactional
	public User updateUser(Long id, String name) {
		User user = this.query.getUser(id);
		String trimmed = name.trim();
		if (this.repository.existsByNameIgnoreCaseAndIdNot(trimmed, id)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "User " + trimmed + " already exists");
		}
		user.rename(trimmed);
		return user;
	}

	@Transactional
	public void deleteUser(Long id) {
		User user = this.query.getUser(id);
		if (this.profileRepository.existsByUserId(id)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "User " + id + " still has profiles");
		}
		this.repository.delete(user);
	}

}
