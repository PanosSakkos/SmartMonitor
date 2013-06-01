package di.kdd.smartmonitor.framework;

import android.content.Context;
import android.widget.Toast;

public class ShowToastOnMainThread implements Runnable {
	private Context context;
	private String message;
	
	public ShowToastOnMainThread(Context context, String message) {
		this.context = context;
		this.message = message;
	}
	
	@Override
	public void run() {
		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
	}

}
