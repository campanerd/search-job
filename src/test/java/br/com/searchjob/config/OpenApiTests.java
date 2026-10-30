package br.com.searchjob.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.datasource.url=jdbc:sqlite::memory:")
@AutoConfigureMockMvc
class OpenApiTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void servesOpenApiDescriptionWithDocumentedProfileEndpoints() throws Exception {
		this.mockMvc.perform(get("/v3/api-docs"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.info.title").value("search-job API"))
			.andExpect(jsonPath("$.paths['/profiles'].post.responses['201']").exists())
			.andExpect(jsonPath("$.paths['/profiles/{id}'].get.responses['404']").exists())
			.andExpect(jsonPath("$.paths['/profiles/{id}'].delete.responses['204']").exists());
	}

	@Test
	void servesSwaggerUi() throws Exception {
		this.mockMvc.perform(get("/swagger-ui.html")).andExpect(status().is3xxRedirection());
		this.mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
	}

}
