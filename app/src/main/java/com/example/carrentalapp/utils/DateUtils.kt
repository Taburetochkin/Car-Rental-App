package com.example.carrentalapp.utils

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Utility object for handling date parsing, formatting, and normalization.
 */
object DateUtils {
    private val inputFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    /**
     * Converts "dd-MM-yyyy" into Firestore Timestamp.
     *
     * @param dateString Raw date string entered by the user.
     * @return A valid Timestamp if parsing succeeds, or `null`
     */
    fun convertToTimestamp(dateString: String): Timestamp? {
        inputFormatter.isLenient = false

        val date: Date = inputFormatter.parse(dateString) ?: return null

        return Timestamp(date)
    }

    /**
     * Normalizes a human-entered date string into strict dd-MM-yyyy format.
     * Accepts separators: "-", ".", "/".
     *
     * @param input Raw user-entered date string.
     * @return Normalized string in dd-MM-yyyy format.
     */
    fun normalizeDate(input: String): String {
        val parts = input.split("-", ".", "/")

        if (parts.size != 3) return input

        val d = parts[0].padStart(2, '0')
        val m = parts[1].padStart(2, '0')
        val y = parts[2]

        return "$d-$m-$y"
    }

    /**
     * Converts a Firestore Timestamp into a human-readable date string.
     *
     * @param timestamp - Timestamp to convert.
     * @return formatted date string.
     */
    fun formatTimestamp(timestamp: Timestamp): String {
        val date = timestamp.toDate()

        val timestampFormatter = SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH)
        timestampFormatter.timeZone = TimeZone.getDefault()

        return timestampFormatter.format(date)
    }
}