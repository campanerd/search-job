package br.com.searchjob.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "search-job API", version = "v1",
		description = "Registration of search profiles to filter IT job openings."))
public class OpenApiConfig {

}
