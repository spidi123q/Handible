package lukeentertainment.example;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.File;
import java.io.IOException;

public class TrainActivity extends AppCompatActivity {

    ImageView imageView;
    FloatingActionButton useCamera,loadFromGallery;
    Mat mat;
    Bitmap bitmap;
    Uri uri;
    int trainAppend;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train);
        Activity activity = this;
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(activity, R.color.colorPrimary));

        Intent i=getIntent();
        trainAppend=i.getIntExtra("trainoption",0);
        uri= i.getParcelableExtra("ImageURI");
        imageView=(ImageView)findViewById(R.id.train_image_view);
        System.out.println(" option : "+trainAppend);
        useCamera=(FloatingActionButton)findViewById(R.id.useCamera);
        useCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            imageView.setImageBitmap(bitmap);
            mat=new Mat(bitmap.getWidth(),bitmap.getHeight(), CvType.CV_8UC4);
            Utils.bitmapToMat(bitmap,mat);
            File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyCameraApp");
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d("MyCameraApp", "failed to create directory");
                }
            }
            OpencvNativeClass.train(mat.getNativeObjAddr(),mediaStorageDir.getPath()+File.separator,trainAppend);
            Utils.matToBitmap(mat,bitmap);
            imageView.setImageBitmap(bitmap);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
