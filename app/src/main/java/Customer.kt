class Customer (
    login: String,
    password: String,
    name: String,
    phoneNumber: Int,
    email: String
): User(login, password, name, phoneNumber, email, "Customer"){
    fun addProduct() {}
    fun editProduct() {}
    fun deleteMyProduct() {}
    fun viewMyProducts() {}
    fun createOrder() {}
    fun viewMyOrders() {}
    fun cancelOrder() { }
}
