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
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import flex.android.magiccube.DBHelper;
import flex.android.magiccube.MagiccubePreference;
import flex.android.magiccube.MusicPlayThread;
import flex.android.magiccube.R;
import flex.android.magiccube.Util;
import flex.android.magiccube.bluetooth.ActivityClient;
import flex.android.magiccube.bluetooth.ActivityTab;
import flex.android.magiccube.bluetooth.BluetoothChatService;
import flex.android.magiccube.bluetooth.BluetoothSignal;
import flex.android.magiccube.bluetooth.MessageSender;
import flex.android.magiccube.dialog.DialogBattleMode;
import flex.android.magiccube.dialog.DialogSetting;
import flex.android.magiccube.interfaces.OnStateListener;
import flex.android.magiccube.interfaces.OnStepListener;
import flex.android.magiccube.interfaces.OnTimerListener;
import flex.android.magiccube.view.ViewBattleMode;

public class ActivityBattleMode extends ActivitySensorListener implements
		OnTimerListener, OnStateListener, OnStepListener, MessageSender {
	private ImageButton btMoveBack;
	private ImageButton btMoveForward;
	private ImageButton btStartOb;

	private LinearLayout layout_moveforward;
	private LinearLayout layout_moveback;

	private SeekBar progress;
	private ImageView clock;
	private ImageView img_nstep;
	private TextView txtTime;
	private TextView txtNStep;

	private ViewBattleMode glView;
	private ViewBattleMode glView2;

	private MediaPlayer GamingbgmPlayer;
	private MediaPlayer FinishbgmPlayer;
	private MediaPlayer ObservingbgmPlayer;

	private int State;
	private int MoveTime;
	private int nStep;
	private int TotalObTime; // in seconds, time to observe

	private ProgressDialog processDialog;

	private float LinearBgVolume;
	private int BgVolume;

	private DialogBattleMode dialog;
	private DialogSetting dialog2;

	private boolean started = false;
	private boolean opleaved = false;

	private int width;
	private int height;

	private String MoveTimes = "";
	private String MoveTimes2 = "";

	private int nMessUp = 30;

	private boolean HeartBeating = false; // indicate that the bluetooth is
											// still alive

	// Bluetooth
	// Name of the connected device
	private String mConnectedDeviceName = null;

	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private BluetoothChatService mChatService = null;
	// Intent request codes

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static final int NOT_HEART_BEATING = 6;
	public static final int NOT_ENABLE_DISCOVERY = 7;
	public static final int CONNECT_FAILED = 8;

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";
	public static final String CLIENT_OR_SERVER = "serverorclient";

	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;
	private static final int REQUEST_SETTING = 3;
	private static final int REQUEST_DISCOVERABLE = 4;


	private DBHelper dbHelper = null;
	private final String TableName = "flex_magiccube_replay";
	private final String TableContent = "_id integer primary key autoincrement, cmdstrbefore text, cmdstrafter text, savetime datetime, movetimes text";

	private Handler uihandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0: // make the progressbar disappear
				btStartOb.setVisibility(View.VISIBLE);
				break;
			case 2:
				glView2.setVisibility(View.VISIBLE);
				progress.setVisibility(View.GONE);

				img_nstep.setVisibility(View.VISIBLE);
				txtTime.setVisibility(View.VISIBLE);
				txtNStep.setVisibility(View.VISIBLE);

				btMoveBack.setVisibility(View.VISIBLE);
				btMoveForward.setVisibility(View.VISIBLE);

				//ActivityBattleMode.this.SetCanVibrate(true);

				break;
			case 3:
				// setTime(ActivityClockingMode.this.MoveTime);
			case 4:
				txtTime.setText(""
						+ Util.TimeFormat(ActivityBattleMode.this.MoveTime));
				if (MoveTimes == "") {
					MoveTimes += "" + MoveTime;
				} else {
					MoveTimes += " " + MoveTime;
				}
				txtNStep.setText("" + nStep);
				break;
			case 5:
				btStartOb.setVisibility(View.INVISIBLE);
				break;
			case 6: // lose
				GamingbgmPlayer.pause();
				MediaPlayer loseplayer = MediaPlayer.create(
						ActivityBattleMode.this, R.raw.lose);
				loseplayer.start();
				dialog = new DialogBattleMode(ActivityBattleMode.this,
						"�Է�����һ��...", MoveTime);
				dialog.show();
				break;
			case 7: // win
				GamingbgmPlayer.pause();
				MediaPlayer winplayer = MediaPlayer.create(
						ActivityBattleMode.this, R.raw.win);
				winplayer.start();
				dialog = new DialogBattleMode(ActivityBattleMode.this,
						"��������ɣ�", MoveTime);
				dialog.show();
				break;
			case 8:
				txtNStep.setText(nStep + "");
				txtNStep.setVisibility(View.INVISIBLE);
				clock.setVisibility(View.INVISIBLE);
				img_nstep.setVisibility(View.INVISIBLE);
				txtTime.setVisibility(View.INVISIBLE);

				glView2.setVisibility(View.INVISIBLE);
				btStartOb.setVisibility(View.VISIBLE);

				btMoveBack.setVisibility(View.INVISIBLE);
				btMoveForward.setVisibility(View.INVISIBLE);
				break;
			case 9:
				txtNStep.setVisibility(View.INVISIBLE);
				clock.setVisibility(View.INVISIBLE);
				img_nstep.setVisibility(View.INVISIBLE);
				txtTime.setVisibility(View.INVISIBLE);
				glView2.setVisibility(View.INVISIBLE);
				btMoveBack.setVisibility(View.INVISIBLE);
				btMoveForward.setVisibility(View.INVISIBLE);
			}
		}
	};

	private Handler buttonhandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				btMoveForward
						.setBackgroundResource(R.drawable.buttons_moveforward);
				break;
			case 1:
				btMoveForward
						.setBackgroundResource(R.drawable.buttons_moveforward_unable);
				break;
			case 2:
				btMoveBack.setBackgroundResource(R.drawable.buttons_moveback);
				break;
			case 3:
				btMoveBack
						.setBackgroundResource(R.drawable.buttons_moveforward_unable);
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mode_battle);
		btMoveBack = (ImageButton) findViewById(R.id.bt_battle_moveback);
		btMoveForward = (ImageButton) findViewById(R.id.bt_battle_moveforward);
		btStartOb = (ImageButton) findViewById(R.id.bt_battle_startob);

		clock = (ImageView) findViewById(R.id.img_battle_clock);
		txtTime = (TextView) findViewById(R.id.txt_battle_time);
		txtNStep = (TextView) findViewById(R.id.txt_battle_nStep);
		img_nstep = (ImageView) findViewById(R.id.img_battle_nstep);
		progress = (SeekBar) findViewById(R.id.sbar_battle_timer);

		/*
		 * clock.setVisibility(View.VISIBLE);
		 * btMoveForward.setVisibility(View.VISIBLE);
		 * btMoveBack.setVisibility(View.VISIBLE);
		 */

		glView = (ViewBattleMode) findViewById(R.id.game_view_battle);
		glView.SetOnTimerListener(this);
		// glView.setOnStateListener(this);
		glView.SetOnStepListener(this);
		glView.SetMessageSender(this);
		glView.SetDrawCube(false);
		glView.setOnStateListener(this);

		glView2 = (ViewBattleMode) findViewById(R.id.game_view_battle2);
		glView2.SetCanMove(false);

		btMoveBack.setOnClickListener(buttonListener);
		btMoveForward.setOnClickListener(buttonListener);
		btStartOb.setOnClickListener(buttonListener);

		float fontsize = 25.f;
		Typeface typeFace = Typeface.createFromAsset(getAssets(),
				"fonts/Angies New House.ttf");
		txtTime.setTypeface(typeFace);
		txtTime.setTextColor(Color.GRAY);
		txtTime.setTextSize(fontsize);

		txtNStep.setTypeface(typeFace);
		txtNStep.setTextColor(Color.GRAY);
		txtNStep.setTextSize(fontsize);

		State = OnStateListener.NONE;

		BgVolume = MagiccubePreference.GetPreference(
				MagiccubePreference.BgVolume, this);
		LinearBgVolume = MusicPlayThread.GetLinearVolume(BgVolume);

		GamingbgmPlayer = MediaPlayer.create(this, R.raw.bg2);
		ObservingbgmPlayer = MediaPlayer.create(this, R.raw.bg);
		FinishbgmPlayer = MediaPlayer.create(this, R.raw.finish);

		GamingbgmPlayer.setVolume(LinearBgVolume, LinearBgVolume);
		ObservingbgmPlayer.setVolume(LinearBgVolume, LinearBgVolume);
		FinishbgmPlayer.setVolume(LinearBgVolume, LinearBgVolume);

		Init();

		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "����������...", Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

		width = wm.getDefaultDisplay().getWidth();
		height = wm.getDefaultDisplay().getHeight();

		layout_moveforward = (LinearLayout) findViewById(R.id.layout_battle_moveforward);
		layout_moveback = (LinearLayout) findViewById(R.id.layout_battle_moveback);

		int buttonwidth = width / 5;
		int buttonheight = height / 13;

		LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(
				buttonwidth, buttonheight);

		btMoveForward.setLayoutParams(parms);
		btMoveBack.setLayoutParams(parms);

		layout_moveforward.setPadding(width / 2 - buttonwidth / 2, height
				- buttonheight, 0, 0);
		layout_moveback.setPadding(width / 15, height - buttonheight, 0, 0);

	}

	private void Init() {
		// TODO Auto-generated method stub

		progress.setEnabled(false);
		// progress.setAlpha(1.f);
		progress.setBackgroundColor(2);

		// clock.setVisibility(View.VISIBLE);
		// btStartOb.setVisibility(View.INVISIBLE);
		// progress.setVisibility(View.INVISIBLE);
		// txtTime.setVisibility(View.INVISIBLE);
		// clock.setVisibility(View.INVISIBLE);
		// txtNStep.setVisibility(View.INVISIBLE);
		img_nstep.setVisibility(View.INVISIBLE);
		btMoveBack.setVisibility(View.INVISIBLE);
		btMoveForward.setVisibility(View.INVISIBLE);

		glView.SetCanMove(false);
		glView.SetCanRotate(false);
		glView.SetDrawCube(false);

		// glView2.setVisibility(View.INVISIBLE);

		State = OnStateListener.NONE;
		nStep = 0;

		this.SetCanVibrate(false);
	}

	@Override
	public void onStart() {
		super.onStart();

		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// Otherwise, setup the chat session
		} else {
			if (mChatService == null)
				setupChat();
			Intent intent = new Intent(ActivityBattleMode.this,
					ActivityTab.class);
			startActivityForResult(intent, REQUEST_SETTING);
		}

	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		// Log.e("onresume", "onresume");
		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity
		// returns.
		if (mChatService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
				// Start the Bluetooth chat services
				mChatService.start();
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	/*
	 * @Override public synchronized void onResume() { super.onResume(); }
	 */

	@Override
	public void AddStep() {
		// TODO Auto-generated method stub
		uihandler.sendEmptyMessage(2);
	}

	@Override
	public void SetStep(int nStep) {
		// TODO Auto-generated method stub
		this.nStep = nStep;
		uihandler.sendEmptyMessage(4);
	}

	@Override
	public void OnStateChanged(int StateMode) {
		// TODO Auto-generated method stub
		State = StateMode;

		if (StateMode == OnStateListener.GAMING) {
			glView.SetCanMove(true);
			glView.SetCanRotate(true);

			uihandler.sendEmptyMessage(2);

			GamingbgmPlayer.setLooping(true);// ����ѭ������
			GamingbgmPlayer.seekTo(0);
			GamingbgmPlayer.start();
			ObservingbgmPlayer.pause();
		} else if (StateMode == OnStateListener.WIN) {
			JSONObject jobj = new JSONObject();
			try {
				jobj.put(BluetoothSignal.Win, MoveTime + "");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sendMessage(jobj.toString());
			uihandler.sendEmptyMessage(7);

			// glView2.set
		} else if (StateMode == OnStateListener.FINISH) {
		} else if (StateMode == OnStateListener.LOSE) {
			uihandler.sendEmptyMessage(6);
			// glView.setMode(OnStateListener.LOSE)
		}
	}

	@Override
	public void onTimer(int nTime) {
		// TODO Auto-generated method stub
		if (State == OnStateListener.OBSERVING) {
			progress.setProgress(nTime);
			if (nTime < 5 && nTime >= 1) {
				MusicPlayThread musicPlay = new MusicPlayThread(this,
						R.raw.onesecond, BgVolume);
				musicPlay.start();
			} else if (nTime == 0) {
				MusicPlayThread musicPlay = new MusicPlayThread(this,
						R.raw.lastsecond, BgVolume);
				musicPlay.start();
			}
		} else if (State == OnStateListener.GAMING) {
			uihandler.sendEmptyMessage(3);
			MoveTime = nTime;
		}
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
			} else if (v == btStartOb) {
				StartOb();

				JSONObject jobj = new JSONObject();
				try {
					jobj.put(BluetoothSignal.StartObserve, "start");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				sendMessage(jobj.toString());
				return;
			}
		}
	};

	/*
	 * @Override public boolean onCreateOptionsMenu(Menu menu) { MenuInflater
	 * inflater = getMenuInflater(); inflater.inflate(R.menu.optionmenu_battle,
	 * menu); return true; }
	 */
	private void StartOb() {
		if (started) {
			return;
		}
		started = true;

		glView.SetCanRotate(true);
		glView.SetDrawCube(true);
		glView.SetTotalObTime(TotalObTime);
		btStartOb.setVisibility(View.GONE);
		// glView2.setVisibility(View.VISIBLE);

		clock.setVisibility(View.VISIBLE);

		if (TotalObTime > 0) {
			ObservingbgmPlayer.setLooping(true);// ����ѭ������
			ObservingbgmPlayer.seekTo(0);
			ObservingbgmPlayer.start();
			glView.StartObserve();
			glView.SetCanMove(false);
			progress.setMax(TotalObTime);
			progress.setProgress(TotalObTime);
			// Log.e("obtime", TotalObTime+"");
			progress.setVisibility(View.VISIBLE);
			State = OnStateListener.OBSERVING;
		} else {
			glView.SetCanMove(true);
			glView.SetCanRotate(true);
			glView.StartGaming();
			State = OnStateListener.GAMING;
			uihandler.sendEmptyMessage(2);

			GamingbgmPlayer.setLooping(true);// ����ѭ������
			GamingbgmPlayer.seekTo(0);
			GamingbgmPlayer.start();
		}
	}

	private void ensureDiscoverable() {
		if (mBluetoothAdapter == null) {
			return;
		}
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivityForResult(discoverableIntent, REQUEST_DISCOVERABLE);
		}
	}

	private void sendMessage(String message) {
		if (opleaved) {
			return;
		}
		// Check that we're actually connected before trying anything
		if (mChatService == null) {
			return;
		}
		if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
			// Toast.makeText(this, R.string.not_connected,
			// Toast.LENGTH_SHORT).show();
			return;
		}

		// Check that there's actually something to send
		if (message.length() > 0) {
			// Get the message bytes and tell the BluetoothChatService to write
			try {
				byte[] send = message.getBytes();
				mChatService.write(send);
			} catch (Exception e) {
				LeaveOnError();
			}

		}
	}

	private void setupChat() {
		// Initialize the BluetoothChatService to perform bluetooth connections
		// Log.e("tag", "setupChat()");
		mChatService = new BluetoothChatService(this, mHandler);

	}

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case BluetoothChatService.STATE_CONNECTED:
					showWait("������Ϸ...");

					try {
						SetupGame();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (Exception ee) {
						LeaveOnError();
					}
					/*
					 * new Thread(new Runnable(){
					 * 
					 * @Override public void run() { // TODO Auto-generated
					 * method stub SetupGame(); waitClose();
					 * 
					 * }}).start();
					 */
					// mTitle.setText(R.string.title_connected_to);
					// mTitle.append(mConnectedDeviceName);
					// mConversationArrayAdapter.clear();
					break;
				case BluetoothChatService.STATE_CONNECTING:
					// mTitle.setText(R.string.title_connecting);
					showWait("������...");
					break;
				case BluetoothChatService.STATE_LISTEN:
				case BluetoothChatService.STATE_NONE:
					// mTitle.setText(R.string.title_not_connected);
					break;
				}
				break;
			case MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				// construct a string from the buffer
				String writeMessage = new String(writeBuf);
				// mConversationArrayAdapter.add("Me:  " + writeMessage);
				break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				// construct a string from the valid bytes in the buffer
				String readMessage = new String(readBuf, 0, msg.arg1);
				// glView2.SetCommand(readMessage);
				try {
					ReadMessage(readMessage);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Connected to " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			case OnStateListener.LEAVING_ON_ERROR:
				LeaveOnError();
				break;
			case NOT_HEART_BEATING:
				Toast toast = Toast.makeText(ActivityBattleMode.this, "�޷�������Ϸ",
						Toast.LENGTH_LONG);
				toast.setGravity(Gravity.BOTTOM, 0, height / 11);
				toast.show();
				Intent intent = new Intent(ActivityBattleMode.this,
						ActivityTab.class);
				startActivityForResult(intent, REQUEST_SETTING);
				break;
			case NOT_ENABLE_DISCOVERY:
				Toast toast2 = Toast.makeText(ActivityBattleMode.this,
						"�޷���������", Toast.LENGTH_LONG);
				toast2.setGravity(Gravity.BOTTOM, 0, height / 11);
				toast2.show();
				Intent intent2 = new Intent(ActivityBattleMode.this,
						ActivityTab.class);
				startActivityForResult(intent2, REQUEST_SETTING);
				break;
			case CONNECT_FAILED:
				Toast toast3 = Toast.makeText(ActivityBattleMode.this,
						"�������Ӳ��ɹ���������...", Toast.LENGTH_LONG);
				toast3.setGravity(Gravity.BOTTOM, 0, height / 11);
				toast3.show();
				Intent intent3 = new Intent(ActivityBattleMode.this,
						ActivityTab.class);
				startActivityForResult(intent3, REQUEST_SETTING);
				break;
			}
		}
	};

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				// Get the device MAC address
				String address = data.getExtras().getString(
						ActivityClient.EXTRA_DEVICE_ADDRESS);
				// Get the BLuetoothDevice object
				BluetoothDevice device = mBluetoothAdapter
						.getRemoteDevice(address);
				// Attempt to connect to the device
				mChatService.connect(device);
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session
				setupChat();
				Intent intent = new Intent(this, ActivityTab.class);
				startActivityForResult(intent, REQUEST_SETTING);
			} else {
				// User did not enable Bluetooth or an error occured
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
			break;
		case REQUEST_SETTING:
			if (resultCode == Activity.RESULT_OK) {
				// Log.e("ok","ok");
				if (MagiccubePreference.GetPreference(
						MagiccubePreference.ServerOrClient, this) == 1) {
					/*
					 * if (!mBluetoothAdapter.isEnabled()) { Intent enableIntent
					 * = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
					 * startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
					 * // Otherwise, setup the chat session } else { if
					 * (mChatService == null) setupChat(); }
					 */
					if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
						ensureDiscoverable();
					} else {
						this.showWait("�ȴ�����...");
					}
				} else if (MagiccubePreference.GetPreference(
						MagiccubePreference.ServerOrClient, this) == 0) {
					String address = this.getSharedPreferences(
							MagiccubePreference.SHAREDPREFERENCES_NAME,
							Context.MODE_PRIVATE).getString(
							MagiccubePreference.ServerAddress, "");
					// Get the BLuetoothDevice object
					BluetoothDevice device = mBluetoothAdapter
							.getRemoteDevice(address);
					// Attempt to connect to the device
					mChatService.connect(device);
				} else {
					finish();
				}
			}

			break;

		case REQUEST_DISCOVERABLE:
			// Log.e("resultCode", resultCode+"");
			if (resultCode > 0) {
				this.showWait("�ȴ�����...");
			} else {
				mHandler.sendEmptyMessage(NOT_ENABLE_DISCOVERY);
			}
			break;
		}
	}

	@Override
	public void SendMessage(String msg) {
		// TODO Auto-generated method stub
		JSONObject jobj = new JSONObject();
		try {
			jobj.put(BluetoothSignal.Command, msg);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.sendMessage(jobj.toString());
	}

	@Override
	public void AddStep(int nStep) {
		// TODO Auto-generated method stub

	}

	public void showWait(final String message) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (processDialog != null) {
					if (processDialog.isShowing()) {
						processDialog.dismiss();
						processDialog = null;
					}
				}

				processDialog = new ProgressDialog(ActivityBattleMode.this);
				processDialog.setMessage(message);
				processDialog.setIndeterminate(true);
				processDialog.setCancelable(true);
				processDialog.setOnCancelListener(new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface arg0) {
						// TODO Auto-generated method stub
						Intent intent = new Intent(ActivityBattleMode.this,
								ActivityTab.class);
						startActivityForResult(intent, REQUEST_SETTING);
					}

				});
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

	public void waitClose2() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				waitClose();

			}
		}).start();
	}

	private void SetupGame() throws JSONException {
		if (MagiccubePreference.GetPreference(
				MagiccubePreference.ServerOrClient, this) == 1) {
			JSONObject jobj = new JSONObject();

			// 1. setup the messup
			String MessUpCmd1 = "";
			String MessUpCmd2 = "";

			switch (MagiccubePreference.GetPreference(
					MagiccubePreference.Difficulty, this)) {
			case 0:
				nMessUp = 4;
				break;
			case 50:
				nMessUp = 10;
				break;
			case 100:
				nMessUp = 30;
				break;
			}

			if (MagiccubePreference.GetPreference(
					MagiccubePreference.IsSameMessup, this) == 1) {
				MessUpCmd1 = glView.MessUp(nMessUp);
				glView2.MessUp(MessUpCmd1);
				jobj.put(BluetoothSignal.MessUpCmd2, MessUpCmd1);
				jobj.put(BluetoothSignal.MessUpCmd1, MessUpCmd1);
			} else {
				MessUpCmd1 = glView.MessUp(nMessUp);
				MessUpCmd2 = glView2.MessUp(nMessUp);

				jobj.put(BluetoothSignal.MessUpCmd2, MessUpCmd1);
				jobj.put(BluetoothSignal.MessUpCmd1, MessUpCmd2);
			}

			// 2. setup the obervetime
			// Log.e("2", "2");
			TotalObTime = MagiccubePreference.GetPreference(
					MagiccubePreference.ObserveTime, this);
			// Toast.makeText(this, TotalObTime+"", Toast.LENGTH_SHORT).show();
			jobj.put(BluetoothSignal.ObserveTime, TotalObTime + "");

			// 3. end up
			// Log.e("3", "3");
			jobj.put(BluetoothSignal.EndSetUp, "end");

			this.sendMessage(jobj.toString());

		} else if (MagiccubePreference.GetPreference(
				MagiccubePreference.ServerOrClient, this) == 0) {
			long waittime = 6000;
			Timer t = new Timer();
			t.schedule(new TimerTask() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					if (!HeartBeating) {
						mHandler.sendEmptyMessage(NOT_HEART_BEATING);

					}
				}
			}, waittime);
		}
	}

	private void ResetViews() {
		started = false;
		this.nStep = 0;
		this.dialog.dismiss();
		glView.SetCanMove(false);
		glView.Reset();
		glView.SetDrawCube(false);
		glView2.Reset();

		uihandler.sendEmptyMessage(8);

	}

	private void ResetViews2() {
		started = false;
		this.nStep = 0;
		if (dialog != null) {
			this.dialog.dismiss();
		}
		glView.Reset();
		glView.SetDrawCube(false);
		glView2.Reset();

		uihandler.sendEmptyMessage(9);

	}

	private void ReSetupGame() throws JSONException {
		ResetViews();
		if (MagiccubePreference.GetPreference(
				MagiccubePreference.ServerOrClient, this) == 1) {
			JSONObject jobj = new JSONObject();
			// 1. reset up the game
			jobj.put(BluetoothSignal.ReSetup, "xx");
			// 2. setup the messup

			int nMessUp = 3;
			String MessUpCmd1 = "";
			String MessUpCmd2 = "";

			switch (MagiccubePreference.GetPreference(
					MagiccubePreference.Difficulty, this)) {
			case 0:
				nMessUp = 5;
			case 50:
				nMessUp = 10;
			case 100:
				nMessUp = 30;
			}

			if (MagiccubePreference.GetPreference(
					MagiccubePreference.IsSameMessup, this) == 1) {
				MessUpCmd1 = glView.MessUp(nMessUp);
				glView2.MessUp(MessUpCmd1);
				jobj.put(BluetoothSignal.MessUpCmd2, MessUpCmd1);
				jobj.put(BluetoothSignal.MessUpCmd1, MessUpCmd1);
			} else {
				MessUpCmd1 = glView.MessUp(nMessUp);
				MessUpCmd2 = glView2.MessUp(nMessUp);

				jobj.put(BluetoothSignal.MessUpCmd2, MessUpCmd1);
				jobj.put(BluetoothSignal.MessUpCmd1, MessUpCmd2);
			}

			// 3. setup the obervetime
			TotalObTime = MagiccubePreference.GetPreference(
					MagiccubePreference.ObserveTime, this);
			// Toast.makeText(this, TotalObTime+"", Toast.LENGTH_SHORT).show();
			jobj.put(BluetoothSignal.ObserveTime, TotalObTime + "");

			// 4. end up
			jobj.put(BluetoothSignal.EndSetUp, "end");

			this.sendMessage(jobj.toString());

		} else if (MagiccubePreference.GetPreference(
				MagiccubePreference.ServerOrClient, this) == 0) {
			JSONObject jobj = new JSONObject();
			// 1. reset up the game
			jobj.put(BluetoothSignal.ReSetup, "xx");
			this.sendMessage(jobj.toString());
		}
	}

	private void ReadMessage(String Message) throws JSONException {

		JSONObject jobj = new JSONObject(Message);
		String value = "";

		if ((value = jobj.optString(BluetoothSignal.Leave)) != "") {
			this.HeartBeating = false;
			if (mChatService != null) {
				// mChatService.stop();
				// mChatService = null;
				Toast toast = Toast.makeText(this, "�Է����뿪", Toast.LENGTH_LONG);
				toast.setGravity(Gravity.BOTTOM, 0, height / 11);
				toast.show();
			}
			this.opleaved = true;
		}
		if ((value = jobj.optString(BluetoothSignal.ReSetup)) != "") {
			ResetViews();
			if (MagiccubePreference.GetPreference(
					MagiccubePreference.ServerOrClient, this) == 1) {
				SetupGame();
			}
		}
		if ((value = jobj.optString(BluetoothSignal.MessUpCmd1)) != "") {
			// Log.e("MessUpCmd1","MessUpCmd1");
			HeartBeating = true;
			glView.MessUp(value);
		}
		if ((value = jobj.optString(BluetoothSignal.MessUpCmd2)) != "") {
			HeartBeating = true;
			glView2.MessUp(value);
		}
		if ((value = jobj.optString(BluetoothSignal.ObserveTime)) != "") {
			HeartBeating = true;
			TotalObTime = Integer.parseInt(value);

			uihandler.sendEmptyMessage(0);
		}
		if ((value = jobj.optString(BluetoothSignal.EndSetUp)) != "") {
			HeartBeating = true;
			JSONObject jobj2 = new JSONObject();
			jobj2.put(BluetoothSignal.ClientEndSetUp, "end");
			// Log.e("EndSetUp","EndSetUp");
			waitClose2();
			// Init();
			this.sendMessage(jobj2.toString());
			uihandler.sendEmptyMessage(0);
		}
		if ((value = jobj.optString(BluetoothSignal.ClientEndSetUp)) != "") {
			// Log.e("ClientEndSetUp","ClientEndSetUp");
			waitClose2();
			// Init();
			uihandler.sendEmptyMessage(0);
		}
		if ((value = jobj.optString(BluetoothSignal.Command)) != "") {
			glView2.SetCommand(value);
			if (MoveTimes2 == "") {
				MoveTimes2 += "" + this.MoveTime;
			} else {
				MoveTimes2 += " " + this.MoveTime;
			}
		}
		if ((value = jobj.optString(BluetoothSignal.StartObserve)) != "") {
			uihandler.sendEmptyMessage(5);
			StartOb();
		}
		if ((value = jobj.optString(BluetoothSignal.Win)) != "") {
			this.MoveTime = Integer.parseInt(value);
			this.txtTime.setText(""
					+ Util.TimeFormat(ActivityBattleMode.this.MoveTime));
			glView.OnStateChanged(OnStateListener.LOSE);
			this.OnStateChanged(OnStateListener.LOSE);
		}

	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void finish() {
		super.finish();

		if (GamingbgmPlayer.isPlaying()) {
			GamingbgmPlayer.stop();
			GamingbgmPlayer.release();
		}
		if (FinishbgmPlayer.isPlaying()) {
			FinishbgmPlayer.stop();
			FinishbgmPlayer.release();
		}
		if (ObservingbgmPlayer.isPlaying()) {
			ObservingbgmPlayer.stop();
			ObservingbgmPlayer.release();
		}
		glView.onStop();
		glView2.onStop();
		Leave2();
	}

	public void Leave() {
		/*
		 * if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED)
		 * { return; }
		 */

		if (!opleaved) {
			JSONObject jobj = new JSONObject();
			try {
				jobj.put(BluetoothSignal.Leave, "xx");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.sendMessage(jobj.toString());
			// Stop the Bluetooth chat services
			if (mChatService != null) {
				mChatService.stop();
				// mChatService = null;
			}
		} else {
			opleaved = false;
			// if (mChatService != null) mChatService.stop();
			// mChatService = null;
		}

		ResetViews2();
		Intent intent = new Intent(this, ActivityTab.class);
		startActivityForResult(intent, REQUEST_SETTING);
	}

	public void LeaveOnError() {
		LeaveOnError("����ͨѶ����...����ϵ��:flexwang@qq.com");
	}

	public void LeaveOnError(String msg) {
		Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.BOTTOM, 0, height / 11);
		toast.show();
		State = OnStateListener.LEAVING_ON_ERROR;
		finish();
	}

	public void Leave2() {
		/*
		 * if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED)
		 * { return; }
		 */
		if (State == OnStateListener.LEAVING_ON_ERROR) {
			if (mChatService != null) {
				mChatService.stop();
				// mChatService = null;
			}
		} else {
			if (!opleaved) {
				JSONObject jobj = new JSONObject();
				try {
					jobj.put(BluetoothSignal.Leave, "xx");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				this.sendMessage(jobj.toString());
				// Stop the Bluetooth chat services
				if (mChatService != null) {
					mChatService.stop();
					// mChatService = null;
				}
			}
		}
	}

	public void Reset() {
		// TODO Auto-generated method stub
		if (!opleaved) {
			try {

				ReSetupGame();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			MoveTimes = "";
			MoveTimes2 = "";
			opleaved = false;
			ResetViews2();
			Intent intent = new Intent(this, ActivityTab.class);
			startActivityForResult(intent, REQUEST_SETTING);
		}
	}

	public void SaveReplay() {
		// TODO Auto-generated method stub
		if (dbHelper == null) {
			dbHelper = new DBHelper(this);
		}

		dbHelper.create(TableName, TableContent);
		ContentValues values = new ContentValues();
		values.put("cmdstrbefore", this.GetCmdStrBefore());
		values.put("cmdstrafter", this.GetCmdStrAfter());

		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		String datestr = dateFormat.format(date);
		values.put("savetime", datestr);

		values.put("movetimes", this.GetMoveTimes());

		dbHelper.insert(TableName, values);

		Toast toast;
		if (State == OnStateListener.WIN) {
			toast = Toast.makeText(getApplicationContext(), "���¼���ѱ�����"
					+ datestr, Toast.LENGTH_LONG);
		} else {
			toast = Toast.makeText(getApplicationContext(), "�Է�¼���ѱ�����"
					+ datestr, Toast.LENGTH_LONG);
		}

		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();

	}

	private String GetMoveTimes() {
		if (State == OnStateListener.WIN) {
			return this.MoveTimes;
		} else {
			return this.MoveTimes2;
		}
	}

	private String GetCmdStrAfter() {
		if (State == OnStateListener.WIN) {
			return this.glView.GetCmdStrAfter();
		} else {
			return this.glView2.GetCmdStrAfter();
		}
	}

	private String GetCmdStrBefore() {
		if (State == OnStateListener.WIN) {
			return this.glView.GetCmdStrBefore();
		} else {
			return this.glView.GetCmdStrBefore();
		}
	}

	@Override
	public void OnStateNotify(int StateMode) {
		// TODO Auto-generated method stub
		// Log.e("statemode","statemode");
		switch (StateMode) {
		case OnStateListener.CANMOVEFORWARD:
			// Log.e("CANMOVEFORWARD", "CANMOVEFORWARD");
			buttonhandler.sendEmptyMessage(0);
			break;
		case OnStateListener.CANNOTMOVEFORWARD:
			buttonhandler.sendEmptyMessage(1);
			break;
		case OnStateListener.CANMOVEBACK:
			// Log.e("CANMOVEBACK", "CANMOVEBACK");
			buttonhandler.sendEmptyMessage(2);
			break;
		case OnStateListener.CANNOTMOVEBACK:
			buttonhandler.sendEmptyMessage(3);
			break;

		}
	}

	@Override
	public void onShake() {
		// TODO Auto-generated method stub

		this.glView.SetCanMove(false);
		SetCanVibrate(false);
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				glView.AutoSolve2("Jaap");

			}

		}).start();
		this.ResetVibrateState();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			dialog2 = new DialogSetting(this, settinghandler);
			dialog2.show();
			return false;
		} else if (keyCode == KeyEvent.KEYCODE_BACK
				|| keyCode == KeyEvent.KEYCODE_HOME) {
			showExitGameAlert();
			return false;
		}
		return super.onKeyDown(keyCode, event);
		// return super.onKeyDown(keyCode, event);
	}

	private Handler settinghandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				glView.onResume();
				break;
			case 1: // make the progressbar disappear
				int volume = MagiccubePreference.GetPreference(
						MagiccubePreference.BgVolume, ActivityBattleMode.this);
				BgVolume = volume;
				GamingbgmPlayer.setVolume(
						MusicPlayThread.GetLinearVolume(volume),
						MusicPlayThread.GetLinearVolume(volume));
				ObservingbgmPlayer.setVolume(
						MusicPlayThread.GetLinearVolume(volume),
						MusicPlayThread.GetLinearVolume(volume));
				FinishbgmPlayer.setVolume(
						MusicPlayThread.GetLinearVolume(volume),
						MusicPlayThread.GetLinearVolume(volume));
				setMinVibration(MagiccubePreference.GetPreference(
						MagiccubePreference.MinVibration,
						ActivityBattleMode.this));

				glView.setVolume(MagiccubePreference
						.GetPreference(MagiccubePreference.MoveVolume,
								ActivityBattleMode.this));
				glView.SetSensitivity((float) MagiccubePreference
						.GetPreference(MagiccubePreference.Sensitivity,
								ActivityBattleMode.this) / 100.f);
				glView.onResume();
				break;
			}
		}
	};

	private void showExitGameAlert() {
		String msg = "�뿪���㽫�����Ϸ��";
		if (this.opleaved) {
			msg = "�뿪��,��Ϸ��Ƚ����ܱ��档";

		}
		new AlertDialog.Builder(this).setTitle("ȷ���뿪").setMessage(msg)
				.setIcon(R.drawable.ic_exit)
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// �����ȷ�ϡ���Ĳ���
						ActivityBattleMode.this.finish();

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
