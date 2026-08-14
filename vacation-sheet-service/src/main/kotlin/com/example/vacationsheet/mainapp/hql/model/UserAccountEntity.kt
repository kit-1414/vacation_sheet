package com.example.vacationsheet.mainapp.hql.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.OffsetDateTime

@Entity
@Table(name = "user_accounts")
@EntityListeners(AuditingEntityListener::class)
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
	@field:CreatedDate
	var ctime: OffsetDateTime? = null,

	@Column(nullable = false)
	@field:LastModifiedDate
	var utime: OffsetDateTime? = null,

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	val id: Long? = null,
)
