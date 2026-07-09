package com.example.carrentalapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.carrentalapp.models.data.user.UserSession
import com.example.carrentalapp.models.types.user.UserType
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class CreateListingActivity : AppCompatActivity() {
    private lateinit var ownerCarsImageButton: ImageButton
    private lateinit var manageBookingsImageButton: ImageButton
    private lateinit var logoutImageButton: ImageButton
    private lateinit var carBrandEditText: EditText
    private lateinit var carModelEditText: EditText
    private lateinit var carColorEditText: EditText
    private lateinit var carLicensePlateEditText: EditText
    private lateinit var carPricePerDayEditText: EditText
    private lateinit var carCityEditText: EditText
    private lateinit var carAddressEditText: EditText
    private lateinit var carPhotoUrlEditText: EditText
    private lateinit var createButton: Button

    private val auth: FirebaseAuth = Firebase.auth
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_listing)

        checkIfLoggedIn()

        bindWidgets()

        setEventListeners()
    }

    private fun bindWidgets() {
        ownerCarsImageButton = findViewById<ImageButton>(R.id.owner_cars_imageButton)
        manageBookingsImageButton = findViewById<ImageButton>(R.id.manage_bookings_imageButton)
        logoutImageButton = findViewById<ImageButton>(R.id.logout_imageButton)
        carBrandEditText = findViewById<EditText>(R.id.car_brand_editText)
        carModelEditText = findViewById<EditText>(R.id.car_model_editText)
        carColorEditText = findViewById<EditText>(R.id.car_color_editText)
        carLicensePlateEditText = findViewById<EditText>(R.id.car_license_plate_editText)
        carPricePerDayEditText = findViewById<EditText>(R.id.car_price_per_day_editText)
        carCityEditText = findViewById<EditText>(R.id.car_city_editText)
        carAddressEditText = findViewById<EditText>(R.id.car_address_editText)
        carPhotoUrlEditText = findViewById<EditText>(R.id.car_photo_url_editText)
        createButton = findViewById<Button>(R.id.create_button)
    }

    private fun setEventListeners() {
        ownerCarsImageButton.setOnClickListener {
            goToScreen(OwnerCarsActivity::class.java)
        }

        manageBookingsImageButton.setOnClickListener {
            goToScreen(ManageBookingsActivity::class.java)
        }

        logoutImageButton.setOnClickListener {
            logoutUser()
        }

        createButton.setOnClickListener {
            createListing()
        }
    }

    private fun goToScreen(target: Class<*>) {
        val intent = Intent(this, target)
        startActivity(intent)
    }

    private fun logoutUser() {
        auth.signOut()
        UserSession.setUser(null)
        Toast.makeText(
            this,
            "You have been logged out.",
            Toast.LENGTH_SHORT
        ).show()
        goToScreen(MainActivity::class.java)
    }

    /**
     * Creates Car Instance and adds it to Firestore.
     */
    private fun createListing() {
        val brand = carBrandEditText.text.toString().trim()
        if (brand.isEmpty()) {
            Toast.makeText(
                this,
                "Please enter car brand.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val model = carModelEditText.text.toString().trim()
        if (model.isEmpty()) {
            Toast.makeText(
                this,
                "Please enter car model.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val color = carColorEditText.text.toString().trim()
        if (color.isEmpty()) {
            Toast.makeText(
                this,
                "Please enter car color.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val licensePlate = carLicensePlateEditText.text.toString().trim()
        if (licensePlate.isEmpty()) {
            Toast.makeText(
                this,
                "Please enter car license plate.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val pricePerDay = carPricePerDayEditText.text.toString().trim()
        val normalizedPricePerDay = pricePerDay.toDoubleOrNull()
        if (normalizedPricePerDay == null || normalizedPricePerDay < 0.0) {
            Toast.makeText(
                this,
                "Please enter correct price per day.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val city = carCityEditText.text.toString().trim()
        if (city.isEmpty()) {
            Toast.makeText(
                this,
                "Please enter city.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val address = carAddressEditText.text.toString().trim()
        if (address.isEmpty()) {
            Toast.makeText(
                this,
                "Please enter address.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val photoUrl = carPhotoUrlEditText.text.toString().trim()
        if (photoUrl.isEmpty()) {
            Toast.makeText(
                this,
                "Please enter car photo URL.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val carListingData = hashMapOf(
            "ownerId" to auth.currentUser!!.uid,
            "brand" to brand,
            "model" to model,
            "color" to color,
            "licensePlate" to licensePlate,
            "pricePerDay" to normalizedPricePerDay,
            "city" to city.lowercase(),
            "address" to address,
            "photoUrl" to photoUrl
        )

        db.collection("car_listings")
            .add(carListingData)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Listing was successfully created!",
                    Toast.LENGTH_SHORT
                ).show()

                clearUi()
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    "Error creating listing.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    /**
     * Clears Edit Texts after creating Car Instance.
     */

    private fun clearUi() {
        carBrandEditText.setText("")
        carModelEditText.setText("")
        carColorEditText.setText("")
        carLicensePlateEditText.setText("")
        carPricePerDayEditText.setText("")
        carCityEditText.setText("")
        carAddressEditText.setText("")
        carPhotoUrlEditText.setText("")
    }

    private fun checkIfLoggedIn() {
        if (auth.currentUser == null || UserSession.getUser()?.type != UserType.OWNER) {
            goToScreen(MainActivity::class.java)

            Toast.makeText(
                this,
                "You are either not logged in or you are not an owner.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}