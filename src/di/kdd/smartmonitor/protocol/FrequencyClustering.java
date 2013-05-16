package di.kdd.smartmonitor.protocol;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class FrequencyClustering {	
	private static Float []means;
	
	public static void clusterFrequencies(int k, List<Float> frequencies) {		
		Float[][] clusters = new Float[k][frequencies.size()];
		means = new Float[k];

		/* Forgy initialization */
		
		Random randomGenerator = new Random();
		
		for(int i = 0; i < k; i++) {
			means[i] = frequencies.get(randomGenerator.nextInt(frequencies.size()));
		}
		
		
	}
	
	public static List<Float> getMeans() {
		return Arrays.asList(means);
	}
}
