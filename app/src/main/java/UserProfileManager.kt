package com.example.flowersshop
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class UserProfileManager(
    private val db: FirebaseFirestore,
    private val userId: String?,
    private val context: AppCompatActivity,
    private val nameText: EditText,
    private val addressText: EditText,
    private val phoneText: EditText,
    private val onCityLoaded: (String) -> Unit,
    private val onEmptySpinner: () -> Unit
) {
    fun loadUserData() {
        if (userId == null) return
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("name") ?: ""
                    val address = document.getString("address") ?: ""
                    val phone = document.getString("phoneNumber") ?: ""
                    nameText.setText(name)
                    addressText.setText(address)
                    phoneText.setText(phone)
                    val city = extractCityFromAddress(address)
                    if (city.isNotEmpty()) {
                        onCityLoaded(city)
                    } else {
                        Toast.makeText(context, "Не вдалося визначити місто з адреси", Toast.LENGTH_SHORT).show()
                        onEmptySpinner()
                    }
                } else {
                    Toast.makeText(context, "Дані користувача не знайдено", Toast.LENGTH_SHORT).show()
                    onEmptySpinner()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Помилка при завантаженні профілю: \${e.message}", Toast.LENGTH_SHORT).show()
                onEmptySpinner()
            }
    }

    private fun extractCityFromAddress(address: String): String {
        if (address.isEmpty()) return ""
        val parts = address.split(",").map { it.trim() }
        val cityPart = parts.find { part ->
            !part.contains("вул.", ignoreCase = true) &&
                    !part.contains("просп.", ignoreCase = true) &&
                    !part.matches(Regex(".*\\d+.*"))
        } ?: parts.lastOrNull() ?: ""
        return cityPart.replace(Regex("^(м\\.|с\\.)\\s*", RegexOption.IGNORE_CASE), "").trim()
    }
}
