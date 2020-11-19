package com.iptsco.interview.data.remote;

import android.content.Context;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public abstract class RetrofitSingleton {
    private static Retrofit instance;

    public static Retrofit getInstance() {
        return instance;
    }

    public static Retrofit setInstance(Context context) {
        if (instance == null) {
            synchronized (RetrofitSingleton.class) {
                OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
                instance = new Retrofit.Builder()
                        .baseUrl("https://www.passwordrandom.com")
                        .addConverterFactory(JacksonConverterFactory.create())
                        .client(httpClient.build())
                        .build();
            }
        }
        return instance;
    }

    public static void destroyInstance() {
        instance = null;
    }
}
