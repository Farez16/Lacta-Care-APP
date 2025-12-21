package com.example.lactacare.datos.network

import android.content.Context
import com.example.lactacare.dev.R
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private var retrofit: Retrofit? = null

    /**
     * Obtiene la instancia de Retrofit
     */
    fun getInstance(context: Context): Retrofit {
        if (retrofit == null) {
            // Obtener la URL del backend desde strings.xml
            val baseUrl = context.getString(R.string.backend_url)

            // Logging Interceptor (para ver las peticiones en Logcat)
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            // OkHttp Client con interceptores
            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            // Retrofit
            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        return retrofit!!
    }

    /**
     * Crea el servicio de autenticación
     */
    fun getAuthService(context: Context): AuthApiService {
        return getInstance(context).create(AuthApiService::class.java)
    }
}