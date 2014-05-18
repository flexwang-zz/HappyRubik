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

import android.app.Activity;
import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import flex.android.magiccube.R;
import flex.android.magiccube.interfaces.OnStateListener;
import flex.android.magiccube.interfaces.OnStepListener;
import flex.android.magiccube.interfaces.OnTimerListener;
import flex.android.magiccube.view.ViewAutoMode;

public class ActivityAutoMode extends Activity implements OnTimerListener,
		OnStateListener, OnStepListener {
	private ImageButton btAutoFinish;
	private ImageButton btMoveBack;
	private ImageButton btMoveForward;
	private ImageView clock;
	private TextView txtTime;
	private TextView txtNStep;
	private ViewAutoMode glView;

	private MediaPlayer GamingbgmPlayer;
	private MediaPlayer FinishbgmPlayer;

	private ProgressDialog processDialog;
	private EditText et;
	private TextView text;

	private int State;
	private int MoveTime;
	private int nStep;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mode_auto);
		btMoveBack = (ImageButton) findViewById(R.id.bt_auto_moveback);
		btMoveForward = (ImageButton) findViewById(R.id.bt_auto_moveforward);
		btAutoFinish = (ImageButton) findViewById(R.id.bt_auto_autofinish);
		clock = (ImageView) findViewById(R.id.clock);
		txtTime = (TextView) findViewById(R.id.txt_clocking_time);
		txtNStep = (TextView) findViewById(R.id.txt_clocking_nStep);

		clock.setVisibility(View.VISIBLE);
		btAutoFinish.setVisibility(View.VISIBLE);
		btMoveForward.setVisibility(View.VISIBLE);
		btMoveBack.setVisibility(View.VISIBLE);

		glView = (ViewAutoMode) findViewById(R.id.game_view_auto);
		glView.SetOnTimerListener(this);
		glView.setOnStateListener(this);
		glView.SetStepListener(this);

		btMoveBack.setOnClickListener(buttonListener);
		btMoveForward.setOnClickListener(buttonListener);
		btAutoFinish.setOnClickListener(buttonListener);

		GamingbgmPlayer = MediaPlayer.create(this, R.raw.bg2);
		FinishbgmPlayer = MediaPlayer.create(this, R.raw.finish);

		State = OnStateListener.NONE;
	}

	@Override
	public void AddStep() {
		

	}

	@Override
	public void SetStep(int nStep) {
		

	}

	@Override
	public void OnStateChanged(int StateMode) {
		

	}

	@Override
	public void onTimer(int leftTime) {
		

	}

	private Button.OnClickListener buttonListener = new Button.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v == btMoveBack) {
				glView.MoveBack();
				return;
			} else if (v == btMoveForward) {
				glView.MoveForward();
				return;
			} else if (v == btAutoFinish) {
				ActivityAutoMode.this.showWait("���ڼ���...");
				new Thread(autosolverun).start();
				// ActivityAutoMode.this.waitClose();
			}
		}
	};

	public void showWait(final String message) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				processDialog = new ProgressDialog(ActivityAutoMode.this);
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
				if (processDialog != null && processDialog.isShowing()) {
					processDialog.dismiss();
				}
			}
		});

	}

	private Runnable autosolverun = new Runnable() {

		@Override
		public void run() {
			
			glView.AutoSolve("Jaap");
			waitClose();
		}
	};

	@Override
	public void AddStep(int nStep) {
		

	}

	@Override
	public void OnStateNotify(int StateMode) {
		

	}

}
