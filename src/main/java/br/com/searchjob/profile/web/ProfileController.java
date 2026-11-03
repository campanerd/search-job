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
@Tag(name = "Perfis", description = "Cadastro de perfis de busca")
public class ProfileController {

	private final ProfileCommand command;

	private final ProfileQuery query;

	@PostMapping
	@Operation(summary = "Cria um perfil")
	@ApiResponse(responseCode = "201", description = "Perfil criado")
	@ApiResponse(responseCode = "400",
			description = "userId ausente, nome em branco, valor fora do padrão ou salário negativo")
	@ApiResponse(responseCode = "404", description = "Usuário não existe")
	@ApiResponse(responseCode = "409",
			description = "O usuário já tem um perfil com esse nome (sem diferenciar maiúsculas)")
	public ResponseEntity<ProfileResponse> createProfile(@RequestBody @Valid ProfileRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(ProfileResponse.from(this.command.createProfile(request)));
	}

	@GetMapping
	@Operation(summary = "Lista os perfis de um usuário, ordenados por id")
	@ApiResponse(responseCode = "200", description = "Lista de perfis (vazia se o usuário não tiver nenhum)")
	@ApiResponse(responseCode = "400", description = "userId ausente")
	@ApiResponse(responseCode = "404", description = "Usuário não existe")
	public List<ProfileResponse> listProfiles(@RequestParam Long userId) {
		return this.query.listProfiles(userId).stream().map(ProfileResponse::from).toList();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Busca um perfil pelo id")
	@ApiResponse(responseCode = "200", description = "Perfil encontrado")
	@ApiResponse(responseCode = "404", description = "Perfil não existe")
	public ProfileResponse getProfile(@PathVariable Long id) {
		return ProfileResponse.from(this.query.getProfile(id));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Troca o nome e os filtros de um perfil (o userId tem de ser o do dono)")
	@ApiResponse(responseCode = "200", description = "Perfil atualizado")
	@ApiResponse(responseCode = "400",
			description = "userId ausente ou diferente do dono, nome em branco, valor fora do padrão ou salário negativo")
	@ApiResponse(responseCode = "404", description = "Perfil não existe")
	@ApiResponse(responseCode = "409",
			description = "O usuário já tem outro perfil com esse nome (sem diferenciar maiúsculas)")
	public ProfileResponse updateProfile(@PathVariable Long id, @RequestBody @Valid ProfileRequest request) {
		return ProfileResponse.from(this.command.updateProfile(id, request));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Apaga um perfil")
	@ApiResponse(responseCode = "204", description = "Perfil apagado")
	@ApiResponse(responseCode = "404", description = "Perfil não existe")
	public void deleteProfile(@PathVariable Long id) {
		this.command.deleteProfile(id);
	}

}
