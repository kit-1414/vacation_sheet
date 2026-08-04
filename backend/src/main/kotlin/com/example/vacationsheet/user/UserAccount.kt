package com.example.vacationsheet.user

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "user_accounts")
class UserAccount(
	@Column(name = "yandex_id", nullable = false, unique = true)
	val yandexId: String,

	@Column(nullable = false, unique = true, length = 320)
	var email: String,

	@Column(name = "display_name")
	var displayName: String?,

	@Column(name = "created_at", nullable = false)
	val createdAt: Instant = Instant.now(),

	@Column(name = "updated_at", nullable = false)
	var updatedAt: Instant = Instant.now(),

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	val id: UUID? = null,
)
