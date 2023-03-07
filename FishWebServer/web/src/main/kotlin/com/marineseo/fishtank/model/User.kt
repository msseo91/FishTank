package com.marineseo.fishtank.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.Table
import javax.persistence.OneToMany

const val TB_USER = "user"

@Entity
@Table(name = TB_USER)
data class User(
    @Id
    var id: String = "",

    @Column
    var name: String = "",

    @Column
    var password: String = "",

    @Column
    var token: String = "",

    @OneToMany
    @JoinColumn(name = "userId")
    val tasks: List<Task> = listOf(),

    @OneToMany
    @JoinColumn(name = "userId")
    val periodicTasks: List<PeriodicTask> = listOf()
)
