package di.kdd.smartmonitor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import di.kdd.smart.R;
import di.kdd.smartmonitor.framework.Acceleration;
import di.kdd.smartmonitor.framework.AccelerationsSQLiteHelper;
import di.kdd.smartmonitor.framework.ModalComputationResults;
import di.kdd.smartmonitor.framework.exceptions.ConnectException;
import di.kdd.smartmonitor.framework.exceptions.MasterException;
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
import android.widget.Toast;

public class ModalActivity extends NodeActivity {

	private XYMultipleSeriesDataset xDataset_fft = new XYMultipleSeriesDataset();
	private XYMultipleSeriesDataset xDataset_ts = new XYMultipleSeriesDataset();
	private XYMultipleSeriesRenderer renderer_X_fft = new XYMultipleSeriesRenderer();
	private XYMultipleSeriesRenderer renderer_X_ts = new XYMultipleSeriesRenderer();
	
	
	private XYMultipleSeriesDataset yDataset_fft = new XYMultipleSeriesDataset();
	private XYMultipleSeriesDataset yDataset_ts = new XYMultipleSeriesDataset();
	private XYMultipleSeriesRenderer renderer_Y_fft = new XYMultipleSeriesRenderer();
	private XYMultipleSeriesRenderer renderer_Y_ts = new XYMultipleSeriesRenderer();
	
	private XYMultipleSeriesDataset zDataset_fft = new XYMultipleSeriesDataset();
	private XYMultipleSeriesDataset zDataset_ts = new XYMultipleSeriesDataset();
	private XYMultipleSeriesRenderer renderer_Z_fft = new XYMultipleSeriesRenderer();
	private XYMultipleSeriesRenderer renderer_Z_ts = new XYMultipleSeriesRenderer();
	

	private GraphicalView view_x_ts;
	private GraphicalView view_x_fft;
	
	private GraphicalView view_y_ts;
	private GraphicalView view_y_fft;

	private GraphicalView view_z_ts;
	private GraphicalView view_z_fft;
	
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.modalactivity);
		
		
		ModalComputationResults xModalResults = distributedSystem.getxModalFreqs();
		ModalComputationResults yModalResults = distributedSystem.getyModalFreqs();
		ModalComputationResults zModalResults = distributedSystem.getzModalFreqs();
		

		
		buildFFTRenderers();
		buildTSRenderers();
		adujustGraphs(xModalResults.getAccelerations(),yModalResults.getAccelerations(),zModalResults.getAccelerations());
		
		
		XYSeriesRenderer seriesrendererX_fft = new XYSeriesRenderer();
		XYSeriesRenderer seriesrendererX_ts = new XYSeriesRenderer();
		seriesrendererX_fft.setColor(Color.YELLOW);
		seriesrendererX_ts.setColor(Color.YELLOW);
		
		XYSeriesRenderer seriesrendererY_fft = new XYSeriesRenderer();
		XYSeriesRenderer seriesrendererY_ts = new XYSeriesRenderer();
		seriesrendererY_fft.setColor(Color.RED);
		seriesrendererY_ts.setColor(Color.RED);
		
		XYSeriesRenderer seriesrendererZ_fft = new XYSeriesRenderer();
		XYSeriesRenderer seriesrendererZ_ts = new XYSeriesRenderer();
		seriesrendererZ_fft.setColor(Color.GREEN);
		seriesrendererZ_ts.setColor(Color.GREEN);
		
		renderer_X_fft.addSeriesRenderer(seriesrendererX_fft);
		renderer_Y_fft.addSeriesRenderer(seriesrendererY_fft);
		renderer_Z_fft.addSeriesRenderer(seriesrendererZ_fft);
		
		renderer_X_ts.addSeriesRenderer(seriesrendererX_ts);
		renderer_Y_ts.addSeriesRenderer(seriesrendererY_ts);
		renderer_Z_ts.addSeriesRenderer(seriesrendererZ_ts);
		renderer_X_ts.setInScroll(true);
		renderer_Y_ts.setInScroll(true);
		renderer_Z_ts.setInScroll(true);
		
		/**
		* sampling frequency
		*/
		float fs = distributedSystem.getSamplingFrequency();
		
		/**
		* last x-value, (starting x-value = 0)
		*/
		float endValue = fs/2;
		xDataset_fft = this.getFrequencyAxis(xModalResults.getFftOutput(), xModalResults.getModalFrequencies(), endValue, "x-axis");
		yDataset_fft = this.getFrequencyAxis(yModalResults.getFftOutput(), yModalResults.getModalFrequencies(), endValue, "y-axis");
		zDataset_fft = this.getFrequencyAxis(zModalResults.getFftOutput(), zModalResults.getModalFrequencies(), endValue, "z-axis");
		
		
	
		
		
		xDataset_ts = this.getTimeAxis(xModalResults.getAccelerations(), "x-axis");
		yDataset_ts = this.getTimeAxis(yModalResults.getAccelerations(), "y-axis");
		zDataset_ts = this.getTimeAxis(zModalResults.getAccelerations(), "z-axis");
	}




	protected void onResume() {
		super.onResume();
		if (view_x_fft == null) {
			LinearLayout layout_x = (LinearLayout) findViewById(R.id.chartXAxisFFT);
//			view_x_fft = ChartFactory
//					.getLineChartView(this, xDataset, renderer_X_fft);
			
			view_x_fft = ChartFactory.getBarChartView(this, xDataset_fft, renderer_X_fft, BarChart.Type.DEFAULT);
			layout_x.addView(view_x_fft, new LayoutParams(
					LayoutParams.MATCH_PARENT, 350));
		} else {
			view_x_fft.repaint();
		}

		if (view_y_fft == null) {
			LinearLayout layout_y = (LinearLayout) findViewById(R.id.chartYAxisFFT);
			view_y_fft = ChartFactory
					.getBarChartView(this, yDataset_fft, renderer_Y_fft, BarChart.Type.DEFAULT);
			layout_y.addView(view_y_fft, new LayoutParams(
					LayoutParams.MATCH_PARENT, 350));
		} else {
			view_y_fft.repaint();
		}
		
		if (view_z_fft == null) {
			LinearLayout layout_z = (LinearLayout) findViewById(R.id.chartZAxisFFT);
			view_z_fft = ChartFactory
					.getBarChartView(this, zDataset_fft, renderer_Z_fft, BarChart.Type.DEFAULT);
			layout_z.addView(view_z_fft, new LayoutParams(
					LayoutParams.MATCH_PARENT,350));
		} else {
			view_z_fft.repaint();
		}
		
		
		
		
		
		
		if (view_x_ts == null) {
			LinearLayout layout_x = (LinearLayout) findViewById(R.id.chartXAxisTS);

			view_x_ts = ChartFactory.getLineChartView(this, xDataset_ts, renderer_X_ts);
			layout_x.addView(view_x_ts, new LayoutParams(
					LayoutParams.MATCH_PARENT, 350));
		} else {
			view_x_ts.repaint();
		}

		if (view_y_ts == null) {
			LinearLayout layout_y = (LinearLayout) findViewById(R.id.chartYAxisTS);
			view_y_ts = ChartFactory.getLineChartView(this, yDataset_ts, renderer_Y_ts);
			layout_y.addView(view_y_ts, new LayoutParams(
					LayoutParams.MATCH_PARENT, 350));
		} else {
			view_y_ts.repaint();
		}
		
		if (view_z_ts == null) {
			LinearLayout layout_z = (LinearLayout) findViewById(R.id.chartZAxisTS);
			view_z_ts = ChartFactory.getLineChartView(this, zDataset_ts, renderer_Z_ts);
			layout_z.addView(view_z_ts, new LayoutParams(
					LayoutParams.MATCH_PARENT, 350));
		} else {
			view_z_ts.repaint();
		}
		
		
		
	}
	
	protected void onDestroy(){
		super.onDestroy();

	}
	

	private void adujustGraphs(List<Acceleration> xAccelerations,
			List<Acceleration> yAccelerations, List<Acceleration> zAccelerations) {
		
		double[] values = new double[xAccelerations.size()];
		int i = 0;
		for (Acceleration accel : xAccelerations)
			values[i++] = accel.getAcceleration();
		
		double xMean = average(values);
		
		renderer_X_ts.setYAxisMin(xMean-2);
		renderer_X_ts.setYAxisMax(xMean+2);
		
		
		values = new double[yAccelerations.size()];
		i = 0;
		for (Acceleration accel : yAccelerations)
			values[i++] = accel.getAcceleration();

		double yMean = average(values);
		renderer_Y_ts.setYAxisMin(yMean-2);
		renderer_Y_ts.setYAxisMax(yMean+2);
		
		
		
		values = new double[zAccelerations.size()];
		i = 0;
		for (Acceleration accel : zAccelerations)
			values[i++] = accel.getAcceleration();
		
		double zMean = average(values);
		
		renderer_Z_ts.setYAxisMin(zMean-2);
		renderer_Z_ts.setYAxisMax(zMean+2);
		
	}
	

	private XYMultipleSeriesDataset getTimeAxis(List<Acceleration> tsData, String title) {
		XYMultipleSeriesDataset rangeData = new  XYMultipleSeriesDataset();

	    String[] titles  = new String[] { title };     // titles of data
	    List<double[]> xAxis = new ArrayList<double[]>(1);  // x-axis values
	    List<double[]> values = new ArrayList<double[]>(1); // y-axis values

	    // Need to have just as many x values as y values
	    xAxis.add(new double[tsData.size()]);  
	    values.add(new double[tsData.size()]);

	    long startTime = distributedSystem.getStartSamplingTimestamp();
	    int i = 0;
	    for (Acceleration acc : tsData){
	    	xAxis.get(0)[i] = (double)(acc.getTimestamp()-startTime)/1000;
	    	values.get(0)[i] = acc.getAcceleration();
	    	i++;
	    }
	    
	    // for the buildDataset method call:
	    rangeData = dataBuilder(titles, xAxis, values);
	    return rangeData;
	}




	public XYMultipleSeriesDataset getFrequencyAxis(Double[] fftData, List<Float> modalFreqs, float endValue, String title) {
	    XYMultipleSeriesDataset rangeData = new  XYMultipleSeriesDataset();

	    String[] titles  = new String[] { title};     // titles of data
	    List<double[]> xAxis = new ArrayList<double[]>(1);  // x-axis values
	    List<double[]> values = new ArrayList<double[]>(1); // y-axis values

	    // Need to have just as many x values as y values
	    xAxis.add(new double[fftData.length]);  
	    values.add(new double[fftData.length]);

	    // Linearly-spaced x-axis values -> increment must be kept constant 
	    double increment = ((double)(endValue))/fftData.length;  // (fs/2)/length of data
	    for(int i =0; i < fftData.length; i++) {
	        xAxis.get(0)[i] = i * increment;
	        values.get(0)[i] = fftData[i];
	    }

	    // for the buildDataset method call:
	    rangeData = dataBuilder(titles, xAxis, values);
	    return rangeData;
	}

	private XYMultipleSeriesDataset dataBuilder(String[] titles,
	        List<double[]> xValues, List<double[]> yValues) {

	    XYMultipleSeriesDataset dataset1 = new XYMultipleSeriesDataset();
	    addXYSeries(dataset1, titles, xValues, yValues, 0);
	    return dataset1;
	}

	private void addXYSeries(XYMultipleSeriesDataset dataset, String[] titles,
	        List<double[]> xValues, List<double[]> yValues, int scale) {

	    int length = titles.length; // # of series to add to plot
	    for (int i = 0; i < length; i++) {

	        // maps the x-axis values to their respective y-axis values
	        double[] xV = xValues.get(i);
	        XYSeries series = new XYSeries(titles[i], scale);
	        double[] yV = yValues.get(i);

	        int seriesLength = xV.length;
	        for (int k = 0; k < seriesLength; k++) {
	            series.add(xV[k], yV[k]);
	        }
	        dataset.addSeries(series);
	    }
	}
	
	
	public double average(double[] values) {  
	    double sum = 0;

	    for (double value : values)
	    	sum+=value;
	    

	    return sum/values.length;	    
	}
	
	protected void buildTSRenderers(){
	
		renderer_X_ts.setApplyBackgroundColor(true);
		renderer_X_ts.setBackgroundColor(Color.BLACK);
		renderer_X_ts.setMarginsColor(Color.BLACK);
		renderer_X_ts.setAxisTitleTextSize(16);
		renderer_X_ts.setChartTitleTextSize(20);
		renderer_X_ts.setLabelsTextSize(15);
		renderer_X_ts.setLegendTextSize(15);
		renderer_X_ts.setAxesColor(Color.WHITE);
		renderer_X_ts.setXTitle("Time (s)");
		renderer_X_ts.setYTitle("Acceleration (m/s^2)");
		renderer_X_ts.setChartTitle("x-Axis");
		renderer_X_ts.setPanEnabled(false, false);
		renderer_X_ts.setZoomEnabled(false, false);
		renderer_X_ts.setInScroll(true);
		
		renderer_Y_ts.setApplyBackgroundColor(true);
		renderer_Y_ts.setBackgroundColor(Color.BLACK);
		renderer_Y_ts.setMarginsColor(Color.BLACK);
		renderer_Y_ts.setAxisTitleTextSize(16);
		renderer_Y_ts.setChartTitleTextSize(20);
		renderer_Y_ts.setLabelsTextSize(15);
		renderer_Y_ts.setLegendTextSize(15);
		renderer_Y_ts.setAxesColor(Color.WHITE);
		renderer_Y_ts.setXTitle("Time (s)");
		renderer_Y_ts.setYTitle("Acceleration (m/s^2)");
		renderer_Y_ts.setChartTitle("y-Axis");
		renderer_Y_ts.setPanEnabled(false, false);
		renderer_Y_ts.setZoomEnabled(false, false);
		renderer_Y_ts.setInScroll(true);
		
		renderer_Z_ts.setApplyBackgroundColor(true);
		renderer_Z_ts.setBackgroundColor(Color.BLACK);
		renderer_Z_ts.setMarginsColor(Color.BLACK);
		renderer_Z_ts.setAxisTitleTextSize(16);
		renderer_Z_ts.setChartTitleTextSize(20);
		renderer_Z_ts.setLabelsTextSize(15);
		renderer_Z_ts.setLegendTextSize(15);
		renderer_Z_ts.setAxesColor(Color.WHITE);
		renderer_Z_ts.setXTitle("Time (s)");
		renderer_Z_ts.setYTitle("Acceleration (m/s^2)");
		renderer_Z_ts.setChartTitle("z-Axis");
		renderer_Z_ts.setPanEnabled(false, false);
		renderer_Z_ts.setZoomEnabled(false, false);
		renderer_Z_ts.setInScroll(true);
	}
	
	
	protected void buildFFTRenderers(){
		
		renderer_X_fft.setApplyBackgroundColor(true);
		renderer_X_fft.setBackgroundColor(Color.BLACK);
		renderer_X_fft.setMarginsColor(Color.BLACK);
		renderer_X_fft.setAxisTitleTextSize(16);
		renderer_X_fft.setChartTitleTextSize(20);
		renderer_X_fft.setLabelsTextSize(15);
		renderer_X_fft.setLegendTextSize(15);
		renderer_X_fft.setAxesColor(Color.WHITE);
		renderer_X_fft.setXTitle("Frequency (Hz)");
		renderer_X_fft.setChartTitle("x-Axis");
		renderer_X_fft.setPanEnabled(false, false);
		renderer_X_fft.setZoomEnabled(false, false);
		
		renderer_Y_fft.setApplyBackgroundColor(true);
		renderer_Y_fft.setBackgroundColor(Color.BLACK);
		renderer_Y_fft.setMarginsColor(Color.BLACK);
		renderer_Y_fft.setAxisTitleTextSize(16);
		renderer_Y_fft.setChartTitleTextSize(20);
		renderer_Y_fft.setLabelsTextSize(15);
		renderer_Y_fft.setLegendTextSize(15);
		renderer_Y_fft.setAxesColor(Color.WHITE);
		renderer_Y_fft.setXTitle("Frequency (Hz)");
		renderer_Y_fft.setChartTitle("y-Axis");
		renderer_Y_fft.setPanEnabled(false, false);
		renderer_Y_fft.setZoomEnabled(false, false);
		
		renderer_Z_fft.setApplyBackgroundColor(true);
		renderer_Z_fft.setBackgroundColor(Color.BLACK);
		renderer_Z_fft.setMarginsColor(Color.BLACK);
		renderer_Z_fft.setAxisTitleTextSize(16);
		renderer_Z_fft.setChartTitleTextSize(20);
		renderer_Z_fft.setLabelsTextSize(15);
		renderer_Z_fft.setLegendTextSize(15);
		renderer_Z_fft.setAxesColor(Color.WHITE);
		renderer_Z_fft.setXTitle("Frequency (Hz)");
		renderer_Z_fft.setChartTitle("z-Axis");
		renderer_Z_fft.setPanEnabled(false, false);
		renderer_Z_fft.setZoomEnabled(false, false);
		
	}



}
