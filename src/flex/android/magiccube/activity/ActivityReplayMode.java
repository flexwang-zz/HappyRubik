package flex.android.magiccube.activity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import flex.android.magiccube.MagiccubePreference;
import flex.android.magiccube.MusicPlayThread;
import flex.android.magiccube.R;
import flex.android.magiccube.Util;
import flex.android.magiccube.dialog.DialogClockingMode;
import flex.android.magiccube.dialog.DialogSetting;
import flex.android.magiccube.interfaces.OnStateListener;
import flex.android.magiccube.interfaces.OnStepListener;
import flex.android.magiccube.view.ViewReplayMode;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ActivityReplayMode extends Activity implements OnStepListener, OnStateListener{
	private ImageButton btMoveBack;
	private ImageButton btMoveForward;
	
	private LinearLayout layout_moveforward;
	private LinearLayout layout_moveback;
	
	private ImageView loading;
	private ViewReplayMode glView;
	private ImageView img_nstep;
	private TextView txtNStep;
	private TextView txtTime;
	private int nStep;
	private Timer timer;
	private int State;
	private String[] movetimes;
	
	private int width;
	private int height;
	
	private int volume;
	
	private MediaPlayer bgm;
	
	private DialogSetting dialog;
	
    private long CreateTime;
    private final long MinLoadingTime = 1500;
	
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
	
	private Button.OnLongClickListener buttonLongListener = new Button.OnLongClickListener()
	{

		@Override
		public boolean onLongClick(View v) {
			// TODO Auto-generated method stub
			if( State == OnStateListener.LOADING)
			{
				return false;
			}
			
			if( v == btMoveForward)
			{
				glView.MoveForward2();
				return false;
			}
			return false;
		}
		
	};
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mode_replay);
        
        btMoveBack = (ImageButton)findViewById(R.id.bt_replay_moveback);
        btMoveForward = (ImageButton)findViewById(R.id.bt_replay_moveforward);
        
        btMoveBack.setVisibility(View.VISIBLE);
        btMoveForward.setVisibility(View.VISIBLE);
        
        glView = (ViewReplayMode)findViewById(R.id.game_view_replay);
        glView.SetCanMove(false);
        glView.SetCanRotate(false);
        glView.setOnStateListener(this);
        
        Intent intent = this.getIntent();
        glView.MessUp(intent.getStringExtra("CmdStrBefore"));
        glView.SetForwardCommand(intent.getStringExtra("CmdStrAfter"));
        movetimes = intent.getStringExtra("movetimes").split(" ");
        glView.SetOnStepListener(this);
         
        btMoveBack.setOnClickListener(buttonListener);
        btMoveForward.setOnClickListener(buttonListener);
        
        btMoveForward.setOnLongClickListener(buttonLongListener);
        
        layout_moveforward = (LinearLayout)findViewById(R.id.layout_replay_moveforward);
        layout_moveback = (LinearLayout)findViewById(R.id.layout_replay_moveback);
        
        img_nstep = (ImageView)findViewById(R.id.img_replay_nstep);
        txtNStep = (TextView)findViewById(R.id.txt_replay_nStep);
        txtTime = (TextView)findViewById(R.id.txt_replay_time);
        
        loading = (ImageView)findViewById(R.id.replay_loading);
        
        int[] loadingresources = {R.drawable.loading7, R.drawable.loading8, R.drawable.loading10, R.drawable.loading11};
        Random r = new Random();
        loading.setImageResource(loadingresources[r.nextInt(4)]);
        
        img_nstep.setVisibility(View.VISIBLE);
        txtNStep.setVisibility(View.VISIBLE);
        
        float fontsize = 25.f;
        Typeface typeFace =Typeface.createFromAsset(getAssets(),"fonts/Angies New House.ttf");
        txtNStep.setTypeface(typeFace);
        txtNStep.setTextColor(Color.GRAY);
        txtNStep.setTextSize(fontsize);
        txtNStep.setText(this.nStep+"");
        
        txtTime.setTypeface(typeFace);
        txtTime.setTextColor(Color.GRAY);
        txtTime.setTextSize(fontsize);
        
        txtTime.setText("00:00:00");
        
/*        timer = new Timer();
        timer.schedule(new TimerTask(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				handler.sendEmptyMessage(1); 
			}
        }, 0,1000);*/
        
        nStep = 0;
        State = OnStateListener.LOADING;
        
        WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        
        width = wm.getDefaultDisplay().getWidth();
        height = wm.getDefaultDisplay().getHeight();
        
        int buttonwidth = width/5;
        int buttonheight = height/13;
        
		LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(buttonwidth, buttonheight);
		
		btMoveForward.setLayoutParams(parms);
		btMoveBack.setLayoutParams(parms);
		
		layout_moveforward.setPadding(width/2-buttonwidth/2, height-buttonheight, 0, 0);
		layout_moveback.setPadding(width/15, height-buttonheight, 0, 0);
		
		volume = MagiccubePreference.GetPreference(MagiccubePreference.BgVolume, this);
		
		CreateTime = System.currentTimeMillis();
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
				//MainActivity.this.setContentView(R.layout.mode_normal);
				//MagicCubeView_Normal glView = (MagicCubeView_Normal)findViewById(R.id.game_view_normal);
				//glView = new MagicCubeView_Normal(MainActivity.this);
			}
			else if(v == btMoveForward)
			{
				glView.MoveForward();
				return;
			}
		}
    };
    
    private void MusicInit()
    {
        bgm = MediaPlayer.create(this, R.raw.mode_replay_bgm);
        bgm.setVolume(MusicPlayThread.GetLinearVolume(volume), MusicPlayThread.GetLinearVolume(volume));
		bgm.setLooping(true);
    	bgm.start();
    }
    
	@Override
	public void AddStep() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void AddStep(int nStep) {
		// TODO Auto-generated method stub
		this.nStep += nStep;
		handler.sendEmptyMessage(0);
	}

	@Override
	public void SetStep(int nStep) {
		// TODO Auto-generated method stub
		this.nStep = nStep;
		handler.sendEmptyMessage(0);
	}
	
	@Override
	public void OnStateChanged(int StateMode) {
		// TODO Auto-generated method stub
		
		
		if( StateMode == OnStateListener.LOADED && State == OnStateListener.LOADING)
		{
			Long TimeLap = System.currentTimeMillis()-CreateTime;
			if(TimeLap >= MinLoadingTime)
			{
				glView.SetCanRotate(true);
				handler.sendEmptyMessage(2);
			}
			else
			{
				Timer t = new Timer();
		        t.schedule(new TimerTask(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						glView.SetCanRotate(true);
						handler.sendEmptyMessage(2);
					}
		        }, MinLoadingTime-TimeLap);
			}
		}
		State = StateMode;
	}
	
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case 0:		//make the progressbar disappear
				txtNStep.setText(""+nStep);
				if( nStep == 0)
				{
					txtTime.setText("00:00:00");
				}
				else
				{
					txtTime.setText(""+Util.TimeFormat(Integer.parseInt(movetimes[nStep-1])));
				}
				break;
			case 1:
				SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");     
				Date curDate = new Date(System.currentTimeMillis());//获取当前时间     
				String str = formatter.format(curDate); 
				txtTime.setText(str);
				break;
			case 2:
				loading.setVisibility(View.GONE);
				MusicInit();
				break;
			}
		}
	};

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
	public void onPause()
	{
		super.onPause();
		if( bgm != null)
		{
			bgm.pause();
		}
		
	}

	
	@Override
	public void onResume()
	{
		super.onResume();
		if( bgm != null)
		{
			bgm.seekTo(0);
			bgm.start();
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
			dialog = new DialogSetting(this, settinghandler);
			dialog.show();
			return false;
		}
		else if( keyCode == KeyEvent.KEYCODE_BACK)
		{
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
			case 1:		//make the progressbar disappear
		        int volume = MagiccubePreference.GetPreference(MagiccubePreference.BgVolume, ActivityReplayMode.this);
		        bgm.setVolume(MusicPlayThread.GetLinearVolume(volume), MusicPlayThread.GetLinearVolume(volume));
		        glView.SetSensitivity((float)MagiccubePreference.GetPreference(MagiccubePreference.Sensitivity, ActivityReplayMode.this)/100.f);
		        break;
			}
		}
    };
    
    private void showExitGameAlert(){
    	new AlertDialog.Builder(this).setTitle("欢乐魔方"). 
    	setMessage("确认离开吗？")  
        .setIcon(R.drawable.ic_exit)  
        .setPositiveButton("确定", new DialogInterface.OnClickListener() {  
      
            @Override  
            public void onClick(DialogInterface dialog, int which) {  
            // 点击“确认”后的操作  
            	ActivityReplayMode.this.finish();  
      
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



