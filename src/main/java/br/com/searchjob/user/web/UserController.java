package br.com.searchjob.user.web;

import br.com.searchjob.user.dto.UserRequest;
import br.com.searchjob.user.dto.UserResponse;
import br.com.searchjob.user.repository.UserCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Cadastro de usuários")
public class UserController {

	private final UserCommand command;

	@PostMapping
	@Operation(summary = "Cria um usuário")
	@ApiResponse(responseCode = "201", description = "Usuário criado")
	@ApiResponse(responseCode = "400", description = "Nome em branco ou com mais de 100 caracteres")
	@ApiResponse(responseCode = "409", description = "Já existe um usuário com esse nome (sem diferenciar maiúsculas)")
	public ResponseEntity<UserResponse> createUser(@RequestBody @Valid UserRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(UserResponse.from(this.command.createUser(request.name())));
	}

}
