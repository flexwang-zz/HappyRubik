package flex.android.magiccube;


public class Face {
	//Face Public Data
	public float[] P1;		//left-bottom
	public float[] P2;		//right-bottom
	public float[] P3;		//right-top
	public float[] P4;		//left-top
	
	public int Subfaces[];	//subface color
	
	//face index
	public static final int FRONT = 0;
	public static final int BACK = 1;
	public static final int LEFT = 2;
	public static final int RIGHT = 3;
	public static final int TOP = 4;
	public static final int BOTTOM = 5;
	
	public static final int F = 0;
	public static final int B = 1;
	public static final int L = 2;
	public static final int R = 3;
	public static final int U = 4;
	public static final int D = 5;
	
	//sub face index
	/*
	 the naming rule:
	 	for the front back right left face, we rotate the face around the y-axis 
	 to let the face face us then we name it
	 	for the up and bottom face, we rotate the face around the x-axis
	 (the right is positive direction), about the rotate direction, we choose the shortest way
	 
	 */
	public static final int Y1 = 0;		//row 1 col 1	left-bottom
	public static final int Y2 = 1;		//row 1 col 2
	public static final int Y3 = 2;		//...
	public static final int E1 = 3;
	public static final int E2 = 4;
	public static final int E3 = 5;
	public static final int S1 = 6;
	public static final int S2 = 7;
	public static final int S3 = 8;
	
	//matrix
	private float[] pro_matrix = new float[16];
	private int [] view_matrix = new int[4];
	private float[] mod_matrix = new float[16];
	
	//index
	private int index;
	
	//Constructor
	public Face(int i)	//front back left right top bottom
	{
		float halfsize = Cube.CubeSize * 1.5f;
		
		P1 = new float[3];
		P2 = new float[3];
		P3 = new float[3];
		P4 = new float[3];
		
		if( i == FRONT)	//front
		{
			P1[0] = -halfsize;
			P1[1] = -halfsize;
			P1[2] = halfsize;
			
			P2[0] = halfsize;
			P2[1] = -halfsize;
			P2[2] = halfsize;
			
			P3[0] = halfsize;
			P3[1] = halfsize;
			P3[2] = halfsize;
			
			P4[0] = -halfsize;
			P4[1] = halfsize;
			P4[2] = halfsize;
			
		}
		else if (i == BACK)	//back
		{
			P1[0] = halfsize;
			P1[1] = -halfsize;
			P1[2] = -halfsize;
			
			P2[0] = -halfsize;
			P2[1] = -halfsize;
			P2[2] = -halfsize;
			
			P3[0] = -halfsize;
			P3[1] = halfsize;
			P3[2] = -halfsize;
			
			P4[0] = halfsize;
			P4[1] = halfsize;
			P4[2] = -halfsize;			
		}
		else if (i == LEFT)	//left
		{
			P1[0] = -halfsize;
			P1[1] = -halfsize;
			P1[2] = -halfsize;
			
			P2[0] = -halfsize;
			P2[1] = -halfsize;
			P2[2] = halfsize;
			
			P3[0] = -halfsize;
			P3[1] = halfsize;
			P3[2] = halfsize;
			
			P4[0] = -halfsize;
			P4[1] = halfsize;
			P4[2] = -halfsize;				
		}
		else if (i == RIGHT)	//right
		{
			P1[0] = halfsize;
			P1[1] = -halfsize;
			P1[2] = halfsize;
			
			P2[0] = halfsize;
			P2[1] = -halfsize;
			P2[2] = -halfsize;
			
			P3[0] = halfsize;
			P3[1] = halfsize;
			P3[2] = -halfsize;
			
			P4[0] = halfsize;
			P4[1] = halfsize;
			P4[2] = halfsize;					
		}
		else if (i == TOP)	//top
		{
			P1[0] = -halfsize;
			P1[1] = halfsize;
			P1[2] = halfsize;
			
			P2[0] = halfsize;
			P2[1] = halfsize;
			P2[2] = halfsize;
			
			P3[0] = halfsize;
			P3[1] = halfsize;
			P3[2] = -halfsize;
			
			P4[0] = -halfsize;
			P4[1] = halfsize;
			P4[2] = -halfsize;				
		}
		else if (i == BOTTOM)	//bottom
		{
			P1[0] = -halfsize;
			P1[1] = -halfsize;
			P1[2] = -halfsize;
			
			P2[0] = halfsize;
			P2[1] = -halfsize;
			P2[2] = -halfsize;
			
			P3[0] = halfsize;
			P3[1] = -halfsize;
			P3[2] = halfsize;
			
			P4[0] = -halfsize;
			P4[1] = -halfsize;
			P4[2] = halfsize;				
		}
		
		Subfaces = new int[9];
		
		for(int j=0; j<9; j++)
		{
			//Subfaces[j] = i*10+j;
			Subfaces[j] = i;
		}
	}
	
	public Face(float []p1, float []p2, float []p3, float []p4)
	{
		P1 = p1;
		P2 = p2;
		P3 = p3;
		P4 = p4;
	}
	
	public boolean IsSameColor()
	{
		for( int i=0; i<8; i++)
		{
			if(Subfaces[i] != Subfaces[i+1])
			{
				return false;
			}
		}
		
		return true;
	}
	
	public Face GetVerticalSubFace(int n)
	{
		float []p1 = new float [3];
		float []p2 = new float [3];
		float []p3 = new float [3];
		float []p4 = new float [3];
		
		if( n == 2)		//right
		{
			for(int i=0; i<3; i++)
			{
				p1[i] = (P2[i]*2.f+P1[i])/3.f;
				p2[i] = P2[i];
				p3[i] = P3[i];
				p4[i] = (P3[i]*2.f+P4[i])/3.f;
			}
		}
		else if( n==1 )		//middle
		{
			for(int i=0; i<3; i++)
			{
				p1[i] = (P1[i]*2.f+P2[i])/3.f;
				p2[i] = (P2[i]*2.f+P1[i])/3.f;
				p3[i] = (P4[i]+P3[i]*2.f)/3.f;
				p4[i] = (P3[i]+P4[i]*2.f)/3.f;
			}			
		}
		else if( n==0 )		//left
		{
			for(int i=0; i<3; i++)
			{
				p1[i] = P1[i];
				p2[i] = (P2[i]+P1[i]*2.f)/3.f;
				p3[i] = (P3[i]+P4[i]*2.f)/3.f;
				p4[i] = P4[i];
			}				
		}
		
		return new Face(p1, p2, p3, p4);	
	}
	
	public Face GetHorizonSubFace(int n)	
	{
		float []p1 = new float [3];
		float []p2 = new float [3];
		float []p3 = new float [3];
		float []p4 = new float [3];
		
		if( n == 2)		//bottom
		{
			for(int i=0; i<3; i++)
			{
				p1[i] = P1[i];
				p2[i] = P2[i];
				p3[i] = (P2[i]*2.f+P3[i])/3.f;
				p4[i] = (P1[i]*2.f+P4[i])/3.f;
			}
		}
		else if( n==1 )		//middle
		{
			for(int i=0; i<3; i++)
			{
				p1[i] = (P1[i]*2.f+P4[i])/3.f;
				p2[i] = (P2[i]*2.f+P3[i])/3.f;
				p3[i] = (P2[i]+P3[i]*2.f)/3.f;
				p4[i] = (P1[i]+P4[i]*2.f)/3.f;
			}			
		}
		else if( n==0 )		//top
		{
			for(int i=0; i<3; i++)
			{
				p1[i] = (P1[i]+P4[i]*2.f)/3.f;
				p2[i] = (P2[i]+P3[i]*2.f)/3.f;
				p3[i] = P3[i];
				p4[i] = P4[i];
			}				
		}
		
		return new Face(p1, p2, p3, p4);
	}
	
	public static String FaceToChar(int f)
	{
		switch(f)
		{
		case Face.F: return "F";
		case Face.D: return "D";
		case Face.R: return "R";
		case Face.L: return "L";
		case Face.U: return "U";
		case Face.B: return "B";
		}
		return "";
	}
}
