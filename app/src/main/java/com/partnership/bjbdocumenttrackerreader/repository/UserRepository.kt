package com.partnership.rfid.repository


import javax.inject.Inject

interface UserRepository {
    fun getUserData(): String
}