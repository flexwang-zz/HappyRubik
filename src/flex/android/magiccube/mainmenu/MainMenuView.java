package flex.android.magiccube.mainmenu;

import flex.android.magiccube.MagicCubeRender;
import flex.android.magiccube.MagiccubePreference;
import flex.android.magiccube.interfaces.OnStateListener;
import flex.android.magiccube.view.MultisampleConfigChooser;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;

public class MainMenuView extends GLSurfaceView{
	
	private Context context;
	private MainMenuRender render;
	private int nMessUp = 20;
	
    private int LeftPadding1;
    private int TopPadding1;
    private int radius1;
    
    private int LeftPadding2;
    private int TopPadding2;
    private int radius2;
    
    private int LeftPadding3;
    private int TopPadding3;
    private int radius3;
	
	private OnMenuClickListener menuClickListener = null;
	private OnStateListener stateListener = null;
	
	private boolean kUseMultisampling = false;
    // If |kUseMultisampling| is set, this is what chose the multisampling config.
    private MultisampleConfigChooser mConfigChooser;
	
    protected float LastPos[] = new float[2];
    protected float Sensitivity;
    private boolean menuHitted;
    
	public MainMenuView(Context context, AttributeSet attrs) {
		super(context, attrs);

		this.context = context;
        setEGLConfigChooser(true);	//enable depth-buffer
        if (kUseMultisampling)
            setEGLConfigChooser(mConfigChooser = new MultisampleConfigChooser());
        
        Sensitivity = (float)MagiccubePreference.GetPreference(MagiccubePreference.Sensitivity, context)/100.f;
        
        render = new MainMenuRender(context, 540, 850);
      
        setRenderer(render);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        
	}

	public void SetSensitivity(float Sensitivity)
	{
		this.Sensitivity = Sensitivity;
	}
	
	public void SetSensitivity()
	{
		Sensitivity = (float)MagiccubePreference.GetPreference(MagiccubePreference.Sensitivity, context)/100.f;
	}
	
	public void SetLayout(int LeftPadding1, int TopPadding1, int radius1,
			int LeftPadding2, int TopPadding2, int radius2,
			int LeftPadding3, int TopPadding3, int radius3)
	{
		render.SetLayout(LeftPadding1, TopPadding1, radius1, LeftPadding2, TopPadding2, radius2, LeftPadding3, TopPadding3, radius3);
		this.LeftPadding1 = LeftPadding1 + radius1;
		this.TopPadding1 = TopPadding1 + radius1;
		this.radius1 = radius1;
		
		this.LeftPadding2 = LeftPadding2 + radius2;
		this.TopPadding2 = TopPadding2 + radius2;
		this.radius2 = radius2;
		
		this.LeftPadding3 = LeftPadding3 + radius3;
		this.TopPadding3 = TopPadding3 + radius3;
		this.radius3 = radius3;
	}
	public void SetOnMenuClickListener(OnMenuClickListener menuClickListener)
	{
		this.menuClickListener = menuClickListener;
	}
	
	public void setOnStateListener(OnStateListener stateListener){
		//this.stateListener = stateListener;
		this.render.SetOnStateListener(stateListener);
	}
	
	private void MessUp(int nMessUp) {
		// TODO Auto-generated method stub
		this.render.MessUp(nMessUp);
	}
	
	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		
		((ActivityMain)menuClickListener).ResetBackPressed();
		float x = event.getX();
		float y = event.getY();
		
		//Log.e("xy", x+" "+y);
		
		float Point[] = new float[2];
		Point[0] = x;
		Point[1] = y;
		
		int [] Pos = null;
		
		if(event.getAction()==MotionEvent.ACTION_DOWN)
		{
			int MenuId = GetMenuID(x, y);
			//Log.e("menuid", MenuId+"");
			if(MenuId>=0)
			{
				menuHitted = true;
				this.menuClickListener.OnMenuClick(MenuId);
				//Log.e("move", render.rx+ " " + render.ry);
			}
			else
			{
				menuHitted = false;
				render.SetCanRotate(false);
				//Log.e("rotate", render.rx+ " " + render.ry);
			}
			if( !menuHitted)
			{
				LastPos[0] = x;
				LastPos[1] = y;
			}
			else
			{
				LastPos[0] = x;
				LastPos[1] = y;
			}
			
		}
		else if(event.getAction()==MotionEvent.ACTION_MOVE)
		{
			if(menuHitted)
				return false;
        	float dx = x - LastPos[0];
        	float dy = y - LastPos[1];
			if(!menuHitted)
			{
	        	if(Math.abs(dy)<Math.abs(dx))
	        	{
	        		if(render.rx<90 && render.rx >-90)
	        			render.ry += dx * Sensitivity;
	        		else
	        			render.ry -= dx*Sensitivity;
	        		if(render.ry>180)
	        			render.ry -= 360;
	        		else if(render.ry<-180)
	        			render.ry += 360;
	        	}
	        	
	        	else
	        	{
	        		float tmp = render.rx;
	        		render.rx += dy * Sensitivity;
	        		
	        		if(render.rx>180)
	        			render.rx -= 360;
	        		else if(render.rx<-180)
	        			render.rx += 360;
	        	}
	        	//render.AdjustFace();
				LastPos[0] = x;
				LastPos[1] = y;
	    		//requestRender();		
			}
		}
		else if(event.getAction()==MotionEvent.ACTION_UP)
		{
			render.SetCanRotate(true);
		}
		return true;
	}
	
	private int GetMenuID(float x, float y)
	{
		if( GetDist(x, y, LeftPadding1, TopPadding1) < radius1)
		{
			return OnMenuClickListener.MODE_NORMAL;
		}
		else if( GetDist(x, y, LeftPadding2, TopPadding2) < radius2)
		{
			return OnMenuClickListener.MODE_CLOCKING;
		}
		else if( GetDist(x, y, LeftPadding3, TopPadding3) < radius3)
		{
			return OnMenuClickListener.MODE_BATTLE;
		}
		
		return -1;
	}
	
	private float GetDist(float x1, float y1, float x2, float y2)
	{
		float dist = (float)Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
		//Log.e("dist", dist+"");
		
		return dist;
	}
	
	public void Move2Steps()
	{
		render.MainMenuMove2Steps();
	}

	public void setVolume(int volume) {
		// TODO Auto-generated method stub
		render.setVolume(volume);
	}

}
