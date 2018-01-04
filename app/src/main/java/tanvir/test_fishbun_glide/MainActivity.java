package tanvir.test_fishbun_glide;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.BitmapCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ablanco.zoomy.ZoomListener;
import com.ablanco.zoomy.Zoomy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.iceteck.silicompressorr.SiliCompressor;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.sangcomz.fishbun.FishBun;
import com.sangcomz.fishbun.define.Define;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import id.zelory.compressor.Compressor;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    ImageView imageView2;
    EditText editText1 , editText2;

    ArrayList<Uri> path = new ArrayList<>();

    private Bitmap bitmap;

    private static final float maxHeight = 1280.0f;
    private static final float maxWidth = 1280.0f;

    TextView textView;

    String[] permissions = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA

    };
    KProgressHUD hud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.IV);
        textView = findViewById(R.id.TV);
        imageView2 = findViewById(R.id.IV2);
        editText1=findViewById(R.id.IN);
        editText2=findViewById(R.id.IN2);

        hud = KProgressHUD.create(MainActivity.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setDimAmount(0.6f)
                .setLabel("Please Wait")
                .setCancellable(false);

        checkPermissions();
    }

    public void pickImage(View view) {

        ///FishBun.with(MainActivity.this).MultiPageMode().
        ///setCamera(true).startAlbum();

        FishBun.with(MainActivity.this).MultiPageMode().setCamera(true).
                setActionBarColor(Color.parseColor("#607D8B"), Color.parseColor("#607D8B"), false)
                .setActionBarTitleColor(Color.parseColor("#ffffff")).startAlbum();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent imageData) {
        super.onActivityResult(requestCode, resultCode, imageData);
        switch (requestCode) {
            case Define.ALBUM_REQUEST_CODE:
                if (resultCode == RESULT_OK) {

                    path = imageData.getParcelableArrayListExtra(Define.INTENT_PATH);

                    /*Glide.with(this)
                            .asBitmap()
                            .load(path.get(0));

                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),path.get(0));
                        final int bitmapByteCount= BitmapCompat.getAllocationByteCount(bitmap);

                        if (bitmapByteCount>5000000)
                        {
                            Toast.makeText(this, "Please select image less than 5 MB", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {

                            Glide.with(MainActivity.this).load(path.get(0)).into(imageView);

                        }*
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/

                    Glide.with(MainActivity.this).load(path.get(0)).into(imageView);


                    break;
                }
        }
    }

    private boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 100);
            return false;
        }
        return true;
    }


    public String ImageToString(Bitmap bitmap, int quality) {

        String imgString;

        MainActivity.this.runOnUiThread(new Runnable() {
            public void run() {
               /// Toast.makeText(MainActivity.this, "ImageToString", Toast.LENGTH_SHORT).show();
            }
        });

        if (bitmap == null) {
            imgString = "null";
        } else {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
            byte[] byteFormat = stream.toByteArray();
            imgString = Base64.encodeToString(byteFormat, Base64.DEFAULT);
        }


        return imgString;

    }


    public void uploadImageToServer(View view) {

        hud.show();


        new Thread(new Runnable() {
            public void run() {

                ///String url = "https://crimecrack.000webhostapp.com/TestImage/uploadImage.php";

                String url = "http://www.farhandroid.com/uploadImage.php";

                /*MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(MainActivity.this, "path[0 : "+path.get(0), Toast.LENGTH_SHORT).show();
                    }
                });*/


                final StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(final String response) {

                                ///if (position==(path.size()-1))
                                /// hud.dismiss();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    public void run() {
                                        hud.dismiss();
                                        Toast.makeText(MainActivity.this, "response " + response, Toast.LENGTH_SHORT).show();
                                    }
                                });


                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(final VolleyError error) {

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                                    }
                                });


                            }
                        }
                ) {
                    @Override
                    protected Map<String, String> getParams() {

                        Map<String, String> params = new HashMap<>();

                        try {

                            ///Toast.makeText(MainActivity.this, "Enter", Toast.LENGTH_SHORT).show();

                            //BitmapFactory.Options options = new BitmapFactory.Options();
                            String filepath = getRealPathFromDocumentUri(MainActivity.this,path.get(0));

                            File file = new File(filepath);
                            bitmap = new Compressor(MainActivity.this).setQuality(75)
                                    .setMaxWidth(640)
                                    .setMaxHeight(480)
                                   .compressToBitmap(file);



                            ///bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), path.get(0));
                            ///bitmap = new Compressor(MainActivity.this).compressToFile(bitmap);
                            ///Uri uri = path.get(0);

                            ///bitmap = SiliCompressor.with(MainActivity.this).getCompressBitmap(uri);


                            /// Bitmap compressedImageBitmap = new Compressor(MainActivity.this).compressToBitmap(bitmap);
                           /* ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                            bitmap = BitmapFactory.decodeFile(String.valueOf(MediaStore.Images.Media.getBitmap(getContentResolver(), path.get(0))), options);


                            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);

                            byte[] byteFormat = stream.toByteArray();
                            String imgString = Base64.encodeToString(byteFormat, Base64.DEFAULT);*/


                            final int bitmapByteCount = BitmapCompat.getAllocationByteCount(bitmap);
                            int quality = 0;

                            if (bitmapByteCount > 50000000)
                                quality = 10;
                            else if (bitmapByteCount > 40000000)
                                quality = 15;
                            else if (bitmapByteCount > 30000000)
                                quality = 25;
                            else if (bitmapByteCount > 20000000)
                                quality = 35;
                            /*else if (bitmapByteCount>10000000)
                                quality=50;*/
                            else
                                quality = 50;

                            /*if (bitmapByteCount>5000000)
                                quality=5;
                            else if (bitmapByteCount>4000000)
                                quality=70;
                            else if (bitmapByteCount>3000000)
                                quality=80;
                            else
                                quality=100;*/

                            MainActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    textView.setText(Integer.toString(bitmapByteCount));
                                }
                            });


                            ///Bitmap.createScaledBitmap(bitmap,300, 300, false);
                            params.put("userName", editText1.getText().toString());
                            params.put("image", ImageToString(bitmap, quality));

                            ///params.put("image", imgString);


                        } catch (final Exception e) {

                            textView.setText(e.toString());

                            MainActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(MainActivity.this, "Exception : " + e, Toast.LENGTH_LONG).show();

                                }
                            });

                        }

                        return params;

                    }
                };


                MySingleton.getInstance(MainActivity.this).addToRequestQueue(postRequest);


            }
        }).start();

    }

    public void retrive(View view) {
        //Picasso.with(MainActivity.this)
                ///.load("http://farhandroid.com/uploads/abul8.jpg")

                ///.into(imageView2);
        hud.show();


        Glide.with(MainActivity.this)
                .load("http://farhandroid.com/uploads/"+editText2.getText().toString()+".jpg")
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        hud.dismiss();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                       hud.dismiss();
                        return false;
                    }
                })
                .into(imageView2);


        Zoomy.Builder builder = new Zoomy.Builder(MainActivity.this)
                .target(imageView2)
                .enableImmersiveMode(false)
                .animateZooming(true)
                .zoomListener(new ZoomListener() {
                    @Override
                    public void onViewStartedZooming(View view) {
                        //View started zooming
                    }

                    @Override
                    public void onViewEndedZooming(View view) {
                        //View ended zooming
                    }
                });

        builder.register();

    }






    public static String getRealPathFromDocumentUri(Context context, Uri uri){
        String filePath = "";

        Pattern p = Pattern.compile("(\\d+)$");
        Matcher m = p.matcher(uri.toString());
        if (!m.find()) {
            ///Log.e(ImageConverter.class.getSimpleName(), "ID for requested image not found: " + uri.toString());
            return filePath;
        }
        String imgId = m.group();

        String[] column = { MediaStore.Images.Media.DATA };
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{ imgId }, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();

        return filePath;
    }

}
