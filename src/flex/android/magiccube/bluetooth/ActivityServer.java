package flex.android.magiccube.bluetooth;

import flex.android.magiccube.MagiccubePreference;
import flex.android.magiccube.R;
import flex.android.magiccube.activity.ActivityBattleMode;
import flex.android.magiccube.dialog.DialogSetting;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.Time;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.SeekBar;

public class ActivityServer extends Activity{
	
	private SeekBar sbar_difficulty = null;
	private SeekBar sbar_observetime = null;
	
	private RadioButton radio_samemessup = null;
	private RadioButton radio_randommessup = null;
	
	private Button btn_start = null;
	private Button btn_reset = null;
	
	private int difficulty;
	private boolean Changed = false;
	private int LastChangedTime;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    setContentView(R.layout.tab_server);
	    
	    sbar_difficulty = (SeekBar)findViewById(R.id.sbar_bluetooth_server_difficulty);
	    sbar_observetime = (SeekBar)findViewById(R.id.ssbar_bluetooth_server_observetime);
	    
	    radio_samemessup = (RadioButton)findViewById(R.id.radioSameMessup);
	    radio_randommessup = (RadioButton)findViewById(R.id.radioRandomMessup);
	
	    sbar_difficulty.setMax(100);
	    sbar_difficulty.setProgress(MagiccubePreference.GetPreference(MagiccubePreference.Difficulty, this));
	    sbar_difficulty.setOnSeekBarChangeListener(seekBarChangeListener);
	    
	    sbar_observetime.setMax(50);
	    sbar_observetime.setProgress(MagiccubePreference.GetPreference(MagiccubePreference.ObserveTime, this));
	    
	    radio_samemessup.setChecked(MagiccubePreference.GetPreference(MagiccubePreference.IsSameMessup, this)==1);
	    radio_randommessup.setChecked(MagiccubePreference.GetPreference(MagiccubePreference.IsSameMessup, this)==0);
	
	    btn_start = (Button)findViewById(R.id.btn_bluetooth_startasserver);
	    btn_reset = (Button)findViewById(R.id.btn_bluetooth_resetserver);
	    
	    btn_reset.setOnClickListener(buttonListener);
	    btn_start.setOnClickListener(buttonListener);
	}
	
	private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener()
	{

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
/*			if( seekBar == sbar_difficulty)
			{
				if( difficulty == 50)
				{
					if( seekBar.getProgress()>50)
					{
						seekBar.setProgress(100);
					}
					else if( seekBar.getProgress()<50)
					{
						seekBar.setProgress(0);
					}
				}
				else if(difficulty == 0)
				{
					if( seekBar.getProgress()>0)
					{
						seekBar.setProgress(50);
					}
				}
				else if( difficulty == 100)
				{
					if( seekBar.getProgress()<100)
					{
						seekBar.setProgress(50);
					}
				}
				
				difficulty = seekBar.getProgress();
			}*/
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			if( seekBar == sbar_difficulty)
			{
				if(!Changed)
				{
					Changed = true;
					LastChangedTime = GetCurSeconds();
				}
				else
				{
					if( GetCurSeconds() - LastChangedTime > 1)
					{
						LastChangedTime = GetCurSeconds();
					}
					else
					{
						return;
					}
				}
				if( difficulty == 50)
				{
					if( seekBar.getProgress()>50)
					{
						seekBar.setProgress(100);
						difficulty = 100;
					}
					else if( seekBar.getProgress()<50)
					{
						seekBar.setProgress(0);
						difficulty = 0;
					}
				}
				else if(difficulty == 0)
				{
					if( seekBar.getProgress()>0)
					{
						seekBar.setProgress(50);
						difficulty = 50;
					}
				}
				else if( difficulty == 100)
				{
					if( seekBar.getProgress()<100)
					{
						seekBar.setProgress(50);
						difficulty = 50;
					}
				}
				Changed = false;
			}
		}
		
	    private int GetCurSeconds()
	    {
	    	Time t=new Time("GMT+8"); 	//Time Zone资料。

	    	t.setToNow(); // 取得系统时间。
	    	int hour = t.hour;
	    	int minute = t.minute;
	    	int second = t.second;
	    	
	    	return second + minute*60 + hour*3600;
	    }
		
	};

    private Button.OnClickListener buttonListener = new Button.OnClickListener()
    {

		@Override
		public void onClick(View v) {
			if(v == btn_start)
			{
				MagiccubePreference.SetPreference(MagiccubePreference.Difficulty, 
						sbar_difficulty.getProgress(), ActivityServer.this);
				MagiccubePreference.SetPreference(MagiccubePreference.ObserveTime, 
						sbar_observetime.getProgress(), ActivityServer.this);
				int value  = radio_samemessup.isChecked()?1:0;
				MagiccubePreference.SetPreference(MagiccubePreference.IsSameMessup, 
						value, ActivityServer.this);
				
				MagiccubePreference.SetPreference(MagiccubePreference.ServerOrClient, 
						1, ActivityServer.this);
					
				ActivityServer.this.finish();
				
				return;
			}
			else if(v == btn_reset)
			{
				sbar_difficulty.setProgress(50);
				sbar_observetime.setProgress(10);
				radio_samemessup.setChecked(true);
				radio_randommessup.setChecked(false);
				return;
			}
		}
    };
}
