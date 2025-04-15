package True.semo.Mantis

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send

@Composable
fun ChatScreen(userId: String) {
    val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance().reference
    val currentUser = auth.currentUser

    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var messageText by remember { mutableStateOf("") }

    val userEmail = currentUser?.email ?: "Unknown User"
    val username = userEmail.substringBefore("@") // Get the part before '@'

    // Fetch messages from Firebase for the specific user or conversation
    LaunchedEffect(userId) {
        database.child("messages").orderByChild("conversationId").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val messagesList = snapshot.children.mapNotNull { data ->
                        data.getValue(Message::class.java)
                    }.reversed() // Show newest messages at the top
                    messages = messagesList
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Chat messages display
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
        ) {
            items(messages) { message ->
                MessageCard(message)
            }
        }

        // Message input section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                label = { Text("Type a message") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = {
                if (messageText.isNotEmpty()) {
                    val newMessage = Message(username, messageText, System.currentTimeMillis(), userId)
                    database.child("messages").push().setValue(newMessage)
                    messageText = "" // Clear input field after sending
                }
            }) {
                Icon(imageVector = Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}

// Custom text styles
val customTitleStyle = TextStyle(
    color = Color.Black,
    fontWeight = FontWeight.Bold,
    fontSize = 20.sp
)

val customSubtitleStyle = TextStyle(
    color = Color.Gray,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp
)

@Composable
fun MessageCard(message: Message) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = message.username,
                style = customTitleStyle
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message.messageContent,
                style = customSubtitleStyle
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = getFormattedTimestamp(message.timestamp),
                style = customSubtitleStyle
            )
        }
    }
}

// Data class for message
data class Message(
    val username: String = "",
    val messageContent: String = "",
    val timestamp: Long = 0,
    val conversationId: String = "" // Added conversationId to link messages to specific chats
)

// Renamed timestamp function
fun getFormattedTimestamp(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("MMM dd, yyyy hh:mm a")
    return sdf.format(java.util.Date(timestamp))
}
