package Activity

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.flowersshop.ProductEditor
import com.example.flowersshop.models.ProductItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.flowersshop.R

class CustomerEditItemActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var product: ProductItem
    private lateinit var productEditor: ProductEditor
    private lateinit var quantityEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_edit_item)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        product = intent.getParcelableExtra("product") ?: run {
            Toast.makeText(this, "Товар не знайдено", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        productEditor = ProductEditor(db, this, product)

        quantityEditText = findViewById(R.id.customer_enter_quantity)
        quantityEditText.setText(product.availableQuantity.toString())

        val currentUser = auth.currentUser
        if (currentUser == null || currentUser.uid != product.userId) {
            Toast.makeText(this, "Ви не можете редагувати цей товар", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val itemPhoto = findViewById<ImageView>(R.id.item_photo)
        val enterName = findViewById<EditText>(R.id.customer_enter_name)
        val enterType = findViewById<EditText>(R.id.customer_enter_type)
        val enterPrice = findViewById<EditText>(R.id.customer_enter_price)
        val enterDesc = findViewById<EditText>(R.id.customer_enter_desc)
        val updateButton = findViewById<Button>(R.id.customer_update_b)
        val deleteButton = findViewById<Button>(R.id.customer_delete_b)
        val backBtn = findViewById<ImageButton>(R.id.back_b_confirmed)

        backBtn.setOnClickListener { finish() }

        Glide.with(this)
            .load(product.photoUrl)
            .placeholder(R.drawable.rounded_image_background)
            .apply(RequestOptions().transform(RoundedCorners(32)))
            .into(itemPhoto)

        enterName.setText(product.name)
        enterType.setText(product.type)
        enterPrice.setText(product.price.toString())
        enterDesc.setText(product.description)

        updateButton.setOnClickListener {
            val updatedName = enterName.text.toString().trim()
            val updatedType = enterType.text.toString().trim()
            val updatedPrice = enterPrice.text.toString()
            val updatedDesc = enterDesc.text.toString().trim()
            val updatedQuantity = quantityEditText.text.toString().toIntOrNull() ?: 0

            productEditor.updateProduct(updatedName, updatedType, updatedPrice, updatedDesc, updatedQuantity) {
                finish()
            }
        }
        deleteButton.setOnClickListener {
            productEditor.deleteProduct {
                finish()
            }
        }
    }
}