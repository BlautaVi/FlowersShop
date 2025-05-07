package Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.flowersshop.Order
import com.example.flowersshop.R

class OrderAdapter(
    private val orders: List<Order>,
    private val showUserId: Boolean = false,
    private val onOrderClick: (Order) -> Unit
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val orderSummary: TextView = itemView.findViewById(R.id.order_summary)

        fun bind(order: Order) {
            val summary = if (showUserId) {
                "Користувач: ${order.userId ?: "Невідомий"} | Замовлення від ${order.getFormattedOrderDate()} - ${order.totalPrice} грн"
            } else {
                "Замовлення від ${order.getFormattedOrderDate()} - ${order.totalPrice} грн"
            }
            orderSummary.text = summary
            itemView.setOnClickListener { onOrderClick(order) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    override fun getItemCount(): Int = orders.size
}