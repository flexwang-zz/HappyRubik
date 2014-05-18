package flex.android.magiccube;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import flex.android.magiccube.R;
import flex.android.magiccube.bluetooth.MessageSender;
import flex.android.magiccube.interfaces.OnStateListener;
import flex.android.magiccube.interfaces.OnStepListener;
import flex.android.magiccube.solver.MagicCubeSolver;
import flex.android.magiccube.solver.SolverFactory;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

public class MagicCubeRender implements GLSurfaceView.Renderer {
	
	protected Context context;
	private int width;
	private int height;
	
	//eye-coordinate
	private float eyex;
	private float eyey;
	protected float eyez;
	private float angle = 65.f;
	protected float ratio = 0.6f;
	protected float zfar = 25.5f;
	private float bgdist = 25.f;
	
	//background texture
	private int[] BgTextureID = new int[1];
	private Bitmap[] bitmap = new Bitmap[1];
	private FloatBuffer bgtexBuffer;     // Texture Coords Buffer for background
	private FloatBuffer bgvertexBuffer;  // Vertex Buffer for background
	
	protected final int nStep = 9;//nstep for one rotate
	
	protected int volume;
	
	private boolean solved = false;	//indicate if the cube has been solved
	
	//background position
	//...
	
	//rotation variable
	public float rx, ry;
	protected Vector<Command>  commands;
	private Vector<Command> commandsBack;	//backward command
	private Vector<Command> commandsForward;	//forward command
	private Vector<Command> commandsAuto;	//forward command
	
	protected int[] command;
	protected boolean HasCommand;
	protected int CommandLoop;
	private String CmdStrBefore = "";
	private String CmdStrAfter = "";
	
	public static final boolean ROTATE = true;
	public static final boolean MOVE = false;
	
	//matrix
	public float[] pro_matrix = new float[16];
	public int [] view_matrix = new int[4];
	public float[] mod_matrix = new float[16];
	
	//minimal valid move distance
	private float MinMovedist;
	
	//the cubes!
	protected Magiccube magiccube;
	private boolean DrawCube;
	
	private boolean Finished = false;
	private boolean Resetting = false;
	
	private boolean CanMoveForward = false;
	private boolean CanMoveBack = false;
	//sound
	//protected MediaPlayer mediaPlayer;
	//public static final int ID_SOUND_MOVE = 0;
	
	protected OnStateListener stateListener = null;
	private MessageSender messageSender = null;
	private OnStepListener stepListener = null;
	
	public MagicCubeRender(Context context, int w, int h) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.width = w;
		this.height = h;
		this.eyex = 0.f;
		this.eyey = 0.f;
		this.eyez = 20.f;
		
		this.rx = 22.f;
		this.ry = -34.f;
		
		this.HasCommand = false;
		this.commands = new Vector<Command>(1,1);
		this.commandsBack = new Vector<Command>(100, 10);
		this.commandsForward = new Vector<Command>(100, 10);
		this.commandsAuto = new Vector<Command>(40,5);
		//this.Command = new int[3];
		
		magiccube = new Magiccube();
	     	    
	    DrawCube = true;
	    
	    solved = false;
	    
	    volume = MagiccubePreference.GetPreference(MagiccubePreference.MoveVolume, context);
	   //SetCommands("L- R2 F- D+ L+ U2 L2 D2 R+ D2 L+ F- D+ L2 D2 R2 B+ L+ U2 R2 U2 F2 R+ D2 U+");
	   //SetCommands("F- U+ F- D- L- D- F- U- L2 D-");
	    // SetCommands("U+");
	   // mediaPlayer = MediaPlayer.create(context, R.raw.move2);
	}
	
	public void SetDrawCube(boolean DrawCube)
	{
		this.DrawCube = DrawCube;
	}
	
	private void LoadBgTexture(GL10 gl)
	{
		//Load texture bitmap
        bitmap[0] = BitmapFactory.decodeStream(
	               context.getResources().openRawResource(R.drawable.mainbg2));
	    gl.glGenTextures(1, BgTextureID, 0); // Generate texture-ID array for 6 IDs
		
	    //Set texture uv
	    float[] texCoords = {
	         0.0f, 1.0f,  // A. left-bottom
	         1.0f, 1.0f,  // B. right-bottom
	         0.0f, 0.0f,  // C. left-top
	         1.0f, 0.0f   // D. right-top
	    };
	    ByteBuffer tbb = ByteBuffer.allocateDirect(texCoords.length * 4 );
	    tbb.order(ByteOrder.nativeOrder());
	    bgtexBuffer = tbb.asFloatBuffer();
        bgtexBuffer.put(texCoords);
        bgtexBuffer.position(0);   // Rewind
        
	    // Generate OpenGL texture images
	    gl.glBindTexture(GL10.GL_TEXTURE_2D, BgTextureID[0]);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_LINEAR);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR); 
		// Build Texture from loaded bitmap for the currently-bind texture ID
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap[0], 0);
		bitmap[0].recycle();
	}
	
	protected void DrawBg(GL10 gl)
	{   
	    gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
	    gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	    
	    gl.glVertexPointer(3, GL10.GL_FLOAT, 0, bgvertexBuffer);
	    gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, bgtexBuffer);
	    gl.glEnable(GL10.GL_TEXTURE_2D);  // Enable texture (NEW)	    
	    gl.glPushMatrix();
	    
	  	gl.glBindTexture(GL10.GL_TEXTURE_2D, this.BgTextureID[0]);
	  	gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
	    
	    gl.glPopMatrix();
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	}
	
	@Override
	public void onDrawFrame(GL10 gl) {
		// TODO Auto-generated method stub
	    // Clear color and depth buffers using clear-value set earlier
	    gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
	     
	    // You OpenGL|ES rendering code here
	    gl.glLoadIdentity();
	    GLU.gluLookAt(gl, eyex, eyey, eyez, 0.f, 0.f, 0.f, 0.f, 1.f, 0.f);	
	    Matrix.setIdentityM(mod_matrix, 0);
	    Matrix.setLookAtM(mod_matrix, 0, eyex, eyey, eyez, 0.f, 0.f, 0.f, 0.f, 1.f, 0.f);
	    DrawScene(gl);

	}

	private void SetBackgroundPosition()
	{

		float halfheight = (float)Math.tan(angle/2.0/180.0*Math.PI)*bgdist*1.f;
		float halfwidth = halfheight/this.height*this.width;
		
        float[] vertices = {
	            -halfwidth, -halfheight, eyez-bgdist,  // 0. left-bottom-front
	            halfwidth, -halfheight, eyez-bgdist,  // 1. right-bottom-front
	            -halfwidth,  halfheight,eyez-bgdist,  // 2. left-top-front
	            halfwidth, halfheight, eyez-bgdist,  // 3. right-top-front
        };
	    
        ByteBuffer vbb = ByteBuffer.allocateDirect(12 * 4);
	    vbb.order(ByteOrder.nativeOrder());
	    bgvertexBuffer = vbb.asFloatBuffer();
        bgvertexBuffer.put(vertices);  // Populate
	    bgvertexBuffer.position(0);    // Rewind
	}
	
	@Override
	public void onSurfaceChanged(GL10 gl, int w, int h) {
		// TODO Auto-generated method stub
		//reset the width and the height;
		//Log.e("screen", w+" "+h);
		
	    if (h == 0) h = 1;   // To prevent divide by zero
		
		this.width = w;
		this.height = h;
		
	    float aspect = (float)w / h;
	   
	    // Set the viewport (display area) to cover the entire window
	    gl.glViewport(0, 0, w, h);
	    
		this.view_matrix[0] = 0;
		this.view_matrix[1] = 0;
		this.view_matrix[2] = w;
		this.view_matrix[3] = h;
		
	    // Setup perspective projection, with aspect ratio matches viewport
	    gl.glMatrixMode(GL10.GL_PROJECTION); // Select projection matrix
	    gl.glLoadIdentity();                 // Reset projection matrix
	    // Use perspective projection
	    angle = 60;
	    
	    //calculate the angle to adjust the screen resolution

	    //float idealwidth = Cube.CubeSize*3.f/(Math.abs(this.eyez-Cube.CubeSize*1.5f))*zfar/ratio;
	    float idealwidth = Cube.CubeSize*3.f/(Math.abs(this.eyez-Cube.CubeSize*1.5f))*zfar/ratio;
	    float idealheight = idealwidth/(float)w*(float)h;
	    
	    angle = (float)(Math.atan2(idealheight/2.f, Math.abs(zfar))*2.f*180.f/Math.PI);
	    
	    SetBackgroundPosition();
	 
	    GLU.gluPerspective(gl, angle, aspect, 0.1f, zfar);
	    
	    MinMovedist = w*ratio/3.f*0.15f;
	    
	    float r = (float)w/(float)h;
	    float size = (float)(0.1*Math.tan(angle/2.0/180.0*Math.PI));
	    
	    Matrix.setIdentityM(pro_matrix, 0);
	    Matrix.frustumM(pro_matrix, 0, -size*r, size*r, -size , size, 0.1f, zfar);
	   
	    gl.glMatrixMode(GL10.GL_MODELVIEW);  // Select model-view matrix
	    gl.glLoadIdentity();                 // Reset
	  
/*	    // You OpenGL|ES display re-sizing code here
	    gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE); 
	    //float []lightparam = {0.6f, 0.2f, 0.2f, 0.6f, 0.2f, 0.2f, 0, 0, 10};
	    float []lightparam = {1f, 1f, 1f, 3f, 1f, 1f, 0, 0, 5};
	    
	    gl.glEnable(GL10.GL_LIGHTING);  
	    // ���û�����  
	    gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, lightparam, 0);  
	    
	    // ���������  
	    gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, lightparam, 3);  
	    
	    // ���þ��淴��
	    gl.glLightfv(GL10.GL_LIGHT0,  GL10.GL_SPECULAR, lightparam, 3); 
	    
	    // ���ù�Դλ��  
	    gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightparam, 6);  
	    
	    // ������Դ  
	    gl.glEnable(GL10.GL_LIGHT0);  
	    */
	    
	    // �������  
	    //gl.glEnable(GL10.GL_BLEND); 
	    if( this.stateListener != null )
	    {
	    	  stateListener.OnStateChanged(OnStateListener.LOADED);
	    }
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig arg1) {
		// TODO Auto-generated method stub
	      gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);  // Set color's clear-value to black
	      gl.glClearDepthf(1.0f);            // Set depth's clear-value to farthest
	      gl.glEnable(GL10.GL_DEPTH_TEST);   // Enables depth-buffer for hidden surface removal
	      gl.glDepthFunc(GL10.GL_LEQUAL);    // The type of depth testing to do
	      gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);  // nice perspective view
	      gl.glShadeModel(GL10.GL_SMOOTH);   // Enable smooth shading of color
	      gl.glDisable(GL10.GL_DITHER);      // Disable dithering for better performance
	  
	      // You OpenGL|ES initialization code here
	      
	      //Initial the cubes
	      magiccube.LoadTexture(gl, context);
	      
	      //this.MessUp(50);

	      LoadBgTexture(gl);
	}
	
	
	private void LogFace()
	{
/*	      for(int i=0; i<6; i++)
	      {
	    	  String s = "";
	    	  for( int j=0; j<9; j++)
	    	  {
	    		  s += faces[i].Subfaces[j] + " ";
	    	  }
	    	  Log.e(""+i, s);
	      }*/
	      		
	}
	
	public void SetMessageSender(MessageSender messageSender)
	{
		this.messageSender = messageSender;
	}
	
	protected void DrawScene(GL10 gl)
	{
	    this.DrawBg(gl);

	    if( !DrawCube)
	    {
	    	return;
	    }
	    
	    if(Resetting)
	    {
	    	//Resetting = false;
	    	//reset();
	    }
	    if(HasCommand)
	    {
	    	Command command = commands.firstElement();
	    	if( CommandLoop == 0 && messageSender != null)
	    	{
	    		messageSender.SendMessage(command.toString());
	    	}
	    	int nsteps = command.N*this.nStep;	//rotate nsteps
	    	if(CommandLoop%nStep == 0 && CommandLoop != nsteps)
	    	{
	   
				MusicPlayThread musicPlayThread = new MusicPlayThread(context, R.raw.move2, volume);
				musicPlayThread.start();
	
	    	}
	    	if(command.Type == Command.ROTATE_ROW)
	    	{
	    		magiccube.RotateRow(command.RowID, command.Direction, 90.f/nStep,CommandLoop%nStep==nStep-1);
	    	}
	    	else if(command.Type == Command.ROTATE_COL)
	    	{
	    		magiccube.RotateCol(command.RowID, command.Direction, 90.f/nStep,CommandLoop%nStep==nStep-1);
	    	}
	    	else
	    	{
	    		magiccube.RotateFace(command.RowID, command.Direction, 90.f/nStep,CommandLoop%nStep==nStep-1);
	    	}
	    	CommandLoop++;
	    	if(CommandLoop==nsteps)
	    	{
	    		CommandLoop = 0;
	    		if(commands.size() > 1)
	    		{
	    			//Log.e("full", "full"+commands.size());
	    		}
	    		this.CmdStrAfter += command.toString() + " ";
	    		commands.removeElementAt(0);
	    		//Log.e("e", commands.size()+"");
	    		if( commands.size() <= 0)
	    		{
	    			HasCommand = false;
	    		}
	    		if( stepListener != null)
	    		{
	    			stepListener.AddStep();
	    		}
	    		//this.ReportFaces();
	    		//Log.e("state", this.GetState());
	    		if(Finished && !this.IsComplete())
	    		{
	    			Finished = false;
	    			if(this.stateListener != null)
	    			{
	    				stateListener.OnStateNotify(OnStateListener.CANAUTOSOLVE);
	    			}
	    		}
	    	}
	    	
	    	if(this.stateListener != null && this.IsComplete())
	    	{
	    		if( !Finished )
	    		{
	    			Finished = true;
	    			stateListener.OnStateChanged(OnStateListener.FINISH);
	    			stateListener.OnStateNotify(OnStateListener.CANNOTAUTOSOLVE);
	    		}
	    	}
	    }
	    
	    DrawCubes(gl);
	}
	
	protected void DrawCubes(GL10 gl)
	{
	    gl.glPushMatrix();
	    gl.glRotatef(rx, 1, 0, 0);	//rotate
	    gl.glRotatef(ry, 0, 1, 0);
	    
	   // Log.e("rxry", rx + " " + ry);
	    if(this.HasCommand)
	    {
	    	magiccube.Draw(gl);
	    }
	    else
	    {
	    	magiccube.DrawSimple(gl);
	    }
	  	gl.glPopMatrix();
	}
	
	public void SetOnStateListener(OnStateListener stateListener)
	{
		this.stateListener = stateListener;
	}
	
	public void SetOnStepListnener(OnStepListener stepListener)
	{
		this.stepListener = stepListener;
	}
	
	private void ReportFaces()
	{
/*	      for(int i=0; i<6; i++)
	      {
	    	  String s = "";
	    	  for( int j=0; j<9; j++)
	    	  {
	    		  s += faces[i].Subfaces[j] + " ";
	    	  }
	    	  //Log.e(""+i, s);
	      }*/
	}
	
	public boolean GetPos(float[] point, int[] Pos)
	{
		//deal with the touch-point is out of the cube
		
		if( true)
		{
			
			//return false;
		}
		if( rx > 0 && rx < 90.f)
		{
			
		}
		return ROTATE;
	}

	public String SetCommand(Command command)
	{
		HasCommand = true;
		this.commands.add(command);
		this.commandsForward.clear();
		this.commandsBack.add(command.Reverse());
		
		if(this.stateListener != null)
		{
			stateListener.OnStateNotify(OnStateListener.CANMOVEBACK);
			stateListener.OnStateNotify(OnStateListener.CANNOTMOVEFORWARD);
		}
		
		return command.CmdToCmdStr();
	}
	
	public void SetForwardCommand(String CmdStr)
	{
		//Log.e("cmdstr", CmdStr);
		this.commandsForward = Reverse(Command.CmdStrsToCmd(CmdStr));
	}
	
	public String SetCommand(int ColOrRowOrFace, int ID, int Direction)
	{
		solved = false;
		return SetCommand(new Command(ColOrRowOrFace, ID, Direction));
	}
	
	public boolean IsSolved()
	{
		return solved;
	}
	
	public String MoveBack()
	{
		if( this.commandsBack.size() <= 0)
		{
			return null;
		}
		
		Command command = commandsBack.lastElement();
		HasCommand = true;
		this.commands.add(command);
		this.commandsBack.remove(this.commandsBack.size()-1);
		
		if(this.commandsBack.size() <= 0 && stateListener != null)
		{
			stateListener.OnStateNotify(OnStateListener.CANNOTMOVEBACK);
		}
		
		
		
		if( solved)
		{
			this.commandsAuto.add(command.Reverse());
		}
		else
		{
			this.commandsForward.add(command.Reverse());
			if(stateListener != null)
			{
				stateListener.OnStateNotify(OnStateListener.CANMOVEFORWARD);
			}
		}
		return command.CmdToCmdStr();
	}
	
	public String MoveForward()
	{
		if( this.commandsForward.size() <= 0)
		{
			return null;
		}
		
		Command command = commandsForward.lastElement();
		HasCommand = true;
		this.commands.add(command);
		this.commandsBack.add(command.Reverse());
		this.commandsForward.remove(commandsForward.size()-1);
		
		if( this.commandsForward.size() <= 0 && stateListener != null)
		{
			this.stateListener.OnStateNotify(OnStateListener.CANNOTMOVEFORWARD);
		}
		if( this.stateListener != null)
		{
			this.stateListener.OnStateNotify(OnStateListener.CANMOVEBACK);
		}
		
		return command.CmdToCmdStr();
	}
	
	public int MoveForward2()
	{
		if( this.commandsForward.size() <= 0)
		{
			return 0;
		}
		
		int n = commandsForward.size();
		
		HasCommand = true;
		this.commands.addAll(Reverse(commandsForward));
		for( int i=commandsForward.size()-1; i>=0; i--)
		{
			this.commandsBack.add(commandsForward.get(i).Reverse());
		}
		this.commandsForward.clear();
		
		if( this.commandsForward.size() <= 0 && stateListener != null)
		{
			this.stateListener.OnStateNotify(OnStateListener.CANNOTMOVEFORWARD);
		}
		if( this.stateListener != null)
		{
			this.stateListener.OnStateNotify(OnStateListener.CANMOVEBACK);
		}
		
		return n;
	}

	public int IsInCubeArea(float []Win)
	{
		int[] HitedFaceIndice = new int[6];
		int HitNumber = 0;
		
		for(int i=0; i<6; i++)
		{
			if(IsInQuad3D(magiccube.faces[i], Win))
			{
				HitedFaceIndice[HitNumber] = i;
				HitNumber++;
			}
		}
		
		if( HitNumber <=0)
		{
			return -1;
		}
		else
		{
			if( HitNumber == 1)
			{
				return HitedFaceIndice[0];
			}
			else			//if more than one hitted, then choose the max z-value face as the hitted one
			{
				float maxzvalue = -1000.f;
				int maxzindex = -1;
				
				for( int i = 0; i< HitNumber; i++)
				{
					float thisz = this.GetCenterZ(magiccube.faces[HitedFaceIndice[i]]);
					if( thisz > maxzvalue)
					{
						maxzvalue = thisz;
						maxzindex = HitedFaceIndice[i];
					}
				}
				return maxzindex;
			}
		}
		
	}
	
	private float GetFaceArea(int faceindex)
	{
		float []P1 = new float[2];
		float []P2 = new float[2];
		float []P3 = new float[2];
		float []P4 = new float[2];
		
		this.Project(magiccube.faces[faceindex].P1, P1);
		this.Project(magiccube.faces[faceindex].P2, P2);
		this.Project(magiccube.faces[faceindex].P3, P3);
		this.Project(magiccube.faces[faceindex].P4, P4);
		
		float area = 0.f;
		
		area += 0.5f*GetLength2D(P1, P4)*GetLength2D(P1, P2)*Math.sin(GetAngle(P1, P4, P2));
		area += 0.5f*GetLength2D(P3, P4)*GetLength2D(P3, P2)*Math.sin(GetAngle(P3, P4, P2));
		
		return area;
	}
	
	private float GetLength2D(float []P1, float []P2)
	{
		float dx = P1[0]-P2[0];
		float dy = P1[1]-P2[1];
		
		return (float)Math.sqrt(dx*dx + dy*dy);
	}
	
	private boolean IsInQuad3D(Face f, float []Win)
	{

		return IsInQuad3D(f.P1, f.P2, f.P3, f.P4, Win);
	}
	
	private boolean IsInQuad3D(float []Point1, float []Point2, float []Point3, float Point4[], float []Win)
	{
		float[] Win1 = new float[2];
		float[] Win2 = new float[2];
		float[] Win3 = new float[2];
		float[] Win4 = new float[2];
		
		Project(Point1, Win1);
		Project(Point2, Win2);
		Project(Point3, Win3);
		Project(Point4, Win4);
		
/*		Log.e("P1", Win1[0] + " " + Win1[1]);
		Log.e("P2", Win2[0] + " " + Win2[1]);
		Log.e("P3", Win3[0] + " " + Win3[1]);
		Log.e("P4", Win4[0] + " " + Win4[1]);*/
		
		float []WinXY = new float[2];
		WinXY[0] = Win[0];
		WinXY[1] = this.view_matrix[3] - Win[1];
		
		//Log.e("WinXY", WinXY[0] + " " + WinXY[1]);
		
		return IsInQuad2D(Win1, Win2, Win3, Win4, WinXY);
	}
	
	private boolean IsInQuad2D(float []Point1, float []Point2, float []Point3, float Point4[], float []Win)
	{
		float angle = 0.f;
		final float ZERO = 0.0001f;
		
		angle += GetAngle(Win, Point1, Point2);
		//Log.e("angle" , angle + " ");
		angle += GetAngle(Win, Point2, Point3);
		//Log.e("angle" , angle + " ");
		angle += GetAngle(Win, Point3, Point4);
		//Log.e("angle" , angle + " ");
		angle += GetAngle(Win, Point4, Point1);
		//Log.e("angle" , angle + " ");
		
		if( Math.abs(angle-Math.PI*2.f) <= ZERO )
		{
			return true;
		}
		return false;
	}
	
	public String CalcCommand(float [] From, float [] To, int faceindex)
	{
		float [] from = new float[2];
		float [] to = new float[2];
		
		float angle, angleVertical, angleHorizon;
		
		from[0] = From[0];
		from[1] = this.view_matrix[3] - From[1];
		to[0] = To[0];
		to[1] = this.view_matrix[3] - To[1];
		
		angle = GetAngle(from, to);//(float)Math.atan((to[1]-from[1])/(to[0]-from[0]))/(float)Math.PI*180.f;

		//calc horizon angle
		float ObjFrom[] = new float[3];
		float ObjTo[] = new float[3];
		float WinFrom[] = new float[2];
		float WinTo[] = new float[2];
		
		for(int i=0; i<3; i++)
		{
			ObjFrom[i] = (magiccube.faces[faceindex].P1[i] + magiccube.faces[faceindex].P4[i])/2.f;
			ObjTo[i] = (magiccube.faces[faceindex].P2[i] + magiccube.faces[faceindex].P3[i])/2.f;
		}
		
		//Log.e("obj", ObjFrom[0]+" "+ObjFrom[1]+" "+ObjFrom[2]);
		this.Project(ObjFrom, WinFrom);
		this.Project(ObjTo, WinTo);
		
		angleHorizon = GetAngle(WinFrom, WinTo);//(float)Math.atan((WinTo[1]-WinFrom[1])/(WinTo[0]-WinFrom[0]))/(float)Math.PI*180.f;
		
		//calc vertical angle
		for(int i=0; i<3; i++)
		{
			ObjFrom[i] = (magiccube.faces[faceindex].P1[i] + magiccube.faces[faceindex].P2[i])/2.f;
			ObjTo[i] = (magiccube.faces[faceindex].P3[i] + magiccube.faces[faceindex].P4[i])/2.f;
		}
		
		this.Project(ObjFrom, WinFrom);
		this.Project(ObjTo, WinTo);
		
		angleVertical = GetAngle(WinFrom, WinTo);//(float)Math.atan((WinTo[1]-WinFrom[1])/(WinTo[0]-WinFrom[0]))/(float)Math.PI*180.f;
		
		//Log.e("angle", angle +" " + angleHorizon + " " + angleVertical);
		
		float dangle = DeltaAngle(angleHorizon, angleVertical);
		float threshold = Math.min(dangle/2.f, 90.f-dangle/2.f)*0.75f;	//this...........
		

		
		if( DeltaAngle(angle, angleHorizon) < threshold || DeltaAngle(angle, (angleHorizon+180.f)%360.f) < threshold)		//the direction
		{
			if(this.IsInQuad3D(magiccube.faces[faceindex].GetHorizonSubFace(0), From))
			{
				if( faceindex == Face.FRONT)
				{
					if(DeltaAngle(angle, angleHorizon) < threshold)
					{
						return this.SetCommand(Command.ROTATE_ROW, 0, Cube.ClockWise);
					}
					else
					{
						return this.SetCommand(Command.ROTATE_ROW, 0, Cube.CounterClockWise);
					}
				}
				else if(faceindex == Face.BACK)
				{
					if(DeltaAngle(angle, angleHorizon) < threshold)
					{
						return this.SetCommand(Command.ROTATE_ROW, 0, Cube.ClockWise);
					}
					else
					{
						return this.SetCommand(Command.ROTATE_ROW, 0, Cube.CounterClockWise);
					}
				}
				else if(faceindex == Face.LEFT)
				{
					if(DeltaAngle(angle, angleHorizon) < threshold)
					{
						return this.SetCommand(Command.ROTATE_ROW, 0, Cube.ClockWise);
					}
					else
					{
						return this.SetCommand(Command.ROTATE_ROW, 0, Cube.CounterClockWise);
					}
				}
				else if(faceindex == Face.RIGHT)
				{
					if(DeltaAngle(angle, angleHorizon) < threshold)
					{
						return this.SetCommand(Command.ROTATE_ROW, 0, Cube.ClockWise);
					}
					else
					{
						return this.SetCommand(Command.ROTATE_ROW, 0, Cube.CounterClockWise);
					}
				}
				else if(faceindex == Face.TOP)
				{
					if(DeltaAngle(angle, angleHorizon) < threshold)
					{
						return this.SetCommand(Command.ROTATE_FACE, 2, Cube.CounterClockWise);
					}
					else
					{
						return this.SetCommand(Command.ROTATE_FACE, 2, Cube.ClockWise);
					}
				}
				else if(faceindex == Face.BOTTOM)
				{
					if(DeltaAngle(angle, angleHorizon) < threshold)
					{
						return this.SetCommand(Command.ROTATE_FACE, 0, Cube.ClockWise);
					}
					else
					{
						return this.SetCommand(Command.ROTATE_FACE, 0, Cube.CounterClockWise);
					}
				}
			}
			else if(this.IsInQuad3D(magiccube.faces[faceindex].GetHorizonSubFace(1), From))
			{
				if( faceindex == Face.FRONT)
				{
					if(DeltaAngle(angle, angleHorizon) < threshold)
					{
						return this.SetCommand(Command.ROTATE_ROW, 1, Cube.ClockWise);
					}
					else
					{
						return this.SetCommand(Command.ROTATE_ROW, 1, Cube.CounterClockWise);
					}
				}
				else if( faceindex == Face.BACK)
				{
					if(DeltaAngle(angle, angleHorizon) < threshold)
					{
						return this.SetCommand(Command.ROTATE_ROW, 1, Cube.ClockWise);
					}
					else
					{
						return this.SetCommand(Command.ROTATE_ROW, 1, Cube.CounterClockWise);
					}
				}
				else if(faceindex == Face.LEFT)
				{
					if(DeltaAngle(angle, angleHorizon) < threshold)
					{
						return this.SetCommand(Command.ROTATE_ROW, 1, Cube.ClockWise);
					}
					else
					{
						return this.SetCommand(Command.ROTATE_ROW, 1, Cube.CounterClockWise);
					}
				}
				else if(faceindex == Face.RIGHT)
				{
					if(DeltaAngle(angle, angleHorizon) < threshold)
					{
						return this.SetCommand(Command.ROTATE_ROW, 1, Cube.ClockWise);
					}
					else
					{
						return this.SetCommand(Command.ROTATE_ROW, 1, Cube.CounterClockWise);
					}
				}
				else if(faceindex == Face.TOP)
				{
					if(DeltaAngle(angle, angleHorizon) < threshold)
					{
						return this.SetCommand(Command.ROTATE_FACE, 1, Cube.CounterClockWise);
					}
					else
					{
						return this.SetCommand(Command.ROTATE_FACE, 1, Cube.ClockWise);
					}
				}
				else if(faceindex == Face.BOTTOM)
				{
					if(DeltaAngle(angle, angleHorizon) < threshold)
					{
						return this.SetCommand(Command.ROTATE_FACE, 1, Cube.ClockWise);
					}
					else
					{
						return this.SetCommand(Command.ROTATE_FACE, 1, Cube.CounterClockWise);
					}
				}
			}
			else if(this.IsInQuad3D(magiccube.faces[faceindex].GetHorizonSubFace(2), From))
			{
				if( faceindex == Face.FRONT)
				{
					if(DeltaAngle(angle, angleHorizon) < threshold)
					{
						return this.SetCommand(Command.ROTATE_ROW, 2, Cube.ClockWise);
					}
					else
					{
						return this.SetCommand(Command.ROTATE_ROW, 2, Cube.CounterClockWise);
					}
				}
				else if( faceindex == Face.BACK)
				{
					if(DeltaAngle(angle, angleHorizon) < threshold)
					{
						return this.SetCommand(Command.ROTATE_ROW, 2, Cube.ClockWise);
					}
					else
					{
						return this.SetCommand(Command.ROTATE_ROW, 2, Cube.CounterClockWise);
					}
				}
				else if(faceindex == Face.LEFT)
				{
					if(DeltaAngle(angle, angleHorizon) < threshold)
					{
						return this.SetCommand(Command.ROTATE_ROW, 2, Cube.ClockWise);
					}
					else
					{
						return this.SetCommand(Command.ROTATE_ROW, 2, Cube.CounterClockWise);
					}
				}
				else if(faceindex == Face.RIGHT)
				{
					if(DeltaAngle(angle, angleHorizon) < threshold)
					{
						return this.SetCommand(Command.ROTATE_ROW, 2, Cube.ClockWise);
					}
					else
					{
						return this.SetCommand(Command.ROTATE_ROW, 2, Cube.CounterClockWise);
					}
				}
				else if(faceindex == Face.TOP)
				{
					if(DeltaAngle(angle, angleHorizon) < threshold)
					{
						return this.SetCommand(Command.ROTATE_FACE, 0, Cube.CounterClockWise);
					}
					else
					{
						return this.SetCommand(Command.ROTATE_FACE, 0, Cube.ClockWise);
					}
				}
				else if(faceindex == Face.BOTTOM)
				{
					if(DeltaAngle(angle, angleHorizon) < threshold)
					{
						return this.SetCommand(Command.ROTATE_FACE, 2, Cube.ClockWise);
					}
					else
					{
						return this.SetCommand(Command.ROTATE_FACE, 2, Cube.CounterClockWise);
					}
				}
			}
		}
		else if( DeltaAngle(angle, angleVertical) < threshold || DeltaAngle(angle, (angleVertical+180.f)%360.f) < threshold)
		{
			if(this.IsInQuad3D(magiccube.faces[faceindex].GetVerticalSubFace(0), From))
			{
				if( faceindex == Face.FRONT)
				{
					if(DeltaAngle(angle, angleVertical) < threshold)
					{
						return this.SetCommand(Command.ROTATE_COL, 0, Cube.CounterClockWise);
					}
					else
					{
						return this.SetCommand(Command.ROTATE_COL, 0, Cube.ClockWise);
					}
				}
				else if(faceindex == Face.BACK)
				{
					if(DeltaAngle(angle, angleVertical) < threshold)
					{
						return this.SetCommand(Command.ROTATE_COL, 2, Cube.ClockWise);
					}
					else
					{
						return this.SetCommand(Command.ROTATE_COL, 2, Cube.CounterClockWise);
					}					
				}
				else if(faceindex == Face.LEFT)
				{
					if(DeltaAngle(angle, angleVertical) < threshold)
					{
						return this.SetCommand(Command.ROTATE_FACE, 2, Cube.CounterClockWise);
					}
					else
					{
						return this.SetCommand(Command.ROTATE_FACE, 2, Cube.ClockWise);
					}					
				}
				else if(faceindex == Face.RIGHT)
				{
					if(DeltaAngle(angle, angleVertical) < threshold)
					{
						return this.SetCommand(Command.ROTATE_FACE, 0, Cube.ClockWise);
					}
					else
					{
						return this.SetCommand(Command.ROTATE_FACE, 0, Cube.CounterClockWise);
					}					
				}
				else if(faceindex == Face.TOP)
				{
					if(DeltaAngle(angle, angleVertical) < threshold)
					{
						return this.SetCommand(Command.ROTATE_COL, 0, Cube.CounterClockWise);
					}
					else
					{
						return this.SetCommand(Command.ROTATE_COL, 0, Cube.ClockWise);
					}					
				}
				else if(faceindex == Face.BOTTOM)
				{
					if(DeltaAngle(angle, angleVertical) < threshold)
					{
						return this.SetCommand(Command.ROTATE_COL, 0, Cube.CounterClockWise);
					}
					else
					{
						return this.SetCommand(Command.ROTATE_COL, 0, Cube.ClockWise);
					}					
				}
			}
			else if(this.IsInQuad3D(magiccube.faces[faceindex].GetVerticalSubFace(1), From))
			{
				if( faceindex == Face.FRONT)
				{
					if(DeltaAngle(angle, angleVertical) < threshold)
					{
						return this.SetCommand(Command.ROTATE_COL, 1, Cube.CounterClockWise);
					}
					else
					{
						return this.SetCommand(Command.ROTATE_COL, 1, Cube.ClockWise);
					}
				}
				else if(faceindex == Face.BACK)
				{
					if(DeltaAngle(angle, angleVertical) < threshold)
					{
						return this.SetCommand(Command.ROTATE_COL, 1, Cube.ClockWise);
					}
					else
					{
						return this.SetCommand(Command.ROTATE_COL, 1, Cube.CounterClockWise);
					}					
				}
				else if(faceindex == Face.LEFT)
				{
					if(DeltaAngle(angle, angleVertical) < threshold)
					{
						return this.SetCommand(Command.ROTATE_FACE, 1, Cube.CounterClockWise);
					}
					else
					{
						return this.SetCommand(Command.ROTATE_FACE, 1, Cube.ClockWise);
					}					
				}
				else if(faceindex == Face.RIGHT)
				{
					if(DeltaAngle(angle, angleVertical) < threshold)
					{
						return this.SetCommand(Command.ROTATE_FACE, 1, Cube.ClockWise);
					}
					else
					{
						return this.SetCommand(Command.ROTATE_FACE, 1, Cube.CounterClockWise);
					}					
				}
				else if(faceindex == Face.TOP)
				{
					if(DeltaAngle(angle, angleVertical) < threshold)
					{
						return this.SetCommand(Command.ROTATE_COL, 1, Cube.CounterClockWise);
					}
					else
					{
						return this.SetCommand(Command.ROTATE_COL, 1, Cube.ClockWise);
					}					
				}
				else if(faceindex == Face.BOTTOM)
				{
					if(DeltaAngle(angle, angleVertical) < threshold)
					{
						return this.SetCommand(Command.ROTATE_COL, 1, Cube.CounterClockWise);
					}
					else
					{
						return this.SetCommand(Command.ROTATE_COL, 1, Cube.ClockWise);
					}					
				}
			}
			else if(this.IsInQuad3D(magiccube.faces[faceindex].GetVerticalSubFace(2), From))
			{
				if( faceindex == Face.FRONT)
				{
					if(DeltaAngle(angle, angleVertical) < threshold)
					{
						return this.SetCommand(Command.ROTATE_COL, 2, Cube.CounterClockWise);
					}
					else
					{
						return this.SetCommand(Command.ROTATE_COL, 2, Cube.ClockWise);
					}
				}
				else if(faceindex == Face.BACK)
				{
					if(DeltaAngle(angle, angleVertical) < threshold)
					{
						return this.SetCommand(Command.ROTATE_COL, 0, Cube.ClockWise);
					}
					else
					{
						return this.SetCommand(Command.ROTATE_COL, 0, Cube.CounterClockWise);
					}					
				}
				else if(faceindex == Face.LEFT)
				{
					if(DeltaAngle(angle, angleVertical) < threshold)
					{
						return this.SetCommand(Command.ROTATE_FACE, 0, Cube.CounterClockWise);
					}
					else
					{
						return this.SetCommand(Command.ROTATE_FACE, 0, Cube.ClockWise);
					}					
				}
				else if(faceindex == Face.RIGHT)
				{
					if(DeltaAngle(angle, angleVertical) < threshold)
					{
						return this.SetCommand(Command.ROTATE_FACE, 2, Cube.ClockWise);
					}
					else
					{
						return this.SetCommand(Command.ROTATE_FACE, 2, Cube.CounterClockWise);
					}					
				}
				else if(faceindex == Face.TOP)
				{
					if(DeltaAngle(angle, angleVertical) < threshold)
					{
						return this.SetCommand(Command.ROTATE_COL, 2, Cube.CounterClockWise);
					}
					else
					{
						return this.SetCommand(Command.ROTATE_COL, 2, Cube.ClockWise);
					}					
				}
				else if(faceindex == Face.BOTTOM)
				{
					if(DeltaAngle(angle, angleVertical) < threshold)
					{
						return this.SetCommand(Command.ROTATE_COL, 2, Cube.CounterClockWise);
					}
					else
					{
						return this.SetCommand(Command.ROTATE_COL, 2, Cube.ClockWise);
					}					
				}
			}			
		}
		return null;
	}
	
	private float GetAngle(float[] From, float[] To)
	{
		float angle = (float)Math.atan((To[1]-From[1])/(To[0]-From[0]))/(float)Math.PI*180.f;
		float dy = To[1]-From[1];
		float dx = To[0]-From[0];
		
		if( dy >= 0.f && dx > 0.f)
		{
			angle = (float)Math.atan(dy/dx)/(float)Math.PI*180.f;
		}
		else if( dx == 0.f)
		{
			if( dy > 0.f)
			{
				angle = 90.f;
			}
			else
			{
				angle = 270.f;
			}
		}
		else if( dy >= 0.f && dx < 0.f)
		{
			angle = 180.f + (float)Math.atan(dy/dx)/(float)Math.PI*180.f;
		}
		else if( dy < 0.f && dx < 0.f)
		{
			angle = 180.f + (float)Math.atan(dy/dx)/(float)Math.PI*180.f;
		}
		else if(dy <0.f && dx > 0.f)
		{
			angle = 360.f + (float)Math.atan(dy/dx)/(float)Math.PI*180.f;
		}
		
		return angle;
	}
	
	private float DeltaAngle(float angle1, float angle2)
	{
		float a1 = Math.max(angle1, angle2);
		float a2 = Math.min(angle1, angle2);
		
		float delta = a1 - a2;
		
		if( delta >= 180.f )
		{
			delta = 360.f - delta;
		}
		
		return delta;
		
	}
	
	private float GetCenterZ(Face f)
	{
		float zvalue = 0.f;
		
		float [] matrix1 = new float[16];
		float [] matrix2 = new float[16];
		float [] matrix = new float[16];
		
		Matrix.setIdentityM(matrix1, 0);
		Matrix.setIdentityM(matrix2, 0);
		Matrix.setIdentityM(matrix, 0);
		
	    Matrix.rotateM(matrix1, 0, rx, 1, 0, 0);
	    Matrix.rotateM(matrix2, 0, ry, 0, 1, 0);
		Matrix.multiplyMM(matrix, 0, matrix1, 0, matrix2, 0);
	    
	    float xyz[]	= new float[3];
	    
	    
	    xyz[0] = f.P1[0];
	    xyz[1] = f.P1[1];
	    xyz[2] = f.P1[2];
	    
	    Transform(matrix, xyz);	
	    
	    zvalue += xyz[2];
	    
	    xyz[0] = f.P2[0];
	    xyz[1] = f.P2[1];
	    xyz[2] = f.P2[2];
	    
	    Transform(matrix, xyz);	
	    
	    zvalue += xyz[2];
	    
	    xyz[0] = f.P3[0];
	    xyz[1] = f.P3[1];
	    xyz[2] = f.P3[2];
	    
	    Transform(matrix, xyz);	
	    
	    zvalue += xyz[2];
	    
	    xyz[0] = f.P4[0];
	    xyz[1] = f.P4[1];
	    xyz[2] = f.P4[2];
	    
	    Transform(matrix, xyz);	
	    
	    zvalue += xyz[2];
	    
	    return zvalue/4.f;
	}
	
	private float GetAngle(float []Point0, float []Point1, float[]Point2)
	{
		float cos_value = (Point2[0]-Point0[0])*(Point1[0]-Point0[0]) + (Point1[1]-Point0[1])*(Point2[1]-Point0[1]);
		cos_value /= Math.sqrt((Point2[0]-Point0[0])*(Point2[0]-Point0[0]) + (Point2[1]-Point0[1])*(Point2[1]-Point0[1])) 
				* Math.sqrt((Point1[0]-Point0[0])*(Point1[0]-Point0[0]) + (Point1[1]-Point0[1])*(Point1[1]-Point0[1]));
		
		return (float)Math.acos(cos_value);
	}
	
	private void Project(float[] ObjXYZ, float [] WinXY)
	{
		float [] matrix1 = new float[16];
		float [] matrix2 = new float[16];
		float [] matrix = new float[16];
		
		Matrix.setIdentityM(matrix1, 0);
		Matrix.setIdentityM(matrix2, 0);
		Matrix.setIdentityM(matrix, 0);
		
	    Matrix.rotateM(matrix1, 0, rx, 1, 0, 0);
	    Matrix.rotateM(matrix2, 0, ry, 0, 1, 0);
		Matrix.multiplyMM(matrix, 0, matrix1, 0, matrix2, 0);
	    
	    float xyz[]	= new float[3];
	    xyz[0] = ObjXYZ[0];
	    xyz[1] = ObjXYZ[1];
	    xyz[2] = ObjXYZ[2];
	    
	    Transform(matrix, xyz);
	    //Log.e("xyz", xyz[0] + " " + xyz[1] + " " + xyz[2]);
	    float []Win = new float[3];
	    GLU.gluProject(xyz[0], xyz[1], xyz[2], mod_matrix, 0, pro_matrix, 0, view_matrix, 0, Win, 0);
	    WinXY[0] = Win[0];
	    WinXY[1] = Win[1];
	}
	
	private void Transform(float[]matrix, float[]Point)
	{
		float w = 1.f;
		
		float x, y, z, ww;
		
		x = matrix[0]*Point[0] + matrix[4]*Point[1] + matrix[8]*Point[2] + matrix[12]*w;
		y = matrix[1]*Point[0] + matrix[5]*Point[1] + matrix[9]*Point[2] + matrix[13]*w;
		z = matrix[2]*Point[0] + matrix[6]*Point[1] + matrix[10]*Point[2] + matrix[14]*w;
		ww = matrix[3]*Point[0] + matrix[7]*Point[1] + matrix[11]*Point[2] + matrix[15]*w;
		
		Point[0] = x/ww;
		Point[1] = y/ww;
		Point[2] = z/ww;

	}

	public boolean IsComplete()
	{
		boolean r = true;
		
		for( int i=0; i<6; i++)
		{
			r = r&&magiccube.faces[i].IsSameColor();
		}
		
		return magiccube.IsComplete();
	}
	
	public String MessUp(int nStep)
	{
		this.solved = false;
		
		this.Finished = false;
		if( this.stateListener != null)
		{
			stateListener.OnStateNotify(OnStateListener.CANAUTOSOLVE);
		}

		
		return magiccube.MessUp(nStep);
	}
	
	public void MessUp(String cmdstr)
	{
		this.solved = false;
		magiccube.MessUp(cmdstr);
		
		this.Finished = false;
		if( this.stateListener != null)
		{
			stateListener.OnStateNotify(OnStateListener.CANAUTOSOLVE);
		}

	}
	
	public boolean IsMoveValid(float[]From, float []To)
	{
		return this.GetLength2D(From, To) > this.MinMovedist;
	}
	

	
	public void SetCommands(String cmdStr)
	{
		
		this.commands = Command.CmdStrsToCmd(cmdStr);
		this.HasCommand = true;
	}
	
	public String SetCommand(String cmdStr)
	{
		return SetCommand(Command.CmdStrToCmd(cmdStr));
	}
	
	public void AutoSolve(String SolverName)
	{
		if( !solved)
		{
			MagicCubeSolver solver = SolverFactory.CreateSolver(SolverName);
			
			if(solver == null)
			{
				return;
			}
			//Log.e("state", this.GetState());
			String SolveCmd = solver.AutoSolve(magiccube.GetState());
			//Log.e("solve", SolveCmd);
			this.commands.clear();
			this.commandsBack.clear();
			this.commandsForward.clear();
			
			if( this.stateListener != null)
			{
				this.stateListener.OnStateNotify(OnStateListener.CANNOTMOVEBACK);
				this.stateListener.OnStateNotify(OnStateListener.CANNOTMOVEFORWARD);
			}
			
			this.commandsAuto = Reverse(Command.CmdStrsToCmd(SolveCmd));
			this.solved = true;
		}
		else if(commandsAuto.size() > 0)
		{
			Command command = commandsAuto.lastElement();
			HasCommand = true;
			this.commands.add(command);
			this.commandsBack.add(command.Reverse());
			this.commandsAuto.remove(commandsAuto.size()-1);
			
			if( this.stateListener != null)
			{
				this.stateListener.OnStateNotify(OnStateListener.CANMOVEBACK);
			}
		}
	}
	
	public void AutoSolve2(String SolverName)
	{

		if( !solved)
		{
			MagicCubeSolver solver = SolverFactory.CreateSolver(SolverName);
				
			if(solver == null)
			{
				return;
			}
				//Log.e("state", this.GetState());
			String SolveCmd = solver.AutoSolve(magiccube.GetState());
				//Log.e("solve", SolveCmd);
			this.commands.clear();
			this.commandsBack.clear();
			this.commandsForward.clear();
			this.commands = Command.CmdStrsToCmd(SolveCmd);
			for( int i=0; i<commands.size(); i++)
			{
				commandsBack.add(commands.get(i).Reverse());
			}
			this.HasCommand = true;
			this.solved = true;
		}
		else
		{
			commands.addAll(Reverse(commandsAuto));
			for( int i=commandsAuto.size()-1; i>=0; i--)
			{
				commandsBack.add(commandsAuto.get(i).Reverse());
			}
			commandsAuto.clear();
			HasCommand = true;
		}
	}
	
	public void AutoSolve3(String SolverName)
	{

		if( !solved)
		{
			MagicCubeSolver solver = SolverFactory.CreateSolver(SolverName);
				
			if(solver == null)
			{
				return;
			}
				//Log.e("state", this.GetState());
			String SolveCmd = solver.AutoSolve(magiccube.GetState());
				//Log.e("solve", SolveCmd);
			this.commands.clear();
			this.commandsBack.clear();
			this.commandsForward.clear();
			this.commands = Command.CmdStrsToCmd(SolveCmd);
			commands.remove(commands.size()-1);
			commands.remove(commands.size()-1);
			for( int i=0; i<commands.size(); i++)
			{
				commandsBack.add(commands.get(i).Reverse());
			}
			this.HasCommand = true;
			this.solved = true;
		}
		else
		{
			commands.addAll(Reverse(commandsAuto));
			for( int i=commandsAuto.size()-1; i>=0; i--)
			{
				commandsBack.add(commandsAuto.get(i).Reverse());
			}
			commandsAuto.clear();
			HasCommand = true;
		}
	}
	

	private Vector<Command> Reverse(Vector<Command> v)
	{
		Vector<Command> v2 = new Vector<Command>(v.size());
		for(int i=v.size()-1; i>=0; i--)
		{
			v2.add(v.elementAt(i));
		}
		return v2;
	}
	
	public void Reset()
	{
		Resetting = true;
		reset();
	}
	
	private void reset()
	{
		this.rx = 22.f;
		this.ry = -34.f;
		
		this.HasCommand = false;
		Finished = false;
	    
		this.CommandLoop = 0;
		this.commands.clear();
		this.commandsBack.clear();
		this.commandsForward.clear();
		this.CmdStrAfter = "";
		this.CmdStrBefore = "";
	    
		magiccube.Reset();
		
		if( this.stateListener != null)
		{
			this.stateListener.OnStateNotify(OnStateListener.CANNOTMOVEBACK);
			this.stateListener.OnStateNotify(OnStateListener.CANNOTMOVEFORWARD);
			this.stateListener.OnStateNotify(OnStateListener.CANNOTAUTOSOLVE);
		}
	}
	
	public String GetCmdStrBefore()
	{
		return this.CmdStrBefore;
	}
	
	public void SetCmdStrBefore(String CmdStrBefore)
	{
		this.CmdStrBefore = CmdStrBefore;
	}
	
	public void SetCmdStrAfter(String CmdStrAfter)
	{
		this.CmdStrAfter = CmdStrAfter;
	}
	
	public String GetCmdStrAfter()
	{
		return this.CmdStrAfter;
	}
	
	public void setVolume(int volume) {
		// TODO Auto-generated method stub
		this.volume = volume;
	}
	
}