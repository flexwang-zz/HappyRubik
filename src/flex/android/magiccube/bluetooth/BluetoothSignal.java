package flex.android.magiccube.bluetooth;

import android.util.Log;

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
/*	public final static String MessUp = "0";
	public final static String MessUp = "0";
	public final static String MessUp = "0";
	public final static String MessUp = "0";
	public final static String MessUp = "0";
	public final static String MessUp = "0";
	public final static String MessUp = "0";
	public final static String MessUp = "0";*/
	
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
