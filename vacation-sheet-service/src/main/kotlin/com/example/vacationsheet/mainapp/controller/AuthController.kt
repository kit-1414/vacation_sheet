package com.example.vacationsheet.mainapp.controller

import com.example.vacationsheet.mainapp.dto.CsrfTokenResponse
import com.example.vacationsheet.mainapp.hql.dto.UserAccountDto
import com.example.vacationsheet.mainapp.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@Tag(name = "Authentication")
@RestController
@RequestMapping("/api/auth")
class AuthController(
	private val userService: UserService,
) {
	@Operation(summary = "Get the current authenticated user")
	@GetMapping("/me")
	fun currentUser(@AuthenticationPrincipal principal: OAuth2User): UserAccountDto {
		val yandexId = principal.getAttribute<Any>("id")?.toString()
			?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
		return userService.findCurrent(yandexId)
	}

	@Operation(summary = "Initialize the CSRF cookie")
	@GetMapping("/csrf")
	fun csrf(csrfToken: CsrfToken) = CsrfTokenResponse(csrfToken.token)
}
