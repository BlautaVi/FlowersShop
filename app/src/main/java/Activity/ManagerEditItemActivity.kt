package Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.flowersshop.models.ProductItem
import com.google.firebase.firestore.FirebaseFirestore
import com.example.flowersshop.R

class ManagerEditItemActivity : AppCompatActivity() {

    private lateinit var itemPhoto: ImageView
    private lateinit var enterName: EditText
    private lateinit var enterType: EditText
    private lateinit var enterPrice: EditText
    private lateinit var enterDesc: EditText
    private lateinit var changeButton: Button
    private lateinit var deleteButton: Button
    private lateinit var product: ProductItem
    private lateinit var quantityEditText: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manager_edit_item)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        itemPhoto = findViewById(R.id.item_photo)
        enterName = findViewById(R.id.man_enter_name)
        enterType = findViewById(R.id.man_enter_type)
        enterPrice = findViewById(R.id.man_enter_price)
        enterDesc = findViewById(R.id.man_enter_decs)
        changeButton = findViewById(R.id.man_change_b)
        deleteButton = findViewById(R.id.man_delete_b)
        product = intent.getParcelableExtra<ProductItem>("product") ?: run {
            Toast.makeText(this, "Товар не знайдено", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        quantityEditText = findViewById(R.id.man_enter_quantity)
        quantityEditText.setText(product.availableQuantity.toString())
        val ShowAllButton = findViewById<Button>(R.id.man_show_all_items_b)
        if (product == null) {
            Toast.makeText(this, "Товар не знайдено", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        Glide.with(this)
            .load(product.photoUrl)
            .placeholder(R.drawable.icon)
            .into(itemPhoto)
        enterName.setText(product.name)
        enterType.setText(product.type)
        enterPrice.setText(product.price?.toString() ?: "")
        enterDesc.setText(product.description)

        changeButton.setOnClickListener {
            val updatedName = enterName.text.toString()
            val updatedType = enterType.text.toString()
            val updatedPriceStr = enterPrice.text.toString()
            val updatedDesc = enterDesc.text.toString()
            val updatedQuantity = quantityEditText.text.toString().toIntOrNull() ?: 0

            if (updatedName.isEmpty() || updatedType.isEmpty() || updatedPriceStr.isEmpty() || updatedDesc.isEmpty() || updatedQuantity < 0) {
                Toast.makeText(this, "Заповніть усі поля коректно, кількість не може бути від’ємною", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updatedPrice = updatedPriceStr.toDoubleOrNull()
            if (updatedPrice == null) {
                Toast.makeText(this, "Ціна має бути числом", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updatedProduct = hashMapOf(
                "name" to updatedName,
                "type" to updatedType,
                "price" to updatedPrice,
                "description" to updatedDesc,
                "photoUrl" to product.photoUrl,
                "userId" to product.userId,
                "availableQuantity" to updatedQuantity
            )

            FirebaseFirestore.getInstance().collection("items").document(product.id)
                .set(updatedProduct)
                .addOnSuccessListener {
                    Toast.makeText(this, "Товар оновлено", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Помилка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        deleteButton.setOnClickListener {
            FirebaseFirestore.getInstance().collection("items").document(product.id)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Товар видалено", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Помилка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
        ShowAllButton.setOnClickListener{
            val intent = Intent(this, MainPageActivity::class.java)
            startActivity(intent)
        }
    }
}