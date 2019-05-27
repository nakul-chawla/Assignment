package com.buzz.flash.assignment;


import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface UploadImageInterface {
    @Multipart
    @POST("/upload_yes/")
    Call<ResponseModel> uploadFile(@Part MultipartBody.Part  file);
}
