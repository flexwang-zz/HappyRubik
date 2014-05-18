package flex.android.magiccube.interfaces;

public interface OnStateListener{
	public static final int LOADING = 0;
	public static final int MAINMENULOADED = 65574;
	public static final int LOADED = -1;
	public static final int NONE = -1;
	public static final int BEFOREOBSERVE = 1;
	public static final int OBSERVING = 2;
	public static final int GAMING = 3;
	public static final int QUIT = 4;
	public static final int FINISH = 5;
	public static final int WIN = 6;
	public static final int LOSE = 7;
	
	public static final int CANMOVEFORWARD = 8;
	public static final int CANMOVEBACK = 9;
	public static final int CANNOTMOVEFORWARD = 10;
	public static final int CANNOTMOVEBACK = 11;
	public static final int CANAUTOSOLVE = 12;
	public static final int CANNOTAUTOSOLVE = 13;
	
	public static final int WAITING_FOR_CONNECTION = -2;
	public static final int LEAVING_ON_ERROR = -13;
	
	public void OnStateChanged(int StateMode);
	public void OnStateNotify(int StateMode);
}
