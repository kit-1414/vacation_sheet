package com.example.vacationsheet.dto

import com.example.vacationsheet.entity.UserAccount
import java.util.UUID

data class UserResponse(
	val id: UUID,
	val email: String,
	val displayName: String?,
)

fun UserAccount.toResponse() = UserResponse(
	id = requireNotNull(id),
	email = email,
	displayName = displayName,
)
