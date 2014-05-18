package flex.android.magiccube.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.WindowManager;

public class BasicGameActivity extends Activity{
    protected int width;
    protected int height;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
       // WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        
       // width = wm.getDefaultDisplay().getWidth();
       // height = wm.getDefaultDisplay().getHeight();
    }
}
