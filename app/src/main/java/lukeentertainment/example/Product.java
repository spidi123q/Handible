package lukeentertainment.example;

import android.widget.ImageView;

/**
 * Created by Nakul on 4/2/2017.
 */

public class Product {
    private int id;
    private String name;
    private String imageIconPath;
    private String date;
    private int items;

    public Product(String name,int id,String date,int items,String path) {

        this.id = id;
        this.items=items;
        this.name = name;
        this.date=date;
        this.imageIconPath=path;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImageIconPath() {
        return imageIconPath;
    }
    public int getItems(){
        return items;
    }
    public String getDate(){
        return date;
    }
}
