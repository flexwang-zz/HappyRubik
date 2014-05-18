package flex.android.magiccube.mainmenu;

public interface OnMenuClickListener {
	public static final int MODE_NORMAL = 0;
	public static final int MODE_CLOCKING = 1;
	public static final int MODE_BATTLE = 2;
	
	public void OnMenuClick(int MenuID);
}
