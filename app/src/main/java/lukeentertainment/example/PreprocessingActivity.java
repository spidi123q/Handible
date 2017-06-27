package lukeentertainment.example;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.GridView;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;

import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;

public class PreprocessingActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 30 ;
    private static final int CROP_PIC = 50 ;
    int position;
    ContentValues values;
    DatabaseOperations db;
    CheckBox cb;
    private static final int PICK_IMAGE_REQUEST_TESTING = 10 ;
    FloatingActionButton gallerFab,cameraFab;
    Uri imageUri;
    String str="",fullText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preprocessing);
        Activity activity = this;
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(activity, R.color.colorPrimary));
        Intent mIntent = getIntent();
        position = mIntent.getIntExtra("position",0);
        db=new DatabaseOperations(getApplicationContext());
        refreshList();
        cb=(CheckBox)findViewById(R.id.checkBox4);

        FabSpeedDial fabSpeedDial = (FabSpeedDial) findViewById(R.id.fab_speed_dial);
        fabSpeedDial.setMenuListener(new SimpleMenuListenerAdapter() {
            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                //TODO: Start some activity
                Intent intent;
                switch (menuItem.getItemId()) {
                    case R.id.action_import_cam:
                        values = new ContentValues();
                        values.put(MediaStore.Images.Media.TITLE, "New Picture");
                        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
                        imageUri = getContentResolver().insert(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                        intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                        startActivityForResult(intent,REQUEST_IMAGE_CAPTURE);
                        return true;

                    case R.id.action_import_gal:
                        intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST_TESTING);
                        return true;

                }
                return false;
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == (PICK_IMAGE_REQUEST_TESTING) && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Cursor cr=db.getAlldata();
            cr.moveToPosition(position);
            Uri uri = data.getData();
            Intent i=new Intent(PreprocessingActivity.this,ProcessImageActivity.class);
            i.putExtra("ImageURI",uri);
            i.putExtra("Position",position);
            i.putExtra("ParentID",cr.getInt(0));
            startActivity(i);
        }
        if(requestCode ==REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK )
        {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                        getContentResolver(), imageUri);
                performCrop();
                Toast.makeText(getApplicationContext(),"success",Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        if(requestCode == CROP_PIC&& resultCode == RESULT_OK&& data != null && data.getData() != null)
        {
            Toast.makeText(getApplicationContext(),"crpped",Toast.LENGTH_SHORT).show();
            Cursor cr=db.getAlldata();
            cr.moveToPosition(position);
            Intent i=new Intent(PreprocessingActivity.this,ProcessImageActivity.class);
            i.putExtra("ImageURI",data.getData());
            i.putExtra("Position",position);
            i.putExtra("ParentID",cr.getInt(0));
            startActivity(i);
        }
    }
    @Override
    protected  void onResume()
    {
        super.onResume();
        refreshList();
    }
    public void refreshList()
    {
        Cursor CR=db.getAlldata();
        CR.moveToPosition(position);
        int parentId=CR.getInt(0);
        GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(new ImageAdapter(this,parentId));
    }
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 1, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
    private void performCrop() {
        try
        {

            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            cropIntent.setDataAndType(imageUri, "image/*");
            cropIntent.putExtra("crop", true);
            cropIntent.putExtra("scale", true);
            cropIntent.putExtra("return-data", true);
            startActivityForResult(cropIntent, CROP_PIC);
        }
        // respond to users whose devices do not support the crop action
        catch (ActivityNotFoundException anfe) {
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.project_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_save_extract_all :
                fullText = "";
                Log.e("sdfs", "onOptionsItemSelected: " )
                ;
                extractAllText();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

     public void extractAllText(){
         String TAG = "DB";
         Cursor CR=db.getAlldata();
         CR.moveToPosition(position);
         int parentId=CR.getInt(0);
         CR = db.getAlldataFromContentTable(parentId);
        if (CR.moveToFirst()){
            Log.e(TAG, "extractAllText: "+CR.getString(2) );
            getBitmapFile(CR.getString(2));
            while(CR.moveToNext()){
                Log.e(TAG, "extractAllText: "+CR.getString(2) );
                getBitmapFile(CR.getString(2));
            }
            Intent i=new Intent(PreprocessingActivity.this,TextEditor.class);
            i.putExtra("ExtractedText",fullText);
            startActivity(i);
        }
     }
     void  getBitmapFile(String path){

         Bitmap bitmap = BitmapFactory.decodeFile(path);
         Mat mat = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC4);
         Utils.bitmapToMat(bitmap,mat);
         File mediaStorageDir = new File(
                 Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyCameraApp");
         if (!mediaStorageDir.exists()) {
             if (!mediaStorageDir.mkdirs()) {
                 Log.d("MyCameraApp", "failed to create directory");
             }
         }

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
         str+="<br>";
         fullText += str;



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
