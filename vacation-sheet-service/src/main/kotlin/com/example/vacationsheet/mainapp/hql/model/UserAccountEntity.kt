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
	@Column(nullable = false, unique = true, length = 320, updatable = false)
	val email: String,

	@Column(name = "first_name", updatable = false)
	val firstName: String?,

	@Column(name = "last_name", updatable = false)
	val lastName: String?,

	@Column(nullable = false, updatable = false)
	var ctime: OffsetDateTime? = null,

	@Column(nullable = false)
	var utime: OffsetDateTime? = null,

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	val id: UUID? = null,
)
