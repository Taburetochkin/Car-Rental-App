package com.example.carrentalapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.carrentalapp.R
import com.example.carrentalapp.models.data.booking.Booking
import com.example.carrentalapp.models.data.user.UserSession
import com.example.carrentalapp.models.types.user.UserType
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class BookingsAdapter(
    private val bookingList: MutableList<Booking>,
    private val onCancelClick: (Booking) -> Unit
) : RecyclerView.Adapter<BookingsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val carImageImageView: ImageView
        val carMainInfoTextView: TextView
        val carPricePerDayTextView: TextView
        val carLicensePlateTextView: TextView
        val carRenterTextView: TextView
        val carConfirmationCodeTextView: TextView
        val carLocationTextView: TextView
        val carBookingDatesTextView: TextView
        val cancelBookingButton: Button

        val carRenterLinearLayout: LinearLayout

        init {
            carImageImageView = view.findViewById<ImageView>(R.id.car_image_imageView)
            carMainInfoTextView = view.findViewById<TextView>(R.id.car_main_info_textView)
            carPricePerDayTextView = view.findViewById<TextView>(R.id.car_price_per_day_textView)
            carLicensePlateTextView = view.findViewById<TextView>(R.id.car_license_plate_textView)
            carRenterTextView = view.findViewById<TextView>(R.id.car_renter_textView)
            carConfirmationCodeTextView = view.findViewById<TextView>(R.id.car_confirmation_code_textView)
            carLocationTextView = view.findViewById<TextView>(R.id.car_location_textView)
            carBookingDatesTextView = view.findViewById<TextView>(R.id.car_booking_dates_textView)
            cancelBookingButton = view.findViewById<Button>(R.id.cancel_booking_button)

            carRenterLinearLayout = view.findViewById<LinearLayout>(R.id.car_renter_linearLayout)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingsAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.owner_booking_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: BookingsAdapter.ViewHolder, position: Int) {
        val booking = bookingList[position]
        val car = booking.car

        Glide.with(viewHolder.itemView.context)
            .load(car.photoUrl)
            .placeholder(R.drawable.ic_launcher_foreground)
            .into(viewHolder.carImageImageView)

        viewHolder.carMainInfoTextView.text = car.mainCarInfo
        viewHolder.carPricePerDayTextView.text = car.formattedPricePerDay
        viewHolder.carLicensePlateTextView.text = car.normalizedLicensePlate

        viewHolder.carRenterTextView.text = booking.renterFullName

        viewHolder.carConfirmationCodeTextView.text = booking.confirmationCode.toString()

        viewHolder.carLocationTextView.text = booking.formattedLocation

        viewHolder.carBookingDatesTextView.text = booking.formattedBookingDates

        viewHolder.cancelBookingButton.setOnClickListener {
            onCancelClick(booking)
        }

        val isOwner = Firebase.auth.currentUser != null
                && Firebase.auth.currentUser?.uid == car.ownerId
                && UserSession.getUser()!!.type == UserType.OWNER

        val isRenter = Firebase.auth.currentUser != null
                && UserSession.getUser()!!.type == UserType.RENTER

        if(isOwner) {
            viewHolder.carRenterLinearLayout.visibility = View.VISIBLE
            viewHolder.carLocationTextView.visibility = View.GONE
        }

        if(isRenter) {
            viewHolder.carRenterLinearLayout.visibility = View.GONE
            viewHolder.carLocationTextView.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int = bookingList.size
}
