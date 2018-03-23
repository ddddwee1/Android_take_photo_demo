package com.potato.word.take_photo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    ImageView mImage = null;
    Button mButton = null;
    String mPhotoPath = null;
    Button mUpload = null;
    String photoStr = "abc";
    Bitmap mBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImage = (ImageView) findViewById(R.id.imageView1);
        mButton = (Button) findViewById(R.id.button1);
        mUpload = (Button) findViewById(R.id.button2);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePicture();
            }
        });

        mUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connect();
            }
        });
    }

    private void dispatchTakePicture(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager())!=null){
            File photoFile = null;
            try {
                photoFile = createImageFile();
            }catch (IOException e){
                Toast tst = Toast.makeText(this,"Cannot create File, check the permission",Toast.LENGTH_LONG);
                tst.show();
            }
            if (photoFile!=null) {
                Uri photoUri = FileProvider.getUriForFile(MainActivity.this, "com.potato.word.take_photo", photoFile);
//                Uri photoUri = Uri.fromFile(photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, 1);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==1 && resultCode == RESULT_OK){
//            Bundle extras = data.getExtras();
//            Bitmap imagebit = (Bitmap) extras.get("data");
//            mImage.setImageBitmap(imagebit);
            setPic();
        }
    }

    private File createImageFile() throws IOException{
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "photo_face_"+timestamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(fileName, ".jpg", storageDir);
        mPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = mImage.getWidth();
        int targetH = mImage.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mPhotoPath, bmOptions);
        mImage.setImageBitmap(bitmap);
    }

    public void connect(){
        new AsyncTask<Void,Void,String>(){
            @Override
            protected void onPostExecute(String s) {
                mUpload.setText(s);
                photoStr = s;
                getBitMap();
            }

            @Override
            protected String doInBackground(Void... voids) {
                Socket skt = null;
                BufferedReader br = null;
                BufferedWriter bw = null;
                String msg = "";
                String host = "192.168.1.105";
                System.out.println("Start connection");
                try{
                    skt = new Socket(host,8080);
                    br = new BufferedReader(new InputStreamReader(skt.getInputStream()));
                    bw = new BufferedWriter(new OutputStreamWriter(skt.getOutputStream()));
                    bw.write(photoStr);
                    bw.flush();
                    String result;
                    //while ((result=br.readLine())!=null){
                       // msg += result;
                    //}
                    msg = br.readLine();
                    System.out.println(msg);

                }catch (Exception e){
                    System.out.println(e);
                }
                return msg;
            }
        }.execute();
    }

    public void getBitMap(){
        byte[] decodedString = Base64.decode(photoStr,Base64.DEFAULT);
        mBitmap = BitmapFactory.decodeByteArray(decodedString,0,decodedString.length);

        // Get the dimensions of the View
        int targetW = mImage.getWidth();
        int targetH = mImage.getHeight();

        int photoW = mBitmap.getWidth();
        int photoH = mBitmap.getHeight();
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        targetH = photoH * scaleFactor;
        targetW = photoW * scaleFactor;

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(mBitmap,targetW,targetH,false);
        mImage.setImageBitmap(scaledBitmap);

    }

}
