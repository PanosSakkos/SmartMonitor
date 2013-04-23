package di.kdd.smartmonitor;

import di.kdd.smart.R;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;

public class MainActivity extends Activity {	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		//accelerationsDb.flushAccelerationBuffers();
	}

	/* Button handlers */
	
	public void connectAsPeer(View _) {

		Intent intent = new Intent(this, PeerActivity.class);
        startActivity(intent);  
		
	}
	
	public void connectAsMasterJ(View _) {

		Intent intent = new Intent(this, MasterActivity.class);
        startActivity(intent);  
		
	}
}
