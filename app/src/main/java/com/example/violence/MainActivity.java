package com.example.violence;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class MainActivity extends AppCompatActivity {

    private  static int Camera_Permission_Code=100;
    private  static int Vide_Record=101;
    private Uri Video_Path;
    private VideoView videoView;
    String loc;
    MediaController mediaController;
    Python py;
    PyObject pyobj;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        videoView=findViewById(R.id.videoView);
        mediaController=new MediaController(this);
        videoView.setMediaController(mediaController);

        if(!Python.isStarted())
            Python.start(new AndroidPlatform(this));
        py=Python.getInstance();
        pyobj=py.getModule("Script");

        if(isCameraPresentInPhone()){
                Log.i("Video_Record_Tag","Camera detected");
                getCameraPermission();
        }
        else{
               Log.i("Video_Record_Tag","Camera detected");
        }


    }

    public void recordVideoButtonPressed(View view){

        recordVideo();
        // communiteWithPython();
    }

    public void pickVideoFromGallery(View view){

         Intent intent=new Intent(Intent.ACTION_PICK);
         intent.setType("video/*");
         startActivityForResult(Intent.createChooser(intent,"Select video") ,101);

    }

    private  boolean isCameraPresentInPhone(){
        if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)){

            return  true;
        }
        else{
            return  false;
        }
    }
    private void getCameraPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)==PackageManager.PERMISSION_DENIED){

            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.CAMERA},Camera_Permission_Code);

        }
    }

    private void recordVideo(){
        Intent intent=new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT,10);
        startActivityForResult(intent,Vide_Record);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==Vide_Record){

            if(resultCode==RESULT_OK){

                Video_Path=data.getData();
                videoView.setVideoURI(Video_Path);
                videoView.start();
                Log.i("Video_Record_Tag","Video is recorded and available at path"+ Video_Path);

                loc= getRealPathFromURI(Video_Path);
                Log.i("Video_Record_Tag","Video is recorded and available at path "+ loc );
                communiteWithPython();
            }
            else if(resultCode==RESULT_CANCELED){

                Log.i("Video_Record_Tag","Found Error");
            }
        }

    }
    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public void communiteWithPython(){
        PyObject obj=pyobj.callAttr("main",loc);
        Log.i("Video_Record_Tag","Video is recorded and available at path "+ obj.toString() );
    }

}