package flex.android.magiccube.view;

import flex.android.magiccube.interfaces.OnStateListener;
import flex.android.magiccube.interfaces.OnStepListener;
import android.content.Context;
import android.util.AttributeSet;

public class ViewAutoMode extends BasicGameView implements OnStateListener{
	private OnStateListener stateListener = null;
	//private RefreshTime refreshTime;
	public ViewAutoMode(Context context, AttributeSet attr) {
		super(context, attr);
		// TODO Auto-generated constructor stub
	}
	
	public void setOnStateListener(OnStateListener stateListener){
		this.stateListener = stateListener;
	}

	@Override
	public void OnStateChanged(int StateMode) {
		// TODO Auto-generated method stub
		
	}
	
	public void SetStepListener(OnStepListener stepListener)
	{
		this.stepListener = stepListener;
	}

	@Override
	public void OnStateNotify(int StateMode) {
		// TODO Auto-generated method stub
		
	}

}
