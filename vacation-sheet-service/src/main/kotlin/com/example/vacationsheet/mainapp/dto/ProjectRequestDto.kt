package com.example.vacationsheet.mainapp.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class ProjectRequestDto(
	@field:NotBlank
	@field:Size(max = 100)
	@field:Pattern(
		regexp = "^[\\p{L}\\p{N}._-]+$",
		message = "Name must contain only letters, numbers, '.', '_' or '-'",
	)
	val name: String,

	@field:Size(max = 1000)
	val description: String? = null,
)
