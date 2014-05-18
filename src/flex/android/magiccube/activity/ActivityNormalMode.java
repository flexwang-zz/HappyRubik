/*
 * Copyright 2011-2014 Zhaotian Wang <zhaotianzju@gmail.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import flex.android.magiccube.mainmenu.ActivityMain;
import flex.android.magiccube.view.ViewNormalMode;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
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
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityNormalMode extends ActivitySensorListener implements OnStepListener, OnStateListener{
	private ImageButton btMoveBack;
	private ImageButton btMoveForward;
	private ImageButton btAutoFinish;
	
	private LinearLayout layout_moveforward;
	private LinearLayout layout_moveback;
	private LinearLayout layout_autofinish;
	
	private ImageView loading;
	
	private TextView txtNStep;
	private TextView txtTime;
	
	private ViewNormalMode glView;
	
	private Timer timer;
	
	private ProgressDialog processDialog;
	
	private int nStep;
	
	private int nMessUp = 10;
	
	private int State;
	
    private int width;
    private int height;
    
    private MediaPlayer bgm = null;
	
    private int volume;
    
    private DialogSetting dialog;
    
    private long CreateTime;
    private final long MinLoadingTime = 1500;
    
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case 0:	
				txtNStep.setText(""+nStep);
				break;	
			case 1:
				//Log.e("time","time");
				SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");     
				Date curDate = new Date(System.currentTimeMillis());//��ȡ��ǰʱ��     
				String str = formatter.format(curDate); 
				txtTime.setText(str);
				break;
			case 2:
				SetCanVibrate(true);
				loading.setVisibility(View.GONE);
				glView.SetCanMove(true);
				glView.SetCanRotate(true);
				MusicInit();
			
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
			case 4:
				btAutoFinish.setBackgroundResource(R.drawable.buttons_autosolve);
				break;
			case 5:
				glView.SetCanMove(true);	//set the glView to canmove
				SetCanVibrate(true);
				btAutoFinish.setBackgroundResource(R.drawable.buttons_autosolve_unable);
				break;
			}
		}
	};
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mode_normal);
        
        btMoveBack = (ImageButton)findViewById(R.id.bt_normal_moveback);
        btMoveForward = (ImageButton)findViewById(R.id.bt_normal_moveforward);
        btAutoFinish = (ImageButton)findViewById(R.id.bt_normal_autofinish);
        
        layout_moveforward = (LinearLayout)findViewById(R.id.layout_normal_moveforward);
        layout_moveback = (LinearLayout)findViewById(R.id.layout_normal_moveback);
        layout_autofinish = (LinearLayout)findViewById(R.id.layout_normal_autofinish);
        
        glView = (ViewNormalMode)findViewById(R.id.game_view_normal);
        glView.SetOnStepListener(this);
        glView.setOnStateListener(this);
        glView.SetCanMove(false);
        glView.SetCanRotate(false);
         
        btMoveBack.setOnClickListener(buttonListener);
        btMoveForward.setOnClickListener(buttonListener);
        btAutoFinish.setOnClickListener(buttonListener);
        
        btAutoFinish.setOnLongClickListener(buttonLongListener);
        
        loading = (ImageView)findViewById(R.id.normal_loading);
        
        int[] loadingresources = {R.drawable.loading1, R.drawable.loading2, R.drawable.loading3, R.drawable.loading10, R.drawable.loading11};
        Random r = new Random();
        loading.setImageResource(loadingresources[r.nextInt(5)]);
        
        txtNStep = (TextView)findViewById(R.id.txt_normal_nStep);
        txtTime = (TextView)findViewById(R.id.txt_normal_time);
        
        float fontsize = 25.f;
        Typeface typeFace =Typeface.createFromAsset(getAssets(),"fonts/Angies New House.ttf");
        txtTime.setTypeface(typeFace);
        txtTime.setTextColor(Color.GRAY);
        txtTime.setTextSize(fontsize);
        
        txtNStep.setTypeface(typeFace);
        txtNStep.setTextColor(Color.GRAY);
        txtNStep.setTextSize(fontsize);
        
        txtNStep.setText("0");
        
        timer = new Timer();
        timer.schedule(new TimerTask(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				handler.sendEmptyMessage(1); 
			}
        }, 0,1000);
        this.SetCanVibrate(false);
        
        volume = MagiccubePreference.GetPreference(MagiccubePreference.BgVolume, this);
        
        State = OnStateListener.LOADING;
        
        WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        
        width = wm.getDefaultDisplay().getWidth();
        height = wm.getDefaultDisplay().getHeight();
        
        int buttonwidth = width/5;
        int buttonheight = height/13;
        
		LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(buttonwidth, buttonheight);
		
		btMoveForward.setLayoutParams(parms);
		btMoveBack.setLayoutParams(parms);
		btAutoFinish.setLayoutParams(parms);
		
		layout_moveforward.setPadding(width/2-buttonwidth/2, height-buttonheight, 0, 0);
		layout_moveback.setPadding(width/15, height-buttonheight, 0, 0);
		layout_autofinish.setPadding(width-width/15-buttonwidth, height-buttonheight, 0, 0);
        
        //btMoveForward.setBackgroundResource(R.drawable.digit0);
        //btMoveForward.setImageResource(R.drawable.digit0);
		CreateTime = System.currentTimeMillis();
    }
    
    private void MusicInit()
    {
        bgm = MediaPlayer.create(this, R.raw.mode_normal_bgm);
        bgm.setVolume(MusicPlayThread.GetLinearVolume(volume), MusicPlayThread.GetLinearVolume(volume));
		bgm.setLooping(true);
    	bgm.start();
    }
    
/*	@Override
	public void onSensorChanged(SensorEvent sensorevent)
	{
		super.onSensorChanged(sensorevent);
		if( this.vibrate_state == STATE_VIBRATE)
		{
			if(glView.IsComplete())
			{
				glView.Reset();
				glView.MessUp(nMessUp);
				nStep = 0;
				handler.sendEmptyMessage(0);
			}
			else
			{
				glView.Reset();
				nStep = 0;
				handler.sendEmptyMessage(0);
				
			}
			this.ResetVibrateState();
		}
	}*/
	
	@Override
	public void finish()
	{
		super.finish();
		timer.cancel();
	}
	
	public void showWait(final String message) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				processDialog = new ProgressDialog(ActivityNormalMode.this);
				processDialog.setMessage(message);
				processDialog.setIndeterminate(true);
				processDialog.setCancelable(false);
				processDialog.show();

			}
		});

	}

	/**
	 * �رյȴ��
	 * */
	public void waitClose() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (processDialog!=null&&processDialog.isShowing()) {
					processDialog.dismiss();
				}
			}
		});

	}
	
	private Button.OnLongClickListener buttonLongListener = new Button.OnLongClickListener()
	{

		@Override
		public boolean onLongClick(View v) {
			// TODO Auto-generated method stub
			if( State == OnStateListener.LOADING)
			{
				return false;
			}
			
			if( v == btAutoFinish)
			{
				SetCanVibrate(false);
				if( glView.IsComplete())
				{
					return false;
				}
				if( glView.IsSolved())
				{
					glView.AutoSolve2("Jaap");
				}
				else
				{
					ActivityNormalMode.this.showWait("���ڼ���...");
					new Thread(autosolverun2).start();
				}
				return false;
			}
			return false;
		}
		
	};
	
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
			else if(v == btAutoFinish)
			{
				if( glView.IsComplete())
				{
					return;
				}
				if( glView.IsSolved())
				{
					glView.AutoSolve("Jaap");
				}
				else
				{
					ActivityNormalMode.this.showWait("���ڼ���...");
					new Thread(autosolverun).start();
				}
				return;
			}
		}
    };
    
    private Runnable autosolverun = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			glView.AutoSolve("Jaap");
			waitClose();
			glView.AutoSolve("Jaap");
		}
	};
	
    private Runnable autosolverun2 = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			glView.AutoSolve2("Jaap");
			waitClose();
		}
	};
	@Override
	public void AddStep() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void AddStep(int nStep) {
		// TODO Auto-generated method stub
		
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
		
		if(StateMode == OnStateListener.LOADED && State == OnStateListener.LOADING)
		{
			Long TimeLap = System.currentTimeMillis()-CreateTime;
			if(TimeLap >= MinLoadingTime)
			{
				handler.sendEmptyMessage(2);
			}
			else
			{
				Timer t = new Timer();
		        t.schedule(new TimerTask(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						handler.sendEmptyMessage(2);
					}
		        }, MinLoadingTime-TimeLap);
			}
		}
		State = StateMode;
		
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
		case OnStateListener.CANAUTOSOLVE:
			buttonhandler.sendEmptyMessage(4);
			break;
		case OnStateListener.CANNOTAUTOSOLVE:
			buttonhandler.sendEmptyMessage(5);
			break;
		}
	}

	@Override
	public void onShake() {
		// TODO Auto-generated method stub
		if(glView.IsComplete())
		{
			glView.Reset();
			glView.MessUp(nMessUp);
			nStep = 0;
			handler.sendEmptyMessage(0);
		}
		else
		{
			glView.Reset();
			nStep = 0;
			handler.sendEmptyMessage(0);
			
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
		//Log.e("onPause", "onPause");
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
		else if(keyCode == KeyEvent.KEYCODE_BACK)
		{
			showExitGameAlert();
			return false;
		}
		return super.onKeyDown(keyCode, event);
		//return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		bgm.release();
		//Log.e("ondestroy", "ondestroy");
	}
	
    private Handler settinghandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case 1:		//make the progressbar disappear
				
		        volume = MagiccubePreference.GetPreference(MagiccubePreference.BgVolume, ActivityNormalMode.this);
		        bgm.setVolume(MusicPlayThread.GetLinearVolume(volume), MusicPlayThread.GetLinearVolume(volume));
				glView.setVolume(MagiccubePreference.GetPreference(MagiccubePreference.MoveVolume, ActivityNormalMode.this));
				glView.SetSensitivity((float)MagiccubePreference.GetPreference(MagiccubePreference.Sensitivity, ActivityNormalMode.this)/100.f);
		        setMinVibration(MagiccubePreference.GetPreference(MagiccubePreference.MinVibration, ActivityNormalMode.this));
				break;
			}
		}
    };
    
    private void showExitGameAlert(){
    	new AlertDialog.Builder(this).setTitle("ȷ���뿪"). 
    	setMessage("�뿪��,��Ϸ��Ƚ����ܱ��档")
        .setIcon(R.drawable.ic_exit)  
        .setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {  
      
            @Override  
            public void onClick(DialogInterface dialog, int which) {  
            // �����ȷ�ϡ���Ĳ���  
            	ActivityNormalMode.this.finish();  
      
            }  
        })  
        .setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {  
      
            @Override  
            public void onClick(DialogInterface dialog, int which) {  
            // ��������ء���Ĳ���,���ﲻ����û���κβ���  
            }  
        }).show();  
    }
    
}

