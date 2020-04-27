package com.example.hdrapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog;


import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
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
    private Bitmap newBitmap;
    private static final float BLUR_RADIUS = 10;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_filter = (Button) findViewById(R.id.button1);
        btn_upload = (Button) findViewById(R.id.button2);
        im = (ImageView) findViewById(R.id.imageV);
        btn_filter.setEnabled(false);
    }

    private static Bitmap scaleToFill(Bitmap b, int width, int height)
    {
        float factorH = height / (float) b.getWidth();
        float factorW = width / (float) b.getWidth();
        float factorToUse = (factorH > factorW) ? factorW : factorH;
        return Bitmap.createScaledBitmap(b, (int) (b.getWidth() * factorToUse),
                (int) (b.getHeight() * factorToUse), true);
    }


    public Bitmap blur(Bitmap image) {
        if (null == image) return null;
    Bitmap outputBitmap = Bitmap.createBitmap(image);
    final RenderScript renderScript = RenderScript.create(this);
    Allocation tmpIn = Allocation.createFromBitmap(renderScript, image);
    Allocation tmpOut = Allocation.createFromBitmap(renderScript, outputBitmap);
    //Intrinsic Gausian blur filter
    ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        theIntrinsic.setRadius(BLUR_RADIUS);
        theIntrinsic.setInput(tmpIn);
        theIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);
        return outputBitmap;
    }

    private static Bitmap makeTransparentBitmap(Bitmap bmp, int alpha) {
        Bitmap transBmp = Bitmap.createBitmap(bmp.getWidth(),
                bmp.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(transBmp);
        final Paint paint = new Paint();
        paint.setAlpha(alpha);
        canvas.drawBitmap(bmp, 0, 0, paint);
        return transBmp;
    }

    private static Bitmap l2overl1(Bitmap bottomImage, Bitmap topImage)
    {
        int sizex = bottomImage.getWidth();
        int sizey = bottomImage.getHeight();

        topImage = makeTransparentBitmap(topImage, 62); //set transparency for layer to 62%

        Paint paint = new Paint();
        Bitmap imageBitmap = Bitmap.createBitmap(sizex, sizey , Bitmap.Config.ARGB_8888);
        Canvas comboImage = new Canvas(imageBitmap);
        comboImage.drawBitmap(bottomImage, 0f, 0f, paint);;
        PorterDuff.Mode mode = PorterDuff.Mode.OVERLAY;//Porterduff MODE
        paint.setXfermode(new PorterDuffXfermode(mode));

        Bitmap ScaledtopImage = Bitmap.createScaledBitmap(topImage, sizex, sizey, false);
        comboImage.drawBitmap(ScaledtopImage, 0f, 0f, paint);

        return imageBitmap;
    }

    private static Bitmap l3overl2andl1(Bitmap bottomImage, Bitmap topImage)
    {
        int sizex = bottomImage.getWidth();
        int sizey = bottomImage.getHeight();

        Paint paint = new Paint();
        Bitmap imageBitmap = Bitmap.createBitmap(sizex, sizey , Bitmap.Config.ARGB_8888);
        Canvas comboImage = new Canvas(imageBitmap);
        comboImage.drawBitmap(bottomImage, 0f, 0f, paint);;
        PorterDuff.Mode mode = PorterDuff.Mode.LIGHTEN;//Porterduff MODE
        paint.setXfermode(new PorterDuffXfermode(mode));

        Bitmap ScaledtopImage = Bitmap.createScaledBitmap(topImage, sizex, sizey, false);
        comboImage.drawBitmap(ScaledtopImage, 0f, 0f, paint);

        return imageBitmap;
    }

    public void filter(View view) throws IOException{

        BitmapDrawable abmp = (BitmapDrawable) im.getDrawable();
        Bitmap original, blackwhite, linear_layer;

        original = abmp.getBitmap();
        linear_layer = abmp.getBitmap();

        newBitmap = Bitmap.createBitmap(original.getWidth(), original.getHeight(), original.getConfig());

        //desaturate+invert
        blackwhite = Bitmap.createBitmap(original.getWidth(), original.getHeight(), original.getConfig());
        Canvas c = new Canvas(blackwhite);
        Paint paint = new Paint();

        ColorMatrix matrixGrayscale = new ColorMatrix();
        matrixGrayscale.setSaturation(0);

        ColorMatrix matrixInvert = new ColorMatrix();
        matrixInvert.set(new float[]
                {
                        -1.0f, 0.0f, 0.0f, 0.0f, 255.0f,
                        0.0f, -1.0f, 0.0f, 0.0f, 255.0f,
                        0.0f, 0.0f, -1.0f, 0.0f, 255.0f,
                        0.0f, 0.0f, 0.0f, 1.0f, 0.0f
                });
        matrixInvert.preConcat(matrixGrayscale);

        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrixInvert);
        paint.setColorFilter(filter);
        c.drawBitmap(original, 0, 0, paint);

        //blur the layer
        blackwhite = blur(blackwhite);

        // overlay original & blackwhite layers
        Bitmap result = l2overl1(original, blackwhite);
        // overlay previous result & linear layer
        result = l3overl2andl1(result, linear_layer);

        try {
            im.setImageBitmap(scaleToFill(result, 900,900));
        }catch (Exception e) {
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
                    btn_filter.setEnabled(true);
                }
            } else if (requestCode == 2) {
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = data.getData();
                    im.setImageURI(selectedImage);
                    btn_filter.setEnabled(true);
                }
            }

        }
    }


}