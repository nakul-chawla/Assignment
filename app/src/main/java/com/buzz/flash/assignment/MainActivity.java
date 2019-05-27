package com.buzz.flash.assignment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    String imageFilePath;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_GALLERY_CODE = 200;
    private static final String SERVER_PATH = "http://nakulimage.herokuapp.com";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button selectUploadButton = (Button)findViewById(R.id.select_image);
        selectUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCameraIntent();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        startUpload();
    }
    void startUpload(){
        File file = new File(imageFilePath);
        Log.d(TAG, "Filename " + file.getName());
        RequestBody mFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData("file", file.getName(), mFile);
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100,TimeUnit.SECONDS).build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SERVER_PATH)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        UploadImageInterface uploadImage = retrofit.create(UploadImageInterface.class);
        final ProgressDialog progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();
        Call<ResponseModel> fileUpload = uploadImage.uploadFile(fileToUpload);
        fileUpload.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                if(response.code()==200)
                {
                    Toast.makeText(MainActivity.this, ""+response.body().getName(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
                else {
                    Toast.makeText(MainActivity.this, "Failed:"+response.code()+" : "+response.message(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                progressDialog.setTitle("Failed");
                Log.d(TAG, "Error " + t.getMessage());
            }
        });
    }
    private void openCameraIntent() {
        Intent pictureIntent = new Intent(
                MediaStore.ACTION_IMAGE_CAPTURE);
        if (pictureIntent.resolveActivity(getPackageManager()) != null) {
            //Create a file to store the image
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.buzz.flash.assignment.provider", photoFile);
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        photoURI);
                startActivityForResult(pictureIntent,
                        REQUEST_GALLERY_CODE);
            }
        }


    }
    private File createImageFile() throws IOException {
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss",
                        Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir =
                getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        imageFilePath = image.getAbsolutePath();
        return image;
    }


}
