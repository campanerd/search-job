package br.com.searchjob.profile.dto;

import jakarta.validation.constraints.NotBlank;// @formatter:off
import jakarta.validation.constraints.Size;
public record ProfileRequest(

	@NotBlank @Size(max = 100)
	String name

) {
}
// @formatter:on
