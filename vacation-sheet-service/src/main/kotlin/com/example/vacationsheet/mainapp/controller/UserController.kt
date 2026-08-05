package com.example.vacationsheet.mainapp.controller

import com.example.vacationsheet.mainapp.hql.dto.UserAccountDto
import com.example.vacationsheet.mainapp.service.UserService
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
	fun findAll(): List<UserAccountDto> = userService.findAll()
}
