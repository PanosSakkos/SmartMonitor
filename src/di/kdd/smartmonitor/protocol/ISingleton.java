package di.kdd.smartmonitor.protocol;

public interface ISingleton <T>{

	/* Returns the one and only instance of the singleton */
	
	public T getInstance();
}
