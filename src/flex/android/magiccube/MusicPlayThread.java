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

import android.content.Context;
import android.media.MediaPlayer;

public class MusicPlayThread extends Thread{
	public static final int MAX_VOLUME = 100;
	
	public MusicPlayThread(Context context, int ID)
	{
		mediaPlayer = MediaPlayer.create(context, ID);
	}
	public MusicPlayThread(Context context, int ID, int Volume)
	{
		float volume;
		if( Volume > MAX_VOLUME)
		{
			Volume = MAX_VOLUME;
		}
		if(Volume < 0)
		{
			Volume = 0;
		}
		volume = (float) (1.f - (Math.log(Math.max(MAX_VOLUME - Volume, 1)) / Math.log(MAX_VOLUME)));
		mediaPlayer = MediaPlayer.create(context, ID);
		mediaPlayer.setVolume(volume, volume);
	}
	private MediaPlayer mediaPlayer;
	
	public static float GetLinearVolume(int Volume)
	{
		float volume;
		if( Volume > MAX_VOLUME)
		{
			Volume = MAX_VOLUME;
		}
		if(Volume < 0)
		{
			Volume = 0;
		}
		volume = (float) (1.f - (Math.log(Math.max(MAX_VOLUME - Volume, 1)) / Math.log(MAX_VOLUME)));
		
		return volume;
	}
	
	//@Override
	public void run()
	{
		mediaPlayer.start();
		try {
			Thread.sleep(1000);
			mediaPlayer.release();
			mediaPlayer = null;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
