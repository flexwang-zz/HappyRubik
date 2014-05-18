package flex.android.magiccube.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.view.SurfaceView;
import android.widget.Button;

public class WelcomeView extends SurfaceView {
	private Activity MainActivity;
	private Context context;
	public WelcomeView(Context context, Activity main) {
		super(context);
		// TODO Auto-generated constructor stub
		this.MainActivity = main;
		this.context = context;
		//MagicCubeView_Normal glView = new MagicCubeView_Normal(context);
		//this.MainActivity.setContentView(glView);
	}
	
	public void OnDraw(Canvas canvas)
	{
		Button b1 = new Button(context);
		b1.setText("sss");
		
	}
	
}
