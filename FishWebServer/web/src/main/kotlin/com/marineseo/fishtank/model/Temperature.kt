package com.marineseo.fishtank.model

import java.util.*
import javax.persistence.*

const val TB_TEMPERATURE = "temperature"

@Entity
@Table(name = TB_TEMPERATURE)
data class Temperature (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0,

    @Column
    var temperature: Float = 0f,

    @Temporal(TemporalType.TIMESTAMP)
    var time: Date = Date()
)