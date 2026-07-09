package com.example.carrentalapp

import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.carrentalapp.adapters.BookingsAdapter
import com.example.carrentalapp.models.data.booking.Booking
import com.example.carrentalapp.models.data.car.Car
import com.example.carrentalapp.models.data.user.UserSession
import com.example.carrentalapp.models.types.user.UserType
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore

class ManageBookingsActivity : AppCompatActivity() {
    private lateinit var ownerCarsImageButton: ImageButton
    private lateinit var createListingImageButton: ImageButton
    private lateinit var searchCarsImageButton: ImageButton
    private lateinit var logoutImageButton: ImageButton
    private lateinit var bookingsMessageTextView: TextView
    private lateinit var bookingsRecyclerView: RecyclerView

    private lateinit var bookingsAdapter: BookingsAdapter

    private val bookings = mutableListOf<Booking>()

    private val db = Firebase.firestore
    private val auth: FirebaseAuth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_bookings)

        checkIfLoggedIn()

        bindWidgets()

        setUpRecyclerView()

        checkUserType()

        setEventListeners()
    }

    private fun bindWidgets() {
        ownerCarsImageButton = findViewById<ImageButton>(R.id.owner_cars_imageButton)
        createListingImageButton = findViewById<ImageButton>(R.id.create_listing_imageButton)
        searchCarsImageButton = findViewById<ImageButton>(R.id.search_cars_imageButton)
        logoutImageButton = findViewById<ImageButton>(R.id.logout_imageButton)
        bookingsMessageTextView = findViewById<TextView>(R.id.bookings_message_textView)
        bookingsRecyclerView = findViewById<RecyclerView>(R.id.bookings_recyclerView)
    }

    private fun setEventListeners() {
        createListingImageButton.setOnClickListener {
            goToScreen(CreateListingActivity::class.java)
        }

        ownerCarsImageButton.setOnClickListener {
            goToScreen(OwnerCarsActivity::class.java)
        }

        searchCarsImageButton.setOnClickListener {
            goToScreen(SearchCarsActivity::class.java)
        }

        logoutImageButton.setOnClickListener {
            logoutUser()
        }
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

    private fun goToScreen(target: Class<*>) {
        val intent = Intent(this, target)
        startActivity(intent)
    }

    private fun checkIfLoggedIn() {
        if (auth.currentUser == null) {
            goToScreen(MainActivity::class.java)

            Toast.makeText(
                this,
                "You are not logged in.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setUpRecyclerView() {
        bookingsAdapter = BookingsAdapter(
            bookings
        ) { booking ->
            cancelBooking(booking)
        }

        bookingsRecyclerView.layoutManager = LinearLayoutManager(this)
        bookingsRecyclerView.adapter = bookingsAdapter
    }

    /**
     * Loads Booking based on User Type and User ID.
     */
    private fun loadBookings() {
        val currentUser = auth.currentUser!!
        val isOwner = UserSession.getUser()!!.type == UserType.OWNER

        val query = if (isOwner) {
            db.collection("car_bookings")
                .whereEqualTo("ownerId", currentUser.uid)
        } else {
            db.collection("car_bookings")
                .whereEqualTo("renterId", currentUser.uid)
        }

        bookings.clear()

        query.get()
            .addOnSuccessListener { documentSnapshots ->

                if (documentSnapshots.isEmpty) {
                    setUiState(bookings)
                    return@addOnSuccessListener
                }

                for (document in documentSnapshots) {
                    val carId = document.getString("carId") ?: ""
                    val renterId = document.getString("renterId") ?: ""

                    loadCarAndRenter(
                        bookingId = document.id,
                        carId = carId,
                        renterId = renterId,
                        bookingDocument = document
                    )
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    "Failed to load bookings.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    /**
     * Loads Car and Renter Info from Firestore.
     *
     * @param bookingId - Booking's ID.
     * @param carId - Car's ID.
     * @param renterId - Renter's ID
     * @param bookingDocument - Booking Instance from Firestore.
     */
    private fun loadCarAndRenter(
        bookingId: String,
        carId: String,
        renterId: String,
        bookingDocument: DocumentSnapshot
    ) {
        db.collection("car_listings")
            .document(carId)
            .get()
            .addOnSuccessListener { document ->

                if (!document.exists()) return@addOnSuccessListener

                val car = Car(
                    id = document.id,
                    ownerId = document.getString("ownerId") ?: "",
                    brand = document.getString("brand") ?: "",
                    model = document.getString("model") ?: "",
                    color = document.getString("color") ?: "",
                    licensePlate = document.getString("licensePlate") ?: "",
                    pricePerDay = document.getDouble("pricePerDay") ?: 0.0,
                    photoUrl = document.getString("photoUrl") ?: ""
                )

                val listingCity = document.getString("city") ?: ""
                val listingAddress = document.getString("address") ?: ""

                loadRenter(
                    bookingId = bookingId,
                    renterId = renterId,
                    car = car,
                    city = listingCity,
                    address = listingAddress,
                    bookingDocument = bookingDocument
                )
            }
    }

    /**
     * Loads Car and Renter Info from Firestore.
     *
     * @param bookingId - Booking's ID.
     * @param car - Car Instance.
     * @param renterId - Renter's ID
     * @param city - Booking City.
     * @param address - Booking Address.
     * @param bookingDocument - Booking Instance from Firestore.
     */
    private fun loadRenter(
        bookingId: String,
        renterId: String,
        car: Car,
        city: String,
        address: String,
        bookingDocument: DocumentSnapshot
    ) {
        db.collection("users")
            .document(renterId)
            .get()
            .addOnSuccessListener { document ->

                val renterFirstName = document.getString("firstName") ?: ""
                val renterLastName = document.getString("lastName") ?: ""

                val booking = Booking(
                    id = bookingId,
                    car = car,
                    renterFirstName = renterFirstName,
                    renterLastName = renterLastName,
                    confirmationCode = bookingDocument.getLong("confirmationCode")?.toInt() ?: 0,
                    startDate = bookingDocument.getTimestamp("startDate") ?: Timestamp.now(),
                    endDate = bookingDocument.getTimestamp("endDate") ?: Timestamp.now(),
                    city = city,
                    address = address
                )

                bookings.add(booking)
                bookingsAdapter.notifyDataSetChanged()
                setUiState(bookings)
            }
    }

    /**
     * Deletes Booking from Firestore.
     *
     * @param booking - Booking Instance.
     */
    private fun cancelBooking(booking: Booking) {
        AlertDialog.Builder(this)
            .setTitle("Cancel Booking")
            .setMessage("Are you sure you want to cancel this booking?")
            .setPositiveButton("Yes") { _, _ ->
                db.collection("car_bookings")
                    .document(booking.id)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Booking cancelled.",
                            Toast.LENGTH_SHORT
                        ).show()

                        bookings.remove(booking)
                        bookingsAdapter.notifyDataSetChanged()

                        setUiState(bookings)
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            this,
                            "Failed to cancel booking.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .setNegativeButton("No", null)
            .show()
    }

    /**
     * Loads UI based on Bookings List.
     *
     * @param bookings - Bookings List.
     */
    private fun setUiState(bookings: MutableList<Booking>) {
        if (bookings.isEmpty()) {
            bookingsMessageTextView.visibility = View.VISIBLE
            bookingsRecyclerView.visibility = View.GONE
        } else {
            bookingsMessageTextView.visibility = View.GONE
            bookingsRecyclerView.visibility = View.VISIBLE
        }
    }

    /**
     * Changes UI based on User Type.
     */
    private fun checkUserType() {
        if (UserSession.getUser()!!.type == UserType.OWNER) {
            searchCarsImageButton.visibility = View.GONE
            ownerCarsImageButton.visibility = View.VISIBLE
            createListingImageButton.visibility = View.VISIBLE
        } else {
            searchCarsImageButton.visibility = View.VISIBLE
            ownerCarsImageButton.visibility = View.GONE
            createListingImageButton.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        loadBookings()
    }
}