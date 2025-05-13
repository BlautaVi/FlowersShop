package Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.flowersshop.R
import com.example.flowersshop.models.ProductItem
import com.google.firebase.auth.FirebaseAuth

class ProductAdapter(
    private val productList: List<ProductItem>,
    private val onItemClick: (ProductItem) -> Unit,
    private val onAddToCartClick: (ProductItem) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    private var isListView = false

    companion object {
        private const val VIEW_TYPE_GRID = 0
        private const val VIEW_TYPE_LIST = 1
    }

    fun toggleViewType() {
        isListView = !isListView
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (isListView) VIEW_TYPE_LIST else VIEW_TYPE_GRID
    }

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.productName)
        val priceTextView: TextView = itemView.findViewById(R.id.productPrice)
        val quantityTextView: TextView? = itemView.findViewById(R.id.productQuantity)
        val productImage: ImageView = itemView.findViewById(R.id.productImage)
        val editButton: ImageButton? = itemView.findViewById(R.id.edit_button)
        val addToCartButton: ImageButton? = itemView.findViewById(R.id.addToCart_b)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val layoutId = if (viewType == VIEW_TYPE_GRID) R.layout.item_product_grid else R.layout.item_product_list
        val view = LayoutInflater.from(parent.context)
            .inflate(layoutId, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        holder.itemView.tag = product

        holder.nameTextView.text = if (!product.isAvailable()) "Товар закінчився...." else product.name
        holder.priceTextView.text = "${product.price} грн"
        holder.quantityTextView?.let {
            it.text = "Наявність: ${product.availableQuantity}"
        }

        if (product.photoUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(product.photoUrl)
                .into(holder.productImage)
        } else {
            holder.productImage.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        holder.itemView.setOnClickListener {
            onItemClick(product)
        }

        if (holder.editButton != null && holder.addToCartButton != null) {
            if (product.userId == currentUserId) {
                holder.editButton.visibility = View.VISIBLE
                holder.addToCartButton.visibility = View.GONE
                holder.editButton.setOnClickListener { onItemClick(product) }
            } else {
                holder.editButton.visibility = View.GONE
                if (!product.isAvailable()) {
                    holder.addToCartButton.visibility = View.GONE
                } else {
                    holder.addToCartButton.visibility = View.VISIBLE
                    holder.addToCartButton.setOnClickListener { onAddToCartClick(product) }
                }
            }
        }
    }
    override fun getItemCount(): Int = productList.size
}