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

@Entity
@Table(name = "user_accounts")
@EntityListeners(JpaTimeHandler::class)
class UserAccountEntity(
	@Column(nullable = false, unique = true, length = 320)
	var email: String,

	@Column(name = "first_name")
	var firstName: String?,

	@Column(name = "last_name")
	var lastName: String?,

	@Column(name = "is_admin", nullable = false)
	var isAdmin: Boolean = false,

	@Column(name = "is_active", nullable = false)
	var isActive: Boolean = true,

	@Column(nullable = false, updatable = false)
	var ctime: OffsetDateTime? = null,

	@Column(nullable = false)
	var utime: OffsetDateTime? = null,

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	val id: Long? = null,
)
