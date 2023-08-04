package com.swanky.readro.service;

import com.swanky.readro.models.ResponseModelOfApi;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface GoogleBooksAPI {

    @GET
    Observable<ResponseModelOfApi> getData(@Url String url);

}
