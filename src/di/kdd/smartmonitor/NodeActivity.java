package di.kdd.smartmonitor;

import android.app.Activity;
import android.content.Intent;
import android.os.Looper;
import android.widget.Toast;
import di.kdd.smartmonitor.protocol.DistributedSystem;

public abstract class NodeActivity extends Activity implements IObserver, ISampler {

	protected AccelerationsSQLiteHelper accelerationsDb;
	protected DistributedSystem distributedSystem = DistributedSystem.getInstance();
	
	/* IObserver implementation */
	
	@Override
	public void update(String message) {
		if(Looper.myLooper() == Looper.getMainLooper() ) {
			Toast.makeText(this, message, Toast.LENGTH_LONG).show();
		}
		else {
			this.runOnUiThread(new ShowToastOnMainThread(this, message));
		}
	}
		
	/* ISampler implementation */
	
	@Override
	public void startSamplingService() {
		if(distributedSystem.isSampling() == false) {
			startService(new Intent(this, SamplingService.class));		
		}
	}	
	
	@Override
	public void stopSamplingService() {
		if(distributedSystem.isSampling()) {		
			stopService(new Intent(this, SamplingService.class));		
		}
	}
	

	

}
