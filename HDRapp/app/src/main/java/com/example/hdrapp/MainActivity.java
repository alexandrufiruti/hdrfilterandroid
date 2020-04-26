package com.example.hdrapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog;


import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends Activity {
    Button btn_filter, btn_upload;
    ImageView im;

    private Bitmap bmp;
    private Bitmap newBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_filter = (Button) findViewById(R.id.button1);
        btn_upload = (Button) findViewById(R.id.button2);
        im = (ImageView) findViewById(R.id.imageV);
    }

    public static Bitmap scaleToFill(Bitmap b, int width, int height)
    {
        float factorH = height / (float) b.getWidth();
        float factorW = width / (float) b.getWidth();
        float factorToUse = (factorH > factorW) ? factorW : factorH;
        return Bitmap.createScaledBitmap(b, (int) (b.getWidth() * factorToUse),
                (int) (b.getHeight() * factorToUse), true);
    }

    public void filter(View view) throws IOException{

        BitmapDrawable abmp = (BitmapDrawable) im.getDrawable();
        bmp = abmp.getBitmap();
        newBitmap = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());
            for (int i = 0; i < bmp.getWidth(); i++) {
                for (int j = 0; j < bmp.getHeight(); j++) {
                    int p = bmp.getPixel(i, j);
                    int r = Color.red(p);
                    int g = Color.green(p);
                    int b = Color.blue(p);

                    r =  0;
                    g =  g+150;
                    b =  0;
                    newBitmap.setPixel(i, j, Color.argb(Color.alpha(p), r, g, b));

                }

            }
        try {
            im.setImageBitmap(scaleToFill(newBitmap, 900,900));
            //im.setImageBitmap(newBitmap);
            //im.setImageResource(0);
        }catch (Exception e) {
            Log.d("myTag", "This is ERROR");
            e.printStackTrace();
        }

    }

    public void upload(View view) {
        final CharSequence[] options = {"Fa o poza", "Alege din galerie", "Renunta"};
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Adauga o fotografie!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Fa o poza")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, 1);
                } else if (options[item].equals("Alege din galerie")) {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 2);
                } else if (options[item].equals("Renunta")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = data.getData();
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    im.setImageBitmap(photo);
                }
            } else if (requestCode == 2) {
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = data.getData();
                    im.setImageURI(selectedImage);
                }
            }

        }
    }


}