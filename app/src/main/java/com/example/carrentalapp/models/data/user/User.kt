package com.example.carrentalapp.models.data.user

import com.example.carrentalapp.models.types.user.UserType

/**
 * Represents User instance.
 *
 * @property id - User ID.
 * @property firstName - User's First Name.
 * @property lastName - User's Last Name.
 * @property email - User's Email.
 * @property type - User Type.
 */
data class User(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val type: UserType
)
