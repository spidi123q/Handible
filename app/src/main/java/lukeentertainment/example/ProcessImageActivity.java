package lukeentertainment.example;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;

import static org.opencv.imgproc.Imgproc.getRotationMatrix2D;

public class ProcessImageActivity extends AppCompatActivity {

    ImageView imageView;
    SeekBar rotateSeekBar;
    Mat mat;
    FloatingActionButton saveFab;
    FloatingActionButton getText;
    EditText extractedText;
    Bitmap bitmap;
    Uri uri;
    int position,parentID;
    DatabaseOperations db;

    String projectName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_image);
        Intent i=getIntent();
        uri= i.getParcelableExtra("ImageURI");
        position=i.getIntExtra("Position",0);
        parentID=i.getIntExtra("ParentID",0);

        db=new DatabaseOperations(getApplicationContext());
        Cursor CR=db.getAlldata();
        CR.moveToPosition(position);
        projectName=CR.getString(1);

        imageView=(ImageView)findViewById(R.id.process_iv);

        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 1, out);
            bitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
            imageView.setImageBitmap(bitmap);
            mat=new Mat(bitmap.getWidth(),bitmap.getHeight(), CvType.CV_8UC4);
            Utils.bitmapToMat(bitmap,mat);
            OpencvNativeClass.processImage(mat.getNativeObjAddr());
            Utils.matToBitmap(mat,bitmap);
            imageView.setImageBitmap(bitmap);

        } catch (IOException e) {
            e.printStackTrace();
        }
        rotateSeekBar=(SeekBar)findViewById(R.id.rotate_sb);
        rotateSeekBar.setMax(360);
        rotateSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Mat m= rotate(mat,progress);
                Utils.matToBitmap(m,bitmap);
                imageView.setImageBitmap(bitmap);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        saveFab=(FloatingActionButton)findViewById(R.id.save_fab);
        saveFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm:ss");
                String date = df.format(Calendar.getInstance().getTime());
                saveImage(bitmap,projectName,projectName+date);
                finish();
            }
        });

        getText=(FloatingActionButton)findViewById(R.id.get_text_fab);
        extractedText=(EditText)findViewById(R.id.editText);
        getText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.bitmapToMat(bitmap,mat);
                File mediaStorageDir = new File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyCameraApp");
                if (!mediaStorageDir.exists()) {
                    if (!mediaStorageDir.mkdirs()) {
                        Log.d("MyCameraApp", "failed to create directory");
                    }
                }
                String str="";
                OpencvNativeClass.testInput(mat.getNativeObjAddr(),(mediaStorageDir.getPath()+File.separator));
                str=readStrFromFile(mediaStorageDir.getPath()+File.separator+"data.txt");
                System.out.println("Text : "+str);
                str=new StringBuffer(str).reverse().toString();
                extractedText.setEnabled(true);
                extractedText.setText(str.toString());
                Utils.matToBitmap(mat,bitmap);
                imageView.setImageBitmap(bitmap);

            }
        });
    }


    Mat rotate(Mat src, double angle)
    {
        Mat dst=new Mat();
        Mat r = getRotationMatrix2D(new Point(src.cols()/2.,src.rows()/2.), angle, 1.0);
        Imgproc.warpAffine(src, dst, r, new Size(new Point(src.cols(), src.rows())));
        return dst;
    }
    public void saveImage(Bitmap b, String prjName, String itemNum){
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyCameraApp/Projects/"+prjName);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
            }
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(new File(mediaStorageDir.toString()+"/"+itemNum+".jpg"));
            // Use the compress method on the BitMap object to write image to the OutputStream
            b.compress(Bitmap.CompressFormat.JPEG,100, fos);
            String imagePath = mediaStorageDir.toString()+"/"+itemNum+".jpg";
            db.addRowContentList(parentID,imagePath);
            Toast.makeText(getApplicationContext(),"Photo Added ",Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private String readStrFromFile(String path) {
        String result = "";
        File file = new File(path);
        if (file.exists()) {
            //byte[] buffer = new byte[(int) new File(filePath).length()];
            FileInputStream fis = null;
            try {
                //f = new BufferedInputStream(new FileInputStream(filePath));
                //f.read(buffer);

                fis = new FileInputStream(file);
                char current;
                while (fis.available() > 0) {
                    int curren= (int) fis.read();
                    System.out.println(""+curren);
                    result = result + (char)(curren);
                }
            } catch (Exception e) {
                Log.d("TourGuide", e.toString());
            } finally {
                if (fis != null)
                    try {
                        fis.close();
                    } catch (IOException ignored) {
                    }
            }
            //result = new String(buffer);
        }

        return result;
    }


}
