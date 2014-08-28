package fkgui;

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
		
		System.out.println("Searching for string:"+expect+" timeout:"+timeOut);
		//Read from port, and if not found within 2 seconds, exit with null
		String in=new String();
		int msLeft=timeOut;
		while(true)
		{
			try {
				if( serialPort.getInputBufferBytesCount() > 0)
				{
					in += serialPort.readString();
					System.out.println("In:"+in);
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
				System.out.println("Exception from expectString:" + e.getMessage() );
			}
		}
		
		return( null );
	}
	
	
	@Override
	protected Void doInBackground() throws Exception {
		publish(Messages.SerialWorker_0+dev);
		/**
		 * Connection strategy:
		 * Open the port, wait for "The Final Key" followed by # on next line, (getLoginHeader)
		 * 	* If not coming, press q and try once more.
		 * When [Granted] record [Keyboard: and query full list with Xk
		 * 
		 */
		
		int numAccounts=0;
		try {
			System.out.println(Messages.SerialWorker_1 + serialPort.openPort());
			System.out.println(Messages.SerialWorker_2 + serialPort.setParams(9600, 8, 1, 0));

			int mask = SerialPort.MASK_BREAK + SerialPort.MASK_ERR + SerialPort.MASK_RLSD;
			serialPort.setEventsMask(mask);
			serialPort.addEventListener(this);
			
			// Getting to the login
			String test = expectString("The Final Key", 1000); //$NON-NLS-1$
			if( test != null )
			{
				publish(Messages.SerialWorker_4);
			} else {
				//Try logging out.
				serialPort.writeByte( (byte)'q');
				test = expectString("The Final Key", 1000); //$NON-NLS-1$
				if( test != null )
				{
					publish(Messages.SerialWorker_4);
				} else {
					publish(Messages.SerialWorker_5);
					disconnect();
					return null;
				}
			}
			
			// Enter the password
			if( expectString( "Pass:", 0 ) != null ) //$NON-NLS-1$
			{
				publish(Messages.SerialWorker_7);
				postStateChange(SerialState.Working);
			} else {
				publish(Messages.SerialWorker_8);
				disconnect();
				return null;
			}
			enterString(pass);

			serialPort.writeByte( (byte)13 ); // press enter
			pass = ""; //$NON-NLS-1$
			
			/*String str = expectString( "[Keyboard: ", 200 );
			if( str != null )
			{
				FkManager.getInstance().setCurrentLayout( str );
			} else {
				publish("Did not get Keyboard layout.");
			}*/


			if( expectString( "[Granted]", 200 ) != null ) //$NON-NLS-1$
			{
				publish(Messages.SerialWorker_11);
			} else {
				publish(Messages.SerialWorker_12);
				disconnect();
				return null;
			}

			publish(Messages.SerialWorker_13);
	
			// Iterate through all 6 pages and collect the accounts
			expectString(">", 200);
			for (int page=0; page <=5; page++) {
				serialPort.writeByte((byte)'l'); //Full list
				serialPort.readBytes(8); // chop of the command itself
				
				String data = expectString(">", 500);
				
				// Timeout, we stop here.
				if (data == null) {
					publish(Messages.SerialWorker_16);
					disconnect();
					return null;
				}
				
				// We have a bunch of data which we need to process
				String[] lines = data.split("\r\n");		
				for (String line:lines) {
					if (line.startsWith(">")) break; // end of page
					if (line.startsWith("Accounts")) continue; // page title
					
					// Check if we have more than one account in one line
					if (line.length() > 44) {
						String first = line.substring(0, 43).trim();
						String second = line.substring(43).trim();
						
						addAccount(first);
						addAccount(second);
						numAccounts += 2;
						
					// Single line
					} else {
						String first = line.trim();
						addAccount(first);
						numAccounts++;
					}
				}
			}

		} catch (Exception ex){
			publish("Error: Exception: "+ex.getMessage() ); //$NON-NLS-1$
			disconnect();
		}

		if (state != SerialState.Disconnected) {
			publish(numAccounts+" accounts ready."); //$NON-NLS-1$
			publish(Messages.SerialWorker_23);

			state = SerialState.Connected;
			postStateChange( state);
		}
		
		return null;
	}
	
	private void addAccount(String accountString) {
		String[] parts = accountString.split(" - ");
		FkManager.getInstance().listAddAcc(parts[0], parts[1]);
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
		
		System.out.println("Event!" + event.getEventType() ); //$NON-NLS-1$

		if( event.isRXCHAR() )
		{
			System.out.print("{char}");
		}
		
		if(event.isBREAK())
		{
			if(state!=SerialState.Disconnected)
			{
				disconnect();
				System.out.println(">>Break);"); //$NON-NLS-1$
			}
		}
		if(event.isERR())
		{
			if(state!=SerialState.Disconnected)
			{
				disconnect();
				System.out.println(">>Error"); //$NON-NLS-1$
			}
		}
		if(serialPort == null)
			System.out.println(">>Null"); //$NON-NLS-1$

	}


}
