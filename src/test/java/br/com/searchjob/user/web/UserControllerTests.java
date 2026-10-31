package br.com.searchjob.user.web;

import br.com.searchjob.user.repository.UserRepository;
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
class UserControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository repository;

	@BeforeEach
	void cleanTable() {
		this.repository.deleteAll();
	}

	@Test
	void createsUserAndTrimsName() throws Exception {
		this.mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"  Davi  \"}"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").isNumber())
			.andExpect(jsonPath("$.name").value("Davi"))
			.andExpect(jsonPath("$.createdAt").isString())
			.andExpect(jsonPath("$.updatedAt").isString());
	}

	@Test
	void rejectsInvalidNameOnCreate() throws Exception {
		String tooLong = "a".repeat(101);
		for (String body : new String[] { "{\"name\":\"   \"}", "{}", "{\"name\":\"" + tooLong + "\"}" }) {
			this.mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isBadRequest());
		}
	}

	@Test
	void rejectsDuplicateNameIgnoringCaseAndSpaces() throws Exception {
		createUser("Davi");

		for (String name : new String[] { "Davi", "davi", "  DAVI  " }) {
			this.mockMvc
				.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"" + name + "\"}"))
				.andExpect(status().isConflict());
		}
	}

	@Test
	void getsUserById() throws Exception {
		long id = createUser("Ana");

		this.mockMvc.perform(get("/users/" + id))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(id))
			.andExpect(jsonPath("$.name").value("Ana"));
	}

	@Test
	void listsUsersOrderedById() throws Exception {
		createUser("Ana");
		createUser("Bia");

		this.mockMvc.perform(get("/users"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2))
			.andExpect(jsonPath("$[0].name").value("Ana"))
			.andExpect(jsonPath("$[1].name").value("Bia"));
	}

	@Test
	void listsNothingWhenThereAreNoUsers() throws Exception {
		this.mockMvc.perform(get("/users")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void updatesName() throws Exception {
		long id = createUser("Ana");

		this.mockMvc
			.perform(put("/users/" + id).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"  Ana Lima  \"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(id))
			.andExpect(jsonPath("$.name").value("Ana Lima"));

		this.mockMvc.perform(get("/users/" + id)).andExpect(jsonPath("$.name").value("Ana Lima"));
	}

	@Test
	void allowsChangingOnlyTheCaseOfOwnName() throws Exception {
		long id = createUser("ana");

		this.mockMvc.perform(put("/users/" + id).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Ana\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("Ana"));
	}

	@Test
	void rejectsNameOfAnotherUserOnUpdate() throws Exception {
		createUser("Ana");
		long id = createUser("Bia");

		this.mockMvc.perform(put("/users/" + id).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"ANA\"}"))
			.andExpect(status().isConflict());
	}

	@Test
	void rejectsBlankNameOnUpdate() throws Exception {
		long id = createUser("Ana");

		this.mockMvc.perform(put("/users/" + id).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"   \"}"))
			.andExpect(status().isBadRequest());
	}

	@Test
	void deletesUser() throws Exception {
		long id = createUser("Ana");

		this.mockMvc.perform(delete("/users/" + id)).andExpect(status().isNoContent());
		this.mockMvc.perform(get("/users/" + id)).andExpect(status().isNotFound());
	}

	@Test
	void returnsNotFoundForUnknownId() throws Exception {
		this.mockMvc.perform(get("/users/999")).andExpect(status().isNotFound());
		this.mockMvc.perform(put("/users/999").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Ana\"}"))
			.andExpect(status().isNotFound());
		this.mockMvc.perform(delete("/users/999")).andExpect(status().isNotFound());
	}

	private long createUser(String name) throws Exception {
		String body = this.mockMvc
			.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"" + name + "\"}"))
			.andExpect(status().isCreated())
			.andReturn()
			.getResponse()
			.getContentAsString();
		return ((Number) JsonPath.read(body, "$.id")).longValue();
	}

}
