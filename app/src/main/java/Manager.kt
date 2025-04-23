class Manager (
    login: String,
    password: String,
    name: String,
    phoneNumber: Int,
    email: String
): User(login, password, name, phoneNumber, email, "Manager")