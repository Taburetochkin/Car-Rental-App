package com.example.carrentalapp

import android.content.Intent
import android.media.Image
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.carrentalapp.models.data.car.Car
import com.example.carrentalapp.models.data.user.UserSession
import com.example.carrentalapp.models.types.user.UserType
import com.example.carrentalapp.utils.DateUtils
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.random.Random

class CarRentActivity : AppCompatActivity() {
    private lateinit var carLicensePlateTextView: TextView
    private lateinit var searchCarsImageButton: ImageButton
    private lateinit var manageBookingsImageButton: ImageButton
    private lateinit var logoutImageButton: ImageButton
    private lateinit var carImageImageView: ImageView
    private lateinit var carMainInfoTextView: TextView
    private lateinit var carOwnerTextView: TextView
    private lateinit var carPricePerDayTextView: TextView
    private lateinit var startDateEditText: EditText
    private lateinit var endDateEditText: EditText
    private lateinit var bookButton: Button

    private val auth: FirebaseAuth = Firebase.auth
    private val db = Firebase.firestore

    private var carId: String? = null
    private var loadedCar: Car? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car_rent)

        checkIfLoggedIn()

        bindWidgets()

        bindIntentData()

        checkIntentData()

        setEventListeners()
    }

    private fun bindWidgets() {
        carLicensePlateTextView = findViewById<TextView>(R.id.car_license_plate_textView)
        searchCarsImageButton = findViewById<ImageButton>(R.id.search_cars_imageButton)
        manageBookingsImageButton = findViewById<ImageButton>(R.id.manage_bookings_imageButton)
        logoutImageButton = findViewById<ImageButton>(R.id.logout_imageButton)
        carImageImageView = findViewById<ImageView>(R.id.car_image_imageView)
        carMainInfoTextView = findViewById<TextView>(R.id.car_main_info_textView)
        carOwnerTextView = findViewById<TextView>(R.id.car_owner_textView)
        carPricePerDayTextView = findViewById<TextView>(R.id.car_price_per_day_textView)
        startDateEditText = findViewById<EditText>(R.id.start_date_editText)
        endDateEditText = findViewById<EditText>(R.id.end_date_editText)
        bookButton = findViewById<Button>(R.id.book_button)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setEventListeners() {
        searchCarsImageButton.setOnClickListener {
            goToScreen(SearchCarsActivity::class.java)
        }

        manageBookingsImageButton.setOnClickListener {
            goToScreen(ManageBookingsActivity::class.java)
        }

        logoutImageButton.setOnClickListener {
            logoutUser()
        }

        bookButton.setOnClickListener {
            loadedCar?.let {
                createBooking(it)
            }
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
     * Binds Intent Data, received from SearchCarsActivity.
     */
    private fun bindIntentData() {
        carId = intent.getStringExtra("extra_car_id")
    }

    /**
     * Checks the received data and changes UI based on the extra's values.
     */
    private fun checkIntentData() {
        if (carId == null) {
            Toast.makeText(
                this,
                "Failed to get car info.",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }

        loadCar()
    }

    /**
     * Binds Activities Data with Widgets.
     *
     * @param car - Car Instance.
     * @param fullName - Owner's Full Name.
     */
    private fun bindData(car: Car, fullName: String) {
        carLicensePlateTextView.text = car.normalizedLicensePlate

        Glide.with(this)
            .load(car.photoUrl)
            .placeholder(R.drawable.ic_launcher_foreground)
            .into(carImageImageView)

        carMainInfoTextView.text = car.mainCarInfo
        carOwnerTextView.text = fullName
        carPricePerDayTextView.text = car.formattedPricePerDay

        loadedCar = car
    }

    private fun checkIfLoggedIn() {
        if (auth.currentUser == null || UserSession.getUser()?.type != UserType.RENTER) {
            goToScreen(MainActivity::class.java)
            Toast.makeText(
                this,
                "You are either not logged in or you are not a renter.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Loads Car from firestore based on its ID.
     */
    private fun loadCar() {
        db.collection("car_listings")
            .document(carId!!)
            .get()
            .addOnSuccessListener { documentSnapshot ->

                if (!documentSnapshot.exists()) {
                    Toast.makeText(this, "Car not found.", Toast.LENGTH_SHORT).show()
                    finish()
                    return@addOnSuccessListener
                }

                val car = Car(
                    id = documentSnapshot.id,
                    ownerId = documentSnapshot.getString("ownerId") ?: "",
                    brand = documentSnapshot.getString("brand") ?: "",
                    model = documentSnapshot.getString("model") ?: "",
                    color = documentSnapshot.getString("color") ?: "",
                    licensePlate = documentSnapshot.getString("licensePlate") ?: "",
                    pricePerDay = documentSnapshot.getDouble("pricePerDay") ?: 0.0,
                    photoUrl = documentSnapshot.getString("photoUrl") ?: ""
                )

                loadOwner(car.ownerId, car)
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    "Failed to load car.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
    }

    /**
     * Loads Owner from firestore based on its ID.
     *
     * @param ownerId - Owner's ID.
     * @param car - Car Instance.
     */
    private fun loadOwner(ownerId: String, car: Car) {
        db.collection("users")
            .document(ownerId)
            .get()
            .addOnSuccessListener { documentSnapshot ->

                val fullName = if (documentSnapshot.exists()) {
                    val firstName = documentSnapshot.getString("firstName") ?: ""
                    val lastName = documentSnapshot.getString("lastName") ?: ""
                    "$firstName $lastName"
                } else {
                    "Unknown Owner"
                }

                bindData(car, fullName)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load owner info.", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Creates Booking Instance and adds it to Firestore.
     *
     * @param car - Car Instance.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createBooking(car: Car) {
        val startDateString = DateUtils.normalizeDate(startDateEditText.text.toString().trim())
        val endDateString = DateUtils.normalizeDate(endDateEditText.text.toString().trim())

        if (startDateString.isEmpty() || endDateString.isEmpty()) {
            Toast.makeText(
                this,
                "Enter both start and end date.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val convertedStartDate = DateUtils.convertToTimestamp(startDateString)
        val convertedEndDate = DateUtils.convertToTimestamp(endDateString)

        if (convertedStartDate == null || convertedEndDate == null) {
            Toast.makeText(
                this,
                "Invalid dates. Use dd-mm-yyyy format.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val startDate = LocalDate.parse(
            startDateString,
            DateTimeFormatter.ofPattern("dd-MM-yyyy")
        )
        val endDate = LocalDate.parse(
            endDateString,
            DateTimeFormatter.ofPattern("dd-MM-yyyy")
        )
        val today = LocalDate.now()

        if (startDate.isBefore(today) || endDate.isBefore(today)) {
            Toast.makeText(
                this,
                "Dates cannot be earlier than today.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (endDate.isBefore(startDate)) {
            Toast.makeText(
                this,
                "End date cannot be earlier than start date.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        db.collection("car_bookings")
            .whereEqualTo("carId", car.id)
            .get()
            .addOnSuccessListener { documentSnapshots ->

                for (document in documentSnapshots) {
                    val existingStartDate = document.getTimestamp("startDate")?.toDate()?.toInstant()
                        ?.atZone(ZoneId.systemDefault())?.toLocalDate()
                    val existingEndDate = document.getTimestamp("endDate")?.toDate()?.toInstant()
                        ?.atZone(ZoneId.systemDefault())?.toLocalDate()

                    if (existingStartDate != null || existingEndDate != null) {
                        val ifDatesOverlap: Boolean =
                            (startDate.isBefore(existingEndDate) || startDate.isEqual(existingEndDate)) &&
                            (endDate.isAfter(existingStartDate) || endDate.isEqual(existingStartDate))

                        if (ifDatesOverlap) {
                            Toast.makeText(
                                this,
                                "These dates are already booked. Choose different dates.",
                                Toast.LENGTH_LONG
                            ).show()

                            return@addOnSuccessListener
                        }
                    }
                }

                val confirmationCode = Random.nextInt(100000, 999999)

                val booking = hashMapOf(
                    "carId" to car.id,
                    "ownerId" to car.ownerId,
                    "renterId" to auth.currentUser!!.uid,
                    "startDate" to convertedStartDate,
                    "endDate" to convertedEndDate,
                    "confirmationCode" to confirmationCode,
                    "createdAt" to FieldValue.serverTimestamp()
                )

                db.collection("car_bookings")
                    .add(booking)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Booking successful! Confirmation: $confirmationCode",
                            Toast.LENGTH_LONG
                        ).show()

                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            this,
                            "Error creating booking.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
    }
}