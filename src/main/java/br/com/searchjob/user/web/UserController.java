package br.com.searchjob.user.web;

import br.com.searchjob.user.dto.UserRequest;
import br.com.searchjob.user.dto.UserResponse;
import br.com.searchjob.user.repository.UserCommand;
import br.com.searchjob.user.repository.UserQuery;
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
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User registration")
public class UserController {

	private final UserCommand command;

	private final UserQuery query;

	@PostMapping
	@Operation(summary = "Create user")
	@ApiResponse(responseCode = "201", description = "User created")
	@ApiResponse(responseCode = "400", description = "Blank name or longer than 100 characters")
	@ApiResponse(responseCode = "409", description = "A user with this name already exists (case-insensitive)")
	public ResponseEntity<UserResponse> createUser(@RequestBody @Valid UserRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(UserResponse.from(this.command.createUser(request.name())));
	}

	@GetMapping
	@Operation(summary = "List users")
	@ApiResponse(responseCode = "200", description = "Users ordered by id (empty if there are none)")
	public List<UserResponse> listUsers() {
		return this.query.listUsers().stream().map(UserResponse::from).toList();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get user by id")
	@ApiResponse(responseCode = "200", description = "User found")
	@ApiResponse(responseCode = "404", description = "User not found")
	public UserResponse getUser(@PathVariable Long id) {
		return UserResponse.from(this.query.getUser(id));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update user", description = "Replaces the name of the user.")
	@ApiResponse(responseCode = "200", description = "User updated")
	@ApiResponse(responseCode = "400", description = "Blank name or longer than 100 characters")
	@ApiResponse(responseCode = "404", description = "User not found")
	@ApiResponse(responseCode = "409", description = "Another user already has this name (case-insensitive)")
	public UserResponse updateUser(@PathVariable Long id, @RequestBody @Valid UserRequest request) {
		return UserResponse.from(this.command.updateUser(id, request.name()));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Delete user")
	@ApiResponse(responseCode = "204", description = "User deleted")
	@ApiResponse(responseCode = "404", description = "User not found")
	@ApiResponse(responseCode = "409", description = "The user still has profiles")
	public void deleteUser(@PathVariable Long id) {
		this.command.deleteUser(id);
	}

}
