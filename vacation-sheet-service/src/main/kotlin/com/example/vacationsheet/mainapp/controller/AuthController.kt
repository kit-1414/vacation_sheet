package com.example.vacationsheet.mainapp.controller

import com.example.vacationsheet.mainapp.hql.dto.UserAccountDto
import com.example.vacationsheet.mainapp.service.UserService
import com.example.vacationsheet.mainapp.utils.logaspect.LogPublicMethods
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.view.RedirectView

@Tag(name = "Authentication")
@RestController
@LogPublicMethods
class AuthController(
	private val userService: UserService,
	@Value("\${app.frontend.url}") private val frontendUrl: String,
) {
	@GetMapping("/")
	fun oauthSuccess(): RedirectView = RedirectView(frontendUrl)

	@Operation(summary = "Get the current authenticated user")
	@GetMapping("/api/auth/me")
	fun currentUser(@AuthenticationPrincipal principal: OAuth2User): UserAccountDto {
		val email = principal.getAttribute<Any>("default_email")?.toString()
			?: throw ResponseStatusException(HttpStatus.FORBIDDEN)
		return userService.findCurrent(email)
	}

}
