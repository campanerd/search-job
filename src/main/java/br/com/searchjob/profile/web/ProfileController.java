package br.com.searchjob.profile.web;

import br.com.searchjob.profile.dto.ProfileRequest;
import br.com.searchjob.profile.dto.ProfileResponse;
import br.com.searchjob.profile.repository.ProfileCommand;
import br.com.searchjob.profile.repository.ProfileQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
@Tag(name = "Profiles", description = "Search profile registration")
public class ProfileController {

	private final ProfileCommand command;

	private final ProfileQuery query;

	@PostMapping
	@Operation(summary = "Create profile", description = "The profile belongs to the user sent in the body.")
	@ApiResponse(responseCode = "201", description = "Profile created")
	@ApiResponse(responseCode = "400",
			description = "Missing userId, blank name, invalid value or negative minimum salary")
	@ApiResponse(responseCode = "404", description = "User not found")
	@ApiResponse(responseCode = "409", description = "The user already has a profile with this name (case-insensitive)")
	public ResponseEntity<ProfileResponse> createProfile(@RequestBody @Valid ProfileRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(ProfileResponse.from(this.command.createProfile(request)));
	}

	@GetMapping
	@Operation(summary = "List profiles", description = "Lists only the profiles of the given user.")
	@ApiResponse(responseCode = "200", description = "Profiles ordered by id (empty if the user has none)")
	@ApiResponse(responseCode = "400", description = "Missing userId")
	@ApiResponse(responseCode = "404", description = "User not found")
	public List<ProfileResponse> listProfiles(@RequestParam Long userId) {
		return this.query.listProfiles(userId).stream().map(ProfileResponse::from).toList();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get profile by id")
	@ApiResponse(responseCode = "200", description = "Profile found")
	@ApiResponse(responseCode = "404", description = "Profile not found")
	public ProfileResponse getProfile(@PathVariable Long id) {
		return ProfileResponse.from(this.query.getProfile(id));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update profile",
			description = "Replaces the name and the filters; the userId must be the owner's.")
	@ApiResponse(responseCode = "200", description = "Profile updated")
	@ApiResponse(responseCode = "400",
			description = "Missing userId or different from the owner, blank name, "
					+ "invalid value or negative minimum salary")
	@ApiResponse(responseCode = "404", description = "Profile not found")
	@ApiResponse(responseCode = "409",
			description = "The user already has another profile with this name (case-insensitive)")
	public ProfileResponse updateProfile(@PathVariable Long id, @RequestBody @Valid ProfileRequest request) {
		return ProfileResponse.from(this.command.updateProfile(id, request));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Delete profile")
	@ApiResponse(responseCode = "204", description = "Profile deleted")
	@ApiResponse(responseCode = "404", description = "Profile not found")
	public void deleteProfile(@PathVariable Long id) {
		this.command.deleteProfile(id);
	}

}
