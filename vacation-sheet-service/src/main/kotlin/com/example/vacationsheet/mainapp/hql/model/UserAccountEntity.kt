package com.example.vacationsheet.mainapp.hql.model

import com.example.vacationsheet.mainapp.hql.handler.JpaTimeHandler
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "user_accounts")
@EntityListeners(JpaTimeHandler::class)
class UserAccountEntity(
	@Column(name = "yandex_id", nullable = false, unique = true)
	val yandexId: String,

	@Column(nullable = false, unique = true, length = 320)
	var email: String,

	@Column(name = "display_name")
	var displayName: String?,

	@Column(nullable = false, updatable = false)
	var ctime: OffsetDateTime? = null,

	@Column(nullable = false)
	var utime: OffsetDateTime? = null,

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	val id: UUID? = null,
)
