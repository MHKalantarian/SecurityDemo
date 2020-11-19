package com.iptsco.interview.data.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.iptsco.interview.data.model.Password;
import com.iptsco.interview.data.remote.PasswordRepository;

public class MainViewModel extends ViewModel {
    private final PasswordRepository repository;

    public MainViewModel() {
        this.repository = new PasswordRepository();
    }

    public LiveData<Password> getGeneratePasswordLiveData() {
        return repository.getGeneratePasswordLiveData();
    }

    public LiveData<Password> generatePassword() {
        return repository.generatePassword();
    }
}
