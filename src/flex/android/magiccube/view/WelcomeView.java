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

package flex.android.magiccube.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.view.SurfaceView;
import android.widget.Button;

public class WelcomeView extends SurfaceView {
	private Activity MainActivity;
	private Context context;
	public WelcomeView(Context context, Activity main) {
		super(context);
		// TODO Auto-generated constructor stub
		this.MainActivity = main;
		this.context = context;
		//MagicCubeView_Normal glView = new MagicCubeView_Normal(context);
		//this.MainActivity.setContentView(glView);
	}
	
	public void OnDraw(Canvas canvas)
	{
		Button b1 = new Button(context);
		b1.setText("sss");
		
	}
	
}
