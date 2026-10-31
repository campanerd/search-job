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
@Tag(name = "Usuários", description = "Cadastro de usuários")
public class UserController {

	private final UserCommand command;

	private final UserQuery query;

	@PostMapping
	@Operation(summary = "Cria um usuário")
	@ApiResponse(responseCode = "201", description = "Usuário criado")
	@ApiResponse(responseCode = "400", description = "Nome em branco ou com mais de 100 caracteres")
	@ApiResponse(responseCode = "409", description = "Já existe um usuário com esse nome (sem diferenciar maiúsculas)")
	public ResponseEntity<UserResponse> createUser(@RequestBody @Valid UserRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(UserResponse.from(this.command.createUser(request.name())));
	}

	@GetMapping
	@Operation(summary = "Lista os usuários, ordenados por id")
	@ApiResponse(responseCode = "200", description = "Lista de usuários (vazia se não houver nenhum)")
	public List<UserResponse> listUsers() {
		return this.query.listUsers().stream().map(UserResponse::from).toList();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Busca um usuário pelo id")
	@ApiResponse(responseCode = "200", description = "Usuário encontrado")
	@ApiResponse(responseCode = "404", description = "Usuário não existe")
	public UserResponse getUser(@PathVariable Long id) {
		return UserResponse.from(this.query.getUser(id));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Troca o nome de um usuário")
	@ApiResponse(responseCode = "200", description = "Usuário atualizado")
	@ApiResponse(responseCode = "400", description = "Nome em branco ou com mais de 100 caracteres")
	@ApiResponse(responseCode = "404", description = "Usuário não existe")
	@ApiResponse(responseCode = "409",
			description = "Já existe outro usuário com esse nome (sem diferenciar maiúsculas)")
	public UserResponse updateUser(@PathVariable Long id, @RequestBody @Valid UserRequest request) {
		return UserResponse.from(this.command.updateUser(id, request.name()));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Apaga um usuário")
	@ApiResponse(responseCode = "204", description = "Usuário apagado")
	@ApiResponse(responseCode = "404", description = "Usuário não existe")
	public void deleteUser(@PathVariable Long id) {
		this.command.deleteUser(id);
	}

}
