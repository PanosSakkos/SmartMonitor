package di.kdd.smartmonitor;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
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
import di.kdd.smartmonitor.Acceleration.AccelerationAxis;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.widget.LinearLayout;

public class PlotActivity extends SmartActivity {

	private GraphicalView xChartView;
	private GraphicalView yChartView;
	private GraphicalView zChartView;

	private SmartActivity view;
	
	private AccelerationsSQLiteHelper database;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.plotactivity);

		if (xChartView == null) {
			LinearLayout xLayout = (LinearLayout) findViewById(R.id.chartXaxis);
			try {
				xChartView = getAxisChart("x", Color.BLUE);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			xLayout.addView(xChartView);
			
			
			LinearLayout yLayout = (LinearLayout) findViewById(R.id.chartYaxis);
			try {
				yChartView = getAxisChart("y", Color.RED);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			yLayout.addView(yChartView);
			
			
			LinearLayout zLayout = (LinearLayout) findViewById(R.id.chartZaxis);
			try {
				zChartView = getAxisChart("z", Color.BLACK);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			zLayout.addView(zChartView);
		} else {
			xChartView.repaint(); // use this whenever data has changed and you
			yChartView.repaint();
			zChartView.repaint();
		}
	}
	
	
	private GraphicalView getAxisChart(String axis, int color) throws Exception{
		
		
		database = new AccelerationsSQLiteHelper(this.getApplicationContext());

		List<Acceleration> accelerations = database.getAllAccelerations(AccelerationAxis.X);
		List<Date[]> dates = new ArrayList<Date[]>();
		List<double[]> values = new ArrayList<double[]>();
		
		Date[] dateValues = new Date[accelerations.size()];
		double[] valuesValues = new double[accelerations.size()];
		int i = 0;
		
		double min = Double.MAX_VALUE;
		double max = -100;
		for(Acceleration acceleration : accelerations) {
			dateValues[i]=new Date(acceleration.getTimestamp());
			valuesValues[i]=acceleration.getAcceleration();
			
			if (acceleration.getAcceleration()>max)
				max = acceleration.getAcceleration();
			if (acceleration.getAcceleration() < min)
				min = acceleration.getAcceleration();
			i++;
		}
		
		dates.add(dateValues);
		values.add(valuesValues);		
		
		
		
		
		String chartTitle = axis + "-axis accelerations";
		String[] titles = new String[] { chartTitle };
	   
//	    Date[] dateValues = new Date[] { new Date(95, 0, 1), new Date(95, 3, 1), new Date(95, 6, 1),
//	        new Date(95, 9, 1), new Date(96, 0, 1), new Date(96, 3, 1), new Date(96, 6, 1),
//	        new Date(96, 9, 1), new Date(97, 0, 1), new Date(97, 3, 1), new Date(97, 6, 1),
//	        new Date(97, 9, 1), new Date(98, 0, 1), new Date(98, 3, 1), new Date(98, 6, 1),
//	        new Date(98, 9, 1), new Date(99, 0, 1), new Date(99, 3, 1), new Date(99, 6, 1),
//	        new Date(99, 9, 1), new Date(100, 0, 1), new Date(100, 3, 1), new Date(100, 6, 1),
//	        new Date(100, 9, 1), new Date(100, 11, 1) };
//	    dates.add(dateValues);
//
//	    values.add(new double[] { 4.9, 5.3, 3.2, 4.5, 6.5, 4.7, 5.8, 4.3, 4, 2.3, -0.5, -2.9, 3.2, 5.5,
//	        4.6, 9.4, 4.3, 1.2, 0, 0.4, 4.5, 3.4, 4.5, 4.3, 4 });
	    int[] colors = new int[] { color };
	    PointStyle[] styles = new PointStyle[] { PointStyle.POINT };
	    XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);
	    setChartSettings(renderer, chartTitle, "Date", "%", dateValues[0].getTime(),
	        dateValues[dateValues.length - 1].getTime(), min, max, Color.GRAY, Color.LTGRAY);
	    renderer.setYLabels(10);
//	     ChartFactory.getTimeChartIntent(this, buildDateDataset(titles, dates, values),
//	        renderer, "MMM yyyy");
	     
	    return ChartFactory.getTimeChartView(this, buildDateDataset(titles, dates, values), renderer, "MMM yyyy");
		
		
	}

	 protected void setChartSettings(XYMultipleSeriesRenderer renderer, String title, String xTitle,
		      String yTitle, double xMin, double xMax, double yMin, double yMax, int axesColor,
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
	   * @param colors the series rendering colors
	   * @param styles the series point styles
	   * @return the XY multiple series renderers
	   */
	  protected XYMultipleSeriesRenderer buildRenderer(int[] colors, PointStyle[] styles) {
	    XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
	    setRenderer(renderer, colors, styles);
	    return renderer;
	  }
	  
	  protected void setRenderer(XYMultipleSeriesRenderer renderer, int[] colors, PointStyle[] styles) {
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
	   * @param titles the series titles
	   * @param xValues the values for the X axis
	   * @param yValues the values for the Y axis
	   * @return the XY multiple time dataset
	   */
	  protected XYMultipleSeriesDataset buildDateDataset(String[] titles, List<Date[]> xValues,
	      List<double[]> yValues) {
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
	protected void onResume() {
		super.onResume();
		if (xChartView != null) {
			xChartView.repaint();
		}
	}

}
