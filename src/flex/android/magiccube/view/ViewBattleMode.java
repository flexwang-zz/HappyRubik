package flex.android.magiccube.view;

import flex.android.magiccube.interfaces.OnStateListener;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

public class ViewBattleMode extends BasicGameView implements OnStateListener{
	private OnStateListener stateListener = null;
	private int TotalObTime;
	private int LeftObTime;
	private RefreshTime refreshTime = null;
	private boolean threadstop = false;
	private boolean threadpause = false;
	private boolean StopOb;
	private int State;
	
	public ViewBattleMode(Context context, AttributeSet attr) {
		super(context, attr);
		// TODO Auto-generated constructor stub
		nStep = 0;
		State = OnStateListener.NONE;
		threadstop = false;
		this.render.SetOnStateListener(this);
		this.render.SetOnStepListnener(this);
	}
	
	public void SetCommand(String CmdStr)
	{
		this.render.SetCommand(CmdStr);
	}
	
	public void SetTotalObTime(int TotalTime)
	{
		this.TotalObTime = TotalTime;	
	}

	public void setOnStateListener(OnStateListener stateListener)
	{
		this.stateListener = stateListener;
	}
	
	public void setMode(int stateMode) {
		if( stateListener != null)
		{
			this.stateListener.OnStateChanged(stateMode);
		}
	}
	
	public void onStop()
	{
		threadstop = true;
		threadState = THREADSTOP;
	}
	
	private class RefreshTime extends Thread {
		public void run() {
			//Log.e("e", "clocking");
			while( true)
			{
				if( threadState == THREADSTOP )
				{
					break;
				}
				else if( threadState == THREADPAUSE )
				{
					continue;
				}
				if( State == OnStateListener.OBSERVING)
				{
					while (LeftObTime >= 0) {
						//Log.e("LeftObTime", LeftObTime+"");
						if( threadState == THREADSTOP )
						{
							break;
						}
						else if( threadState == THREADPAUSE )
						{
							continue;
						}

						timerListener.onTimer(LeftObTime);
						LeftObTime--;
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						MoveTime = 0;
					}
					if(LeftObTime < 0){
						//Log.e("LeftObTime", LeftObTime+"");
						State = OnStateListener.GAMING;
						setMode(OnStateListener.GAMING);
					}
				}
				else if(State == OnStateListener.GAMING )
				{
					while( State == OnStateListener.GAMING)
					{
						//Log.e("MoveTime", MoveTime+"");
						if( threadState == THREADSTOP )
						{
							break;
						}
						else if( threadState == THREADPAUSE )
						{
							continue;
						}
						timerListener.onTimer(MoveTime);
						MoveTime++;
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	public void StartObserve()
	{
		this.LeftObTime = TotalObTime;
		State = OnStateListener.OBSERVING;
		if( refreshTime == null)
		{
			refreshTime = new RefreshTime();
			refreshTime.start();
		}
		StopOb = false;
		
	}
	
	public void StartGaming()
	{
		this.LeftObTime = 0;
		State = OnStateListener.GAMING;
		MoveTime = 0;
		if( refreshTime == null)
		{
			refreshTime = new RefreshTime();
			refreshTime.start();
		}
		StopOb = true;
		
	}

	@Override
	public void OnStateChanged(int StateMode) {
		// TODO Auto-generated method stub
		
		if(stateListener == null)
		{
			return;
		}
		if( StateMode == OnStateListener.FINISH)
		{
			if( State != OnStateListener.LOSE)
			{
				State = OnStateListener.WIN;
				this.stateListener.OnStateChanged(State);
			}
			else
			{
				
			}
			State = StateMode;
		}
		else if(StateMode == OnStateListener.LOSE)
		{
			State = StateMode;
		}
		
	}
	
	public void AutoSolve(String SolverName)
	{
		this.render.AutoSolve(SolverName);
	}
	
	public String GetCmdStrBefore()
	{
		return this.render.GetCmdStrBefore();
	}
		
	public String GetCmdStrAfter()
	{
		return this.render.GetCmdStrAfter();
	}

	@Override
	public void OnStateNotify(int StateMode) {
		// TODO Auto-generated method stub
		if( this.stateListener != null)
		{
			stateListener.OnStateNotify(StateMode);
		}
	}
	
	
	@Override
	public void onPause()
	{
		super.onPause();
		threadState = THREADPAUSE;
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		threadState = THREADRUNNING;
	}
}
