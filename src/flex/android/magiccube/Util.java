package flex.android.magiccube;

public class Util {
	public static String TimeFormat(int nS)
	{
		int h, m, s;
		String H, M, S;
		
		s = nS%60;
		if( s < 10)
		{
			S = "0"+s;
		}
		else
		{
			S = "" + s;
		}
		
		m = (nS-s)/60%60;
		if( m < 10)
		{
			M = "0"+m;
		}
		else
		{
			M = "" + m;
		}
		
		h = nS/3600%24;
		if(h < 10)
		{
			H = "0"+h;
		}
		else
		{
			H = "" + h;
		}
		
		return H+":"+M+":"+S;
	}
	
	
}
