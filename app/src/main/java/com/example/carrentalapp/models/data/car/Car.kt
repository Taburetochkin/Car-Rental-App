package com.example.carrentalapp.models.data.car

/**
 * Represents Car instance.
 *
 * @property id - Car ID.
 * @property ownerId - Owner ID.
 * @property brand - Car's Brand.
 * @property model - Car's Model.
 * @property color - Car's Color.
 * @property licensePlate - Car's License Plate.
 * @property pricePerDay - Car's Price.
 * @property photoUrl - Car's Photo URL.
 */
data class Car(
    val id: String,
    val ownerId: String,
    val brand: String,
    val model: String,
    val color: String,
    val licensePlate: String,
    val pricePerDay: Double,
    val photoUrl: String
) {
    val mainCarInfo = "$brand $model, $color"
    val normalizedLicensePlate = licensePlate.uppercase()
    val formattedPricePerDay = "$${String.format("%.2f", pricePerDay)}/day"
}
