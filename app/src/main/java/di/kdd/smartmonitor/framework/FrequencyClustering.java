package di.kdd.smartmonitor.framework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.util.Log;

public class FrequencyClustering {	
	private static Float []means;
	
	private static final String TAG = "frequency clustering";
	
	public static void clusterFrequencies(int k, List<Float> frequencies) {		
		ArrayList<ArrayList<Float>> clusters;

		//android.os.Debug.waitForDebugger();
		
		means = new Float[k];

		for(int i = 0; i < k; i++) {
			means[i] = frequencies.get(i);
		}		
		
		boolean meansChange = true;

		while(meansChange) {
			clusters = new ArrayList<ArrayList<Float>>();
			
			for(int i = 0; i < k; i++) {
				clusters.add(new ArrayList<Float>());
			}
			
			meansChange = false;
			
			/* Assignment step */
			
			for(Float frequency : frequencies) {

				/* Find the minimum mean distance */

				int cluster = 0;
				Float minimumDistance = Float.MAX_VALUE;
				
				for(int i = 0; i < k; i++) {
					float meanDistance = Math.abs(means[i] - frequency);

					if(meanDistance < minimumDistance) {
						minimumDistance = meanDistance;
						cluster = i;
					}
				}
				
				/* Assign the frequency to the cluster with the shortest mean distance */
				
				clusters.get(cluster).add(frequency);				
			}
			
			/* Update step */
			
			for(int i = 0; i < k; i++) {
				
				/* Compute mean of i-th cluster */

				Float clusterSum = 0.0f, newMean;

				for(Float frequency : clusters.get(i)) {
					clusterSum += frequency;
				}

				newMean = clusterSum / clusters.get(i).size();

				if(Float.compare(means[i],  newMean) != 0) { 

					/* Didn't reach fix-point */

					meansChange = true;
					means[i] = newMean;
				}
			}
		}
		
		Log.i(TAG, "finished frequency clustering");
		
		for(Float mean : means) {
			Log.i(TAG, Float.toString(mean));
		}
	}
	
	public static List<Float> getMeans() {
		return Arrays.asList(means);
	}
}
