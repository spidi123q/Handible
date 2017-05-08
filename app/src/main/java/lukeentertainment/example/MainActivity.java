package lukeentertainment.example;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.pdfcrowd.Client;
import com.pdfcrowd.PdfcrowdError;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST_FOR_TRAIN = 25 ;
    private static final int PICK_IMAGE_REQUEST_FOR_ENCRYPT_TRAIN = 35 ;
    private static final int PICK_IMAGE_REQUEST_DECRYPTING = 45 ;
    private static String TAG = "MaintActivity";
    private static final int PICK_IMAGE_REQUEST=10;

    DatabaseOperations db;
    Button loadImage,cvtImage,aditiveLearn;
    FloatingActionButton enctrain;
    ImageView imageView;
    TextView itt;
    CheckBox cb;
    int trainAppendTrue=0;
    Mat mRgba, mGray;

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

    ListView lyProduct;
    ProductListAdapter adapter;
    List<Product> mProductList;
    RelativeLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.temp);
        layout=(RelativeLayout)findViewById(R.id.mainLayout);
        Activity activity = this;
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(activity, R.color.colorPrimary));
        db=new DatabaseOperations(getApplicationContext());
        lyProduct=(ListView)findViewById(R.id.list_view);
        refreshList();
        cb=(CheckBox)findViewById(R.id.checkBox3);
        enctrain=(FloatingActionButton)findViewById(R.id.enctrain);
        enctrain.setVisibility(View.INVISIBLE);
        enctrain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"encryptionTraining Started from 0",Toast.LENGTH_SHORT).show();
                File imgFile=new File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyCameraApp");
                if(imgFile.exists()) {
                    Bitmap bit =BitmapFactory.decodeResource(getResources(), R.drawable.encrypt);
                    Mat mat=new Mat(bit.getWidth(),bit.getHeight(), CvType.CV_8UC4);
                    Utils.bitmapToMat(bit,mat);
                    imgFile=new File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyCameraApp/Set");
                    if(!imgFile.exists())
                        if(imgFile.mkdirs());
                    int o=OpencvNativeClass.encryptTrain(mat.getNativeObjAddr(),imgFile.getPath().toString()+File.separator);

                    Toast.makeText(getApplicationContext(),"encryptionTraining Completed at "+Character.toString((char)(o-1)),Toast.LENGTH_SHORT).show();
                }

            }
        });

        FabSpeedDial fabSpeedDialHome = (FabSpeedDial) findViewById(R.id.fab_speed_dial);
        fabSpeedDialHome.setMenuListener(new SimpleMenuListenerAdapter() {
            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                //TODO: Start some activity
                Intent intent;
                switch (menuItem.getItemId()) {
                    case R.id.action_add_project:
                        LayoutInflater li = LayoutInflater.from(getApplicationContext());
                        View promptsView = li.inflate(R.layout.project_prompt, null);

                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);

                        alertDialogBuilder.setView(promptsView);

                        final EditText userInput = (EditText) promptsView
                                .findViewById(R.id.new_proj_edittext);
                        alertDialogBuilder
                                .setCancelable(false)
                                .setPositiveButton("Done",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog,int id) {

                                                if (!userInput.getText().toString().isEmpty()) {
                                                    String name=userInput.getText().toString();
                                                    DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm:ss");
                                                    String date = df.format(Calendar.getInstance().getTime());
                                                    db.addRowProjectList(name,date,0,"null");
                                                    //Toast.makeText(getBaseContext(), "Registerd Successfully", Toast.LENGTH_SHORT).show();
                                                    refreshList();
                                                }

                                            }
                                        })
                                .setNegativeButton("Cancel",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                        return true;

                    case R.id.action_send_msg :
                        dialogSend(getCurrentFocus());
                        break;

                    case R.id.action_receive_msg :
                        intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST_DECRYPTING);
                        break;

                }
                return false;
            }
        });



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST_FOR_TRAIN && resultCode == RESULT_OK && data != null && data.getData() != null&&cb.isChecked()) {

            Uri uri = data.getData();
            Intent i=new Intent(MainActivity.this,TrainActivity.class);
            i.putExtra("ImageURI",uri);
            i.putExtra("trainoption",trainAppendTrue);
            i.putExtra("lang",1);
            startActivity(i);
        }
        if (requestCode == PICK_IMAGE_REQUEST_FOR_TRAIN && resultCode == RESULT_OK && data != null && data.getData() != null&&!cb.isChecked()) {

            Uri uri = data.getData();
            Intent i=new Intent(MainActivity.this,TrainActivity.class);
            i.putExtra("ImageURI",uri);
            i.putExtra("trainoption",trainAppendTrue);
            i.putExtra("lang",0);
            startActivity(i);
        }

        if (requestCode == PICK_IMAGE_REQUEST_DECRYPTING && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();
            try {

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                Mat mat = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC4);
                Utils.bitmapToMat(bitmap, mat);
                int size=(int)mat.total()*mat.channels();
                byte[] b=new byte[size];
                mat.get(0,0,b);
                for(int i=0;i<size;)
                {
                    int r = b[i]& 0xFF;
                    int g=b[i+1]&0xFF;
                    int a=b[i+2]&0xFF;
                    if((r==0&&g==0&&a==0)||(r==255&&g==255&&a==255)) {

                        b[i] = (byte) 255;
                        b[i + 1] = (byte) 255;
                        b[i + 2] = (byte) 255;

                    }else if((r==1&&g==1&&a==1)||(r==254&&g==254&&a==254))
                    {
                        b[i] = (byte) (0);
                        b[i + 1] = (byte) (0);
                        b[i + 2] = (byte) (0);

                    }

                    i=i+4;
                }
                mat.put(0,0,b);
                Toast.makeText(getApplicationContext(),"Dopne",Toast.LENGTH_SHORT).show();
                File imgFile=new File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyCameraApp/Set");
                if(!imgFile.exists())
                    if(imgFile.mkdirs());
                int o=OpencvNativeClass.decryptTest(mat.getNativeObjAddr(),imgFile.getPath().toString()+File.separator);
                Utils.matToBitmap(mat,bitmap);

                String str=readStrFromFile(imgFile.getPath()+File.separator+"data.txt");
                String[] strSeg=str.split(" ");
                int i,key=0;
                for(i=0;i<strSeg.length-1;i++)
                {
                    key = Integer.parseInt(strSeg[strSeg.length-1]);
                    String mainKey="";
                    for(int j=0;j<strSeg[i].length();j++)
                    {
                        int num=Integer.parseInt(""+strSeg[i].charAt(j));
                        int res=(num - key);
                        mainKey+=(Math.abs((num+1)*10+res)%10);

                    }

                    Toast.makeText(getApplicationContext(),"Pin is : "+mainKey,Toast.LENGTH_SHORT).show();
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



    }

    private String readStrFromFile(String path) {
        String result = "";
        File file = new File(path);
        if (file.exists()) {

            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                char current;
                while (fis.available() > 0) {
                    current = (char) fis.read();
                    result = result + String.valueOf(current);
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
    protected void onResume() {
        super.onResume();
        refreshList();
        if (OpenCVLoader.initDebug()) {
            Log.i(TAG, "OpenCV loaded successfully");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        } else {
            Log.i(TAG, "Opencv not loaded");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mLoaderCallback);
        }
    }
    private void refreshList()
    {
        mProductList=new ArrayList<>();
        Cursor CR=db.getAlldata();
        if(CR.moveToFirst())
        {
            do{
                String name=CR.getString(1);
                int id=CR.getInt(0);
                String date=CR.getString(2);
                int items=CR.getInt(3);
                String path= CR.getString(4);

                //Toast.makeText(getApplicationContext(),path,Toast.LENGTH_SHORT).show();
                mProductList.add(new Product(name,id,date,items,path));

            }while(CR.moveToNext());
        }
        else
        {
            //Toast.makeText(getApplicationContext(),"Nothing in database",Toast.LENGTH_SHORT).show();
        }
        adapter=new ProductListAdapter(this,mProductList);
        lyProduct.setAdapter(adapter);
        lyProduct.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                Cursor CR=db.getAlldata();
                CR.moveToPosition(position);
                Intent i=new Intent(MainActivity.this,PreprocessingActivity.class);
                i.putExtra("position",position);
                startActivity(i);


            }
        });
        lyProduct.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor CR=db.getAlldata();
                CR.moveToPosition(position);
                int projid=CR.getInt(0);
                db.deleteFromProjectList("DELETE FROM project_list WHERE Project_id = "+projid+";");
                db.deleteFromProjectList("DELETE FROM Content_list WHERE Parent_id = "+projid+";");
                //Toast.makeText(getApplicationContext(),"deleteed" +id,Toast.LENGTH_SHORT).show();
                refreshList();

                return true;
            }
        });


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_train:
                trainAppendTrue=0;
                intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST_FOR_TRAIN);
                return true;

            case R.id.action_train_append:
                trainAppendTrue=1;
                intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST_FOR_TRAIN);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
    public void dialogSend(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = this.getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.dialog_send_msg, null))
                // Add action buttons
                .setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        final String TAG = "Dialog send msg";
                        Dialog f = (Dialog)dialog;
                        EditText msgEditText = (EditText)f.findViewById(R.id.editTextSendMsg);
                        String sendMsg = msgEditText.getText().toString();
                        encrypt(sendMsg);

                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        builder.create().show();
    }
    public void dialogReceive(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = this.getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.dialog_save_file, null))
                // Add action buttons
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        final String TAG = "Dialog receive msg";
                        Dialog f = (Dialog)dialog;

                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        builder.create().show();
    }

    public void encrypt(String s)
    {
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap canvasBit = Bitmap.createBitmap(512,512, conf);
        Mat canvasMat=new  Mat(canvasBit.getWidth(),canvasBit.getHeight(), CvType.CV_8UC4,new Scalar(255,255,255,255));
        int pWidth=0;
        Calendar c = Calendar.getInstance();
        int minutes = (c.get(Calendar.SECOND)+1)%10;
        Toast.makeText(getApplicationContext(),""+s,Toast.LENGTH_SHORT).show();
        for(int i=0;i<s.length();i++) {
            int ency=(Integer.parseInt(""+s.charAt(i))+minutes)%10;
            File imgFile = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyCameraApp/Set/" +ency+ ".jpg");
            if (imgFile.exists()) {

                Bitmap bit = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

                Mat mat=new Mat(bit.getWidth(),bit.getHeight(), CvType.CV_8UC4);
                Utils.bitmapToMat(bit,mat);
                Imgproc.resize(mat,mat,new Size(mat.size().width*0.3,mat.size().height*0.3));
                if(i<=0) {
                    mat.copyTo(canvasMat.rowRange(2, 2+mat.height()).colRange(2, 2+mat.width()));
                    pWidth=mat.width()+2;
                }
                else
                {
                    if(10+pWidth+mat.width()>canvasMat.cols())
                    {
                        Utils.matToBitmap(canvasMat,canvasBit);
                        Matrix matrix = new Matrix();
                        // RESIZE THE BIT MAP
                        matrix.postScale(2, canvasBit.getHeight());
                        Bitmap resizedBitmap = Bitmap.createBitmap(
                              canvasBit  , 0, 0, canvasBit.getWidth(), canvasBit.getHeight(), matrix, false);
                        canvasBit.recycle();
                        Utils.bitmapToMat(resizedBitmap,canvasMat);
                        mat.copyTo(canvasMat.rowRange(2, 2 + mat.height()).colRange(pWidth + 2, 2 + pWidth + mat.width()));
                        pWidth = pWidth + mat.width() + 2;
                    }
                    else {
                        mat.copyTo(canvasMat.rowRange(2, 2 + mat.height()).colRange(pWidth + 2, 2+ pWidth + mat.width()));
                        pWidth = pWidth + mat.width() + 2;
                    }
                }

            }
        }
        String key=""+minutes;
        pWidth=0;
        for(int i=0;i<key.length();i++) {

            File imgFile = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyCameraApp/Set/" +key.charAt(i)+ ".jpg");
            if (imgFile.exists()) {

                Bitmap bit = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

                Mat mat=new Mat(bit.getWidth(),bit.getHeight(), CvType.CV_8UC4);
                Utils.bitmapToMat(bit,mat);
                Imgproc.resize(mat,mat,new Size(mat.size().width*0.3,mat.size().height*0.3));
                if(i<=0) {
                    mat.copyTo(canvasMat.rowRange(256, 256+mat.height()).colRange(10, 10+mat.width()));
                    pWidth=mat.width()+10;
                }
                else
                {

                    mat.copyTo(canvasMat.rowRange(256,256+ mat.height()).colRange(pWidth+10, 10+pWidth+mat.width()));
                    pWidth=pWidth+mat.width()+10;
                }

            }
        }
        int size=(int)canvasMat.total()*canvasMat.channels();
        byte[] b=new byte[size];
        canvasMat.get(0,0,b);
        for(int i=0;i<size;)
        {
            int r = b[i]& 0xFF;
            int g=b[i+1]&0xFF;
            int a=b[i+2]&0xFF;
            Random rand=new Random();
            int total=(r+g+a)/3;
            if(total>210) {

                if(rand.nextBoolean()) {
                    b[i] = (byte) (0);
                    b[i + 1] = (byte) (0);
                    b[i + 2] = (byte) (0);
                }else
                {
                    b[i] = (byte) 255;
                    b[i + 1] = (byte) 255;
                    b[i + 2] = (byte) 255;
                }
            }else if(total<10)
            {
                if(rand.nextBoolean()) {
                    b[i] = (byte) (1);
                    b[i + 1] = (byte) (1);
                    b[i + 2] = (byte) (1);
                }
                else
                {
                    b[i] = (byte) 254;
                    b[i + 1] = (byte) 254;
                    b[i + 2] = (byte) 254;
                }
            }
            else
            {
                if(rand.nextBoolean()) {
                    b[i] = (byte) (1);
                    b[i + 1] = (byte) (1);
                    b[i + 2] = (byte) (1);
                }
                else
                {
                    b[i] = (byte) 254;
                    b[i + 1] = (byte) 254;
                    b[i + 2] = (byte) 254;
                }
            }

            i=i+4;
        }

        canvasMat.put(0,0,b);

        Utils.matToBitmap(canvasMat,canvasBit);

        saveImage(canvasBit, s);



    }
    public void saveImage(Bitmap b,String name){
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyCameraApp/"+"Encrypt");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
            }
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(new File(mediaStorageDir.toString()+"/"+name+".jpg"));
            b.compress(Bitmap.CompressFormat.PNG,100, fos);
            Toast.makeText(getApplicationContext(),"Encrypted",Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

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
