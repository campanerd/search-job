package br.com.searchjob.profile.web;

import br.com.searchjob.profile.dto.ProfileRequest;
import br.com.searchjob.profile.dto.ProfileResponse;
import br.com.searchjob.profile.repository.ProfileCommand;
import br.com.searchjob.profile.repository.ProfileQuery;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
public class ProfileController {

	private final ProfileCommand command;

	private final ProfileQuery query;

	@PostMapping
	public ResponseEntity<ProfileResponse> createProfile(@RequestBody @Valid ProfileRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(ProfileResponse.from(this.command.createProfile(request.name())));
	}

	@GetMapping
	public List<ProfileResponse> listProfiles() {
		return this.query.listProfiles().stream().map(ProfileResponse::from).toList();
	}

	@GetMapping("/{id}")
	public ProfileResponse getProfile(@PathVariable Long id) {
		return ProfileResponse.from(this.query.getProfile(id));
	}

	@PutMapping("/{id}")
	public ProfileResponse updateProfile(@PathVariable Long id, @RequestBody @Valid ProfileRequest request) {
		return ProfileResponse.from(this.command.updateProfile(id, request.name()));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteProfile(@PathVariable Long id) {
		this.command.deleteProfile(id);
	}

}
