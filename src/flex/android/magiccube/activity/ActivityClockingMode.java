package flex.android.magiccube.activity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import flex.android.magiccube.DBHelper;
import flex.android.magiccube.MagiccubePreference;
import flex.android.magiccube.MusicPlayThread;
import flex.android.magiccube.R;
import flex.android.magiccube.Util;
import flex.android.magiccube.dialog.DialogClockingMode;
import flex.android.magiccube.dialog.DialogSetting;
import flex.android.magiccube.interfaces.OnStateListener;
import flex.android.magiccube.interfaces.OnStepListener;
import flex.android.magiccube.interfaces.OnTimerListener;
import flex.android.magiccube.view.ViewClockingMode;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.hardware.SensorEvent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityClockingMode extends ActivitySensorListener implements OnTimerListener, OnStateListener, OnStepListener{
	private ImageButton btStartOb;
	private ImageButton btMoveBack;
	private ImageButton btMoveForward;
	private SeekBar progress;
	private ImageView clock;
	//private ImageView imgTime;
	private ImageView loading;
	
	private LinearLayout layout_moveforward;
	private LinearLayout layout_moveback;
	
	private ImageView img_nstep;
	private TextView txtTime;
	private TextView txtNStep;
	private ViewClockingMode glView;
	
	private MediaPlayer GamingbgmPlayer;
	private MediaPlayer ObservingbgmPlayer;
	private MediaPlayer FinishbgmPlayer;
	
	private int TotalObTime;	//in seconds, time to observe
	private int State;
	private int MoveTime;
	private int nStep;
	private int LeftObTime;
	
	private Animation scale;
	
	private float LinearBgVolume;
	private int BgVolume;
	
	private Bitmap []bmpdigit = new Bitmap[11];	//bmp for digit
	private int TimeImgWidth, TimeImgHeight;
	
	private DialogClockingMode dialog;
	private DialogSetting dialog2;
	
	private int width;
	private int height;
	
	private long CreateTime;
	private long MinLoadingTime = 1500;
	
	private String MoveTimes = "";
	
	private DBHelper dbHelper = null;
	private final String TableName = "flex_magiccube_replay";
	private final String TableContent = "_id integer primary key autoincrement, cmdstrbefore text, cmdstrafter text, savetime datetime, movetimes text";
	
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case 0:		//make the progressbar disappear
				//clock.setVisibility(View.INVISIBLE);
				progress.setVisibility(View.INVISIBLE);
				break;
			case 1:	
	    		Animation scaleOut = AnimationUtils.loadAnimation(ActivityClockingMode.this,R.anim.scale_anim_out);
	    		btStartOb.startAnimation(scaleOut);
				btStartOb.setVisibility(View.GONE);
				clock.setVisibility(View.VISIBLE);
				progress.setVisibility(View.VISIBLE);
				break;
			case 2:		//make the move back forward button appear
				btMoveBack.setVisibility(View.VISIBLE);
				btMoveForward.setVisibility(View.VISIBLE);
				txtTime.setVisibility(View.VISIBLE);
				txtNStep.setVisibility(View.VISIBLE);
				img_nstep.setVisibility(View.VISIBLE);
				//ActivityClockingMode.this.SetCanVibrate(true);
				break;
				//imgTime.setVisibility(View.VISIBLE);
			case 3:
				//setTime(ActivityClockingMode.this.MoveTime);
			case 4:
				txtTime.setText(""+Util.TimeFormat(ActivityClockingMode.this.MoveTime));
				txtNStep.setText(""+nStep);
				break;
			case 5:
				dialog = new DialogClockingMode(ActivityClockingMode.this, "完成！", MoveTime);
				dialog.show();
				break;
			case 6:
				loading.setVisibility(View.GONE);
		        btStartOb.startAnimation(scale);
				break;
			case 7:
				progress.setProgress(LeftObTime);
				break;
				
			}
		}
	};
	
	private Handler buttonhandler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			switch(msg.what)
			{
			case 0:
				btMoveForward.setBackgroundResource(R.drawable.buttons_moveforward);
				break;
			case 1:
				btMoveForward.setBackgroundResource(R.drawable.buttons_moveforward_unable);
				break;
			case 2:
				btMoveBack.setBackgroundResource(R.drawable.buttons_moveback);
				break;
			case 3:
				btMoveBack.setBackgroundResource(R.drawable.buttons_moveback_unable);
				break;
			}
		}
	};
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mode_clocking);
        btMoveBack = (ImageButton)findViewById(R.id.bt_clocking_moveback);
        btMoveForward = (ImageButton)findViewById(R.id.bt_clocking_moveforward);
        btStartOb = (ImageButton)findViewById(R.id.bt_clocking_startob);
        clock = (ImageView) findViewById(R.id.img_clocking_clock);
        img_nstep = (ImageView)findViewById(R.id.img_clocking_nstep);
        txtTime = (TextView)findViewById(R.id.txt_clocking_time);
        txtNStep = (TextView)findViewById(R.id.txt_clocking_nStep);
        //imgTime = (ImageView)findViewById(R.id.img_clocking_time);
        progress = (SeekBar) findViewById(R.id.bt_clocking_timer);
        
        glView = (ViewClockingMode)findViewById(R.id.game_view_clocking);
        
        
        
        btMoveBack.setOnClickListener(buttonListener);
        btMoveForward.setOnClickListener(buttonListener);
        btStartOb.setOnClickListener(buttonListener);
        
        
        layout_moveforward = (LinearLayout)findViewById(R.id.layout_clocking_moveforward);
        layout_moveback = (LinearLayout)findViewById(R.id.layout_clocking_moveback);
        
        loading = (ImageView)findViewById(R.id.clocking_loading);
        int[] loadingresources = {R.drawable.loading4, R.drawable.loading5, R.drawable.loading10, R.drawable.loading11};
        Random r = new Random();
        loading.setImageResource(loadingresources[r.nextInt(4)]);
        
        float fontsize = 25.f;
        Typeface typeFace =Typeface.createFromAsset(getAssets(),"fonts/Angies New House.ttf");
        txtTime.setTypeface(typeFace);
        txtTime.setTextColor(Color.GRAY);
        txtTime.setTextSize(fontsize);
        
        txtNStep.setTypeface(typeFace);
        txtNStep.setTextColor(Color.GRAY);
        txtNStep.setTextSize(fontsize);
        
        glView.SetOnTimerListener(this);
        glView.setOnStateListener(this);
        glView.SetStepListener(this);
        
        scale = AnimationUtils.loadAnimation(this,R.anim.scale_anim);
        
        dbHelper = null;
        
        BgVolume = MagiccubePreference.GetPreference(MagiccubePreference.BgVolume, this);
        LinearBgVolume = MusicPlayThread.GetLinearVolume(BgVolume);
        
        State = OnStateListener.LOADING;	
        Init();
/*        for(int i=0; i<10; i++)
        {
        	bmpdigit[i]	= BitmapFactory.decodeStream(
 	               getResources().openRawResource(digitsrc[i]));
        }
        
        bmpdigit[10] = BitmapFactory.decodeStream(
	               getResources().openRawResource(R.drawable.fg));
        
        TimeImgWidth = bmpdigit[0].getWidth()*6 + bmpdigit[10].getWidth()*2;
        TimeImgHeight = bmpdigit[0].getHeight();*/
        WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        
        width = wm.getDefaultDisplay().getWidth();
        height = wm.getDefaultDisplay().getHeight();
        
        int buttonwidth = width/5;
        int buttonheight = height/13;
        
		LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(buttonwidth, buttonheight);
		
		RelativeLayout.LayoutParams parms2 = new RelativeLayout.LayoutParams(buttonwidth, buttonheight);
		//btStartOb.setLayoutParams(parms2);
		
		btMoveForward.setLayoutParams(parms);
		btMoveBack.setLayoutParams(parms);
		
		layout_moveforward.setPadding(width/2-buttonwidth/2, height-buttonheight, 0, 0);
		layout_moveback.setPadding(width/15, height-buttonheight, 0, 0);
		
		CreateTime = System.currentTimeMillis();
    }
    
    private void Init()
    {
        GamingbgmPlayer = MediaPlayer.create(this, R.raw.bg2);
        ObservingbgmPlayer = MediaPlayer.create(this, R.raw.bg);
        FinishbgmPlayer = MediaPlayer.create(this, R.raw.finish);
        
        GamingbgmPlayer.setVolume(LinearBgVolume,LinearBgVolume);
        ObservingbgmPlayer.setVolume(LinearBgVolume,LinearBgVolume);
        FinishbgmPlayer.setVolume(LinearBgVolume,LinearBgVolume);
        
        TotalObTime = 10;
        progress.setEnabled(false);
        progress.setMax(TotalObTime);
        progress.setProgress(TotalObTime);
		//progress.setAlpha(1.f);
		//progress.setBackgroundColor(2);
		
		//clock.setVisibility(View.INVISIBLE);
		btStartOb.setVisibility(View.VISIBLE);
		//progress.setVisibility(View.INVISIBLE);
		txtTime.setVisibility(View.INVISIBLE);
		//clock.setVisibility(View.INVISIBLE);
		txtNStep.setVisibility(View.INVISIBLE);
		img_nstep.setVisibility(View.INVISIBLE);
		btMoveBack.setVisibility(View.INVISIBLE);
		btMoveForward.setVisibility(View.INVISIBLE);
		
        glView.SetCanMove(false);
        glView.SetCanRotate(false);
        glView.SetDrawCube(false);
        glView.SetTotalObTime(TotalObTime);
        
        
        nStep = 0;
        
        this.SetCanVibrate(false);
    }
    
    private void Init2()
    {
    	clock.setVisibility(View.INVISIBLE);
    	Init();
    	State = OnStateListener.LOADED;
    }
    
    public void Reset()
    {
    	MoveTimes = "";
    	Init2();
    	glView.Reset();
    	btStartOb.startAnimation(scale);
    }
    
    public void SaveReplay()
    {
    	if( dbHelper == null)
    	{
    		dbHelper = new DBHelper(this);
    	}
    	
    	//dbHelper.execute("drop table "+TableName);
    	dbHelper.create(TableName, TableContent);
        ContentValues values = new ContentValues();  
        values.put("cmdstrbefore", this.GetCmdStrBefore());  
        values.put("cmdstrafter", this.GetCmdStrAfter());  
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
        Date date = new Date();
        String datestr = dateFormat.format(date);
        values.put("savetime", datestr);
        
        values.put("movetimes", this.MoveTimes);
        
    	dbHelper.insert(TableName, values);
    	
    	Toast toast = Toast.makeText(getApplicationContext(),
    		     "录像已保存至"+datestr, Toast.LENGTH_LONG);
    	toast.setGravity(Gravity.CENTER, 0, 0);
    	toast.show();
    }
    
    private Button.OnClickListener buttonListener = new Button.OnClickListener()
    {

		@Override
		public void onClick(View v) {
			if( State == OnStateListener.LOADING)
			{
				return;
			}
			
			if(v == btMoveBack)
			{
				glView.MoveBack();
				return;
			}
			else if(v == btMoveForward)
			{
				glView.MoveForward();
				return;
			}
			else if(v == btStartOb)
			{
				handler.sendEmptyMessage(1);
				
				glView.SetCanRotate(true);
				glView.SetDrawCube(true);
				
				glView.StartObserve();
				
				ObservingbgmPlayer.setLooping(true);//设置循环播放
				ObservingbgmPlayer.start();
				
				State = OnStateListener.OBSERVING;
				
			}
		}
    };
    
	@Override
	public void onTimer(int nTime) {
		// TODO Auto-generated method stub
		if( State == OnStateListener.OBSERVING)
		{	
			LeftObTime = nTime;
			handler.sendEmptyMessage(7);
			if(nTime <5 && nTime >=1)
			{
				MusicPlayThread musicPlay = new MusicPlayThread(this, R.raw.onesecond, BgVolume);
				musicPlay.start();
			}
			else if( nTime == 0)
			{
				MusicPlayThread musicPlay = new MusicPlayThread(this, R.raw.lastsecond, BgVolume);
				musicPlay.start();
			}
		}
		else if( State == OnStateListener.GAMING)
		{
			handler.sendEmptyMessage(3);
			MoveTime = nTime;
		}
	}
	
	@Override
	public void OnStateChanged(int StateMode) {
		// TODO Auto-generated method stub
		
		
		if(StateMode == OnStateListener.GAMING)
		{
			glView.SetCanMove(true);
			glView.SetCanRotate(true);
			glView.StopObserve();
			
			handler.sendEmptyMessage(0);
			handler.sendEmptyMessage(2);
			handler.sendEmptyMessage(3);
			
			GamingbgmPlayer.setLooping(true);//设置循环播放
			GamingbgmPlayer.start();
			ObservingbgmPlayer.pause();
		}
		else if( StateMode == OnStateListener.FINISH)
		{
			GamingbgmPlayer.stop();
			FinishbgmPlayer.setLooping(false);
			FinishbgmPlayer.start();
			handler.sendEmptyMessage(5);
		}
		else if( StateMode == OnStateListener.LOADED)
		{
			if( State != OnStateListener.LOADING)
			{
				return;
			}
			
			Long TimeLap = System.currentTimeMillis()-CreateTime;
			if(TimeLap >= MinLoadingTime)
			{
				handler.sendEmptyMessage(6);
			}
			else
			{
				Timer t = new Timer();
		        t.schedule(new TimerTask(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						handler.sendEmptyMessage(6);;
					}
		        }, MinLoadingTime-TimeLap);
			}
			
		}
		State = StateMode;
	}
	
	@Override
	public void finish()
	{
		//Log.e("finish","finish");
		super.finish();
/*		if( this.State == OnStateListener.OBSERVING)
		{
	    	ObservingbgmPlayer.stop();
	    	ObservingbgmPlayer.release();
		}
		if( this.State == OnStateListener.GAMING)
		{
	    	GamingbgmPlayer.stop();
	    	GamingbgmPlayer.release();
		}*/
/*	    if( GamingbgmPlayer.isPlaying())
	    {
	    	GamingbgmPlayer.stop();
	    	GamingbgmPlayer.release();
	    }
	    if( ObservingbgmPlayer.isPlaying())
	    {
	    	ObservingbgmPlayer.stop();
	    	ObservingbgmPlayer.release();
	    }*/
	    if( dbHelper != null)
	    {
	    	dbHelper.close();
	    }
	    glView.onStop();
	}
	
	@Override
    public void onStop()
	{
	    // Stop play
	    
/*	    if( GamingbgmPlayer.isPlaying())
	    {
	    	GamingbgmPlayer.stop();
	    	GamingbgmPlayer.release();
	    }
	    if( ObservingbgmPlayer.isPlaying())
	    {
	    	ObservingbgmPlayer.stop();
	    	ObservingbgmPlayer.release();
	    }
	    if( dbHelper != null)
	    {
	    	dbHelper.close();
	    }*/
		//Log.e("onstop","onstop");
	    super.onStop();
	}
	


	@Override
	public void AddStep() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void SetStep(int nStep) {
		// TODO Auto-generated method stub
		this.nStep = nStep;
		if( MoveTimes == "")
		{
			MoveTimes += this.MoveTime;
		}
		else
		{
			MoveTimes += " "+this.MoveTime;
		}
		handler.sendEmptyMessage(4);
	}
	
	public void setTime(int nTime)
	{
		int curwidth = 0;
		String strtime = Util.TimeFormat(nTime);
		//Log.e("e",TimeImgHeight+"");

		Bitmap bmptime = Bitmap.createBitmap(this.TimeImgWidth, this.TimeImgHeight, Config.ARGB_8888);
		Canvas canvas = new Canvas(bmptime);
		for(int i=0; i<8; i++)
		{
			if( i== 2 || i==5)
			{
				canvas.drawBitmap(this.bmpdigit[10], curwidth, 0, null);
				curwidth += this.bmpdigit[10].getWidth();
			}
			else
			{
				int d = Integer.parseInt(String.valueOf(strtime.charAt(i)));
				canvas.drawBitmap(this.bmpdigit[d], curwidth, 0, null);
				curwidth += this.bmpdigit[d].getWidth();
			}
		}
		
		Canvas canvas2 = new Canvas(bmptime);
		Paint paint = new Paint();
		paint.setFilterBitmap(true);
		Matrix matrix = new Matrix();
		matrix.reset();
		matrix.preScale(0.5f, 0.5f);
		canvas2.drawBitmap(bmptime, matrix, null);
		
		//imgTime.setImageBitmap(bmptime);
	}
	
	private String GetCmdStrAfter()
	{
		return this.glView.GetCmdStrAfter();
	}
	
	private String GetCmdStrBefore()
	{
		return this.glView.GetCmdStrBefore();
	}
		
    private Runnable autosolverun = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			glView.AutoSolve2("Jaap");
			//glView.SetCanMove(true);
		}
	};
	@Override
	public void AddStep(int nStep) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		glView.onPause();
		
		if( this.State == OnStateListener.OBSERVING)
		{
			if( ObservingbgmPlayer != null)
			{
				this.ObservingbgmPlayer.pause();
			}
		}
		if( this.State == OnStateListener.GAMING)
		{
			if( GamingbgmPlayer != null)
			{
				this.GamingbgmPlayer.pause();
			}
		}
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		glView.onResume();
		if( this.State == OnStateListener.OBSERVING)
		{
			this.ObservingbgmPlayer.start();
		}
		if( this.State == OnStateListener.GAMING)
		{
			this.GamingbgmPlayer.start();
		}
	}

	@Override
	public void OnStateNotify(int StateMode) {
		// TODO Auto-generated method stub
		//Log.e("statemode","statemode");
		switch(StateMode)
		{
		case OnStateListener.CANMOVEFORWARD:
			//Log.e("CANMOVEFORWARD", "CANMOVEFORWARD");
			buttonhandler.sendEmptyMessage(0);
			break;
		case OnStateListener.CANNOTMOVEFORWARD:
			buttonhandler.sendEmptyMessage(1);
			break;
		case OnStateListener.CANMOVEBACK:
			//Log.e("CANMOVEBACK", "CANMOVEBACK");
			buttonhandler.sendEmptyMessage(2);
			break;
		case OnStateListener.CANNOTMOVEBACK:
			buttonhandler.sendEmptyMessage(3);
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if( State == OnStateListener.LOADING)
		{
			return false;
		}
		if(keyCode == KeyEvent.KEYCODE_MENU)
		{
			glView.onPause();
			dialog2 = new DialogSetting(this, settinghandler);
			dialog2.show();
			return false;
		}
		else if(keyCode == KeyEvent.KEYCODE_BACK)
		{
			if( this.State == OnStateListener.LOADED)
			{
				return super.onKeyDown(keyCode, event);
			}
			showExitGameAlert();
			return false;
		}
		return super.onKeyDown(keyCode, event);
		//return super.onKeyDown(keyCode, event);
	}
	
    private Handler settinghandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case 0:
				glView.onResume();
				break;
			case 1:		//make the progressbar disappear
		        int volume = MagiccubePreference.GetPreference(MagiccubePreference.BgVolume, ActivityClockingMode.this);
		        BgVolume = volume;
		        GamingbgmPlayer.setVolume(MusicPlayThread.GetLinearVolume(volume), MusicPlayThread.GetLinearVolume(volume));
		        ObservingbgmPlayer.setVolume(MusicPlayThread.GetLinearVolume(volume), MusicPlayThread.GetLinearVolume(volume));
		        FinishbgmPlayer.setVolume(MusicPlayThread.GetLinearVolume(volume), MusicPlayThread.GetLinearVolume(volume));
		        setMinVibration(MagiccubePreference.GetPreference(MagiccubePreference.MinVibration, ActivityClockingMode.this));
		        
		        glView.setVolume(MagiccubePreference.GetPreference(MagiccubePreference.MoveVolume, ActivityClockingMode.this));
				glView.SetSensitivity((float)MagiccubePreference.GetPreference(MagiccubePreference.Sensitivity, ActivityClockingMode.this)/100.f);
		        glView.onResume();
				break;
			}
		}
    };
	
	@Override
	public void onShake() {
		// TODO Auto-generated method stub
		this.SetCanVibrate(false);
		this.glView.SetCanMove(false);
		new Thread(autosolverun).start();
	}
	
    private void showExitGameAlert(){
    	new AlertDialog.Builder(this).setTitle("确认离开"). 
    	setMessage("离开后,游戏进度将不能保存。").
    	setIcon(R.drawable.ic_exit)  
        .setPositiveButton("确定", new DialogInterface.OnClickListener() {  
      
            @Override  
            public void onClick(DialogInterface dialog, int which) {  
            // 点击“确认”后的操作  
            	ActivityClockingMode.this.finish();  
      
            }  
        })  
        .setNegativeButton("取消", new DialogInterface.OnClickListener() {  
      
            @Override  
            public void onClick(DialogInterface dialog, int which) {  
            // 点击“返回”后的操作,这里不设置没有任何操作  
            }  
        }).show();  
    }
    
    
}



