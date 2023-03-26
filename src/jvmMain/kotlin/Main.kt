import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FileInputStream


@Composable
@Preview
fun App(count: MutableState<String>) {
    var text by remember { mutableStateOf("Hello, World!") }

    MaterialTheme {
        Button(onClick = {
            text = "Hello, Desktop!"
        }) {
            Text(count.value)
        }
    }
}

fun main() = application {

   // val printer = Printer()
    val serviceAccount = FileInputStream("src/jvmMain/resources/serviceAccountKey.json")

    val options = FirebaseOptions.Builder()
        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
        .setDatabaseUrl("https://app-easter-default-rtdb.firebaseio.com")
        .build()
    FirebaseApp.initializeApp(options)

    val ref = FirebaseDatabase.getInstance().getReference("applications")

    val countapplications = remember {
        mutableStateOf("")
    }
    ref.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot?) {
            countapplications.value = snapshot?.children?.count().toString()
        }

        override fun onCancelled(error: DatabaseError?) {

        }

    })

    Window(onCloseRequest = ::exitApplication) {
        App(countapplications)
        rememberCoroutineScope().launch(Dispatchers.IO){
           // printer.printDocument("sadasdasdasda")
        }
    }
}
