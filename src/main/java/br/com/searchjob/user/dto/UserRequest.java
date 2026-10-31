package br.com.searchjob.user.dto;

import jakarta.validation.constraints.NotBlank;// @formatter:off
import jakarta.validation.constraints.Size;
public record UserRequest(

	@NotBlank @Size(max = 100)
	String name

) {
}
// @formatter:on
