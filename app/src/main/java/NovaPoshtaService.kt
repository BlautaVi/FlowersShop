package com.example.flowersshop
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONObject

class NovaPoshtaService(
    private val requestQueue: RequestQueue,
    private val apiKey: String,
    private val progressBar: ProgressBar,
    private val context: AppCompatActivity,
    private val spinner: Spinner
) {
    fun loadWarehouses(city: String) {
        progressBar.visibility = View.VISIBLE
        getCityRef(city) { cityRef ->
            if (cityRef != null) {
                getWarehouses(cityRef) { warehouses ->
                    progressBar.visibility = View.GONE
                    if (warehouses.isNotEmpty()) {
                        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, warehouses)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spinner.adapter = adapter
                        spinner.isEnabled = true
                    } else {
                        Toast.makeText(context, "Відділення не знайдено", Toast.LENGTH_SHORT).show()
                        setEmptySpinner()
                    }
                }
            } else {
                progressBar.visibility = View.GONE
                Toast.makeText(context, "Не знайдено місто", Toast.LENGTH_SHORT).show()
                setEmptySpinner()
            }
        }
    }
    private fun getCityRef(cityName: String, callback: (String?) -> Unit) {
        val requestBody = JSONObject().apply {
            put("apiKey", apiKey)
            put("modelName", "Address")
            put("calledMethod", "searchSettlements")
            put("methodProperties", JSONObject().apply {
                put("CityName", cityName)
                put("Limit", 1)
            })
        }

        val request = JsonObjectRequest(
            Request.Method.POST, "https://api.novaposhta.ua/v2.0/json/", requestBody,
            { response ->
                try {
                    val dataArray = response.getJSONArray("data")
                    if (dataArray.length() > 0) {
                        val firstData = dataArray.getJSONObject(0)
                        val settlements = firstData.getJSONArray("Addresses")
                        if (settlements.length() > 0) {
                            val deliveryCity = settlements.getJSONObject(0).getString("DeliveryCity")
                            callback(deliveryCity)
                        } else {
                            callback(null)
                        }
                    } else {
                        callback(null)
                    }
                } catch (e: Exception) {
                    callback(null)
                }
            },
            { error ->
                callback(null)
            })

        requestQueue.add(request)
    }

    private fun getWarehouses(cityRef: String, callback: (List<String>) -> Unit) {
        val requestBody = JSONObject().apply {
            put("apiKey", apiKey)
            put("modelName", "AddressGeneral")
            put("calledMethod", "getWarehouses")
            put("methodProperties", JSONObject().apply {
                put("CityRef", cityRef)
                put("Limit", 50)
            })
        }

        val request = JsonObjectRequest(
            Request.Method.POST, "https://api.novaposhta.ua/v2.0/json/", requestBody,
            { response ->
                try {
                    val data = response.getJSONArray("data")
                    val warehouses = mutableListOf<String>()
                    for (i in 0 until data.length()) {
                        val warehouse = data.getJSONObject(i).getString("Description")
                        warehouses.add(warehouse)
                    }
                    warehouses.sortBy { it.split("№").getOrNull(1)?.toIntOrNull() ?: 0 }
                    callback(warehouses)
                } catch (e: Exception) {
                    callback(emptyList())
                }
            },
            { error ->
                callback(emptyList())
            })

        requestQueue.add(request)
    }

    private fun setEmptySpinner() {
        val adapter = ArrayAdapter.createFromResource(
            context,
            R.array.empty_post_offices,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.isEnabled = false
    }
}
