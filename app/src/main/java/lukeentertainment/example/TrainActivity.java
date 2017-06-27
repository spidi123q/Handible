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
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Scanner;

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
        int lang=i.getIntExtra("lang",1);
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
        Toast.makeText(getApplicationContext(),"qwe",Toast.LENGTH_SHORT).show();
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
                Scanner scanner = new Scanner(new File(mediaStorageDir.getPath()+File.separator+"lastPos.txt"));
                int [] lastPos = new int [1];
                int a = 0;
                while(scanner.hasNextInt())
                {
                    lastPos[a++] = scanner.nextInt();
                }
                if(trainAppend==0&&lang==0)
                    lastPos[0]=48;
                else if(trainAppend==0&&lang==1)
                    lastPos[0]=3349;

                Toast.makeText(getApplicationContext(),""+lastPos[0],Toast.LENGTH_SHORT).show();
                int lastTrainedItem;
                if(lang==0)
                    lastTrainedItem=OpencvNativeClass.train(mat.getNativeObjAddr(),mediaStorageDir.getPath()+File.separator,trainAppend,lastPos[0]);
                else
                    lastTrainedItem=OpencvNativeClass.trainMalayalam(mat.getNativeObjAddr(),mediaStorageDir.getPath()+File.separator+"Malayalam/",trainAppend,lastPos[0]);


                Writer wr = new FileWriter(mediaStorageDir.getPath()+File.separator+"lastPos.txt");
                wr.write(new Integer(lastTrainedItem).toString());
                wr.close();
                Utils.matToBitmap(mat,bitmap);
                imageView.setImageBitmap(bitmap);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
