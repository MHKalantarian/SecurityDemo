package com.iptsco.interview.data.remote;

import androidx.lifecycle.MutableLiveData;

import com.iptsco.interview.data.model.Password;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PasswordRepository {
    private final PasswordService api;
    private final MutableLiveData<Password> generatePasswordLiveData = new MutableLiveData<>();

    public PasswordRepository() {
        this.api = RetrofitSingleton.getInstance().create(PasswordService.class);
    }

    public MutableLiveData<Password> generatePassword() {
        generatePasswordLiveData.postValue(Password.loading());
        api.generatePassword("password", "json", 1).enqueue(new Callback<Password>() {
            @Override
            public void onResponse(Call<Password> call, Response<Password> response) {
                if (response.isSuccessful()) {
                    generatePasswordLiveData.postValue(Password.success(response.body()));
                } else {
                    generatePasswordLiveData.postValue(Password.error(response.message()));
                }
            }

            @Override
            public void onFailure(Call<Password> call, Throwable t) {
                generatePasswordLiveData.postValue(Password.error(t));
            }
        });
        return generatePasswordLiveData;
    }

    public MutableLiveData<Password> getGeneratePasswordLiveData() {
        return generatePasswordLiveData;
    }
}
