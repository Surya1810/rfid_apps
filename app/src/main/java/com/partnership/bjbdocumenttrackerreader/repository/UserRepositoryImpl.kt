package com.partnership.rfid.repository

import javax.inject.Inject

class UserRepositoryImpl @Inject constructor() : UserRepository {
    override fun getUserData(): String {
        return "User Data from Repository"
    }
}