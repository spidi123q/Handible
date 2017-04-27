package lukeentertainment.example;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;

public class PreprocessingActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 30 ;
    private static final int CROP_PIC = 50 ;
    int position;
    ContentValues values;
    DatabaseOperations db;
    private static final int PICK_IMAGE_REQUEST_TESTING = 10 ;
    FloatingActionButton gallerFab,cameraFab;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preprocessing);
        Intent mIntent = getIntent();
        position = mIntent.getIntExtra("position",0);
        db=new DatabaseOperations(getApplicationContext());
        refreshList();

        gallerFab=(FloatingActionButton)findViewById(R.id.gallery_fab);
        cameraFab=(FloatingActionButton)findViewById(R.id.useCameraFab);
        cameraFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, "New Picture");
                values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
                imageUri = getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent,REQUEST_IMAGE_CAPTURE);
            }
        });
        gallerFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST_TESTING);
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


}
