package com.example.carrentalapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.carrentalapp.adapters.OwnerCarsAdapter
import com.example.carrentalapp.models.data.car.Car
import com.example.carrentalapp.models.data.user.UserSession
import com.example.carrentalapp.models.types.user.UserType
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class OwnerCarsActivity : AppCompatActivity() {
    private lateinit var createListingImageButton: ImageButton
    private lateinit var manageBookingsImageButton: ImageButton
    private lateinit var logoutImageButton: ImageButton
    private lateinit var ownerCarsMessageTextView: TextView
    private lateinit var ownerCarsRecyclerView: RecyclerView

    private lateinit var ownerCarsAdapter: OwnerCarsAdapter

    private val ownerCars = mutableListOf<Car>()

    private val db = Firebase.firestore
    private val auth: FirebaseAuth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_cars)

        checkIfLoggedIn()

        bindWidgets()

        setUpRecyclerView()

        setEventListeners()
    }

    private fun bindWidgets() {
        createListingImageButton = findViewById<ImageButton>(R.id.create_listing_imageButton)
        manageBookingsImageButton = findViewById<ImageButton>(R.id.manage_bookings_imageButton)
        logoutImageButton = findViewById<ImageButton>(R.id.logout_imageButton)
        ownerCarsMessageTextView = findViewById<TextView>(R.id.owner_cars_message_textView)
        ownerCarsRecyclerView = findViewById<RecyclerView>(R.id.owner_cars_recyclerView)
    }

    private fun setEventListeners() {
        createListingImageButton.setOnClickListener {
            goToScreen(CreateListingActivity::class.java)
        }

        manageBookingsImageButton.setOnClickListener {
            goToScreen(ManageBookingsActivity::class.java)
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
        if (auth.currentUser == null || UserSession.getUser()?.type != UserType.OWNER) {
            goToScreen(MainActivity::class.java)

            Toast.makeText(
                this,
                "You are either not logged in or you are not an owner.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setUpRecyclerView() {
        ownerCarsAdapter = OwnerCarsAdapter(ownerCars)

        ownerCarsRecyclerView.layoutManager = LinearLayoutManager(this)
        ownerCarsRecyclerView.adapter = ownerCarsAdapter
    }

    /**
     * Loads Owner Cars from Firestore based on its ID.
     */
    private fun loadOwnerCars() {
        val user = auth.currentUser
        val userId = user?.uid

        ownerCars.clear()

        db.collection("car_listings")
            .whereEqualTo("ownerId", userId)
            .get()
            .addOnSuccessListener { documentSnapshots ->
                for (document in documentSnapshots.documents) {
                    val car = Car(
                        id = document.id,
                        ownerId = document.getString("ownerId") ?: "",
                        brand = document.getString("brand") ?: "",
                        model = document.getString("model") ?: "",
                        color = document.getString("color") ?: "",
                        licensePlate = document.getString("licensePlate") ?: "",
                        pricePerDay = document.getDouble("pricePerDay") ?: 0.0,
                        photoUrl = document.getString("photoUrl") ?: "",
                    )

                    ownerCars.add(car)
                }

                ownerCarsAdapter.notifyDataSetChanged()
                setUiState(ownerCars)
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    "Failed to load owner cars.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
    }

    /**
     * Changes UI based on Owner Cars List.
     */
    private fun setUiState(ownerCars: MutableList<Car>) {
        if (ownerCars.isEmpty()) {
            ownerCarsMessageTextView.visibility = View.VISIBLE
            ownerCarsRecyclerView.visibility = View.GONE
        } else {
            ownerCarsMessageTextView.visibility = View.GONE
            ownerCarsRecyclerView.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        loadOwnerCars()
    }
}