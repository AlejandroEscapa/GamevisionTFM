package es.androidtfm.gamevision.retrofit

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val NEWS_URL = "https://newsapi.org/"
    private const val RAWG_URL = "https://api.rawg.io/api/"

    val newsApi: NewsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(NEWS_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NewsApiService::class.java)
    }

    val gamesApi: GameApiService by lazy {
        Retrofit.Builder()
            .baseUrl(RAWG_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GameApiService::class.java)
    }
}