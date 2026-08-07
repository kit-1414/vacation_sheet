package com.example.vacationsheet.mainapp.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class ProjectRequestDto(
	@field:NotBlank
	@field:Size(max = 100)
	@field:Pattern(regexp = "^[A-Za-z0-9]+$", message = "Name must contain only alphanumeric characters")
	val name: String,

	@field:Size(max = 1000)
	val description: String? = null,
)
