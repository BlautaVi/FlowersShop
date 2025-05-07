package Activity

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import android.util.Patterns
import java.util.regex.Pattern
import com.example.flowersshop.R
class RegistrationActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var registerBtn: Button
    val db = Firebase.firestore
    private val PHONE_PATTERN = Pattern.compile("^\\+380\\d{9}$")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registration)
        auth = FirebaseAuth.getInstance()
        email = findViewById(R.id.enter_login)
        password = findViewById(R.id.enter_passwd2)
        registerBtn = findViewById(R.id.reg_b)
        val name = findViewById<EditText>(R.id.enter_name)
        val phoneNum = findViewById<EditText>(R.id.enter_phone)
        val customers_adress = findViewById<EditText>(R.id.enter_adress)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        registerBtn.setOnClickListener {
            val emailText = email.text.toString().trim()
            val passwordText = password.text.toString().trim()
            val nameText = name.text.toString().trim()
            val phoneText = phoneNum.text.toString().trim()
            val addressText = customers_adress.text.toString().trim()

            if (emailText.isEmpty() || passwordText.isEmpty() || nameText.isEmpty() || phoneText.isEmpty() || addressText.isEmpty()) {
                Toast.makeText(this, "Заповніть всі поля!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
                Toast.makeText(this, "Некоректний формат email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (passwordText.length < 6) {
                Toast.makeText(this, "Пароль має бути щонайменше 6 символів", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            if (!PHONE_PATTERN.matcher(phoneText).matches()) {
                Toast.makeText(this, "Номер телефону має бути у форматі +380xxxxxxxxx", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerUser(emailText, passwordText) {
                val user = hashMapOf(
                    "name" to nameText,
                    "email" to emailText,
                    "phoneNumber" to phoneText,
                    "address" to addressText
                )
                Log.d(TAG, "Спроба запису даних у Firestore: $user")
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    db.collection("users")
                        .document(userId)
                        .set(user)
                        .addOnSuccessListener {
                            startActivity(Intent(this, MainPageActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                "Помилка збереження даних: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    Toast.makeText(
                        this,
                        "Помилка: користувач не автентифікований",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun registerUser(email: String, password: String, onSuccess: () -> Unit) {
        Log.d(TAG, "Спроба реєстрації: email=$email, password=$password")
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Реєстрація успішна, UID: ${auth.currentUser?.uid}")
                    Toast.makeText(this, "Успішно зареєстровано!", Toast.LENGTH_SHORT).show()
                    onSuccess()
                } else {
                    val exception = task.exception
                    Log.e(TAG, "Помилка реєстрації: ${exception?.message}", exception)
                    Toast.makeText(this, "Помилка реєстрації: ${exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}