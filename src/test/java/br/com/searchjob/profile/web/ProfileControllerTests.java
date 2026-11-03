package br.com.searchjob.profile.web;

import br.com.searchjob.profile.repository.ProfileRepository;
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
class ProfileControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ProfileRepository repository;

	@Autowired
	private UserRepository userRepository;

	private long userId;

	@BeforeEach
	void cleanTablesAndCreateUser() throws Exception {
		this.repository.deleteAll();
		this.userRepository.deleteAll();
		this.userId = createUser("Davi");
	}

	@Test
	void createsProfileWithFiltersAndTrimsText() throws Exception {
		String body = "{\"userId\":" + this.userId + ",\"name\":\"  Davi Java  \",\"level\":\"JUNIOR\","
				+ "\"area\":\"BACKEND\",\"workMode\":\"REMOTE\",\"contractType\":\"CLT\","
				+ "\"city\":\"  Sao Paulo  \",\"state\":\"SP\",\"minSalary\":5000}";

		this.mockMvc.perform(post("/profiles").contentType(MediaType.APPLICATION_JSON).content(body))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").isNumber())
			.andExpect(jsonPath("$.userId").value(this.userId))
			.andExpect(jsonPath("$.name").value("Davi Java"))
			.andExpect(jsonPath("$.level").value("JUNIOR"))
			.andExpect(jsonPath("$.area").value("BACKEND"))
			.andExpect(jsonPath("$.workMode").value("REMOTE"))
			.andExpect(jsonPath("$.contractType").value("CLT"))
			.andExpect(jsonPath("$.city").value("Sao Paulo"))
			.andExpect(jsonPath("$.state").value("SP"))
			.andExpect(jsonPath("$.minSalary").value(5000))
			.andExpect(jsonPath("$.createdAt").isString())
			.andExpect(jsonPath("$.updatedAt").isString());
	}

	@Test
	void createsProfileWithoutOptionalFilters() throws Exception {
		this.mockMvc.perform(post("/profiles").contentType(MediaType.APPLICATION_JSON).content(body("Ana")))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.name").value("Ana"))
			.andExpect(jsonPath("$.level").isEmpty())
			.andExpect(jsonPath("$.area").isEmpty())
			.andExpect(jsonPath("$.workMode").isEmpty())
			.andExpect(jsonPath("$.contractType").isEmpty())
			.andExpect(jsonPath("$.city").isEmpty())
			.andExpect(jsonPath("$.state").isEmpty())
			.andExpect(jsonPath("$.minSalary").isEmpty());
	}

	@Test
	void turnsBlankCityIntoNull() throws Exception {
		String body = "{\"userId\":" + this.userId + ",\"name\":\"Ana\",\"city\":\"   \"}";

		this.mockMvc.perform(post("/profiles").contentType(MediaType.APPLICATION_JSON).content(body))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.city").isEmpty());
	}

	@Test
	void rejectsInvalidBodyOnCreate() throws Exception {
		String tooLong = "a".repeat(101);
		String[] bodies = { "{\"userId\":" + this.userId + ",\"name\":\"   \"}", "{\"userId\":" + this.userId + "}",
				"{\"name\":\"Ana\"}", "{}", "{\"userId\":" + this.userId + ",\"name\":\"" + tooLong + "\"}",
				"{\"userId\":" + this.userId + ",\"name\":\"Ana\",\"city\":\"" + tooLong + "\"}",
				"{\"userId\":" + this.userId + ",\"name\":\"Ana\",\"minSalary\":-1}",
				"{\"userId\":" + this.userId + ",\"name\":\"Ana\",\"level\":\"BOSS\"}",
				"{\"userId\":" + this.userId + ",\"name\":\"Ana\",\"workMode\":\"remote\"}" };
		for (String body : bodies) {
			this.mockMvc.perform(post("/profiles").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isBadRequest());
		}
	}

	@Test
	void rejectsUnknownUserOnCreate() throws Exception {
		this.mockMvc
			.perform(post("/profiles").contentType(MediaType.APPLICATION_JSON)
				.content("{\"userId\":999999,\"name\":\"Ana\"}"))
			.andExpect(status().isNotFound());
	}

	@Test
	void rejectsDuplicateNameOfTheSameUserIgnoringCaseAndSpaces() throws Exception {
		createProfile("Java Backend");

		for (String name : new String[] { "Java Backend", "java backend", "  JAVA BACKEND  " }) {
			this.mockMvc.perform(post("/profiles").contentType(MediaType.APPLICATION_JSON).content(body(name)))
				.andExpect(status().isConflict());
		}
	}

	@Test
	void allowsTheSameNameForDifferentUsers() throws Exception {
		createProfile("Java Backend");
		long otherUser = createUser("Ana");

		this.mockMvc
			.perform(post("/profiles").contentType(MediaType.APPLICATION_JSON).content(body(otherUser, "Java Backend")))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.userId").value(otherUser));
	}

	@Test
	void getsProfileById() throws Exception {
		long id = createProfile("Ana");

		this.mockMvc.perform(get("/profiles/" + id))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(id))
			.andExpect(jsonPath("$.userId").value(this.userId))
			.andExpect(jsonPath("$.name").value("Ana"));
	}

	@Test
	void listsOnlyProfilesOfTheUserOrderedById() throws Exception {
		createProfile("Ana");
		createProfile("Bia");
		long otherUser = createUser("Carlos");
		createProfile(otherUser, "Carlos Java");

		this.mockMvc.perform(get("/profiles").param("userId", String.valueOf(this.userId)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2))
			.andExpect(jsonPath("$[0].name").value("Ana"))
			.andExpect(jsonPath("$[1].name").value("Bia"));
	}

	@Test
	void listsNothingWhenTheUserHasNoProfiles() throws Exception {
		this.mockMvc.perform(get("/profiles").param("userId", String.valueOf(this.userId)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void requiresUserIdToList() throws Exception {
		this.mockMvc.perform(get("/profiles")).andExpect(status().isBadRequest());
	}

	@Test
	void returnsNotFoundWhenListingForUnknownUser() throws Exception {
		this.mockMvc.perform(get("/profiles").param("userId", "999999")).andExpect(status().isNotFound());
	}

	@Test
	void updatesNameAndFilters() throws Exception {
		long id = createProfile("Ana");
		String body = "{\"userId\":" + this.userId + ",\"name\":\"  Ana Lima  \",\"level\":\"SENIOR\","
				+ "\"area\":\"DATA\",\"minSalary\":9000}";

		this.mockMvc.perform(put("/profiles/" + id).contentType(MediaType.APPLICATION_JSON).content(body))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(id))
			.andExpect(jsonPath("$.userId").value(this.userId))
			.andExpect(jsonPath("$.name").value("Ana Lima"))
			.andExpect(jsonPath("$.level").value("SENIOR"))
			.andExpect(jsonPath("$.area").value("DATA"))
			.andExpect(jsonPath("$.minSalary").value(9000));

		this.mockMvc.perform(get("/profiles/" + id))
			.andExpect(jsonPath("$.name").value("Ana Lima"))
			.andExpect(jsonPath("$.level").value("SENIOR"));
	}

	@Test
	void updateReplacesTheFiltersNotSent() throws Exception {
		String create = "{\"userId\":" + this.userId + ",\"name\":\"Ana\",\"level\":\"JUNIOR\",\"city\":\"Recife\"}";
		String created = this.mockMvc.perform(post("/profiles").contentType(MediaType.APPLICATION_JSON).content(create))
			.andExpect(status().isCreated())
			.andReturn()
			.getResponse()
			.getContentAsString();
		long id = ((Number) JsonPath.read(created, "$.id")).longValue();

		this.mockMvc.perform(put("/profiles/" + id).contentType(MediaType.APPLICATION_JSON).content(body("Ana")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.level").isEmpty())
			.andExpect(jsonPath("$.city").isEmpty());
	}

	@Test
	void allowsChangingOnlyTheCaseOfOwnName() throws Exception {
		long id = createProfile("ana");

		this.mockMvc.perform(put("/profiles/" + id).contentType(MediaType.APPLICATION_JSON).content(body("Ana")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("Ana"));
	}

	@Test
	void rejectsNameOfAnotherProfileOfTheSameUserOnUpdate() throws Exception {
		createProfile("Ana");
		long id = createProfile("Bia");

		this.mockMvc.perform(put("/profiles/" + id).contentType(MediaType.APPLICATION_JSON).content(body("ANA")))
			.andExpect(status().isConflict());
	}

	@Test
	void rejectsChangingTheUserOnUpdate() throws Exception {
		long id = createProfile("Ana");
		long otherUser = createUser("Bia");

		this.mockMvc
			.perform(put("/profiles/" + id).contentType(MediaType.APPLICATION_JSON).content(body(otherUser, "Ana")))
			.andExpect(status().isBadRequest());
	}

	@Test
	void rejectsBlankNameOnUpdate() throws Exception {
		long id = createProfile("Ana");

		this.mockMvc.perform(put("/profiles/" + id).contentType(MediaType.APPLICATION_JSON).content(body("   ")))
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
		this.mockMvc.perform(put("/profiles/999").contentType(MediaType.APPLICATION_JSON).content(body("Ana")))
			.andExpect(status().isNotFound());
		this.mockMvc.perform(delete("/profiles/999")).andExpect(status().isNotFound());
	}

	@Test
	void blocksDeletingUserThatHasProfiles() throws Exception {
		long profileId = createProfile("Ana");

		this.mockMvc.perform(delete("/users/" + this.userId)).andExpect(status().isConflict());
		this.mockMvc.perform(get("/users/" + this.userId)).andExpect(status().isOk());

		this.mockMvc.perform(delete("/profiles/" + profileId)).andExpect(status().isNoContent());
		this.mockMvc.perform(delete("/users/" + this.userId)).andExpect(status().isNoContent());
	}

	private String body(String name) {
		return body(this.userId, name);
	}

	private String body(long userId, String name) {
		return "{\"userId\":" + userId + ",\"name\":\"" + name + "\"}";
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

	private long createProfile(String name) throws Exception {
		return createProfile(this.userId, name);
	}

	private long createProfile(long userId, String name) throws Exception {
		String body = this.mockMvc
			.perform(post("/profiles").contentType(MediaType.APPLICATION_JSON).content(body(userId, name)))
			.andExpect(status().isCreated())
			.andReturn()
			.getResponse()
			.getContentAsString();
		return ((Number) JsonPath.read(body, "$.id")).longValue();
	}

}
