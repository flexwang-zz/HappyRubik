package flex.android.magiccube.dialog;

import flex.android.magiccube.MagiccubePreference;
import flex.android.magiccube.R;
import flex.android.magiccube.interfaces.OnStateListener;
import flex.android.magiccube.view.ViewNormalMode;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.SeekBar;

public class DialogSetting extends Dialog implements OnClickListener {
	private Context context = null;
	private SharedPreferences preferences = null;

	private SeekBar sbar_sensitivity = null;
	private SeekBar sbar_difficulty = null;
	private SeekBar sbar_bgvolume = null;
	private SeekBar sbar_movevolume = null;
	private SeekBar sbar_minvibration = null;

	private Button btn_save = null;
	private Button btn_reset = null;

	private RadioButton radio_guideon = null;
	private RadioButton radio_guideoff = null;

	private int sensitivity;
	private int difficulty;
	private int bgvolume;
	private int movevolume;

	private int maxvibration = 55;

	private Handler handler;

	private boolean Changed = false;
	private int LastChangedTime = 0;

	private boolean Saved = false;

	public DialogSetting(Context context, Handler handler) {
		super(context, R.style.dialog);
		this.context = context;

		this.handler = handler;
		this.setContentView(R.layout.setting_dialog_view);

		preferences = context.getSharedPreferences(
				MagiccubePreference.SHAREDPREFERENCES_NAME,
				Context.MODE_PRIVATE);

		sbar_sensitivity = (SeekBar) findViewById(R.id.sbar_setting_sensitivity);
		sbar_difficulty = (SeekBar) findViewById(R.id.sbar_setting_difficulty);
		sbar_bgvolume = (SeekBar) findViewById(R.id.sbar_setting_bgvolume);
		sbar_movevolume = (SeekBar) findViewById(R.id.sbar_setting_movevolume);
		sbar_minvibration = (SeekBar) findViewById(R.id.sbar_setting_minvibration);

		radio_guideon = (RadioButton) findViewById(R.id.radioGuideOn);
		radio_guideoff = (RadioButton) findViewById(R.id.radioGuideOff);

		btn_save = (Button) findViewById(R.id.btn_setting_save);
		btn_reset = (Button) findViewById(R.id.btn_setting_reset);

		btn_save.setOnClickListener(buttonListener);
		btn_reset.setOnClickListener(buttonListener);

		sbar_sensitivity.setMax(150);
		sbar_sensitivity.setProgress(MagiccubePreference.GetPreference(
				MagiccubePreference.Sensitivity, context));

		sbar_difficulty.setMax(100);
		difficulty = MagiccubePreference.GetPreference(
				MagiccubePreference.Difficulty, context);
		sbar_difficulty.setProgress(difficulty);

		sbar_bgvolume.setMax(100);
		sbar_bgvolume.setProgress(MagiccubePreference.GetPreference(
				MagiccubePreference.BgVolume, context));

		sbar_movevolume.setMax(100);
		sbar_movevolume.setProgress(MagiccubePreference.GetPreference(
				MagiccubePreference.MoveVolume, context));

		sbar_minvibration.setMax(maxvibration);
		sbar_minvibration.setProgress(maxvibration
				- MagiccubePreference.GetPreference(
						MagiccubePreference.MinVibration, context));

		radio_guideon.setChecked(MagiccubePreference.GetPreference(
				MagiccubePreference.IsShowGuide, context) == 1);
		radio_guideoff.setChecked(MagiccubePreference.GetPreference(
				MagiccubePreference.IsShowGuide, context) == 0);

		sbar_sensitivity.setOnSeekBarChangeListener(seekBarChangeListener);
		sbar_difficulty.setOnSeekBarChangeListener(seekBarChangeListener);
		sbar_bgvolume.setOnSeekBarChangeListener(seekBarChangeListener);
		sbar_movevolume.setOnSeekBarChangeListener(seekBarChangeListener);
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub

	}

	private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			/*
			 * if( seekBar == sbar_difficulty) { if( difficulty == 50) { if(
			 * seekBar.getProgress()>50) { seekBar.setProgress(100); } else if(
			 * seekBar.getProgress()<50) { seekBar.setProgress(0); } } else
			 * if(difficulty == 0) { if( seekBar.getProgress()>0) {
			 * seekBar.setProgress(50); } } else if( difficulty == 100) { if(
			 * seekBar.getProgress()<100) { seekBar.setProgress(50); } }
			 * 
			 * difficulty = seekBar.getProgress(); }
			 */
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			if (seekBar == sbar_difficulty) {
				if (!Changed) {
					Changed = true;
					LastChangedTime = GetCurSeconds();
				} else {
					if (GetCurSeconds() - LastChangedTime > 1) {
						LastChangedTime = GetCurSeconds();
					} else {
						return;
					}
				}
				if (difficulty == 50) {
					if (seekBar.getProgress() > 50) {
						seekBar.setProgress(100);
						difficulty = 100;
					} else if (seekBar.getProgress() < 50) {
						seekBar.setProgress(0);
						difficulty = 0;
					}
				} else if (difficulty == 0) {
					if (seekBar.getProgress() > 0) {
						seekBar.setProgress(50);
						difficulty = 50;
					}
				} else if (difficulty == 100) {
					if (seekBar.getProgress() < 100) {
						seekBar.setProgress(50);
						difficulty = 50;
					}
				}
				Changed = false;
			}
		}

		private int GetCurSeconds() {
			Time t = new Time("GMT+8"); // Time Zone资料。

			t.setToNow(); // 取得系统时间。
			int hour = t.hour;
			int minute = t.minute;
			int second = t.second;

			return second + minute * 60 + hour * 3600;
		}

	};

	@Override
	public void dismiss() {
		super.dismiss();
		if (!Saved) {
			handler.sendEmptyMessage(0);
		}
	}

	private Button.OnClickListener buttonListener = new Button.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v == btn_save) {
				MagiccubePreference.SetPreference(
						MagiccubePreference.Sensitivity,
						sbar_sensitivity.getProgress(), context);
				MagiccubePreference.SetPreference(
						MagiccubePreference.Difficulty,
						sbar_difficulty.getProgress(), context);
				MagiccubePreference.SetPreference(MagiccubePreference.BgVolume,
						sbar_bgvolume.getProgress(), context);
				MagiccubePreference.SetPreference(
						MagiccubePreference.MoveVolume,
						sbar_movevolume.getProgress(), context);
				int value = radio_guideon.isChecked() ? 1 : 0;
				MagiccubePreference.SetPreference(
						MagiccubePreference.IsShowGuide, value, context);
				MagiccubePreference.SetPreference(
						MagiccubePreference.MinVibration, maxvibration
								- sbar_minvibration.getProgress(), context);
				DialogSetting.this.dismiss();
				Saved = true;
				handler.sendEmptyMessage(1);
				return;
			} else if (v == btn_reset) {
				sbar_sensitivity.setProgress(50);
				sbar_difficulty.setProgress(50);
				sbar_bgvolume.setProgress(50);
				sbar_movevolume.setProgress(50);
				sbar_minvibration.setProgress(maxvibration - 13);
				return;
			}
		}
	};
}
