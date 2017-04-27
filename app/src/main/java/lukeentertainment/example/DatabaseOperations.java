package lukeentertainment.example;

import lukeentertainment.example.TableData.TableInfo;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.IntegerRes;
import android.util.Log;


/**
 * Created by Nakul on 4/2/2017.
 */
public class DatabaseOperations extends SQLiteOpenHelper {
    public static int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME="Handible.db";
    public static final String PROJECT_LIST_TABLE="project_list";
    public static final String PROJECT_ID="Project_id";
    public static final String PROJECT_NAME="Project_name";
    public static final String PROJECT_DATE="Project_date";
    public static final String PROJECT_ITEMS="Project_items";
    public static final String PROJECT_ICON_PATH="Project_icon_path";

    public static final String CONTENT_TABLE="Content_list";
    public static final String CONTENT_ID="Content_id";
    public static final String PARENT_ID="Parent_id";
    public static final String CONTENT_PATH="Content_path";


    public DatabaseOperations(Context cntxt) {
        super(cntxt, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d("Database Operations ", "database created");

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table "+PROJECT_LIST_TABLE+"("+PROJECT_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+PROJECT_NAME+" text,"+PROJECT_DATE+" text,"+PROJECT_ITEMS+" integer default 0,"+PROJECT_ICON_PATH+" text);");
        db.execSQL("create table "+CONTENT_TABLE+"("+PARENT_ID+" INTEGER,"+CONTENT_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+CONTENT_PATH+" TEXT);");

    }

    public boolean addRowProjectList(String name,String date,int items,String path)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put(PROJECT_NAME,name);
        cv.put(PROJECT_DATE,date);
        cv.put(PROJECT_ITEMS,items);
        cv.put(PROJECT_ICON_PATH,path);
        long result=db.insert(PROJECT_LIST_TABLE,null,cv);
        if(result==-1)
            return  false;
        return true;
    }
    public boolean addRowContentList(int parent ,String path)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put(PARENT_ID,parent);
        cv.put(CONTENT_PATH,path);
        long result=db.insert(CONTENT_TABLE,null,cv);
        if(result==-1)
            return  false;
        return true;
    }

    public Cursor getAlldata()
    {
        SQLiteDatabase db=this.getWritableDatabase();
        Cursor cr=db.rawQuery("select * from "+PROJECT_LIST_TABLE,null);
        return cr;
    }
    public Cursor getAllDataContentTable()
    {
        SQLiteDatabase db=this.getWritableDatabase();
        Cursor cr=db.rawQuery("select * from "+CONTENT_TABLE,null);
        return cr;
    }
    public Cursor getAlldataFromContentTable(int parent)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        Cursor cr=db.rawQuery("select * from "+CONTENT_TABLE+" WHERE "+PARENT_ID+" = ?",new String[]{Integer.toString(parent)});
        return cr;
    }
    public void updateProjectList(int id,String colom, int value)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        db.execSQL("UPDATE "+PROJECT_LIST_TABLE+" SET "+colom+" = "+ Integer.toString(value)+" WHERE "+PROJECT_ID+" = "+Integer.toString(id));
    }
    public void updateProjectListIcon(int id,String colom,String icon)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        db.execSQL("UPDATE "+PROJECT_LIST_TABLE+" SET "+colom+" = '"+icon+"' WHERE "+PROJECT_ID+" = "+Integer.toString(id));
    }
    public void deleteFromProjectList(String query)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        db.execSQL(query);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TableInfo.TABLE_NAME);
        onCreate(db);
    }
}
