package fkgui;

import java.awt.Menu;
import java.awt.MenuItem;
import java.util.List;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

import org.eclipse.swt.widgets.Display;


public class SerialWorker extends javax.swing.SwingWorker<Void, String> implements SerialPortEventListener {
	public String dev;
	public String pass;
	public SerialPort serialPort;
	SerialState state;
	private ConsoleMsg delegate; 
	
	public enum SerialState { Connecting,Working, Connected, Disconnected };


	public SerialWorker(ConsoleMsg d) {
		delegate=d;
		state = SerialState.Disconnected;
		FkManager.getInstance().setWorker(this);
	}
	
	public void connect(String d, String p)
	{
		dev=d;
		pass=p;
		
		serialPort = new SerialPort(dev);		

		state = SerialState.Connecting;
		postStateChange( state );
		FkManager.getInstance().listClear();
		execute();
	}
	
	public void disconnect()
	{
		if(serialPort != null && serialPort.isOpened() )
		{
			try {
				serialPort.writeByte( (byte)'q'); //Machine commands with uppercase X
				Thread.sleep(400);
				serialPort.closePort();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		state = SerialState.Disconnected;
		postStateChange( SerialState.Disconnected);
	}

	public String expectString(String expect, int timeOut)
	{
		
		//Read from port, and if not found within 2 seconds, exit with null
		String in=new String();
		int msLeft=timeOut;
		while(true)
		{
			try {
				if( serialPort.getInputBufferBytesCount() > 0)
				{
					in += serialPort.readString(serialPort.getInputBufferBytesCount());
					
					if( in.contains(expect))
					{
						return(in);
					}
				} else {
					Thread.sleep(10);
					if(timeOut>0)
					{
						msLeft -=10;
						if( msLeft < 1)
						{
							break;
						}
					}
				}
			} catch (Exception e)
			{
				//I don't care
			}
		}
		
		return( null );
	}
	
	
	@Override
	protected Void doInBackground() throws Exception {
		publish("Trying to connect to "+dev);
		/**
		 * Connection strategy:
		 * Open the port, wait for "The Final Key" followed by # on next line, (getLoginHeader)
		 * 	* If not coming, press q and try once more.
		 * When [Granted] record [Keyboard: and query full list with Xk
		 * 
		 */
		
		int numAccounts=0;
		try {
			System.out.println("Port opened: " + serialPort.openPort());
			System.out.println("Params setted: " + serialPort.setParams(9600, 8, 1, 0));

			int mask = SerialPort.MASK_BREAK + SerialPort.MASK_ERR + SerialPort.MASK_RLSD;
			serialPort.setEventsMask(mask);

			serialPort.addEventListener(this);
			String test = expectString("The Final Key", 1000);
			if( test != null )
			{
				publish("\n* Press the button on the Final Key *");
			} else {
				//Try logging out.
				serialPort.writeByte( (byte)'q');
				publish("State error, try again.");
				disconnect();
				return null;
			}
			
			if( expectString( "Pass:", 0 ) != null )
			{
				publish("Logging in.");
				postStateChange(SerialState.Working);
			} else {
				publish("Error: Did not get password prompt. Unplug and try again.");
				disconnect();
				return null;
			}

			enterString(pass);

			serialPort.writeByte( (byte)13 );
			pass = "";
			
			/*String str = expectString( "[Keyboard: ", 200 );
			if( str != null )
			{
				FkManager.getInstance().setCurrentLayout( str );
			} else {
				publish("Did not get Keyboard layout.");
			}*/
			
			if( expectString( "[Granted]", 200 ) != null )
			{
				publish("\nAccess Granted.");
			} else {
				publish("\nError: Access Denied.");
				disconnect();
				return null;
			}

			publish("Getting account list.");
			serialPort.writeByte( (byte)'X'); //Machine commands with uppercase X
			expectString("[auto]", 200);
			serialPort.writeByte( (byte)'l'); //Full list 

			
			String accounts = new String();
			
			int timeOut = 10000;
			while(true)
			{
				
				if( serialPort.getInputBufferBytesCount() > 0 )
				{
					accounts += serialPort.readString();
					String sub = accounts.substring( accounts.length()-3 );
					if( sub.equals("\r\n>") )
					{
						accounts = accounts.substring( 0, accounts.length()-3 );
						break;
					}
				} else {
					Thread.sleep(10);
					timeOut-=10;
					if(timeOut < 1)
					{
						publish("Error getting account list.");
						disconnect();
						return null;
					}
				}
			}
			
			//Trim first 3
			accounts = accounts.substring(3);

			String[] lines = accounts.split( "\r\n" );
			numAccounts=lines.length;
			Boolean kbList=false;
			for(String l:lines)
			{
				if( ! kbList )
				{
					if( l.compareTo("[KBL]") == 0 )
					{
						kbList=true;
					} else {
						String ac = l.substring(0,2);
						String an = l.substring(2);
						FkManager.getInstance().listAddAcc(ac, an);
					}
				} else {
					//Next entries are supported keyboard layouts
					publish("Supported layout:" + l);
				}
			}

		}
		catch (Exception ex){
			publish("Error: Exception: "+ex.getMessage() );
			disconnect();
		}

		if( state != SerialState.Disconnected )
		{
			if(numAccounts==1)
			{
				publish(numAccounts+" account.");
			} else {
				publish(numAccounts+" accounts ready.");
			}
	
			publish("\n* Use the FinalKey icon in the systray to access your logins *");

			state = SerialState.Connected;
			postStateChange( state);
		}
		
		return null;
	}


	private void enterString(String str) throws SerialPortException, InterruptedException {
		for(int i=0; i < str.length(); i++)
		{
			serialPort.writeByte( str.getBytes()[i] );
			Thread.sleep(20);
		}
	}


	private class MainWinMsg implements Runnable {
		private String msg;
		private ConsoleMsg delegate;
		MainWinMsg(ConsoleMsg d, String m)
		{
			delegate=d;
			msg=m;
		}
		public void run() {
			delegate.log(msg);
		}
	}
	
	private class MainWinSerialChange implements Runnable  {
		private ConsoleMsg delegate;
		private SerialState state;
		private MainWinSerialChange(ConsoleMsg d, SerialState s )
		{
			delegate=d;
			state=s;
		}
		@Override
		public void run() {
			delegate.serialEvent(state);
		}
	}

	private void postStateChange(SerialState state)
	{
		Display.getDefault().asyncExec( new MainWinSerialChange(delegate, state) );
	}

	@Override
	protected void process(List<String> msgs) {
		for(String s : msgs)
		{
			
			Display.getDefault().asyncExec( new MainWinMsg(delegate, s) );
		}
	}

	@Override
	public void serialEvent(SerialPortEvent event) {
		
		System.out.println("Event!" + event.getEventType() );

		if(event.isBREAK())
		{
			if(state!=SerialState.Disconnected)
			{
				disconnect();
				System.out.println(">>Break);");
			}
		}
		if(event.isERR())
		{
			if(state!=SerialState.Disconnected)
			{
				disconnect();
				System.out.println(">>Error");
			}
		}
		if(serialPort == null)
			System.out.println(">>Null");

	}


}
