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
	void namesTheOperationsAndTagsThatBecomeTheBrunoRequestsAndFolders() throws Exception {
		this.mockMvc.perform(get("/v3/api-docs"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.paths['/health'].get.tags[0]").value("Health"))
			.andExpect(jsonPath("$.paths['/health'].get.summary").value("Health check"))
			.andExpect(jsonPath("$.paths['/users'].post.tags[0]").value("Users"))
			.andExpect(jsonPath("$.paths['/users'].post.summary").value("Create user"))
			.andExpect(jsonPath("$.paths['/users'].get.summary").value("List users"))
			.andExpect(jsonPath("$.paths['/users/{id}'].get.summary").value("Get user by id"))
			.andExpect(jsonPath("$.paths['/users/{id}'].put.summary").value("Update user"))
			.andExpect(jsonPath("$.paths['/users/{id}'].delete.summary").value("Delete user"))
			.andExpect(jsonPath("$.paths['/users/{id}'].delete.responses['409']").exists())
			.andExpect(jsonPath("$.paths['/profiles'].post.tags[0]").value("Profiles"))
			.andExpect(jsonPath("$.paths['/profiles'].post.summary").value("Create profile"))
			.andExpect(jsonPath("$.paths['/profiles'].get.summary").value("List profiles"))
			.andExpect(jsonPath("$.paths['/profiles/{id}'].get.summary").value("Get profile by id"))
			.andExpect(jsonPath("$.paths['/profiles/{id}'].put.summary").value("Update profile"))
			.andExpect(jsonPath("$.paths['/profiles/{id}'].delete.summary").value("Delete profile"));
	}

	@Test
	void servesSwaggerUi() throws Exception {
		this.mockMvc.perform(get("/swagger-ui.html")).andExpect(status().is3xxRedirection());
		this.mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
	}

}
