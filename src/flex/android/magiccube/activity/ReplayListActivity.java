package flex.android.magiccube.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import flex.android.magiccube.DBHelper;
import flex.android.magiccube.R;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ReplayListActivity extends Activity{
	
    private ArrayAdapter<String> mReplayDatetimeArrayAdapter;
    private Cursor c = null;

    private DBHelper dbhelper = null;
	private final String TableName = "flex_magiccube_replay";
	private final String TableContent = "_id integer primary key autoincrement, cmdstrbefore text, cmdstrafter text, savetime datetime, movetimes text";
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.replay_list);

        // Set result CANCELED incase the user backs out
        setResult(Activity.RESULT_CANCELED);

        // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
        mReplayDatetimeArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
       
        // Find and set up the ListView for paired devices
        ListView datetimeListView = (ListView) findViewById(R.id.replay_datetime);
        datetimeListView.setAdapter(mReplayDatetimeArrayAdapter);

        // If there are paired devices, add each one to the ArrayAdapter
        if( dbhelper == null)
        {
        	dbhelper = new DBHelper(this);
        }
        //dbhelper.execute("drop table "+TableName);
        dbhelper.create(TableName, TableContent);
        
        c = dbhelper.query(TableName);
        
        if( c.getCount() <= 0)
        {
            ContentValues values = new ContentValues();  
            
            values.put("cmdstrbefore", "0211 0211 1211 0211");  
            values.put("cmdstrafter", "1201 1211 2001 2001 2011 2011 1201 0211 0201 2201 0211 0201 2211 1011 0211 0211 2001 1001 2201 2201 1011 2011 0202 1011 1201 0011 1211 0202 2202 0201 2202 0202 1212 0011 0202 1002 2202 0012 1002 0202 2012 0202 1002 2202");  
            values.put("savetime", "2013-10-08 17:43:58");
            values.put("movetimes", "0 2 3 4 8 8 9 12 15 17 18 19 20 23 24 25 26 30 32 33 37 38 39 48 58 61 62 63 65 70 88 112 114 122 125 128 130 133 134 135 137 139 141 142");
            dbhelper.insert(TableName, values);
            
            c = dbhelper.query(TableName);
        }
        

        if(c.getCount() > 0)
        {
	        c.moveToFirst();
	        
	    	do
	    	{
	    		mReplayDatetimeArrayAdapter.add(c.getString(3));
	    		//mReplayCmdStrBefore.add(c.getString(1));
	    		//mReplayCmdStrAfter.add(c.getString(2));
	    	}while(c.moveToNext());
	    	datetimeListView.setOnItemClickListener(mReplayClickListener);
        }
        else
        {
        	mReplayDatetimeArrayAdapter.add("没有录像");
        }
        
        //dbhelper.exec("drop table "+TableName);
        //SELECT name FROM sqlite_master WHERE type='table' AND name='table_name'
        
        /*Cursor cursor = dbhelper.query("sqlite_master", "type='table' AND name='"+TableName+"'");
        
        if( cursor.getCount() > 0)
        {
	        c = dbhelper.query(TableName);
	        
	        if(c.getCount() > 0)
	        {
		        c.moveToFirst();
		        
		    	do
		    	{
		    		mReplayDatetimeArrayAdapter.add(c.getString(3));
		    		//mReplayCmdStrBefore.add(c.getString(1));
		    		//mReplayCmdStrAfter.add(c.getString(2));
		    	}while(c.moveToNext());
		    	datetimeListView.setOnItemClickListener(mReplayClickListener);
	        }
	        else
	        {
	        	mReplayDatetimeArrayAdapter.add("没有录像");
	        }
        }
        else
        {
        	mReplayDatetimeArrayAdapter.add("没有录像");
        }*/
    }
    
    // The on-click listener for all devices in the ListViews
    private OnItemClickListener mReplayClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            finish();
			Intent intent = new Intent(ReplayListActivity.this,ActivityReplayMode.class);
	    	c.moveToFirst();
			do
	    	{
				if( c.getString(3).equals(((TextView)v).getText().toString().trim()))
				{
					intent.putExtra("CmdStrBefore", c.getString(1));
					intent.putExtra("CmdStrAfter", c.getString(2));
					intent.putExtra("movetimes", c.getString(4));
					break;
				}
	    	}while(c.moveToNext());
			startActivity(intent);
        }
    };
}
