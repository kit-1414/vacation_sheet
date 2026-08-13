package com.example.vacationsheet.mainapp.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UserAccountRequestDto(
	@field:NotBlank
	@field:Email
	@field:Size(max = 320)
	val email: String,

	@field:Size(max = 255)
	val firstName: String? = null,

	@field:Size(max = 255)
	val lastName: String? = null,

	val isAdmin: Boolean = false,

	val isActive: Boolean = true,
)
