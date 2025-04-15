package True.semo.Mantis
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import True.semo.Mantis.ui.theme.TrueSemoTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrueSemoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val authViewModel: AuthViewModel = viewModel()
                    val context = LocalContext.current
                    val auth = Firebase.auth

                    NavHost(navController = navController, startDestination = "login") {
                        // Login Screen
                        composable("login") {
                            LoginScreen(
                                onLoginClick = { email, password ->
                                    authViewModel.login(email, password) { success ->
                                        if (success) {
                                            // Navigate to main screen after login
                                            Toast.makeText(
                                                context,
                                                "Login Successful",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            navController.navigate("main") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Login Failed",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                },
                                onNavigateToSignUp = { navController.navigate("signUp") }
                            )
                        }

                        // Sign-Up Screen
                        composable("signUp") {
                            SignUpScreen(
                                onNavigateToLogin = { navController.navigate("login") }
                            )
                        }

                        // Main Screen with Logout Handling
                        composable("main") {
                            MainScreen(
                                onLogout = {
                                    auth.signOut() // Sign out the user
                                    navController.navigate("login") {
                                        popUpTo("main") { inclusive = true } // Clear backstack
                                    }
                                    Toast.makeText(context, "Logged Out", Toast.LENGTH_SHORT).show()
                                },
                                navigateToDirectMessages = {
                                    navController.navigate("directMessages")
                                }
                            )
                        }
//                        composable("directMessages") {
//                            DirectMessageScreen()
//                        }
                        composable("chat/{userId}") { backStackEntry ->
                            val userId = backStackEntry.arguments?.getString("userId")
                            if (userId != null) {
                                ChatScreen(userId = userId) // Pass userId to ChatScreen
                            }
                        }
                    }
                }
            }
        }
    }
}
