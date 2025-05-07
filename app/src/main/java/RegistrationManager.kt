package com.example.flowersshop
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.regex.Pattern

class RegistrationManager(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val context: AppCompatActivity
) {
    private val PHONE_PATTERN = Pattern.compile("^\\+380d{9}\$")
    private val TAG = "RegistrationManager"
    fun registerUser(email: String, password: String, name: String, phone: String, address: String, onSuccess: () -> Unit) {
        if (!validateInput(email, password, name, phone, address)) return

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(context) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Реєстрація успішна, UID: ${auth.currentUser?.uid}")
                    Toast.makeText(context, "Успішно зареєстровано!", Toast.LENGTH_SHORT).show()
                    saveUserData(email, name, phone, address, onSuccess)
                } else {
                    val exception = task.exception
                    Log.e(TAG, "Помилка реєстрації: ${exception?.message}", exception)
                    Toast.makeText(context, "Помилка реєстрації: ${exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun validateInput(email: String, password: String, name: String, phone: String, address: String): Boolean {
        if (email.isEmpty() || password.isEmpty() || name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(context, "Заповніть всі поля!", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(context, "Некоректний формат email", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.length < 6) {
            Toast.makeText(context, "Пароль має бути щонайменше 6 символів", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!PHONE_PATTERN.matcher(phone).matches()) {
            Toast.makeText(context, "Номер телефону має бути у форматі +380xxxxxxxxx", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun saveUserData(email: String, name: String, phone: String, address: String, onSuccess: () -> Unit) {
        val user = hashMapOf(
            "name" to name,
            "email" to email,
            "phoneNumber" to phone,
            "address" to address
        )
        Log.d(TAG, "Спроба запису даних у Firestore: $user")
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users")
                .document(userId)
                .set(user)
                .addOnSuccessListener {
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        context,
                        "Помилка збереження даних: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            Toast.makeText(context, "Помилка: користувач не автентифікований", Toast.LENGTH_SHORT)
                .show()
        }
    }
}