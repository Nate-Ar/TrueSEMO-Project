package True.semo.Mantis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
@Composable
fun MainScreen(onLogout: () -> Unit, navigateToDirectMessages: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val userEmail = auth.currentUser?.email ?: "Unknown User"
    val username = userEmail.substringBefore("@") // Get the part before '@'

    var postText by remember { mutableStateOf(TextFieldValue("")) }
    var showPostSection by remember { mutableStateOf(false) } // Show/hide post section
    var postList by remember { mutableStateOf<List<Post>>(emptyList()) }

    val database = FirebaseDatabase.getInstance().reference

    // Fetch posts from Firebase in real-time
    LaunchedEffect(Unit) {
        database.child("posts").orderByChild("timestamp")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val posts = snapshot.children.mapNotNull { data ->
                        data.getValue(Post::class.java)
                    }.reversed() // Show newest posts at the top
                    postList = posts
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error
                }
            })
    }

    Scaffold(
        floatingActionButton = {
            IconButton(
                onClick = { showPostSection = !showPostSection }
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = "Add Post",
                    tint = Color.Red,
                    modifier = Modifier.size(48.dp)
                )
            }
        },
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Red)
            ) {
                // Top Bar - True SEMO centered
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "True SEMO",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                // Second row - Icon, Username, and Logout
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // User Icon and Username on the left
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "User Icon",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = username,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f)) // Push logout button to the right

                    // Logout Button
                    IconButton(onClick = {
                        auth.signOut()
                        onLogout()  // Call the logout function passed as parameter
                    }) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = Color.White
                        )
                    }
                }
            }
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFEEEEEE))
            ) {
//                Button(
//                    onClick = navigateToDirectMessages,
//                    modifier = Modifier.fillMaxWidth().padding(16.dp),
//                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
//                ) {
//                    Text("Go to Direct Messages", color = Color.Red)
//                }
                // Post Section Visibility
                if (showPostSection) {
                    PostSection(
                        postText = postText,
                        onPostTextChange = { postText = it },
                        onPostSubmit = {
                            if (postText.text.isNotEmpty()) {
                                val postMap = mapOf(
                                    "username" to username,
                                    "postContent" to postText.text,
                                    "timestamp" to System.currentTimeMillis()
                                )
                                database.child("posts").push().setValue(postMap)
                                postText = TextFieldValue("") // Clear input after posting
                                showPostSection = false // Hide post section after submission
                            }
                        }
                    )
                }

                // Displaying Posts
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    items(postList) { post ->
                        PostCard(post)
                    }
                }
            }
        }
    )
}

// Post Section UI
@Composable
fun PostSection(
    postText: TextFieldValue,
    onPostTextChange: (TextFieldValue) -> Unit,
    onPostSubmit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Create a Post",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = postText,
                onValueChange = onPostTextChange,
                label = { Text("What's on your mind?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onPostSubmit,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Post")
            }
        }
    }
}

// Post Card UI
@Composable
fun PostCard(post: Post) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = post.username,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = post.postContent,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatTimestamp(post.timestamp),
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

// Data Model for Post
data class Post(
    val username: String = "",
    val postContent: String = "",
    val timestamp: Long = 0
)

// Formatting Timestamp
fun formatTimestamp(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("MMM dd, yyyy hh:mm a")
    return sdf.format(java.util.Date(timestamp))
}
