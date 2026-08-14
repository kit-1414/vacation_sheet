package com.example.vacationsheet.mainapp.service

import com.example.vacationsheet.mainapp.dto.CurrentUserDto
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class CurrentUserService(
	private val userService: UserService,
) {
	fun getCurrentUser(): CurrentUserDto {
		val principal = SecurityContextHolder.getContext().authentication?.principal as? OAuth2User
			?: throw ResponseStatusException(HttpStatus.FORBIDDEN)
		val email = principal.getAttribute<Any>("default_email")?.toString()
			?: throw ResponseStatusException(HttpStatus.FORBIDDEN)
		val roles = principal.authorities.mapNotNull { authority ->
			UserRole.entries.find { it.roleName == authority.authority }
		}.toSet()
		return CurrentUserDto(userService.findCurrent(email), roles)
	}
}
