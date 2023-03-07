package com.marineseo.fishtank.service

import com.marineseo.fishtank.mapper.DatabaseMapper
import com.marineseo.fishtank.mapper.UserRepository
import com.marineseo.fishtank.model.User
import com.marineseo.fishtank.util.makeRandomString
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

const val TOKEN_LENGTH = 64
@Service
class UserService(
    private val userRepository: UserRepository
) {
   private val tokenMap = mutableMapOf<String, User>()

    fun signIn(id: String, password: String): User? {
        return userRepository.findByIdOrNull(id)?.let {
            if(it.password == password) {
                it.token = generateUserToken()
                tokenMap[it.token] = it
                it
            } else {
                null
            }
        }
    }

    private fun generateUserToken(): String {
        return makeRandomString(TOKEN_LENGTH)
    }

    fun getUserByToken(token: String): User? {
        return tokenMap[token]
    }
}