class Manager (
    login: String,
    password: String,
    name: String,
    phoneNumber: Int,
    email: String
): User(login, password, name, phoneNumber, email, "Manager"){
    fun viewAllProducts() {}
    fun removeProduct() {}
    fun viewAllOrders(){}
    fun confirmOrder() {}
    fun deleteOrder() {}
    fun addProduct() {}
    fun viewOrderDetails() {}
}