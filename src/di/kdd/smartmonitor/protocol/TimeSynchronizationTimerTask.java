package di.kdd.smartmonitor.protocol;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.TimerTask;

public class TimeSynchronizationTimerTask extends TimerTask {

	private List<Socket> commandSockets;
	
	public TimeSynchronizationTimerTask(List<Socket> commandSockets) {
		this.commandSockets = commandSockets;
	}
	
	@Override
	public void run() {
		for(Socket socket : commandSockets) {
			try {
				Node.send(socket, new TimeSynchronizationMessage());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
