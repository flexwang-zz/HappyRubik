package flex.android.magiccube.view;

import flex.android.magiccube.interfaces.OnStateListener;
import android.content.Context;
import android.util.AttributeSet;

public class ViewNormalMode extends BasicGameView{
	public ViewNormalMode(Context context, AttributeSet attr) {
		super(context, attr);
		this.render.SetOnStepListnener(this);
	}
	
	public void setOnStateListener(OnStateListener stateListener){
		this.render.SetOnStateListener(stateListener);
	}

}
