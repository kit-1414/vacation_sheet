package com.example.vacationsheet.mainapp.service

const val ROLE_NOBODY = "ROLE_NOBODY"
const val ROLE_ADMIN = "ROLE_ADMIN"
const val ROLE_MANAGER = "ROLE_MANAGER"
const val ROLE_USER = "ROLE_USER"

enum class UserRole(val roleName: String) {
	NOBODY(ROLE_NOBODY),
	ADMIN(ROLE_ADMIN),
	MANAGER(ROLE_MANAGER),
	USER(ROLE_USER),
}
