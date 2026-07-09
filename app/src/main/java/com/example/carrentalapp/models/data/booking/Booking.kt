package com.example.carrentalapp.models.data.booking

import com.example.carrentalapp.models.data.car.Car
import com.example.carrentalapp.utils.DateUtils
import com.google.firebase.Timestamp

/**
 * Represents Booking instance.
 *
 * @property id - Booking ID.
 * @property car - Car instance.
 * @property renterFirstName - Renter's First Name.
 * @property renterLastName - Renter's Last Name.
 * @property confirmationCode - Booking Confirmation Code.
 * @property city - Booking City.
 * @property address - Booking Address.
 * @property startDate - Booking Start Date.
 * @property endDate - Booking End Date.
 */
data class Booking(
    val id: String,
    val car: Car,
    val renterFirstName: String,
    val renterLastName: String,
    val confirmationCode: Int,
    val city: String,
    val address: String,
    val startDate: Timestamp,
    val endDate: Timestamp
) {
    val renterFullName = "$renterFirstName $renterLastName"
    val formattedBookingDates = "${DateUtils.formatTimestamp(startDate)} - " +
            "${DateUtils.formatTimestamp(endDate)}"

    val capitalizedCity = city.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString()
    }
    val formattedLocation = "$address, $capitalizedCity"
}
