package di.kdd.smartmonitor;

public class AccelerometerData {
	double x;
	double y;
	double z;
	
	long time;

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getZ() {
		return z;
	}

	public void setZ(double z) {
		this.z = z;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public AccelerometerData(double x, double y, double z, long time) {
		super();
		this.x = x;
		this.y = y;
		this.z = z;
		this.time = time;
	}
	
}
