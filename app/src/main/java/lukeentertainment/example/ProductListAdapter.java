package lukeentertainment.example;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

/**
 * Created by Nakul on 4/2/2017.
 */

public class ProductListAdapter extends BaseAdapter {
    private Context context;
    private List<Product> productList;
    TextView prDate;
    TextView prName;
    View v;

    public ProductListAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @Override
    public int getCount() {
        return productList.size();
    }

    @Override
    public Object getItem(int position) {
        return productList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        v=View.inflate(context,R.layout.prodeuct_item_list,null);
        prName=(TextView)v.findViewById(R.id.product_name);
        prDate=(TextView)v.findViewById(R.id.project_date);
        ImageView prIcon=(ImageView)v.findViewById(R.id.icon_iv);
        prName.setText(productList.get(position).getName().toString());
        prDate.setText(productList.get(position).getDate().toString());
        if(!productList.get(position).getImageIconPath().equals("null"))
        {

            File image = new File(productList.get(position).getImageIconPath());
            Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath());
            bitmap = Bitmap.createScaledBitmap(bitmap,prIcon.getWidth(),prIcon.getHeight(),true);
            prIcon.setImageBitmap(bitmap);
        }
        v.setTag(productList.get(position).getId());
        return v;
    }
}
