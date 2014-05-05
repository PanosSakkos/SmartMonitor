package di.kdd.smartmonitor;

import di.kdd.smart.R;
import di.kdd.smartmonitor.framework.AccelerationsSQLiteHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PeerActivity extends NodeActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.peeractivity);		
		
		accelerationsDb = new AccelerationsSQLiteHelper(this.getApplicationContext());
		accelerationsDb.subscribe(this);	

		distributedSystem.subscribe(this);
		distributedSystem.setSampler(this);
		distributedSystem.setDatabase(accelerationsDb);
		
		TextView t =(TextView)findViewById(R.id.NodeIPValueId); 
	    t.setText(getLocalIpAddress());
	}
	
	/* Button handlers */
	
	public void connectAt(View _) {
		EditText editText;
		
		editText = (EditText) findViewById(R.id.ipText);
		//TODO check ip address string
		distributedSystem.connectAt(editText.getText().toString());
	}
	
	public void connect(View _) {
		if(distributedSystem.isConnected() == false) {
				distributedSystem.connect();				
		}
		else {
			Toast.makeText(this, "Already connected!", Toast.LENGTH_LONG).show();
		}
	}
}
