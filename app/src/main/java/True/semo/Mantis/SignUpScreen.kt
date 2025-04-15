package True.semo.Mantis

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

@Composable
fun SignUpScreen(
    onNavigateToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val auth = FirebaseAuth.getInstance()  // Firebase Authentication instance
    val database: DatabaseReference = FirebaseDatabase.getInstance().reference  // Firebase Realtime Database reference

    // Function to sign up the user and store additional info in Realtime Database
    fun signUpUser(email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        if (user != null) {
                            // Store the user information in Realtime Database
                            val userId = user.uid
                            val userData = hashMapOf(
                                "email" to email,
                                "uid" to userId
                            )

                            // Save user data to the "users" node in Realtime Database
                            database.child("users").child(userId).setValue(userData)
                                .addOnSuccessListener {
                                    Log.d("SignUp", "User data saved to Realtime Database")
                                    // Clear any previous error message
                                    errorMessage = null
                                    successMessage = "Account created successfully!"
                                }
                                .addOnFailureListener { e ->
                                    Log.w("SignUp", "Error saving user data", e)
                                    // Only show the error message if saving fails
                                    successMessage = null
                                    errorMessage = "Failed to save user data: ${e.message}"
                                }
                        }
                    } else {
                        // Only set the error message if the account creation failed
                        successMessage = null
                        errorMessage = task.exception?.localizedMessage ?: "Unknown error"
                    }
                }
        } else {
            errorMessage = "Please fill in all fields"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { signUpUser(email, password) }) {
            Text("Create Account")
        }
        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }
        successMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onNavigateToLogin) {
            Text("Already have an account? Login")
        }
    }
}
