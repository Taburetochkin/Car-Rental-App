package com.example.carrentalapp.models.data.car

/**
 * Represents Car Marker on the Map.
 *
 * @property id - Car Marker ID.
 * @property pricePerDay - Car's price.
 */
data class CarMarker(
    val id: String,
    val pricePerDay: Double
) {
    val formattedPricePerDay = "$${String.format("%.0f", pricePerDay)}"
}
