package com.marineseo.fishtank.repo

import com.marineseo.fishtank.model.User
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository: CrudRepository<User, String>