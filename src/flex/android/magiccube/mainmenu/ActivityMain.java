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

package flex.android.magiccube.mainmenu;



import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import flex.android.magiccube.MagiccubePreference;
import flex.android.magiccube.MusicPlayThread;
import flex.android.magiccube.R;
import flex.android.magiccube.activity.ActivityAutoMode;
import flex.android.magiccube.activity.ActivityBattleMode;
import flex.android.magiccube.activity.ActivityClockingMode;
import flex.android.magiccube.activity.ActivityNormalMode;
import flex.android.magiccube.activity.ReplayListActivity;
import flex.android.magiccube.dialog.DialogSetting;
import flex.android.magiccube.dialog.DialogThanksList;
import flex.android.magiccube.interfaces.OnStateListener;


import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ActivityMain extends Activity implements OnMenuClickListener, OnStateListener{
	private Button btNormalMode = null;
	private Button btClockingMode = null;
	private Button btBattleMode = null;
	private Button btAutoMode = null;
	private ImageButton btSetting = null;
	private LinearLayout layout_setting = null;
	private ImageView loading = null;
	
	private MainMenuView glView = null;
	
	private RotateAnimation animation1 = null;
	private RotateAnimation animation2 = null;
	private RotateAnimation animation3 = null;
	
	private ImageView img_normal = null;
	private LinearLayout layout_normal = null;
	
	private ImageView img_clocking = null;
	private LinearLayout layout_clocking = null;
	
	private ImageView img_battle = null;
	private LinearLayout layout_battle = null;
	
	private MainMenuView mainMenuView = null;
	
	private ImageButton btReplayMode = null;
	private DialogSetting dialog = null;
	
	private int volume;
	private MediaPlayer clickmedia = null;
	private MediaPlayer bgm = null;
	
    private int LeftPadding1;
    private int TopPadding1;
    private int radius1;
    
    private int LeftPadding2;
    private int TopPadding2;
    private int radius2;
    
    private int LeftPadding3;
    private int TopPadding3;
    private int radius3;
    
    private int width;
    private int height;
        
    private int BackPressed;
    
    private int State;
    
    private Timer timer;
    private boolean timerstop = false;
    
    private long CreateTime;
    private final long MinLoadingTime = 1500;
    
    private Handler uihandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case 0:		//make the progressbar disappear
				loading.setVisibility(View.GONE);
				MusicInit();
				showThanksList();
				break;
			}
		}
    };
    
    private Handler settinghandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case 1:		//make the progressbar disappear
				
		        volume = MagiccubePreference.GetPreference(MagiccubePreference.BgVolume, ActivityMain.this);
		        if( bgm != null)
		        {
		        	bgm.setVolume(MusicPlayThread.GetLinearVolume(volume), MusicPlayThread.GetLinearVolume(volume));
		        }
		        if( clickmedia != null)
		        {
		        	clickmedia.setVolume(MusicPlayThread.GetLinearVolume(volume), MusicPlayThread.GetLinearVolume(volume));
		        }
		        if( glView != null)
		        {
		        	glView.setVolume(volume);
		        	glView.SetSensitivity((float)MagiccubePreference.GetPreference(MagiccubePreference.Sensitivity, ActivityMain.this)/100.f);
		        }
				break;
			case 2:
		        if( bgm != null)
		        {
		        	bgm.setVolume(MusicPlayThread.GetLinearVolume(volume), MusicPlayThread.GetLinearVolume(volume));
		        }
		        if( clickmedia != null)
		        {
		        	clickmedia.setVolume(MusicPlayThread.GetLinearVolume(volume), MusicPlayThread.GetLinearVolume(volume));
		        }
		        break;
			}
		}
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //ImageView imageView = new ImageView(this);
      //  imageView.setImageResource(R.drawable.loading);
        
        setContentView(R.layout.menu);
        
        //initial the buttons
/*        btNormalMode = (Button)findViewById(R.id.bt_normal);
        btClockingMode = (Button)findViewById(R.id.bt_clocking);
        btBattleMode = (Button)findViewById(R.id.bt_battle);
        btAutoMode = (Button)findViewById(R.id.bt_auto);*/
        btReplayMode = (ImageButton)findViewById(R.id.bt_replay);
        btSetting = (ImageButton)findViewById(R.id.bt_setting);
        layout_setting = (LinearLayout)findViewById(R.id.layout_menu_setting);
        glView = (MainMenuView)findViewById(R.id.main_menu_view);
        
        glView.setOnStateListener(this);
        
        mainMenuView = (MainMenuView)findViewById(R.id.main_menu_view);
        mainMenuView.SetOnMenuClickListener(this);
        
        img_normal = (ImageView)findViewById(R.id.img_menu_normal);
        layout_normal = (LinearLayout)findViewById(R.id.layout_menu_normal);
        
        img_clocking = (ImageView)findViewById(R.id.img_menu_clocking);
        layout_clocking = (LinearLayout)findViewById(R.id.layout_menu_clocking);
        
        img_battle = (ImageView)findViewById(R.id.img_menu_battle);
        layout_battle = (LinearLayout)findViewById(R.id.layout_menu_battle);
        
        loading = (ImageView)findViewById(R.id.main_menu_loading);
        
        int[] loadingresources = {R.drawable.loading0, R.drawable.loading10, R.drawable.loading11};
        Random r = new Random();
        loading.setImageResource(loadingresources[r.nextInt(3)]);
        
        animation1 = (RotateAnimation) AnimationUtils.loadAnimation(this, R.anim.anim1);
        animation2 = (RotateAnimation) AnimationUtils.loadAnimation(this, R.anim.anim2);
        animation3 = (RotateAnimation) AnimationUtils.loadAnimation(this, R.anim.anim3);
        //animation.start();
        
/*        btNormalMode.setOnClickListener(buttonListener);
        btClockingMode.setOnClickListener(buttonListener);
        btBattleMode.setOnClickListener(buttonListener);
        btAutoMode.setOnClickListener(buttonListener);*/
        btReplayMode.setOnClickListener(buttonListener);
        btSetting.setOnClickListener(buttonListener);
        
        volume = MagiccubePreference.GetPreference(MagiccubePreference.BgVolume, this);
        clickmedia = MediaPlayer.create(this, R.raw.move2);
        clickmedia.setVolume(MusicPlayThread.GetLinearVolume(volume), MusicPlayThread.GetLinearVolume(volume));
        
        WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        
        width = wm.getDefaultDisplay().getWidth();
        height = wm.getDefaultDisplay().getHeight();
        
        int height2 = height*7/8;
        int lap = (height-height2)/2;
        
        //Log.e("width,height", width+" "+height);
        radius1 = (int) (width/5.f);
        radius1 = (int) (height2/7.f);
        LeftPadding1 = width/2-radius1; 
        //LeftPadding1 = 200;
        TopPadding1 = height2/6-radius1+lap; 
        
        //radius1 = width/12;
        radius2 = (int) (height2/7.f);
		LeftPadding2 = width/2-radius2; 
		TopPadding2 = height2/2-radius2+lap; 
		
		//radius2 = width/12; 
		radius3 = (int) (height2/7.f);
		LeftPadding3 = width/2-radius3;
		TopPadding3 = height2*5/6-radius3+lap;
		
		
		mainMenuView.SetLayout(LeftPadding1, TopPadding1, radius1, 
        		LeftPadding2, TopPadding2, radius2, 
        		LeftPadding3, TopPadding3, radius3);
		
		LinearLayout.LayoutParams parms1 = new LinearLayout.LayoutParams(radius1*2,radius1*2);
		LinearLayout.LayoutParams parms2 = new LinearLayout.LayoutParams(radius2*2,radius2*2);
		LinearLayout.LayoutParams parms3 = new LinearLayout.LayoutParams(radius3*2,radius3*2);
		
		img_normal.setLayoutParams(parms1);
		img_clocking.setLayoutParams(parms2);
		img_battle.setLayoutParams(parms3);
		
		int btSettingWidth = height/15;
		int btSettingHeight = btSettingWidth;
		LinearLayout.LayoutParams parms4 = new LinearLayout.LayoutParams(btSettingWidth, btSettingHeight);
		btSetting.setLayoutParams(parms4);
		layout_setting.setPadding(width-btSettingWidth-10, 10, 0, 0);
		//btSetting.setLayoutParams(new LinearLayout.LayoutParams(btSettingWidth,btSettingHeight));
		
		
		//loading.setLayoutParams(new LinearLayout.LayoutParams(width, height));
		
		layout_normal.setPadding(LeftPadding1, TopPadding1, 0, 0);
		img_normal.setAlpha(150);
		
		layout_clocking.setPadding(LeftPadding2, TopPadding2, 0, 0);
		img_clocking.setAlpha(150);
		
		layout_battle.setPadding(LeftPadding3, TopPadding3, 0, 0);
		img_battle.setAlpha(150);
		//img_normal.setLayoutParams(LinearLayout.LayoutParams(200,200));
		img_normal.startAnimation(animation1);
		img_clocking.startAnimation(animation2);
		img_battle.startAnimation(animation3);
		//btSetting.startAnimation(animation2);
		
		BackPressed = 0;
		
		State = OnStateListener.LOADING;
		//setContentView(imageView);
		CreateTime = System.currentTimeMillis();
    }
    
    @Override
    protected void onStart()
    {
    	super.onStart();
    	// animation.start();

    }
    
    private void MusicInit()
    {
        bgm = MediaPlayer.create(this, R.raw.main_menu_bgm);
        bgm.setVolume(MusicPlayThread.GetLinearVolume(volume), MusicPlayThread.GetLinearVolume(volume));
		bgm.setLooping(true);
    	bgm.start();
    	timer = new Timer();
        timer.schedule(new TimerTask(){

			@Override
			public void run() {
				if(!timerstop)
				{
					glView.Move2Steps();
				}
				// TODO Auto-generated method stub
/*		    	for(int i=0; i<2; i++)
		    	{
		    		if( !timerstop)
		    		{
						MusicPlayThread musicPlayThread = new MusicPlayThread(ActivityMain.this, R.raw.move2, volume);
						musicPlayThread.start();
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		    		}
		    	}*/
			}
        }, 1000,4000);
    }

    private Button.OnClickListener buttonListener = new Button.OnClickListener()
    {

		@Override
		public void onClick(View v) {
			if( State != OnStateListener.MAINMENULOADED)
			{
				return;
			}
			if(v == btNormalMode)
			{
				//animation = new RotateAnimation(0, 100);
				//img_normal.startAnimation(animation);
				//animation.start();
				//Intent intent = new Intent(ActivityMain.this,ActivityNormalMode.class);
				//startActivity(intent);
				return;
			}
			else if(v == btClockingMode)
			{
				Intent intent = new Intent(ActivityMain.this,ActivityClockingMode.class);
				startActivity(intent);
			}
			else if(v == btAutoMode)
			{
/*				Intent intent = new Intent(ActivityMain.this,ActivityTab.class);
				startActivity(intent);*/
	            Intent intent = new Intent(ActivityMain.this, ActivityAutoMode.class);
	            startActivity(intent);
	         //   return true;
			}
			else if(v == btBattleMode)
			{
				Intent intent = new Intent(ActivityMain.this,ActivityBattleMode.class);
				startActivity(intent);
			}
			else if(v == btReplayMode)
			{
	            Intent Intent = new Intent(ActivityMain.this, ReplayListActivity.class);
	            startActivity(Intent);
			}
			else if(v == btSetting)
			{
				dialog = new DialogSetting(ActivityMain.this, settinghandler);
				dialog.show();
			}
		}
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
		if( State != OnStateListener.MAINMENULOADED)
		{
			return false;
		}
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

	@Override
	public void OnMenuClick(int MenuID) {
		// TODO Auto-generated method stub
		if( State != OnStateListener.MAINMENULOADED)
		{
			return;
		}
		Intent intent;
		clickmedia.start();
		
		ResetBackPressed(); //reset the backpress record
		
		switch(MenuID)
		{
		case MODE_NORMAL:
			intent = new Intent(ActivityMain.this,ActivityNormalMode.class);
			startActivity(intent);
			break;
		case MODE_CLOCKING:
			intent = new Intent(ActivityMain.this,ActivityClockingMode.class);
			startActivity(intent);
			break;
		case MODE_BATTLE:
			intent = new Intent(ActivityMain.this,ActivityBattleMode.class);
			startActivity(intent);
			break;
		}
	}
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		if( State != OnStateListener.MAINMENULOADED)
		{
			return false;
		}
		// exit
		if(keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME){
			BackPressed ++;
			if( BackPressed >= 2)
			{
				finish();
			}
			else
			{
		    	Toast toast = Toast.makeText(this,
		    		     "�ٰ�һ���˳���Ϸ", Toast.LENGTH_LONG);
		    	toast.setGravity(Gravity.BOTTOM, 0, height/11);
		    	toast.show();
		    
			}
		}
		else if(keyCode == KeyEvent.KEYCODE_MENU)
		{
			dialog = new DialogSetting(ActivityMain.this, settinghandler);
			dialog.show();
		}
		return false;
		//return super.onKeyDown(keyCode, event);
	}
	
	public void ResetBackPressed()
	{
		BackPressed = 0;
	}
/*	protected void showExitGameAlert() 
	{
		Builder builder= new Builder(this);
		builder.setMessage("ȷ���˳���");  
		builder.setTitle("��ʾ");
		builder.setPositiveButton("ȷ��", new OnClickListener() {
				@Override  
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					ActivityMain.this.finish();
					}
				});
		builder.setNegativeButton("ȡ��", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				}
			});
		builder.create().show(); 
	}*/
	

	@Override
	public void OnStateChanged(int StateMode) {
		// TODO Auto-generated method stub
		if(StateMode == OnStateListener.MAINMENULOADED && State == OnStateListener.LOADING)
		{
			State = StateMode;
			Long TimeLap = System.currentTimeMillis()-CreateTime;
			if(TimeLap >= MinLoadingTime)
			{
				uihandler.sendEmptyMessage(0);
			}
			else
			{
				Timer t = new Timer();
		        t.schedule(new TimerTask(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						uihandler.sendEmptyMessage(0);
					}
		        }, MinLoadingTime-TimeLap);
			}
			
		}
	}

	@Override
	public void OnStateNotify(int StateMode) {
		// TODO Auto-generated method stub
		
	}	
	
	@Override
	public void onPause()
	{
		timerstop = true;
		super.onPause();
		if( bgm != null)
		{
			bgm.pause();
		}
		
	}

	
	@Override
	public void onResume()
	{
		timerstop = false;
		super.onResume();
		if( bgm != null)
		{
			bgm.seekTo(0);
			bgm.start();
		}
		if( settinghandler != null)
		{
			settinghandler.sendEmptyMessage(1);
		}
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		bgm.release();
		clickmedia.release();
		//Log.e("ondestroy", "ondestroy");
	}
	
	private void showThanksList()
	{
		if( MagiccubePreference.GetPreference(MagiccubePreference.IsShowThanksList, this) > 0)
		{
			MagiccubePreference.SetPreference(MagiccubePreference.IsShowThanksList, 0, this);
			DialogThanksList d = new DialogThanksList(ActivityMain.this);
			d.getWindow().setLayout(width*3/4, height*3/4);
			d.show();
		}
	}
}
