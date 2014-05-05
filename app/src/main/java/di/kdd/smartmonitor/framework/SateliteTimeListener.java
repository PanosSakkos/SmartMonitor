package di.kdd.smartmonitor.framework;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

/**
 * Created by panos on 5/5/14.
 */

public class SateliteTimeListener implements LocationListener {
    @Override
    public void onLocationChanged(Location location) {
        DistributedSystem.sateliteTime = location.getTime();
        DistributedSystem.localTime = System.currentTimeMillis();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
