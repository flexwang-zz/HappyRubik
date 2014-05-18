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

package flex.android.magiccube.bluetooth;


public class BluetoothSignal {
	public final static String MessUpCmd1 = "0";
	public final static String MessUpCmd2 = "1";
	//public final static String MessUpNum = "1";
	public final static String ObserveTime = "2";
	public final static String Win = "3";
	public final static String StartObserve = "4";
	public final static String Command = "5";
	public final static String HandShaked = "6";
	public final static String EndSetUp = "a";
	public final static String ClientEndSetUp = "8";
	public final static String ReSetup = "9";
	public final static String Leave = "b";
	
	public static String GenerateMsg(String tag, String content)
	{
		return tag + " " + content;
	}
	
	public static String GenerateMsg(String tag)
	{
		return tag;
	}
	
	public static String GenerateMsg(String tag, int content)
	{
		return tag + " " + content;
	}
}
