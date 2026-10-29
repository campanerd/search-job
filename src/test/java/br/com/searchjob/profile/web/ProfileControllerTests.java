package br.com.searchjob.profile.web;

import br.com.searchjob.profile.repository.ProfileRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.datasource.url=jdbc:sqlite::memory:")
@AutoConfigureMockMvc
class ProfileControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ProfileRepository repository;

	@BeforeEach
	void cleanTable() {
		this.repository.deleteAll();
	}

	@Test
	void createsProfileAndTrimsName() throws Exception {
		this.mockMvc
			.perform(post("/profiles").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"  Davi Java  \"}"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").isNumber())
			.andExpect(jsonPath("$.name").value("Davi Java"))
			.andExpect(jsonPath("$.createdAt").isString())
			.andExpect(jsonPath("$.updatedAt").isString());
	}

	@Test
	void rejectsInvalidNameOnCreate() throws Exception {
		String tooLong = "a".repeat(101);
		for (String body : new String[] { "{\"name\":\"   \"}", "{}", "{\"name\":\"" + tooLong + "\"}" }) {
			this.mockMvc.perform(post("/profiles").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isBadRequest());
		}
	}

	@Test
	void getsProfileById() throws Exception {
		long id = createProfile("Ana");

		this.mockMvc.perform(get("/profiles/" + id))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(id))
			.andExpect(jsonPath("$.name").value("Ana"));
	}

	@Test
	void listsProfilesOrderedById() throws Exception {
		createProfile("Ana");
		createProfile("Bia");

		this.mockMvc.perform(get("/profiles"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2))
			.andExpect(jsonPath("$[0].name").value("Ana"))
			.andExpect(jsonPath("$[1].name").value("Bia"));
	}

	@Test
	void listsNothingWhenThereAreNoProfiles() throws Exception {
		this.mockMvc.perform(get("/profiles")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void updatesName() throws Exception {
		long id = createProfile("Ana");

		this.mockMvc
			.perform(put("/profiles/" + id).contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"  Ana Lima  \"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(id))
			.andExpect(jsonPath("$.name").value("Ana Lima"));

		this.mockMvc.perform(get("/profiles/" + id)).andExpect(jsonPath("$.name").value("Ana Lima"));
	}

	@Test
	void rejectsBlankNameOnUpdate() throws Exception {
		long id = createProfile("Ana");

		this.mockMvc
			.perform(put("/profiles/" + id).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"   \"}"))
			.andExpect(status().isBadRequest());
	}

	@Test
	void deletesProfile() throws Exception {
		long id = createProfile("Ana");

		this.mockMvc.perform(delete("/profiles/" + id)).andExpect(status().isNoContent());
		this.mockMvc.perform(get("/profiles/" + id)).andExpect(status().isNotFound());
	}

	@Test
	void returnsNotFoundForUnknownId() throws Exception {
		this.mockMvc.perform(get("/profiles/999")).andExpect(status().isNotFound());
		this.mockMvc.perform(put("/profiles/999").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Ana\"}"))
			.andExpect(status().isNotFound());
		this.mockMvc.perform(delete("/profiles/999")).andExpect(status().isNotFound());
	}

	private long createProfile(String name) throws Exception {
		String body = this.mockMvc
			.perform(post("/profiles").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"" + name + "\"}"))
			.andExpect(status().isCreated())
			.andReturn()
			.getResponse()
			.getContentAsString();
		return ((Number) JsonPath.read(body, "$.id")).longValue();
	}

}
