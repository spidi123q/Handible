package lukeentertainment.example;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
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
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
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
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedWriter;
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

import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST_FOR_TRAIN = 25 ;
    private static String TAG = "MaintActivity";
    private static final int PICK_IMAGE_REQUEST=10;

    DatabaseOperations db;
    Button loadImage,cvtImage,aditiveLearn;
    FloatingActionButton fab,trainFab,trainAppendFab;
    ImageView imageView;
    TextView itt;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.temp);
        Activity activity = this;
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(activity, R.color.colorPrimary));
        db=new DatabaseOperations(getApplicationContext());
        lyProduct=(ListView)findViewById(R.id.list_view);
        refreshList();
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
                        dialogReceive(getCurrentFocus());
                        break;

                }
                return false;
            }
        });



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST_FOR_TRAIN && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();
            Intent i=new Intent(MainActivity.this,TrainActivity.class);
            i.putExtra("ImageURI",uri);
            i.putExtra("trainoption",trainAppendTrue);
            startActivity(i);
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
                        Log.e(TAG, "onClick: "+sendMsg );

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



}
