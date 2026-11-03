package br.com.searchjob.profile.model;

import br.com.searchjob.profile.enums.Area;
import br.com.searchjob.profile.enums.ContractType;
import br.com.searchjob.profile.enums.Level;
import br.com.searchjob.profile.enums.WorkMode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "profile")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Profile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(columnDefinition = "INTEGER")
	private Long id;

	@Column(nullable = false, columnDefinition = "INTEGER", updatable = false)
	private Long userId;

	@Column(nullable = false, length = 100)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private Level level;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private Area area;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private WorkMode workMode;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private ContractType contractType;

	@Column(length = 100)
	private String city;

	@Column(length = 100)
	private String state;

	private Integer minSalary;

	@CreationTimestamp
	private LocalDateTime createdAt;

	@UpdateTimestamp
	private LocalDateTime updatedAt;

	public Profile(Long userId, String name, Level level, Area area, WorkMode workMode, ContractType contractType,
			String city, String state, Integer minSalary) {
		this.userId = userId;
		update(name, level, area, workMode, contractType, city, state, minSalary);
	}

	public void update(String name, Level level, Area area, WorkMode workMode, ContractType contractType, String city,
			String state, Integer minSalary) {
		this.name = name;
		this.level = level;
		this.area = area;
		this.workMode = workMode;
		this.contractType = contractType;
		this.city = city;
		this.state = state;
		this.minSalary = minSalary;
	}

}
