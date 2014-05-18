package flex.android.magiccube.view;

import flex.android.magiccube.interfaces.OnStateListener;
import android.content.Context;
import android.util.AttributeSet;

public class ViewReplayMode extends BasicGameView {
	private int State = OnStateListener.LOADING;
	private OnStateListener stateListener;
	
	public ViewReplayMode(Context context, AttributeSet attr) {
		super(context, attr);
		
		
	}
	
	public void MessUp(String CmdStr)
	{
		this.render.MessUp(CmdStr);
	}
	
	public void SetForwardCommand(String CmdStr)
	{
		this.render.SetForwardCommand(CmdStr);
	}
	
	@Override
	public void MoveBack()
	{
		String cmdstr = "";
		if((cmdstr = this.render.MoveBack()) != null)
		{
			if( this.stepListener != null)
			{
				this.stepListener.SetStep(--nStep);
			}
		}
	}
	
	@Override
	public void MoveForward()
	{
		String cmdstr = "";
		if((cmdstr = this.render.MoveForward()) != null)
		{
			if( this.stepListener != null)
			{
				this.stepListener.SetStep(++nStep);
			}
		}
	}
	
	@Override
	public void MoveForward2()
	{
		int n = this.render.MoveForward2();
		nStep += n;
		if( this.stepListener != null)
		{
			this.stepListener.SetStep(nStep);
		}
	}
	
	public void setOnStateListener(OnStateListener stateListener){
		this.render.SetOnStateListener(stateListener);
	}
}
