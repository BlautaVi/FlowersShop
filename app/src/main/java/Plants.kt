import android.media.Image

abstract class Plants (
    name: String,
    type: String,
    price: Double,
    description: String,
    image: Image,
    seller: String
): Products(name, type, price, description, image, seller){

}
