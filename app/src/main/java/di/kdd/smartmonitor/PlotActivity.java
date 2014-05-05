package di.kdd.smartmonitor;

import java.util.Date;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import di.kdd.smart.R;
import di.kdd.smartmonitor.framework.AccelerationsSQLiteHelper;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

public class PlotActivity extends Activity implements SensorEventListener {

	private XYMultipleSeriesDataset xDataset = new XYMultipleSeriesDataset();
	private XYMultipleSeriesRenderer renderer_X = new XYMultipleSeriesRenderer();
	
	private XYMultipleSeriesDataset yDataset = new XYMultipleSeriesDataset();
	private XYMultipleSeriesRenderer renderer_Y = new XYMultipleSeriesRenderer();
	
	private XYMultipleSeriesDataset zDataset = new XYMultipleSeriesDataset();
	private XYMultipleSeriesRenderer renderer_Z = new XYMultipleSeriesRenderer();
	
	private XYSeries xSeries;
	private XYSeries ySeries;
	private XYSeries zSeries;
	private GraphicalView view_x;
	private GraphicalView view_y;
	private GraphicalView view_z;

	private SensorManager sensorManager;


	private long t;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.plotactivity);

		buildRenderers();
		XYSeriesRenderer seriesrendererX = new XYSeriesRenderer();
		seriesrendererX.setColor(Color.YELLOW);
		
		XYSeriesRenderer seriesrendererY = new XYSeriesRenderer();
		seriesrendererY.setColor(Color.RED);
		
		XYSeriesRenderer seriesrendererZ = new XYSeriesRenderer();
		seriesrendererZ.setColor(Color.GREEN);
		
		
		renderer_X.addSeriesRenderer(seriesrendererX);
		renderer_Y.addSeriesRenderer(seriesrendererY);
		renderer_Z.addSeriesRenderer(seriesrendererZ);
		
		xSeries = new XYSeries("x-Axis");
		ySeries = new XYSeries("y-Axis");
		zSeries = new XYSeries("z-Axis");
		
		
		xDataset.addSeries(xSeries);
		yDataset.addSeries(ySeries);
		zDataset.addSeries(zSeries);

		
		t = System.currentTimeMillis();
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		
		Sensor accelerometer =
		 sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		 sensorManager.registerListener(this,
		 accelerometer,SensorManager.SENSOR_DELAY_FASTEST);
		 
		 
		Thread xTimer = new Thread() {
			public void run() {
				try {
					for (int r = 0; r <= 10000; r++) {
						sleep(100);

						double maxX = xSeries.getMaxX();
						double minX = maxX - 10;

						if (maxX>10) {
						 maxX = xSeries.getMaxX();
						 minX = maxX - 10;

							renderer_X.setXAxisMin(minX);
							renderer_X.setXAxisMax(maxX);
						}
						view_x.repaint();
						
					}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
					
			
			Thread yTimer = new Thread() {
				public void run() {
					try {
						for (int r = 0; r <= 10000; r++) {
							sleep(100);
						double maxY = ySeries.getMaxX();
						double minY = maxY - 10;
//						System.out.println("Y: Max: " + maxY + " Min: " + minY);
						
						if (maxY>10) {
						 maxY = ySeries.getMaxX();
						 minY = maxY - 10;
//							System.out.println("Y: Max: " + maxY + " Min: " + minY);
							renderer_Y.setXAxisMin(minY);
							renderer_Y.setXAxisMax(maxY);
						}
						view_y.repaint();
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};	
					
			Thread zTimer = new Thread() {
				public void run() {
					try {
						for (int r = 0; r <= 10000; r++) {
							sleep(100);
						
						double maxZ = zSeries.getMaxX();
						double minZ = maxZ - 10;

						if (maxZ>10) {
						 maxZ = zSeries.getMaxX();
						 minZ = maxZ - 10;
							renderer_Z.setXAxisMin(minZ);
							renderer_Z.setXAxisMax(maxZ);
						}
						view_z.repaint();

					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		
		xTimer.start();
		yTimer.start();
		zTimer.start();
				
		
	}

	protected void onResume() {
		super.onResume();
		if (view_x == null) {
			LinearLayout layout_x = (LinearLayout) findViewById(R.id.chartXaxis);
			view_x = ChartFactory
					.getLineChartView(this, xDataset, renderer_X);
			layout_x.addView(view_x, new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		} else {
			view_x.repaint();
		}
		
		if (view_y == null) {
			LinearLayout layout_y = (LinearLayout) findViewById(R.id.chartYaxis);
			view_y = ChartFactory
					.getLineChartView(this, yDataset, renderer_Y);
			layout_y.addView(view_y, new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		} else {
			view_y.repaint();
		}
		
		if (view_z == null) {
			LinearLayout layout_z = (LinearLayout) findViewById(R.id.chartZaxis);
			view_z = ChartFactory
					.getLineChartView(this, zDataset, renderer_Z);
			layout_z.addView(view_z, new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		} else {
			view_z.repaint();
		}
	}
	
	protected void onDestroy(){
		super.onDestroy();
		Sensor accelerometer =
				 sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
				 
		sensorManager.unregisterListener(this, accelerometer);
	}
	


	// // myReceiver = new MyReceiver();
	//
	// view = line.getView(this);
	// layout = (LinearLayout) findViewById(R.id.chartXaxis);
	// sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
	// // new ArrayList<AccelemoterData>();
	//
	//
	// t = System.currentTimeMillis();
	// Sensor accelerometer =
	// sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	// sensorManager.registerListener(this,
	// accelerometer,SensorManager.SENSOR_DELAY_FASTEST);
	//
	//

	// if (xChartView == null) {
	// LinearLayout xLayout = (LinearLayout) findViewById(R.id.chartXaxis);
	// try {
	// xChartView = getAxisChart("x", Color.BLUE);
	// } catch (Exception e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// xLayout.addView(xChartView);
	//
	// LinearLayout yLayout = (LinearLayout) findViewById(R.id.chartYaxis);
	// try {
	// yChartView = getAxisChart("y", Color.RED);
	// } catch (Exception e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// yLayout.addView(yChartView);
	//
	// LinearLayout zLayout = (LinearLayout) findViewById(R.id.chartZaxis);
	// try {
	// zChartView = getAxisChart("z", Color.BLACK);
	// } catch (Exception e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// zLayout.addView(zChartView);
	// } else {
	// xChartView.repaint(); // use this whenever data has changed and you
	// yChartView.repaint();
	// zChartView.repaint();
	// }
	// }

	

	protected void setChartSettings(XYMultipleSeriesRenderer renderer,
			String title, String xTitle, String yTitle, double xMin,
			double xMax, double yMin, double yMax, int axesColor,
			int labelsColor) {
		renderer.setChartTitle(title);
		renderer.setXTitle(xTitle);
		renderer.setYTitle(yTitle);
		renderer.setXAxisMin(xMin);
		renderer.setXAxisMax(xMax);
		renderer.setYAxisMin(yMin);
		renderer.setYAxisMax(yMax);
		renderer.setAxesColor(axesColor);
		renderer.setLabelsColor(labelsColor);
	}

	/**
	 * Builds an XY multiple series renderer.
	 * 
	 * @param colors
	 *            the series rendering colors
	 * @param styles
	 *            the series point styles
	 * @return the XY multiple series renderers
	 */

	protected XYMultipleSeriesRenderer buildRenderer(int[] colors,
			PointStyle[] styles) {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		setRenderer(renderer, colors, styles);
		return renderer;
	}
	
	protected void buildRenderers(){
	
		renderer_X.setApplyBackgroundColor(true);
		renderer_X.setBackgroundColor(Color.BLACK);
		renderer_X.setMarginsColor(Color.BLACK);
		renderer_X.setAxisTitleTextSize(16);
		renderer_X.setChartTitleTextSize(20);
		renderer_X.setLabelsTextSize(15);
		renderer_X.setLegendTextSize(15);
		renderer_X.setYAxisMin(-2);
		renderer_X.setYAxisMax(1);
		renderer_X.setXAxisMax(10);
		renderer_X.setXAxisMin(0);
		renderer_X.setAxesColor(Color.WHITE);
		renderer_X.setXTitle("Time");
		renderer_X.setChartTitle("x-Axis");
	
		renderer_Y.setApplyBackgroundColor(true);
		renderer_Y.setBackgroundColor(Color.BLACK);
		renderer_Y.setMarginsColor(Color.BLACK);
		renderer_Y.setAxisTitleTextSize(16);
		renderer_Y.setChartTitleTextSize(20);
		renderer_Y.setLabelsTextSize(15);
		renderer_Y.setLegendTextSize(15);
		renderer_Y.setYAxisMin(-2);
		renderer_Y.setYAxisMax(1);
		renderer_Y.setXAxisMax(10);
		renderer_Y.setXAxisMin(0);
		renderer_Y.setAxesColor(Color.WHITE);
		renderer_Y.setXTitle("Time");
		renderer_Y.setChartTitle("y-Axis");
		
		renderer_Z.setApplyBackgroundColor(true);
		renderer_Z.setBackgroundColor(Color.BLACK);
		renderer_Z.setMarginsColor(Color.BLACK);
		renderer_Z.setAxisTitleTextSize(16);
		renderer_Z.setChartTitleTextSize(20);
		renderer_Z.setLabelsTextSize(15);
		renderer_Z.setLegendTextSize(15);
		renderer_Z.setYAxisMin(10);
		renderer_Z.setYAxisMax(15);
		renderer_Z.setXAxisMax(10);
		renderer_Z.setXAxisMin(0);
		renderer_Z.setAxesColor(Color.WHITE);
		renderer_Z.setXTitle("Time");
		renderer_Z.setChartTitle("z-Axis");
	}

	protected void setRenderer(XYMultipleSeriesRenderer renderer, int[] colors,
			PointStyle[] styles) {
		renderer.setAxisTitleTextSize(16);
		renderer.setChartTitleTextSize(20);
		renderer.setLabelsTextSize(15);
		renderer.setLegendTextSize(15);
		renderer.setPointSize(5f);
		renderer.setMargins(new int[] { 20, 30, 15, 20 });
		int length = colors.length;
		for (int i = 0; i < length; i++) {
			XYSeriesRenderer r = new XYSeriesRenderer();
			r.setColor(colors[i]);
			r.setPointStyle(styles[i]);
			renderer.addSeriesRenderer(r);
		}
	}

	/**
	 * Builds an XY multiple time dataset using the provided values.
	 * 
	 * @param titles
	 *            the series titles
	 * @param xValues
	 *            the values for the X axis
	 * @param yValues
	 *            the values for the Y axis
	 * @return the XY multiple time dataset
	 */

	protected XYMultipleSeriesDataset buildDateDataset(String[] titles,
			List<Date[]> xValues, List<double[]> yValues) {
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		int length = titles.length;
		for (int i = 0; i < length; i++) {
			TimeSeries series = new TimeSeries(titles[i]);
			Date[] xV = xValues.get(i);
			double[] yV = yValues.get(i);
			int seriesLength = xV.length;
			for (int k = 0; k < seriesLength; k++) {
				series.add(xV[k], yV[k]);
			}
			dataset.addSeries(series);
		}
		return dataset;
	}



	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		double x = event.values[0];
		double y = event.values[1];
		double z = event.values[2];
		long time = System.currentTimeMillis();
		// AccelerometerData data = new AccelerometerData(x, y, z, time);

		double timestamp = ((double)(time-t))/1000;
		
		xSeries.add(timestamp, x);
		ySeries.add(timestamp, y);
		zSeries.add(timestamp, z);
		
		System.out.println(z);

	}

}
