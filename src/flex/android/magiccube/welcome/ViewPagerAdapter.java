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

package flex.android.magiccube.welcome;

import java.util.List;

import flex.android.magiccube.MagiccubePreference;
import flex.android.magiccube.R;
import flex.android.magiccube.mainmenu.ActivityMain;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;


public class ViewPagerAdapter extends PagerAdapter {

	// 鐣岄潰鍒楄〃
	private List<View> views;
	private Activity activity;
	private CheckBox checkbox = null;

	private static final String SHAREDPREFERENCES_NAME = "first_pref_flex_magiccube";

	public ViewPagerAdapter(List<View> views, Activity activity) {
		this.views = views;
		this.activity = activity;
		
		checkbox = (CheckBox)views.get(3).findViewById(R.id.checkBox_nevershow);
	}

	// 閿�瘉arg1浣嶇疆鐨勭晫闈�
	@Override
	public void destroyItem(View arg0, int arg1, Object arg2) {
		((ViewPager) arg0).removeView(views.get(arg1));
	}

	@Override
	public void finishUpdate(View arg0) {
	}

	// 鑾峰緱褰撳墠鐣岄潰鏁�
	@Override
	public int getCount() {
		if (views != null) {
			return views.size();
		}
		return 0;
	}

	// 鍒濆鍖朼rg1浣嶇疆鐨勭晫闈�
	@Override
	public Object instantiateItem(View arg0, int arg1) {
		((ViewPager) arg0).addView(views.get(arg1), 0);
		if (arg1 == views.size() - 1) {
			ImageView mStartWeiboImageButton = (ImageView) arg0
					.findViewById(R.id.iv_start_weibo);
			mStartWeiboImageButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// 璁剧疆宸茬粡寮曞
					setGuided();
					goHome();

				}

			});
		}
		return views.get(arg1);
	}

	private void goHome() {
		// 璺宠浆
		Intent intent = new Intent(activity, ActivityMain.class);
		activity.startActivity(intent);
		activity.finish();
	}

	/**
	 * 
	 * method desc锛氳缃凡缁忓紩瀵艰繃浜嗭紝涓嬫鍚姩涓嶇敤鍐嶆寮曞
	 */
	private void setGuided() {
		int value = checkbox.isChecked()?0:1;
		MagiccubePreference.SetPreference(MagiccubePreference.IsShowGuide, 0, activity);
	}

	// 鍒ゆ柇鏄惁鐢卞璞＄敓鎴愮晫闈�
	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return (arg0 == arg1);
	}

	@Override
	public void restoreState(Parcelable arg0, ClassLoader arg1) {
	}

	@Override
	public Parcelable saveState() {
		return null;
	}

	@Override
	public void startUpdate(View arg0) {
	}

}
