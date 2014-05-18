package flex.android.magiccube.bluetooth;


import flex.android.magiccube.MagiccubePreference;
import flex.android.magiccube.R;
import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
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
	    setContentView(R.layout.bluetooth_tab_view);//这里使用了上面创建的xml文件（Tab页面的布局）
        // Set result CANCELED incase the user backs out
        setResult(Activity.RESULT_OK);
        
        MagiccubePreference.SetPreference(MagiccubePreference.ServerOrClient, -1, this);
	    
        Resources res = getResources(); // Resource object to get Drawables
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabSpec spec;
	    Intent intent;  // Reusable Intent for each tab
 
	  //第一个TAB
	    intent = new Intent(this,ActivityServer.class);//新建一个Intent用作Tab1显示的内容
	    spec = tabHost.newTabSpec("建立游戏")//新建一个 Tab
	    .setIndicator("建立游戏", res.getDrawable(R.drawable.server))//设置名称以及图标
	    .setContent(intent);//设置显示的intent，这里的参数也可以是R.id.xxx
	    tabHost.addTab(spec);//添加进tabHost
 
	    //第二个TAB
	    intent = new Intent(this,ActivityClient.class);//第二个Intent用作Tab1显示的内容
	    spec = tabHost.newTabSpec("加入别人")//新建一个 Tab
	    .setIndicator("加入别人", res.getDrawable(R.drawable.in))//设置名称以及图标
	    .setContent(intent);//设置显示的intent，这里的参数也可以是R.id.xxx
	    tabHost.addTab(spec);//添加进tabHost
 
	    tabHost.setCurrentTab(0);
	}
	
 
}