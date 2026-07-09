package com.example.carrentalapp.models.data.user

object UserSession {
    private var user: User? = null

    fun getUser(): User? {
        return user
    }

    fun setUser(user: User?) {
        this.user = user
    }
}