package True.semo.Mantis

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener


@Composable
fun DirectMessageScreen(navigateToChatScreen: (String) -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val currentUserId = currentUser?.uid ?: return // Use Firebase UID for uniqueness
    val database = FirebaseDatabase.getInstance().reference

    var conversations by remember { mutableStateOf<List<Conversation>>(emptyList()) }

    // Fetch active conversations for the current user
    LaunchedEffect(Unit) {
        database.child("conversations")
            .orderByChild("participants/$currentUserId")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val convos = snapshot.children.mapNotNull { data ->
                        data.getValue(Conversation::class.java)
                    }
                    conversations = convos
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
        // Display active conversations
        if (conversations.isEmpty()) {
            Text("No active conversations. Start a new conversation!")
            Spacer(modifier = Modifier.height(16.dp))
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
        ) {
            items(conversations) { conversation ->
                ConversationCard(conversation) {
                    navigateToChatScreen(conversation.id) // Navigate to ChatScreen with conversation ID
                }
            }
        }

        // Button to create new conversation (start a direct message)
        Button(
            onClick = { createNewConversation(currentUserId, database) },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
        ) {
            Text("Start New Conversation", color = Color.White)
        }
    }
}

// Function to create a new conversation
fun createNewConversation(currentUserId: String, database: DatabaseReference) {
    // Let's assume the user selects a friend (this can be done through another screen where users are listed)
    val otherUserId = "someOtherUserId" // This should come from the user's selection

    // Generate conversation ID based on the combination of both user IDs
    val conversationId = if (currentUserId < otherUserId) {
        "$currentUserId-$otherUserId"
    } else {
        "$otherUserId-$currentUserId"
    }

    val newConversation = Conversation(
        id = conversationId,
        participants = mapOf(
            currentUserId to true,
            otherUserId to true
        ),
        timestamp = System.currentTimeMillis()
    )

    // Store the new conversation in the database
    database.child("conversations").child(conversationId).setValue(newConversation)
}

// Conversation data model
data class Conversation(
    val id: String = "",
    val participants: Map<String, Boolean> = emptyMap(),
    val timestamp: Long = 0
)

@Composable
fun ConversationCard(conversation: Conversation, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Conversation ID: ${conversation.id}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Participants: ${conversation.participants.keys.joinToString(", ")}",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Last Updated: ${formatTimestamp(conversation.timestamp)}",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}
