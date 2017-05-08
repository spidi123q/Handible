package lukeentertainment.example;

import android.app.Activity;
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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
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

import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY_INV;
import static org.opencv.imgproc.Imgproc.getRotationMatrix2D;

public class ProcessImageActivity extends AppCompatActivity {

    ImageView imageView;
    SeekBar rotateSeekBar;
    Mat mat,spareMat,originalMat;
    FloatingActionButton invertFab;
    EditText extractedText;
    Bitmap bitmap,tmp;
    Uri uri;
    CheckBox cb;
    int position,parentID;
    DatabaseOperations db;

    String projectName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_image);
        Activity activity = this;
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(activity, R.color.colorPrimary));
        Intent i=getIntent();
        uri= i.getParcelableExtra("ImageURI");
        position=i.getIntExtra("Position",0);
        parentID=i.getIntExtra("ParentID",0);

        db=new DatabaseOperations(getApplicationContext());
        Cursor CR=db.getAlldata();
        CR.moveToPosition(position);
        projectName=CR.getString(1);
        cb=(CheckBox)findViewById(R.id.checkBox);

        imageView=(ImageView)findViewById(R.id.process_iv);

        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            tmp= MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 1, out);
            bitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
            imageView.setImageBitmap(bitmap);
            mat=new Mat(bitmap.getWidth(),bitmap.getHeight(), CvType.CV_8UC4);
            Utils.bitmapToMat(bitmap,mat);
            OpencvNativeClass.processImage(mat.getNativeObjAddr());
            Utils.matToBitmap(mat,bitmap);
            spareMat=mat.clone();
            originalMat=mat.clone();
            imageView.setImageBitmap(bitmap);

        } catch (IOException e) {
            e.printStackTrace();
        }
        invertFab=(FloatingActionButton)findViewById(R.id.invert_fab);
        invertFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Imgproc.threshold(mat,mat,0,255,THRESH_BINARY_INV);
                spareMat=mat.clone();
                Utils.matToBitmap(mat,bitmap);
                imageView.setImageBitmap(bitmap);
            }
        });
        invertFab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mat=originalMat.clone();
                spareMat=originalMat.clone();
                Utils.matToBitmap(mat,bitmap);
                imageView.setImageBitmap(bitmap);

                return true;
            }
        });
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

        extractedText=(EditText)findViewById(R.id.editText);
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
            b.compress(Bitmap.CompressFormat.PNG,100, fos);
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.process_image_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch ( item.getItemId() ){
            case R.id.action_save :

                DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm:ss");
                String date = df.format(Calendar.getInstance().getTime());
                saveImage(bitmap,projectName,projectName+date);
                finish();
                return true;

            case R.id.action_get_text :

                Utils.matToBitmap(spareMat,bitmap);
                Utils.bitmapToMat(bitmap,mat);

                File mediaStorageDir = new File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyCameraApp");
                if (!mediaStorageDir.exists()) {
                    if (!mediaStorageDir.mkdirs()) {
                        Log.d("MyCameraApp", "failed to create directory");
                    }
                }
                String str="";
                if(!cb.isChecked()){
                    OpencvNativeClass.testInput(mat.getNativeObjAddr(),(mediaStorageDir.getPath()+File.separator));
                    str=readStrFromFile(mediaStorageDir.getPath()+File.separator+"data.txt");
                }
                else
                {
                    OpencvNativeClass.testInputMalayalam(mat.getNativeObjAddr(),(mediaStorageDir.getPath()+File.separator+"Malayalam/"));
                    str=readStrFromFile(mediaStorageDir.getPath()+File.separator+"Malayalam/"+"data.txt");
                }
                System.out.println("Text : "+str);
                str=new StringBuffer(str).toString();
                extractedText.setEnabled(true);
                extractedText.setText(str);
                Utils.matToBitmap(mat,bitmap);
                imageView.setImageBitmap(bitmap);
                Intent i=new Intent(ProcessImageActivity.this,TextEditor.class);
                Log.e("PRoc", "onOptionsItemSelected: "+ str);
                i.putExtra("ExtractedText",str);
                startActivity(i);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private String processString(String str)
    {
        int length=str.length();
        char[] s = str.toCharArray();
        for(int i=0;i<length-1;i++)
        {
            if(Character.isLowerCase(s[i]))
            {
                if(Character.isUpperCase(s[i+1]))
                {
                    //Character.toLowerCase(s[i+1]);
                }
            }
        }
        str=s.toString();
        return str;
    }
}
