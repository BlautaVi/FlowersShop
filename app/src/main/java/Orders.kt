class Orders (
    var id: Int,
    var buyer: String,
    var seller: String,
    var products: List<Products>,
    var status: Boolean,
){
    fun createOrder(){}
    fun viewOrderDetails() {}
    fun cancelOrder(){}
}
