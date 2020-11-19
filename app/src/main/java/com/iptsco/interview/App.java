package com.iptsco.interview;

import android.app.Application;

import com.iptsco.interview.data.remote.RetrofitSingleton;

/**
 * Created by MHK on 11/19/2020.
 * www.MHKSoft.com
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Retrofit
        RetrofitSingleton.setInstance(this);
    }
}
