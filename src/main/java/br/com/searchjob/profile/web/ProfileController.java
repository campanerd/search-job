package br.com.searchjob.profile.web;

import br.com.searchjob.profile.dto.ProfileRequest;
import br.com.searchjob.profile.dto.ProfileResponse;
import br.com.searchjob.profile.model.Profile;
import br.com.searchjob.profile.repository.ProfileCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
public class ProfileController {

	private final ProfileCommand command;

	@PostMapping
	public ResponseEntity<ProfileResponse> create(@RequestBody @Valid ProfileRequest request) {
		Profile created = this.command.create(request.name());

		ProfileResponse body = new ProfileResponse(created.getId(), created.getName(),
				created.getCreatedAt(), created.getUpdatedAt());

		return ResponseEntity.status(HttpStatus.CREATED).body(body);
	}

}
