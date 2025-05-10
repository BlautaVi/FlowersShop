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
import com.example.flowersshop.ProductEditor
import com.example.flowersshop.models.ProductItem
import com.example.flowersshop.R
import com.google.firebase.firestore.FirebaseFirestore

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
        quantityEditText = findViewById(R.id.man_enter_quantity)
        val showAllButton = findViewById<Button>(R.id.man_show_all_items_b)

        product = intent.getParcelableExtra("product") ?: run {
            Toast.makeText(this, "Товар не знайдено", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val productEditor = ProductEditor(FirebaseFirestore.getInstance(), this, product)

        quantityEditText.setText(product.availableQuantity.toString())

        Glide.with(this).load(product.photoUrl).placeholder(R.drawable.icon).into(itemPhoto)
        enterName.setText(product.name)
        enterType.setText(product.type)
        enterPrice.setText(product.price.toString())
        enterDesc.setText(product.description)

        changeButton.setOnClickListener {
            val updatedName = enterName.text.toString()
            val updatedType = enterType.text.toString()
            val updatedPriceStr = enterPrice.text.toString()
            val updatedDesc = enterDesc.text.toString()
            val updatedQuantity = quantityEditText.text.toString().toIntOrNull() ?: 0

            productEditor.updateProduct(updatedName, updatedType, updatedPriceStr, updatedDesc, updatedQuantity) {
                finish()
            }
        }

        deleteButton.setOnClickListener {
            productEditor.deleteProduct {
                finish()
            }
        }

        showAllButton.setOnClickListener {
            startActivity(Intent(this, MainPageActivity::class.java))
        }
    }
}