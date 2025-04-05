import android.media.Image

abstract class Products (
    var name: String,
    var type: String,
    var price: Double,
    var description: String,
    var image: Image,
    var seller: String
){
    fun addProduct(){}
    fun editProduct(){}
    fun deleteProduct(){}
    fun viewProductDetails(){}
    abstract fun isAvailable(): Boolean
}