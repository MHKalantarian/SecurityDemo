package com.iptsco.interview.data.remote;

import com.iptsco.interview.data.model.Password;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PasswordService {
    @GET("query")
    Call<Password> generatePassword(@Query("command") String command,
                                    @Query("format") String format,
                                    @Query("count") int count);
}
