package lukeentertainment.example;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

/**
 * Created by Nakul on 4/2/2017.
 */

public class ImageAdapter extends BaseAdapter {
    private Context context;
    private int id;
    DatabaseOperations db;
    Cursor cr;
    int count;

    public ImageAdapter(Context c,int parent) {
        context=c;
        id=parent;
        db=new DatabaseOperations(context);
        cr=db.getAlldataFromContentTable(id);

        count= cr.getCount();
    }

    @Override
    public int getCount() {

        return count;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams(300, 300));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }

        if(cr.moveToPosition(position)) {
            String path = cr.getString(2);
            File imgFile = new  File(path);
            if(imgFile.exists()){

                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                imageView.setImageBitmap(myBitmap);

            }else
            {
                Toast.makeText(context,"no files",Toast.LENGTH_SHORT).show();
            }

        }

        return imageView;

    }


}
