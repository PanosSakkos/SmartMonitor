package di.kdd.smartmonitor.framework;

import java.util.List;

import di.kdd.smartmonitor.framework.Acceleration.AccelerationAxis;

public class ModalComputationResults {

	private Double[] fftOutput;
	private List<Float> modalFrequencies;
	private List<Acceleration> accelerations; 
	private AccelerationAxis axis;
	
	public ModalComputationResults(Double[] fftOutput,
			List<Float> modalFrequencies, List<Acceleration> accelerations,
			AccelerationAxis axis) {
		super();
		this.fftOutput = fftOutput;
		this.modalFrequencies = modalFrequencies;
		this.accelerations = accelerations;
		this.axis = axis;
	}
	
	
	public Double[] getFftOutput() {
		return fftOutput;
	}
	public void setFftOutput(Double[] fftOutput) {
		this.fftOutput = fftOutput;
	}
	public List<Float> getModalFrequencies() {
		return modalFrequencies;
	}
	public void setModalFrequencies(List<Float> modalFrequencies) {
		this.modalFrequencies = modalFrequencies;
	}
	public List<Acceleration> getAccelerations() {
		return accelerations;
	}
	public void setAccelerations(List<Acceleration> accelerations) {
		this.accelerations = accelerations;
	}
	public AccelerationAxis getAxis() {
		return axis;
	}
	public void setAxis(AccelerationAxis axis) {
		this.axis = axis;
	}
	
}
