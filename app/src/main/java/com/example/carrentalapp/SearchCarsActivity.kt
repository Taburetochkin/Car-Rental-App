package com.example.carrentalapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.location.Geocoder
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.carrentalapp.models.data.car.Car
import com.example.carrentalapp.models.data.car.CarMarker
import com.example.carrentalapp.models.data.user.UserSession
import com.example.carrentalapp.models.types.user.UserType
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import java.util.Locale
import androidx.core.graphics.createBitmap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory

class SearchCarsActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var manageBookingsImageButton: ImageButton
    private lateinit var logoutImageButton: ImageButton
    private lateinit var searchCarsByCityEditText: EditText
    private lateinit var searchButton: Button

    private lateinit var mMap: GoogleMap

    private val auth: FirebaseAuth = Firebase.auth
    private val db = Firebase.firestore

    private val markerCarMap = mutableMapOf<Marker, CarMarker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_cars)

        checkIfLoggedIn()

        bindWidgets()

        setEventListeners()

        setUpMapFragment()
    }

    private fun bindWidgets() {
        manageBookingsImageButton = findViewById<ImageButton>(R.id.manage_bookings_imageButton)
        logoutImageButton = findViewById<ImageButton>(R.id.logout_imageButton)
        searchCarsByCityEditText = findViewById<EditText>(R.id.search_cars_by_city_editText)
        searchButton = findViewById<Button>(R.id.search_button)
    }

    private fun setUpMapFragment() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true

        val defaultCity = LatLng(43.6532, -79.3832)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultCity, 12f))
    }

    private fun setEventListeners() {
        manageBookingsImageButton.setOnClickListener {
            goToScreen(ManageBookingsActivity::class.java)
        }

        logoutImageButton.setOnClickListener {
            logoutUser()
        }

        searchButton.setOnClickListener {
            val city = searchCarsByCityEditText.text.toString().trim()

            if (city.isEmpty()) {
                Toast.makeText(this, "Please enter city name.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val normalizedCity = city.lowercase()

            searchCars(normalizedCity)
        }
    }

    /**
     * Searches Cars based on typed City.
     *
     * @param city - Listing's City.
     */
    private fun searchCars(city: String) {
        mMap.clear()
        markerCarMap.clear()

        db.collection("car_listings")
            .whereEqualTo("city", city)
            .get()
            .addOnSuccessListener { documentSnapshots ->
                if (documentSnapshots.isEmpty) {
                    Toast.makeText(
                        this,
                        "No cars found in $city.",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@addOnSuccessListener
                }

                val geocoder = Geocoder(this, Locale.getDefault())
                val bounds = LatLngBounds.Builder()

                for (document in documentSnapshots) {
                    val address = document.getString("address") ?: ""
                    val fullAddress = "$address, $city"

                    val results = geocoder.getFromLocationName(fullAddress, 1)

                    if (!results.isNullOrEmpty()) {

                        val location = results[0]
                        val position = LatLng(location.latitude, location.longitude)

                        val carMarker = CarMarker(
                            id = document.id,
                            pricePerDay = document.getDouble("pricePerDay") ?: 0.0
                        )

                        val marker = mMap.addMarker(
                            MarkerOptions()
                                .position(position)
                                .icon(createPriceMarker(carMarker.formattedPricePerDay))
                        )

                        if (marker != null) {
                            markerCarMap[marker] = carMarker
                        }

                        bounds.include(position)
                    }
                }


                val update = CameraUpdateFactory.newLatLngBounds(bounds.build(), 120)
                mMap.animateCamera(update)

                setupMarkerClick()

                clearSearchBar()
            }
    }

    private fun setupMarkerClick() {
        mMap.setOnMarkerClickListener { marker ->
            val carMarker = markerCarMap[marker]
            if (carMarker != null) {
                goToCarRentScreen(carMarker)
                return@setOnMarkerClickListener true
            } else {
                return@setOnMarkerClickListener false
            }
        }
    }

    /**
     * Redirects to CarRentActivity based on Car's ID.
     *
     * @param carMarker - Instance of Car Marker.
     */
    private fun goToCarRentScreen(carMarker: CarMarker) {
        val intent = Intent(this, CarRentActivity::class.java)
        intent.putExtra("extra_car_id", carMarker.id)
        startActivity(intent)
    }

    /**
     * Creates Price Markers on the Page.
     *
     * @param priceText - Price of certain Car.
     */
    private fun createPriceMarker(priceText: String): BitmapDescriptor {
        val drawable = getDrawable(R.drawable.marker)!!
        val width = drawable.intrinsicWidth
        val height = drawable.intrinsicHeight

        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)

        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)

        val paint = Paint().apply {
            color = Color.WHITE
            textSize = 40f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }

        canvas.drawText(priceText, width / 2f, height / 2f, paint)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
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
     * Clears Search Bar.
     */
    private fun clearSearchBar() {
        searchCarsByCityEditText.setText("")
    }
}