package com.example.vacationsheet.controller

import com.example.vacationsheet.dto.UserResponse
import com.example.vacationsheet.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Users")
@RestController
@RequestMapping("/api/users")
class UserController(
	private val userService: UserService,
) {
	@Operation(summary = "List all registered users")
	@GetMapping
	fun findAll(): List<UserResponse> = userService.findAll()
}
