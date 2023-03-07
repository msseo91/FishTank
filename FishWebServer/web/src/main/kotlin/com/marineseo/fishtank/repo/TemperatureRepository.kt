package com.marineseo.fishtank.repo

import com.marineseo.fishtank.model.Temperature
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TemperatureRepository: CrudRepository<Temperature, Int> {

    fun findByTimeBetween(from: Date, until: Date): List<Temperature>
}