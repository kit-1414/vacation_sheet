package com.example.vacationsheet.mainapp.hql.model

import com.example.vacationsheet.mainapp.hql.handler.JpaTimeHandler
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
import java.time.OffsetDateTime

@Entity
@Table(name = "projects")
@EntityListeners(JpaTimeHandler::class)
class ProjectEntity(
	@Column(nullable = false, length = 100)
	var name: String,

	@Column(length = 1000)
	var description: String?,

	@Column(nullable = false, updatable = false)
	var ctime: OffsetDateTime? = null,

	@Column(nullable = false)
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
