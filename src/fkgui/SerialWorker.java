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
	private SerialPort serialPort;
	SerialState state;
	private ConsoleMsg delegate; 
	
	public enum SerialState { Connecting, Connected, Disconnected };


	public SerialWorker(ConsoleMsg d) {
		delegate=d;
		state = SerialState.Disconnected;
	}
	
	public void connect(String d, String p)
	{
		dev=d;
		pass=p;
		
		serialPort = new SerialPort(dev);		
		firePropertyChange("serialState", state, SerialState.Connecting );
		state = SerialState.Connecting;
		
		execute();
	}
	
	public void disconnect()
	{
		firePropertyChange("serialState", state, SerialState.Disconnected );

		
		if(serialPort != null && serialPort.isOpened() )
		{
			try {
				serialPort.closePort();
			} catch (SerialPortException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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
			
			serialPort.addEventListener(this);
			String test = expectString("The Final Key", 1000);
			if( test != null )
			{
				publish("Ready to log in, press button now.");
				publish("Waiting for button press...");
			} else {
				//Try logging out.
				serialPort.writeByte( (byte)'q');
				publish("State error, try again.");
				disconnect();
				return null;
			}
			
			if( expectString( "Pass:", 0 ) != null )
			{
				publish("Logging in...");
			} else {
				publish("Error: Did not get password prompt. Unplug and try again.");
				disconnect();
				return null;
			}

			serialPort.writeBytes(pass.getBytes());
			serialPort.writeByte( (byte)13 );
			pass = "";
			
			
			if( expectString( "[Granted]", 200 ) != null )
			{
				publish("Access Granted.");
			} else {
				publish("Error: Access Denied.");
				disconnect();
				return null;
			}

			publish("Getting account list...");
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
			for(String l:lines)
			{
				String ac = l.substring(0,2);
				String an = l.substring(2);
				
				//publish( "Account number: "+ac+" ["+an+"]");

				Menu menu = new Menu(an+" ["+ac+"]");
				MenuItem both = new MenuItem("User + Pass");
				MenuItem usr = new MenuItem("User");
				MenuItem psw = new MenuItem("Pass");
				menu.add(both);
				menu.add(usr);
				menu.add(psw);

				FireActionListener fal = new FireActionListener();
				fal.action = "%";
				fal.name = an;
				fal.num = ac;
				fal.port = serialPort;
				both.addActionListener(fal);


				fal = new FireActionListener();
				fal.action = "p";
				fal.name = an;
				fal.num = ac;
				fal.port = serialPort;
				psw.addActionListener(fal);	                

				fal = new FireActionListener();
				fal.action = "u";
				fal.name = an;
				fal.num = ac;
				fal.port = serialPort;
				usr.addActionListener(fal);	                

				delegate.getPopup().add(menu);
			}

		}
		catch (SerialPortException ex){
			System.out.println(ex);
		} catch (Exception e )
		{
			System.out.println("Other exception: "+e.getMessage() );
			e.printStackTrace();
		}

		if(numAccounts==1)
		{
			publish(numAccounts+" account.");
		} else {
			publish(numAccounts+" accounts ready.");
		}

		publish("Use the systray icon to trigger.");

		firePropertyChange("serialState", state, SerialState.Connected );
		state = SerialState.Connected;
		
		return null;
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
	
	@Override
	protected void process(List<String> msgs) {
		for(String s : msgs)
		{
			
			Display.getDefault().asyncExec( new MainWinMsg(delegate, s) );
		}
	}

	@Override
	public void serialEvent(SerialPortEvent event) {
		
		if(event.isBREAK())
			System.out.println(">>BREAK);");
		if(event.isERR())
			System.out.println(">>Err");
		if(serialPort == null)
			System.out.println(">>Null");

	}


}
