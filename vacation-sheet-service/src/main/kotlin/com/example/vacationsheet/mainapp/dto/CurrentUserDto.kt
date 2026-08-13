package com.example.vacationsheet.mainapp.dto

import com.example.vacationsheet.mainapp.hql.dto.UserAccountDto
import com.example.vacationsheet.mainapp.service.UserRole
import java.time.OffsetDateTime

data class CurrentUserDto(
	val id: Long,
	val email: String,
	val firstName: String?,
	val lastName: String?,
	val isAdmin: Boolean,
	val isActive: Boolean,
	val ctime: OffsetDateTime?,
	val utime: OffsetDateTime?,
	val roles: Set<UserRole>,
) {
	constructor(user: UserAccountDto, roles: Set<UserRole>) : this(
		id = user.id,
		email = user.email,
		firstName = user.firstName,
		lastName = user.lastName,
		isAdmin = user.isAdmin,
		isActive = user.isActive,
		ctime = user.ctime,
		utime = user.utime,
		roles = roles,
	)
}
