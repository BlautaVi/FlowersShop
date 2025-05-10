package com.example.flowersshop
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.flowersshop.models.CartItem
class CartItemsAdapter(
    private val context: Context,
    private val cartItems: MutableList<CartItem>,
    private val cartManager: CartManager
) : BaseAdapter() {
    override fun getCount(): Int = cartItems.size
    override fun getItem(position: Int): Any = cartItems[position]
    override fun getItemId(position: Int): Long = position.toLong()
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.cart_item_simple_layout, parent, false)
        val cartItem = cartItems[position]
        val itemImage = view.findViewById<ImageView>(R.id.cart_item_image)
        val itemType = view.findViewById<TextView>(R.id.cart_item_type)
        val itemName = view.findViewById<TextView>(R.id.cart_item_name)
        val itemPrice = view.findViewById<TextView>(R.id.cart_item_price)
        val itemQuantity = view.findViewById<TextView>(R.id.cart_item_quantity)
        val deleteButton = view.findViewById<ImageButton>(R.id.delete_cart_item_button)
        itemType.text = "Вид: ${cartItem.productType}"
        itemName.text = cartItem.productName
        itemPrice.text = "Ціна: ${cartItem.productPrice} грн"
        itemQuantity.text = "Кількість: ${cartItem.quantity}"
        if (cartItem.productPhotoUrl.isNotEmpty()) {
            Glide.with(context)
                .load(cartItem.productPhotoUrl)
                .placeholder(R.drawable.icon)
                .error(R.drawable.icon)
                .into(itemImage)
        } else {
            itemImage.setImageResource(R.drawable.icon)
            Log.w("CartItemPhoto", "Photo URL is empty for product: ${cartItem.productName}")
        }
        deleteButton.setOnClickListener {
            cartManager.removeCartItem(cartItem)
        }
        return view
    }
}
