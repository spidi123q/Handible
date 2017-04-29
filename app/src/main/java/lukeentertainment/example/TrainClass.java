package lukeentertainment.example;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.File;
import java.io.IOException;

public class TrainClass extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 10;
    private static String TAG = "TrainClass";

    static {
        System.loadLibrary("MyOpencvLibs");
    }

    BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case BaseLoaderCallback.SUCCESS:

                    break;
                default:
                    super.onManagerConnected(status);

            }
        }
    };


    Button saveButton;
    ImageView fullImage,segmentedImage;
    EditText charSetField;
    Bitmap bitmap,bm;
    File mediaStorageDir;
    Mat mat;
    Uri uri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train_class);
        Activity activity = this;
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(activity, R.color.colorPrimary));
        initializeViews();
        initializeListeners();

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);

    }

    private void initializeListeners() {
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text=charSetField.getText().toString();
                if(!TextUtils.isEmpty(text))
                {
                    mat=new Mat(bitmap.getWidth(),bitmap.getHeight(), CvType.CV_8UC4);
                    Utils.bitmapToMat(bitmap,mat);
                    OpencvNativeClass.trainIndi(mat.getNativeObjAddr(),(mediaStorageDir.getPath()+File.separator),Integer.parseInt(charSetField.getText().toString()));
                    Utils.matToBitmap(mat,bm);
                    fullImage.setImageBitmap(bm);
                }
            }
        });
    }

    private void initializeViews() {
        saveButton=(Button)findViewById(R.id.save_button);
        fullImage=(ImageView)findViewById(R.id.segment_image_view);
        segmentedImage=(ImageView)findViewById(R.id.full_image_view);
        charSetField=(EditText)findViewById(R.id.char_edit_field);
    }

    protected void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug()) {
            Log.i(TAG, "OpenCV loaded successfully");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        } else {
            Log.i(TAG, "Opencv not loaded");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mLoaderCallback);
        }
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            uri = data.getData();
            try {

                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                bm=bitmap;
                mat=new Mat(bitmap.getWidth(),bitmap.getHeight(), CvType.CV_8UC4);
                Utils.bitmapToMat(bitmap,mat);
                fullImage.setImageBitmap(bitmap);
                mediaStorageDir = new File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyCameraApp");
                if (!mediaStorageDir.exists()) {
                    if (!mediaStorageDir.mkdirs()) {
                        Log.d("MyCameraApp", "failed to create directory");
                    }
                }
               // OpencvNativeClass.trainIndi(mat.getNativeObjAddr(),(mediaStorageDir.getPath()+File.separator),1);
                //Utils.matToBitmap(mat,bitmap);
               // fullImage.setImageBitmap(bitmap);
                Toast.makeText(getApplicationContext(),"bannu",Toast.LENGTH_SHORT);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    @Override
    protected void onStart()
    {
        super.onStart();
        mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyCameraApp");
    }
    @Override
    protected void onStop() {
        super.onStop();
        File fdelete = new File(mediaStorageDir.getPath()+"/seg.txt");
        if (fdelete.exists()) {
           fdelete.delete();
        }
        Toast.makeText(getApplicationContext(),"bannu",Toast.LENGTH_SHORT).show();

    }

}
