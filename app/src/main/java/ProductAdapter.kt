package com.example.flowersshop.models

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.flowersshop.ActivityManagerEditItem
import com.example.flowersshop.Item_page
import com.example.flowersshop.R

class ProductAdapter(
    private val productList: List<ProductItem>,
    private val isManager: Boolean
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.productImage)
        val name: TextView = itemView.findViewById(R.id.productName)
        val price: TextView = itemView.findViewById(R.id.productPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun getItemCount() = productList.size

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        holder.name.text = product.name
        holder.price.text = "${product.price ?: 0.0} грн"
        Glide.with(holder.itemView.context)
            .load(product.photoUrl)
            .placeholder(R.drawable.icon)
            .into(holder.image)

        holder.itemView.setOnClickListener {
            val intent = if (isManager) {
                Intent(holder.itemView.context, ActivityManagerEditItem::class.java)
            } else {
                Intent(holder.itemView.context, Item_page::class.java)
            }
            intent.putExtra("product", product)
            holder.itemView.context.startActivity(intent)
        }
    }
}