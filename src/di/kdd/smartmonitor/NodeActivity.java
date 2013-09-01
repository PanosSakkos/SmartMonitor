package di.kdd.smartmonitor;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import org.apache.http.conn.util.InetAddressUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import di.kdd.smart.R;
import di.kdd.smartmonitor.framework.Acceleration.AccelerationAxis;
import di.kdd.smartmonitor.framework.AccelerationsSQLiteHelper;
import di.kdd.smartmonitor.framework.DistributedSystem;
import di.kdd.smartmonitor.framework.IObserver;
import di.kdd.smartmonitor.framework.ISampler;
import di.kdd.smartmonitor.framework.SamplingService;
import di.kdd.smartmonitor.framework.ShowToastOnMainThread;

public abstract class NodeActivity extends Activity implements IObserver, ISampler {

	protected AccelerationsSQLiteHelper accelerationsDb;
	protected DistributedSystem distributedSystem = DistributedSystem.getInstance(this);

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
	
	public String getLocalIpAddress() {
	    try {
	        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
	            NetworkInterface intf = en.nextElement();
	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
	                InetAddress inetAddress = enumIpAddr.nextElement();
	                if (!inetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(inetAddress.getHostAddress().toString())) {
	                    return inetAddress.getHostAddress().toString();
	                }
	            }
	        }
	    } catch (SocketException ex) {
//	        Log.e(LOG_TAG, ex.toString());
	    }
	    return null;
	}
	
	public List<Float> getModalFrequencies(AccelerationAxis axis){
		return distributedSystem.getModalFrequencies(axis);
	}

	/**
	 * Handler of the plot button.
	 * Calls the plotting activity 
	 * @param ignored
	 */	
	
	public void plot(View _) {
			Intent intent = new Intent(this, PlotActivity.class);
	        startActivity(intent);
	}	
	

}


