package di.kdd.smartmonitor.framework;

import java.util.ArrayList;
import java.util.List;

import di.kdd.smart.R;
import di.kdd.smartmonitor.ModalActivity;
import di.kdd.smartmonitor.NodeActivity;
import di.kdd.smartmonitor.PlotActivity;
import di.kdd.smartmonitor.framework.Acceleration.AccelerationAxis;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.ListView;
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
		//Display modal frequencies
		if (message.contains("Got modal frequencies")){
			
			ListView Xlistview = (ListView) ((Activity)context).findViewById(R.id.XaxisList);
			ListView Ylistview = (ListView) ((Activity)context).findViewById(R.id.YaxisList);
			ListView Zlistview = (ListView) ((Activity)context).findViewById(R.id.ZaxisList);
			
			List<Float> Xfreqs = ((NodeActivity) context).getModalFrequencies(AccelerationAxis.X);
			List<Float> Yfreqs = ((NodeActivity) context).getModalFrequencies(AccelerationAxis.Y);
			List<Float> Zfreqs = ((NodeActivity) context).getModalFrequencies(AccelerationAxis.Z);
			
			String tempFreq;
			List<String> xFreqsList = new ArrayList<String>();
			for (Float freq : Xfreqs){
				if (freq.isNaN() || freq.isInfinite())
					continue;
				tempFreq = freq.toString().substring(0, freq.toString().indexOf('.')+3);
				xFreqsList.add(tempFreq +" Hz");
			}
			
			
			List<String> yFreqsList = new ArrayList<String>();
			//yFreqsList.add("null");
			
			for (Float freq : Yfreqs){
				if (freq.isNaN() || freq.isInfinite())
					continue;

				tempFreq = freq.toString().substring(0, freq.toString().indexOf('.')+3);
				yFreqsList.add(tempFreq +" Hz");
			}

			List<String> zFreqsList = new ArrayList<String>();
			for (Float freq : Zfreqs){
				if (freq.isNaN() || freq.isInfinite())
					continue;
				tempFreq = freq.toString().substring(0, freq.toString().indexOf('.')+3);
				zFreqsList.add(tempFreq+" Hz");
			}
			
			StableArrayAdapter Xadapter = new StableArrayAdapter(context,
			        android.R.layout.simple_list_item_1, xFreqsList);
			StableArrayAdapter Yadapter = new StableArrayAdapter(context,
			        android.R.layout.simple_list_item_1, yFreqsList);
			StableArrayAdapter Zadapter = new StableArrayAdapter(context,
			        android.R.layout.simple_list_item_1, zFreqsList);
			
			Xlistview.setAdapter(Xadapter);
			Ylistview.setAdapter(Yadapter); 
			Zlistview.setAdapter(Zadapter);
			
			Intent intent = new Intent(context, ModalActivity.class);
			context.startActivity(intent);
			
		}
		
		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
		
	}

}
