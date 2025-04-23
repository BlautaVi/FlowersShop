class Customer (
    login: String,
    password: String,
    name: String,
    phoneNumber: Int,
    email: String
): User(login, password, name, phoneNumber, email, "Customer")

