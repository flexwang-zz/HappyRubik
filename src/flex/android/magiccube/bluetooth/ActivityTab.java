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


import flex.android.magiccube.MagiccubePreference;
import flex.android.magiccube.R;
import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
 
public class ActivityTab extends TabActivity {
 
	public static ActivityTab _instance = null;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    _instance = this;
	    
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
	    setContentView(R.layout.bluetooth_tab_view);//����ʹ�������洴����xml�ļ���Tabҳ��Ĳ��֣�
        // Set result CANCELED incase the user backs out
        setResult(Activity.RESULT_OK);
        
        MagiccubePreference.SetPreference(MagiccubePreference.ServerOrClient, -1, this);
	    
        Resources res = getResources(); // Resource object to get Drawables
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabSpec spec;
	    Intent intent;  // Reusable Intent for each tab
 
	  //��һ��TAB
	    intent = new Intent(this,ActivityServer.class);//�½�һ��Intent����Tab1��ʾ������
	    spec = tabHost.newTabSpec("������Ϸ")//�½�һ�� Tab
	    .setIndicator("������Ϸ", res.getDrawable(R.drawable.server))//��������Լ�ͼ��
	    .setContent(intent);//������ʾ��intent������Ĳ���Ҳ������R.id.xxx
	    tabHost.addTab(spec);//��ӽ�tabHost
 
	    //�ڶ���TAB
	    intent = new Intent(this,ActivityClient.class);//�ڶ���Intent����Tab1��ʾ������
	    spec = tabHost.newTabSpec("�������")//�½�һ�� Tab
	    .setIndicator("�������", res.getDrawable(R.drawable.in))//��������Լ�ͼ��
	    .setContent(intent);//������ʾ��intent������Ĳ���Ҳ������R.id.xxx
	    tabHost.addTab(spec);//��ӽ�tabHost
 
	    tabHost.setCurrentTab(0);
	}
	
 
}
