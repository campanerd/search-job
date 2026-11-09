package br.com.searchjob.technology.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "technology")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Technology {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(columnDefinition = "INTEGER")
	private Long id;

	@Column(nullable = false, length = 100)
	private String name;

	public Technology(String name) {
		this.name = name;
	}

}
