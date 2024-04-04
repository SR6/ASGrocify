import com.example.grocify.BuildConfig
import com.example.grocify.api.IKrogerService
import com.example.grocify.models.KrogerProductsResponse
import com.example.grocify.models.KrogerProductResponse
import com.example.grocify.models.AuthTokenResponse
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object KrogerClient {
    private const val BASE_URL = "https://api.kroger.com/v1/"

    private val retrofit: Retrofit by lazy {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY) // Set your desired logging level here

        val httpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()
    }

    public fun getCredentials(): String {
        val clientId = BuildConfig.CLIENT_ID
        val clientSecret = BuildConfig.CLIENT_SECRET
        return Credentials.basic(clientId, clientSecret)
    }

    val krogerService: IKrogerService by lazy {
        retrofit.create(IKrogerService::class.java)
    }
}

class KrogerService : IKrogerService {
    override fun getAuthToken(credentials: String, grantType: String): Call<AuthTokenResponse> {
        return try {
            KrogerClient.krogerService.getAuthToken(credentials, grantType)
        }
        catch (e: Exception) {
            e.printStackTrace()
            throw java.lang.Exception("Failed to get token", e)
        }
    }

    override fun getProducts(
        token: String,
        accept: String,
        brand: String?,
        limit: String?,
        productId: String?,
        start: String?,
        term: String?
    ): Call<KrogerProductsResponse> {
        return try {
            KrogerClient.krogerService.getProducts(token, accept, brand, limit, productId, start, term)
        }
        catch (e: Exception) {
            e.printStackTrace()
            throw java.lang.Exception("Failed to get products", e)
        }
    }

    override fun getProductById(
        token: String,
        accept: String,
        productId: String
    ): Call<KrogerProductResponse> {
        return try {
            KrogerClient.krogerService.getProductById(token, accept, productId)
        }
        catch (e: Exception) {
            e.printStackTrace()
            throw java.lang.Exception("Failed to get product", e)
        }
    }
}