package flex.android.magiccube.mainmenu;

import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import flex.android.magiccube.Command;
import flex.android.magiccube.Cube;
import flex.android.magiccube.Face;
import flex.android.magiccube.MagicCubeRender;
import flex.android.magiccube.Magiccube;
import flex.android.magiccube.MagiccubePreference;
import flex.android.magiccube.MusicPlayThread;
import flex.android.magiccube.R;
import flex.android.magiccube.interfaces.OnStateListener;

public class MainMenuRender extends MagicCubeRender{
	private Magiccube magiccube2;
	private Magiccube magiccube3;
	
    private int LeftPadding1;
    private int TopPadding1;
    private int radius1;
    private float x1, y1, z1;
    private float scale1;
    private float halfsize1;
    
    private int LeftPadding2;
    private int TopPadding2;
    private int radius2;
    private float x2, y2, z2;
    private float scale2;
    private float halfsize2;
    
    private int LeftPadding3;
    private int TopPadding3;
    private int radius3;
    private float x3, y3, z3;
    private float scale3;
    private float halfsize3;
    
    private int nMessUp = 8;
    
    protected Vector<Command>  commands2;
    protected Vector<Command>  commands3;
    
    private float ratio2 = 0.37f;
    //private float ratio2 = 1.0f;
    
    private boolean CanRotate = true;
    
    private int move2steps = 0;
	
	public MainMenuRender(Context context, int w, int h) {
		super(context, w, h);
		// TODO Auto-generated constructor stub
		magiccube2 = new Magiccube();
		magiccube3 = new Magiccube();
		volume = MagiccubePreference.GetPreference(MagiccubePreference.BgVolume, context);
	}
	
	public void SetCanRotate(boolean CanRotate)
	{
		this.CanRotate = CanRotate;
	}
	
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig arg1) {
		super.onSurfaceCreated(gl, arg1);
		
	      //Initial the cubes
		magiccube2.LoadTexture(gl, context);
		magiccube3.LoadTexture(gl, context);
		
		commands = Command.CmdStrsToCmd(magiccube.MessUp(nMessUp));
		commands2 = Command.CmdStrsToCmd(magiccube2.MessUp(nMessUp));
		commands3 = Command.CmdStrsToCmd(magiccube3.MessUp(nMessUp));
	      
	}
	
	public void SetLayout(int LeftPadding1, int TopPadding1, int radius1,
			int LeftPadding2, int TopPadding2, int radius2,
			int LeftPadding3, int TopPadding3, int radius3)
	{
		this.LeftPadding1 = LeftPadding1 + radius1;
		this.TopPadding1 = TopPadding1 + radius1;
		this.radius1 = radius1;
		halfsize1 = ratio2*radius1;
		
		this.LeftPadding2 = LeftPadding2 + radius2;
		this.TopPadding2 = TopPadding2 + radius2;
		this.radius2 = radius2;
		halfsize2 = ratio2*radius2;
		
		this.LeftPadding3 = LeftPadding3 + radius3;
		this.TopPadding3 = TopPadding3 + radius3;
		this.radius3 = radius3;
		halfsize3 = ratio2*radius3;
		
	}
	
	@Override
	public void onSurfaceChanged(GL10 gl, int w, int h) {
		super.onSurfaceChanged(gl, w, h);
		
		float halfcube = Cube.CubeSize*1.5f;
		float halfsqrt3 = 0.8660254037844386f;
		float halfdiagonal = 3.f*halfsqrt3;
		
/*		z1 = -((ratio*w*0.5f)/halfsize1*(eyez-halfcube)+halfcube-eyez);
		float w1 = Cube.CubeSize*3.f/ratio/(eyez-halfcube)*(eyez-z1-halfcube);
		float h1 = w1/(float)w*(float)h;
		x1 = ((float)LeftPadding1/(float)w-0.5f)*w1;
		y1 = 0.5f*h1 - ((float)TopPadding1/(float)h)*h1;
		
		
		z2 = -((ratio*w*0.5f)/halfsize2*(eyez-halfcube)+halfcube-eyez);
		float w2 = Cube.CubeSize*3.f/ratio/(eyez-halfcube)*(eyez-z2-halfcube);
		float h2 = w2/(float)w*(float)h;
		x2 = 0.5f*w2 - (1.f- (float)LeftPadding2/(float)w)*w2;
		y2 = 0.5f*h2 - ((float)TopPadding2/(float)h)*h2;
		
		z3 = -((ratio*w*0.5f)/halfsize3*(eyez-halfcube)+halfcube-eyez);
		float w3 = Cube.CubeSize*3.f/ratio/(eyez-halfcube)*(eyez-z3-halfcube);
		float h3 = w3/(float)w*(float)h;
		x3 = 0.5f*w3 -(1.f- (float)LeftPadding3/(float)w)*w3;
		y3 = 0.5f*h3 - ((float)TopPadding3/(float)h)*h3;*/
		
		z1 = 0.f;
		float w1 = Cube.CubeSize*3.f/ratio/(eyez-halfcube)*(eyez-z1);
		float h1 = w1/(float)w*(float)h;
		x1 = 0.5f*w1 - (1.f- (float)LeftPadding1/(float)w)*w1;
		y1 = 0.5f*h1 - ((float)TopPadding1/(float)h)*h1;
		scale1 = halfsize1/((float)w*ratio/(Cube.CubeSize*3.f)/(eyez-halfcube)*(eyez-z1)*halfdiagonal);
		//scale1 = (float)halfsize1/((float)w*ratio/Cube.CubeSize*3.f*Cube.CubeSize*1.5f);
		
		
		z2 = 0.f;
		float w2 = Cube.CubeSize*3.f/ratio/(eyez-halfcube)*(eyez-z2);
		float h2 = w2/(float)w*(float)h;
		x2 = 0.5f*w2 - (1.f- (float)LeftPadding2/(float)w)*w2;
		y2 = 0.5f*h2 - ((float)TopPadding2/(float)h)*h2;
		scale2 = halfsize2/((float)w*ratio/(Cube.CubeSize*3.f)/(eyez-halfcube)*(eyez-z2)*halfdiagonal);
		
		z3 = 0.f;
		float w3 = Cube.CubeSize*3.f/ratio/(eyez-halfcube)*(eyez-z3);
		float h3 = w3/(float)w*(float)h;
		x3 = 0.5f*w3 -(1.f- (float)LeftPadding3/(float)w)*w3;
		y3 = 0.5f*h3 - ((float)TopPadding3/(float)h)*h3;
		scale3 = halfsize3/((float)w*ratio/(Cube.CubeSize*3.f)/(eyez-halfcube)*(eyez-z3)*halfdiagonal);
	
	    if( this.stateListener != null )
	    {
	    	  stateListener.OnStateChanged(OnStateListener.MAINMENULOADED);
	    }
	}
	
	@Override
	protected void DrawScene(GL10 gl)
	{
	    this.DrawBg(gl);

	    if(HasCommand)
	    {
	    	Command command1 = commands.lastElement();
	    	Command command2 = commands2.lastElement();
	    	Command command3 = commands3.lastElement();
	    	
	    	int nsteps1 = command1.N*this.nStep;	//rotate nsteps
	    	int nsteps2 = command1.N*this.nStep;	//rotate nsteps
	    	int nsteps3 = command1.N*this.nStep;	//rotate nsteps
	    	
	    	if(CommandLoop%nStep == 0 && CommandLoop != nsteps1)
	    	{
	   
				MusicPlayThread musicPlayThread = new MusicPlayThread(context, R.raw.move2, volume);
				musicPlayThread.start();
	
	    	}
	    	
	    	if(command1.Type == Command.ROTATE_ROW)
	    	{
	    		magiccube.RotateRow(command1.RowID, (1-command1.Direction), 90.f/nStep,CommandLoop%nStep==nStep-1);
	    	}
	    	else if(command1.Type == Command.ROTATE_COL)
	    	{
	    		magiccube.RotateCol(command1.RowID, (1-command1.Direction), 90.f/nStep,CommandLoop%nStep==nStep-1);
	    	}
	    	else
	    	{
	    		magiccube.RotateFace(command1.RowID, (1-command1.Direction), 90.f/nStep,CommandLoop%nStep==nStep-1);
	    	}
	    	
	    	if(command2.Type == Command.ROTATE_ROW)
	    	{
	    		magiccube2.RotateRow(command2.RowID, (1-command2.Direction), 90.f/nStep,CommandLoop%nStep==nStep-1);
	    	}
	    	else if(command2.Type == Command.ROTATE_COL)
	    	{
	    		magiccube2.RotateCol(command2.RowID, (1-command2.Direction), 90.f/nStep,CommandLoop%nStep==nStep-1);
	    	}
	    	else
	    	{
	    		magiccube2.RotateFace(command2.RowID, (1-command2.Direction), 90.f/nStep,CommandLoop%nStep==nStep-1);
	    	}
	    	
	    	if(command3.Type == Command.ROTATE_ROW)
	    	{
	    		magiccube3.RotateRow(command3.RowID, (1-command3.Direction), 90.f/nStep,CommandLoop%nStep==nStep-1);
	    	}
	    	else if(command3.Type == Command.ROTATE_COL)
	    	{
	    		magiccube3.RotateCol(command3.RowID, (1-command3.Direction), 90.f/nStep,CommandLoop%nStep==nStep-1);
	    	}
	    	else
	    	{
	    		magiccube3.RotateFace(command3.RowID, (1-command3.Direction), 90.f/nStep,CommandLoop%nStep==nStep-1);
	    	}
	    	
	    	CommandLoop++;
	    	if(CommandLoop==nsteps1)
	    	{
	    		CommandLoop = 0;
	    		commands.removeElementAt(commands.size()-1);
	    		commands2.removeElementAt(commands2.size()-1);
	    		commands3.removeElementAt(commands3.size()-1);
	    		move2steps++;
	    		if( commands.size() <= 0 ||  move2steps == 2)
	    		{
	    			move2steps = 0;
	    			HasCommand = false;
	    		}
	    	}
	    }
	    
	    DrawCubes(gl);
	}
	
	@Override
	protected void DrawCubes(GL10 gl)
	{
		if(CanRotate)
		{
			rx += 0.5f;
			ry += 0.5f;
		}
	    
	    gl.glPushMatrix();
	    gl.glTranslatef(x1, y1, z1);    
	    gl.glScalef(scale1, scale1, scale1);
	    gl.glRotatef(rx, 1, 0, 0);	//rotate
	    gl.glRotatef(ry, 0, 1, 0);
	    if(this.HasCommand)
	    {
	    	magiccube.Draw(gl);
	    }
	    else
	    {
	    	magiccube.DrawSimple(gl);
	    }
	    
	  	gl.glPopMatrix();
	  	
	    gl.glPushMatrix();
	    gl.glTranslatef(x2, y2, z2);
	    gl.glScalef(scale2, scale2, scale2);
	    
	    gl.glRotatef(-(rx+90), 1, 0, 0);	//rotate
	    gl.glRotatef(ry, 0, 1, 0);
	    if(this.HasCommand)
	    {
	    	magiccube2.Draw(gl);
	    }
	    else
	    {
	    	magiccube2.DrawSimple(gl);
	    }
	    
	  	gl.glPopMatrix();
	  	
	    gl.glPushMatrix();
	    gl.glTranslatef(x3, y3, z3);
	    gl.glScalef(scale3, scale3, scale3);
	    
	    gl.glPushMatrix();
	    gl.glRotatef(rx, 1, 0, 0);	//rotate
	    gl.glRotatef(-(ry+90), 0, 1, 0);
	    if(this.HasCommand)
	    {
	    	magiccube3.Draw(gl);
	    }
	    else
	    {
	    	magiccube3.DrawSimple(gl);
	    }
	    gl.glPopMatrix();
	    
	  	gl.glPopMatrix();
	}
	
	public void MainMenuMove2Steps()
	{
		if( this.IsComplete())
		{
			if( move2steps > 0)
			{
				commands = Command.CmdStrsToCmd(magiccube.MessUp(nMessUp));
				commands2 = Command.CmdStrsToCmd(magiccube2.MessUp(nMessUp));
				commands3 = Command.CmdStrsToCmd(magiccube3.MessUp(nMessUp));
			}
			else
			{
				move2steps++;
				return;
			}
		}
		move2steps = 0;
		this.HasCommand = true;
	}
}
