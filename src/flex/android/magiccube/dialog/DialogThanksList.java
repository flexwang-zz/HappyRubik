package flex.android.magiccube.dialog;

import flex.android.magiccube.R;
import android.app.Dialog;
import android.content.Context;

public class DialogThanksList extends Dialog{

	public DialogThanksList(Context context) {
		super(context, R.style.dialog);
		this.setContentView(R.layout.thankslist_dialog_view);
	}

}
