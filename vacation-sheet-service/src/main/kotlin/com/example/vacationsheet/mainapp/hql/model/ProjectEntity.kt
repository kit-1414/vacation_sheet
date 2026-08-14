package com.example.vacationsheet.mainapp.hql.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.OffsetDateTime

@Entity
@Table(name = "projects")
@EntityListeners(AuditingEntityListener::class)
class ProjectEntity(
	@Column(nullable = false, length = 100)
	var name: String,

	@Column(length = 1000)
	var description: String?,

	@Column(nullable = false, updatable = false)
	@field:CreatedDate
	var ctime: OffsetDateTime? = null,

	@Column(nullable = false)
	@field:LastModifiedDate
	var utime: OffsetDateTime? = null,

	@ManyToMany
	@JoinTable(
		name = "project_members",
		joinColumns = [JoinColumn(name = "project_id")],
		inverseJoinColumns = [JoinColumn(name = "user_account_id")],
	)
	val members: MutableSet<UserAccountEntity> = linkedSetOf(),

	@ManyToMany
	@JoinTable(
		name = "project_manager",
		joinColumns = [JoinColumn(name = "project_id")],
		inverseJoinColumns = [JoinColumn(name = "user_account_id")],
	)
	val managers: MutableSet<UserAccountEntity> = linkedSetOf(),

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	val id: Long? = null,
)
