package com.example.carrentalapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.carrentalapp.models.data.user.User
import com.example.carrentalapp.models.data.user.UserSession
import com.example.carrentalapp.models.types.user.UserType
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore

class RegisterActivity : AppCompatActivity() {
    private lateinit var userFirstNameEditText: EditText
    private lateinit var userLastNameEditText: EditText
    private lateinit var userEmailEditText: EditText
    private lateinit var userPasswordEditText: EditText
    private lateinit var userTypeRadioGroup: RadioGroup
    private lateinit var userOwnerRadioButton: RadioButton
    private lateinit var userRenterRadioButton: RadioButton
    private lateinit var registerButton: Button
    private lateinit var loginButton: Button

    private val auth = Firebase.auth
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        bindWidgets()

        setEventListeners()
    }

    private fun bindWidgets() {
        userFirstNameEditText = findViewById<EditText>(R.id.user_first_name_editText)
        userLastNameEditText = findViewById<EditText>(R.id.user_last_name_editText)
        userEmailEditText = findViewById<EditText>(R.id.user_email_editText)
        userPasswordEditText = findViewById<EditText>(R.id.user_password_editText)
        userTypeRadioGroup = findViewById<RadioGroup>(R.id.user_type_radioGroup)
        userOwnerRadioButton = findViewById<RadioButton>(R.id.user_owner_radioButton)
        userRenterRadioButton = findViewById<RadioButton>(R.id.user_renter_radioButton)
        registerButton = findViewById<Button>(R.id.register_button)
        loginButton = findViewById<Button>(R.id.login_button)
    }

    private fun setEventListeners() {
        registerButton.setOnClickListener {
            registerUser()
        }

        loginButton.setOnClickListener {
            goToScreen(MainActivity::class.java)
        }
    }

    /**
     * Registers User and adds User Instance in Firestore.
     */
    private fun registerUser() {
        val firstName = userFirstNameEditText.text.toString().trim()
        if (firstName.isEmpty()) {
            Toast.makeText(
                this,
                "Please enter your first name.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val lastName = userLastNameEditText.text.toString().trim()
        if (lastName.isEmpty()) {
            Toast.makeText(
                this,
                "Please enter your last name.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val email = userEmailEditText.text.toString().trim()
        if (email.isEmpty()) {
            Toast.makeText(
                this,
                "Please enter your email.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val password = userPasswordEditText.text.toString().trim()
        if (password.length < 6) {
            Toast.makeText(
                this,
                "Password must be at least 6 characters.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val chosenRadioButtonId = userTypeRadioGroup.checkedRadioButtonId
        if (chosenRadioButtonId == -1) {
            Toast.makeText(
                this,
                "Choose user type",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val chosenUserType = when(chosenRadioButtonId) {
            userOwnerRadioButton.id -> UserType.OWNER
            else -> UserType.RENTER
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user?.uid

                    val profileData = hashMapOf(
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "email" to email,
                        "type" to chosenUserType,
                        "createdAt" to FieldValue.serverTimestamp()
                    )

                    db.collection("users")
                        .document(userId!!)
                        .set(profileData)
                        .addOnSuccessListener {
                            val newUser = User(
                                id = userId,
                                firstName = firstName,
                                lastName = lastName,
                                email = email,
                                type = chosenUserType
                            )

                            UserSession.setUser(newUser)

                            Toast.makeText(
                                this,
                                "Account created!",
                                Toast.LENGTH_LONG
                            ).show()

                            if (chosenUserType == UserType.OWNER) {
                                goToScreen(OwnerCarsActivity::class.java)
                            } else {
                                goToScreen(SearchCarsActivity::class.java)
                            }
                        }
                        .addOnFailureListener { error ->
                            Toast.makeText(
                                this,
                                "Failed to save profile: ${error.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    Toast.makeText(
                        this,
                        "Fail to create.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun goToScreen(target: Class<*>) {
        val intent = Intent(this, target)
        startActivity(intent)
    }
}