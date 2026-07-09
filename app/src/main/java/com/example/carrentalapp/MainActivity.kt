package com.example.carrentalapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.carrentalapp.models.data.user.User
import com.example.carrentalapp.models.data.user.UserSession
import com.example.carrentalapp.models.types.user.UserType
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class MainActivity : AppCompatActivity() {
    private lateinit var userEmailEditText: EditText
    private lateinit var userPasswordEditText: EditText
    private lateinit var loginErrorTextView: TextView
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button

    private val auth: FirebaseAuth = Firebase.auth
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkIfLoggedIn()

        bindWidgets()

        setEventListeners()
    }

    private fun bindWidgets() {
        userEmailEditText = findViewById<EditText>(R.id.user_email_editText)
        userPasswordEditText = findViewById<EditText>(R.id.user_password_editText)
        loginErrorTextView = findViewById<TextView>(R.id.login_error_textView)
        loginButton = findViewById<Button>(R.id.login_button)
        registerButton = findViewById<Button>(R.id.register_button)
    }

    private fun setEventListeners() {
        loginButton.setOnClickListener {
            loginUser()
        }

        registerButton.setOnClickListener {
            goToScreen(RegisterActivity::class.java)
        }
    }

    private fun goToScreen(target: Class<*>) {
        val intent = Intent(this, target)
        startActivity(intent)
    }

    /**
     * Logins User.
     * If Succeeds -> redirects to other Activities.
     * Else -> Shows Error Message.
     */
    private fun loginUser() {
        val email = userEmailEditText.text.toString().trim()
        val password = userPasswordEditText.text.toString().trim()
        if (email.isEmpty() || password.isEmpty()) {
            showErrorMessage("Enter both email and password.")
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user?.uid
                    db.collection("users")
                        .document(userId!!)
                        .get()
                        .addOnSuccessListener { documentSnapshot ->
                            if(documentSnapshot.exists()) {
                                val parsedUserType = documentSnapshot.getString("type") ?: "OWNER"
                                val normalizedUserType = if (parsedUserType == "OWNER") UserType.OWNER
                                    else UserType.RENTER

                                val loggedUser = User(
                                    id = documentSnapshot.id,
                                    firstName = documentSnapshot.getString("firstName") ?: "",
                                    lastName = documentSnapshot.getString("lastName") ?: "",
                                    email = documentSnapshot.getString("email") ?: "",
                                    type = normalizedUserType
                                )

                                UserSession.setUser(loggedUser)
                                Toast.makeText(this, "You have been logged-in.", Toast.LENGTH_SHORT).show()

                                navigateByRole(normalizedUserType)
                            } else {
                                showErrorMessage("User not found.")
                                return@addOnSuccessListener
                            }
                        }
                        .addOnFailureListener { error ->
                            showErrorMessage("Failed to read profile: ${error.message}")
                        }
                } else {
                    showErrorMessage("Incorrect email or password.")
                    return@addOnCompleteListener
                }
            }

    }

    /**
     * Sets Error Message
     *
     * @param message - Error Message.
     */
    private fun showErrorMessage(message: String) {
        userEmailEditText.setText("")
        userPasswordEditText.setText("")
        loginErrorTextView.text = message
        loginErrorTextView.visibility = View.VISIBLE
    }

    /**
     * Redirects users to other activities.
     *
     * @param type - User Type.
     */
    private fun navigateByRole(type: UserType) {
        if (type == UserType.OWNER) {
            goToScreen(OwnerCarsActivity::class.java)
        } else {
            goToScreen(SearchCarsActivity::class.java)
        }
    }

    /**
     * Redirects already Logged In User.
     *
     * @param userId - User ID.
     */
    private fun redirectLoggedInUser(userId: String) {
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if(documentSnapshot.exists()) {
                    val parsedUserType = documentSnapshot.getString("type") ?: "OWNER"
                    val normalizedUserType = if (parsedUserType == "OWNER") UserType.OWNER
                    else UserType.RENTER

                    val loggedUser = User(
                        id = documentSnapshot.id,
                        firstName = documentSnapshot.getString("firstName") ?: "",
                        lastName = documentSnapshot.getString("lastName") ?: "",
                        email = documentSnapshot.getString("email") ?: "",
                        type = normalizedUserType
                    )

                    UserSession.setUser(loggedUser)
                    Toast.makeText(
                        this,
                        "You have been logged-in.",
                        Toast.LENGTH_SHORT
                    ).show()

                    navigateByRole(normalizedUserType)
                } else {
                    Toast.makeText(
                        this,
                        "User not found.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@addOnSuccessListener
                }
            }
            .addOnFailureListener { error ->
                Toast.makeText(
                    this,
                    "Failed to read profile: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun checkIfLoggedIn() {
        if (auth.currentUser != null) {
            val userId = auth.currentUser!!.uid
            redirectLoggedInUser(userId)
        }
    }
}