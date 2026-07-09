package com.example.carrentalapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.carrentalapp.R
import com.example.carrentalapp.models.data.car.Car

class OwnerCarsAdapter(
    private val ownerCarList: MutableList<Car>
): RecyclerView.Adapter<OwnerCarsAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val carImageImageView: ImageView
        val carMainInfoTextView: TextView
        val carPricePerDayTextView: TextView
        val carLicensePlateTextView: TextView

        init {
            carImageImageView = view.findViewById<ImageView>(R.id.car_image_imageView)
            carMainInfoTextView = view.findViewById<TextView>(R.id.car_main_info_textView)
            carPricePerDayTextView = view.findViewById<TextView>(R.id.car_price_per_day_textView)
            carLicensePlateTextView = view.findViewById<TextView>(R.id.car_license_plate_textView)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): OwnerCarsAdapter.ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.owner_listing_row, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: OwnerCarsAdapter.ViewHolder, position: Int) {
        val ownerCar = ownerCarList[position]

        Glide.with(viewHolder.itemView.context)
            .load(ownerCar.photoUrl)
            .placeholder(R.drawable.ic_launcher_foreground)
            .into(viewHolder.carImageImageView)

        viewHolder.carMainInfoTextView.text = ownerCar.mainCarInfo

        viewHolder.carPricePerDayTextView.text = ownerCar.formattedPricePerDay

        viewHolder.carLicensePlateTextView.text = ownerCar.normalizedLicensePlate
    }

    override fun getItemCount() = ownerCarList.size
}