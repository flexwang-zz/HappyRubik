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

package flex.android.magiccube.activity;

import flex.android.magiccube.MagiccubePreference;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.format.Time;
import android.util.Log;

public abstract class ActivitySensorListener extends Activity implements SensorEventListener {
    //Sensor manager
    private SensorManager mSensorManager = null;
    
    //Vibrator
    private Vibrator mVibrator = null;
    
    public final static int STATE_NOT_VIBRATE = 0;
    public final static int STATE_VIBRATE = 1;
    protected int vibrate_state = STATE_NOT_VIBRATE;
    private boolean CanVibrate = false;
    
    //anti-jagging
    private long LastVibrateTime;
    private long LastValidVibrateTime;
    private float[] LastVibrateValues = null;
    private long lasttest = 0;
    
    private float[] LastMaxValues = null;
    private long[] LastMaxTime = null;
    
    private float[] LastMinValues = null;
    private long[] LastMinTime = null;
   
    private boolean IsVibrating = false;
    private final long TimeLap = 700;
    private final long TimeLap2 = 700;
    private float MinVibration;
    private float MinVibration2;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mVibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        MinVibration = MagiccubePreference.GetPreference(MagiccubePreference.MinVibration, this);
        MinVibration2 = 2*MinVibration+1;
    }
    
    public void setMinVibration(float MinVibration)
    {
    	this.MinVibration = MinVibration;
    	MinVibration2 = 2*MinVibration+1;
    }

    public void onAccuracyChanged(Sensor arg0, int arg1) {
        // TODO Auto-generated method stub
    }
    
    public void onSensorChanged(SensorEvent sensorevent) {
    	float[] values = sensorevent.values;
    	lasttest = System.currentTimeMillis();
    	ResetVibrateState();

    	if( !CanVibrate)
    	{
    		return;
    	}
    	
    	if( this.IsVibrating && System.currentTimeMillis()-LastValidVibrateTime <= this.TimeLap)
    	{
    		return;
    	}
    	
    	IsVibrating = false;
    	
        int sensorType = sensorevent.sensor.getType();
        
        if (sensorType == Sensor.TYPE_ACCELEROMETER)
        {
            if (Math.abs(values[0]) > MinVibration || Math.abs(values[1]) > MinVibration || Math.abs(values[2]) > MinVibration){
            	
            	long CurMilli = System.currentTimeMillis();
            	
            	if( LastMinValues == null)
            	{
            		LastMinValues = new float[3];
            		this.LastMinTime = new long[3];
            		for(int i=0; i<3; i++)
            		{
            			LastMinValues[i] = sensorevent.values[i];
            			LastMinTime[i] = CurMilli;
            		}
            	}
            	
            	if( LastMaxValues == null)
            	{
            		LastMaxValues = new float[3];
            		this.LastMaxTime = new long[3];
            		for(int i=0; i<3; i++)
            		{
            			LastMaxValues[i] = sensorevent.values[i];
            			LastMaxTime[i] = CurMilli;
            		}
            	}
            	
            	//Log.e("values[0]-LastMinValues[0]", values[0]-LastMinValues[0]+"");
            	//Log.e("CurMilli-LastMinTime[0]", CurMilli-LastMinTime[0]+"");
            	if((CurMilli-LastMinTime[0])<TimeLap2 && (values[0]-LastMinValues[0])>MinVibration2
            			|| (CurMilli-LastMinTime[1])<TimeLap2 && (values[1]-LastMinValues[1])>MinVibration2
            			|| (CurMilli-LastMinTime[2])<TimeLap2 && (values[2]-LastMinValues[2])>MinVibration2
            			|| (CurMilli-LastMaxTime[0])<TimeLap2 && (LastMaxValues[0]-values[0])>MinVibration2
            			|| (CurMilli-LastMaxTime[1])<TimeLap2 && (LastMaxValues[1]-values[1])>MinVibration2
            			|| (CurMilli-LastMaxTime[2])<TimeLap2 && (LastMaxValues[2]-values[2])>MinVibration2
            			)
            	{
            		this.IsVibrating =true;
            		LastValidVibrateTime = CurMilli;
	            	LastMinValues = null;
	            	LastMaxValues = null;
	            	mVibrator.vibrate(100); 
	            	LastVibrateValues = null;
	                SetVibrateState();
	                onShake();
            	}
            	else
            	{
            		for(int i=0; i<3; i++)
            		{
            			if( values[i] < -MinVibration)
            			{
            				LastMinValues[i] = values[i];
            				LastMinTime[i] = CurMilli;
            			}
            			if( values[i] > MinVibration)
            			{
            				LastMaxValues[i] = values[i];
            				LastMaxTime[i] = CurMilli;
            			}
            		}
            	}
            	
            	
            }
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        mSensorManager.unregisterListener(this);
        super.onStop();        
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        mSensorManager.unregisterListener(this);
        super.onPause();
    }    
    
    public void ResetVibrateState()
    {
    	this.vibrate_state = STATE_NOT_VIBRATE;
    }
    
    public void SetVibrateState()
    {
    	this.vibrate_state = STATE_VIBRATE;
    }
    
/*    private int GetCurSeconds()
    {
    	Time t=new Time("GMT+8"); 	//Time Zone���ϡ�

    	t.setToNow(); // ȡ��ϵͳʱ�䡣
    	int hour = t.hour;
    	int minute = t.minute;
    	int second = t.second;
    	
    	return second + minute*60 + hour*3600;
    }*/
    
    public void SetCanVibrate(boolean vibratable)
    {
    	CanVibrate = vibratable;
    }
    
    public abstract void onShake();
}
