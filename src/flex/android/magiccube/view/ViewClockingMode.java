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

package flex.android.magiccube.view;

import flex.android.magiccube.MagiccubePreference;
import flex.android.magiccube.interfaces.OnStateListener;
import flex.android.magiccube.interfaces.OnStepListener;
import flex.android.magiccube.interfaces.OnTimerListener;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;


public class ViewClockingMode extends BasicGameView implements OnStateListener{
	private OnStateListener stateListener = null;
	private RefreshTime refreshTime = null;
	private boolean threadstop = false;
	private int TotalObTime;
	private int LeftObTime;
	private boolean StopOb;
	private int State;
	private int nMessUp;
	
	
	public ViewClockingMode(Context context, AttributeSet attr) {
		super(context, attr);
		// TODO Auto-generated constructor stub
		nMessUp = 30;
    	switch(MagiccubePreference.GetPreference(MagiccubePreference.Difficulty, 
    		context))
		{
	    	case 0: nMessUp = 4; break;
	    	case 50: nMessUp = 10; break;
	    	case 100: nMessUp = 30; break;
		}
		if( render != null)
			SetCmdStrBefore(MessUp(nMessUp));
		this.render.SetOnStateListener(this);
		this.render.SetOnStepListnener(this);
		nStep = 0;
		State = OnStateListener.NONE;
		threadState = THREADRUNNING;
	}
	
	@Override
	public void Reset()
	{
		super.Reset();
		nMessUp = 30;
    	switch(MagiccubePreference.GetPreference(MagiccubePreference.Difficulty, 
        		context))
    	{
    		case 0: nMessUp = 4; break;
    		case 50: nMessUp = 10; break;
    	    case 100: nMessUp = 30; break;
    	}
		SetCmdStrBefore(MessUp(nMessUp));
		State = OnStateListener.NONE;
		threadstop = false;
	}
	
	public void setOnStateListener(OnStateListener stateListener){
		this.stateListener = stateListener;
	}
	
	public void SetCmdStrBefore(String CmdStrBefore)
	{
		this.render.SetCmdStrBefore(CmdStrBefore);
	}
	
	public String GetCmdStrBefore()
	{
		return this.render.GetCmdStrBefore();
	}
		
	public String GetCmdStrAfter()
	{
		return this.render.GetCmdStrAfter();
	}
	
	public void SetOnTimerListener(OnTimerListener TimerListener)
	{
		this.timerListener = TimerListener;
	}
	
	public void SetStepListener(OnStepListener stepListener)
	{
		this.stepListener = stepListener;
	}
	

	
	public void SetTotalObTime(int TotalTime)
	{
		this.TotalObTime = TotalTime;	
	}
	
	public void setMode(int stateMode) {
		this.stateListener.OnStateChanged(stateMode);
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
	
	public void StopObserve()
	{
		//refreshTime.suspend();
		//refreshTime = null;
	}
	
	public void onStop()
	{
		threadstop = true;
		threadState = THREADSTOP;
	}
	
	private class RefreshTime extends Thread {
		public void run() {
			Log.e("e", "clocking");
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
					while(State == OnStateListener.GAMING)
					//while(true)
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

	@Override
	public void OnStateChanged(int StateMode) {
		// TODO Auto-generated method stub
		if( StateMode != OnStateListener.LOADED)
		{
			State = StateMode;
		}
		this.stateListener.OnStateChanged(StateMode);
	}
	
	public void AutoSolve(String SolverName)
	{
		this.render.AutoSolve(SolverName);
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

	@Override
	public void OnStateNotify(int StateMode) {
		// TODO Auto-generated method stub
		this.stateListener.OnStateNotify(StateMode);
	}
}

