package com.example.flowersshop.models

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.flowersshop.R
import com.google.firebase.auth.FirebaseAuth

class ProductAdapter(
    private val productList: List<ProductItem>,
    private val onItemClick: (ProductItem) -> Unit,
    private val onAddToCartClick: (ProductItem) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.productName)
        val priceTextView: TextView = itemView.findViewById(R.id.productPrice)
        val productImage: ImageView = itemView.findViewById(R.id.productImage)
        val editButton: Button? = itemView.findViewById(R.id.edit_button)
        val addToCartButton: Button? = itemView.findViewById(R.id.addToCart_b)

        init {
            itemView.setOnClickListener {
                val product = itemView.tag as? ProductItem
                product?.let {
                    (it)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        holder.itemView.tag = product

        holder.nameTextView.text = product.name
        holder.priceTextView.text = "${product.price} грн"

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
                holder.addToCartButton.visibility = View.VISIBLE
                holder.addToCartButton.setOnClickListener { onAddToCartClick(product) }
            }
        }
    }

    override fun getItemCount(): Int = productList.size
}