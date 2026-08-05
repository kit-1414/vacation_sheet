package com.example.vacationsheet.mainapp.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ProjectRequestDto(
	@field:NotBlank
	@field:Size(max = 120)
	val name: String,

	@field:Size(max = 2000)
	val description: String? = null,
)
