package flex.android.magiccube;

import android.content.ContentValues;  
import android.content.Context;  
import android.database.Cursor;  
import android.database.sqlite.SQLiteDatabase;  
import android.database.sqlite.SQLiteOpenHelper;  
public class DBHelper extends SQLiteOpenHelper {  
    private static final String DB_NAME = "flex_magiccube_db";  
    private static final String TBL_NAME = "CollTbl";  
    private static final String CREATE_TBL = " create table "  
            + " CollTbl(_id integer primary key autoincrement,name text,url text, desc text) ";  
      
    private SQLiteDatabase db;  
    public DBHelper(Context c) {  
        super(c, DB_NAME, null, 2);  
    }  
    @Override  
    public void onCreate(SQLiteDatabase db) {  
        this.db = db;  
        //db.execSQL(CREATE_TBL);  
    }  
    
    public void create(String TableName, String TableContent)
    {
        if (db == null)  
            db = getWritableDatabase();  
    	db.execSQL("create table if not exists " +
    			TableName + "(" + TableContent + ")");
    }
    
    public void execute(String sql)
    {
        if (db == null)  
            db = getWritableDatabase();  
    	db.execSQL(sql);
    }
    
    public void insert(String TableName, ContentValues values) {  
        if (db == null)  
            db = getWritableDatabase();  
        db.insert(TableName, null, values);  
        //db.close();  
    }  
    public Cursor query(String TableName) {  
        if (db == null)  
            db = getWritableDatabase();  
        Cursor c = db.query(TableName, null, null, null, null, null, null);  
        return c;  
    }  
    public Cursor query(String TableName, String WhereClaus) {  
        if (db == null)  
            db = getWritableDatabase();  
        Cursor c = db.query(TableName, null, WhereClaus, null, null, null, null);
        return c;  
    }  
    public void del(int id) {  
        if (db == null)  
            db = getWritableDatabase();  
        db.delete(TBL_NAME, "_id=?", new String[] { String.valueOf(id) });  
    }  
    public void clear(String TableName)
    {
        if (db == null)  
            db = getWritableDatabase();  
        db.execSQL("delete from " + TableName);
    }
    
    public void exec(String sqlstr)
    {
        if (db == null)  
            db = getWritableDatabase();  
        db.execSQL(sqlstr);
    }
    public void close() {  
        if (db == null)  
            db = getWritableDatabase();  
        db.close();  
    }  
    @Override  
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {  
    }  
}