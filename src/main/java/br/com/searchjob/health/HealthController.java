package br.com.searchjob.health;

import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Health", description = "Application status")
public class HealthController {

	@GetMapping("/health")
	@Operation(summary = "Health check")
	@ApiResponse(responseCode = "200", description = "Application is up")
	public Map<String, String> health() {
		return Map.of("status", "UP");
	}

}
