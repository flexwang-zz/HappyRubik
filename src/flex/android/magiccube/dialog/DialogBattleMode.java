package flex.android.magiccube.dialog;

import flex.android.magiccube.R;
import flex.android.magiccube.activity.ActivityBattleMode;
import flex.android.magiccube.activity.ActivityClockingMode;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class DialogBattleMode extends Dialog implements OnClickListener{
	private Context context;
	private Activity activity;
	private boolean HasSaved = false;
	
	public DialogBattleMode(Activity activity, String msg, int time) {
		super(activity,R.style.dialog);
		this.activity = activity;
		this.context = activity;
		this.setContentView(R.layout.battle_dialog_view);
		TextView text_msg = (TextView) findViewById(R.id.text_battle_message);
		TextView text_time = (TextView) findViewById(R.id.text_battle_time);
		ImageButton btn_menu = (ImageButton) findViewById(R.id.menu_battle_imgbtn);
		ImageButton btn_next = (ImageButton) findViewById(R.id.replay_battle_imgbtn);
		ImageButton btn_replay = (ImageButton) findViewById(R.id.saverep_battle_imgbtn);
		
		text_msg.setText(msg);
		text_time.setText(text_time.getText().toString().replace("$", String.valueOf(time)));
		btn_menu.setOnClickListener(this);
		btn_next.setOnClickListener(this);
		btn_replay.setOnClickListener(this);
		this.setCancelable(false);
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.menu_battle_imgbtn:
			this.dismiss();
			((ActivityBattleMode)activity).Leave();
			break;
		case R.id.replay_battle_imgbtn:
			this.dismiss();
			((ActivityBattleMode)activity).Reset();
			break;
		case R.id.saverep_battle_imgbtn:
			if(!HasSaved)
			{
				((ActivityBattleMode)activity).SaveReplay();
				HasSaved = true;
			}
			else
			{
		    	Toast toast = Toast.makeText(context,
		    		     "Â¼ÏñÒÑ±£´æ£¬ÇëÎðÖØ¸´±£´æ", Toast.LENGTH_LONG);
		    	toast.setGravity(Gravity.CENTER, 0, 0);
		    	toast.show();
			}
			break;
		}
	}

}
