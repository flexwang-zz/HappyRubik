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

package flex.android.magiccube.dialog;

import flex.android.magiccube.R;
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


public class DialogClockingMode extends Dialog implements OnClickListener{
	private Context context;
	private Activity activity;
	private boolean HasSaved = false;	//if saved the replay
	
	public DialogClockingMode(Activity activity, String msg, int time) {
		super(activity,R.style.dialog);
		this.activity = activity;
		this.context = activity;
		this.setContentView(R.layout.clocking_dialog_view);
		TextView text_msg = (TextView) findViewById(R.id.text_clocking_message);
		TextView text_time = (TextView) findViewById(R.id.text_clocking_time);
		ImageButton btn_menu = (ImageButton) findViewById(R.id.menu_clocking_imgbtn);
		ImageButton btn_next = (ImageButton) findViewById(R.id.saverep_clocking_imgbtn);
		ImageButton btn_replay = (ImageButton) findViewById(R.id.replay_clocking_imgbtn);
		
		text_msg.setText(msg);
		text_time.setText(text_time.getText().toString().replace("$", String.valueOf(time)));
		btn_menu.setOnClickListener(this);
		btn_next.setOnClickListener(this);
		btn_replay.setOnClickListener(this);
		this.setCancelable(false);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.menu_clocking_imgbtn:
			this.dismiss();
			activity.finish();
			break;
		case R.id.replay_clocking_imgbtn:
			this.dismiss();
			((ActivityClockingMode)activity).Reset();
			break;
		case R.id.saverep_clocking_imgbtn:
			if(!HasSaved)
			{
				((ActivityClockingMode)activity).SaveReplay();
				HasSaved = true;
			}
			else
			{
		    	Toast toast = Toast.makeText(context,
		    		     "¼���ѱ��棬�����ظ�����", Toast.LENGTH_LONG);
		    	toast.setGravity(Gravity.CENTER, 0, 0);
		    	toast.show();
			}
			break;
		}

	}
	
}
