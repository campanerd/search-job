package br.com.searchjob.user.repository;

import br.com.searchjob.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserQuery {

	private final UserRepository repository;

	@Transactional(readOnly = true)
	public User getUser(Long id) {
		return this.repository.findById(id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User " + id + " not found"));
	}

	@Transactional(readOnly = true)
	public List<User> listUsers() {
		return this.repository.findAll(Sort.by("id"));
	}

}
