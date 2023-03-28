import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import java.io.FileInputStream

object FirebaseConfig {
    val serviceAccount = FileInputStream("src/jvmMain/resources/serviceAccountKey.json")

    val options = FirebaseOptions.Builder()
        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
        .setDatabaseUrl("https://app-easter-default-rtdb.firebaseio.com")
        .build()

    fun initialize(){
        FirebaseApp.initializeApp(options)
    }
}