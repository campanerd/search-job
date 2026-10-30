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
	@ApiResponse(responseCode = "400", description = "Nome em branco ou com mais de 100 caracteres")
	public ResponseEntity<ProfileResponse> createProfile(@RequestBody @Valid ProfileRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(ProfileResponse.from(this.command.createProfile(request.name())));
	}

	@GetMapping
	@Operation(summary = "Lista os perfis, ordenados por id")
	@ApiResponse(responseCode = "200", description = "Lista de perfis (vazia se não houver nenhum)")
	public List<ProfileResponse> listProfiles() {
		return this.query.listProfiles().stream().map(ProfileResponse::from).toList();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Busca um perfil pelo id")
	@ApiResponse(responseCode = "200", description = "Perfil encontrado")
	@ApiResponse(responseCode = "404", description = "Perfil não existe")
	public ProfileResponse getProfile(@PathVariable Long id) {
		return ProfileResponse.from(this.query.getProfile(id));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Troca o nome de um perfil")
	@ApiResponse(responseCode = "200", description = "Perfil atualizado")
	@ApiResponse(responseCode = "400", description = "Nome em branco ou com mais de 100 caracteres")
	@ApiResponse(responseCode = "404", description = "Perfil não existe")
	public ProfileResponse updateProfile(@PathVariable Long id, @RequestBody @Valid ProfileRequest request) {
		return ProfileResponse.from(this.command.updateProfile(id, request.name()));
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
