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
