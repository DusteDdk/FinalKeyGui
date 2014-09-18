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
				serialPort.setDTR(false);
				serialPort.setRTS(false);

				serialPort.removeEventListener();

				serialPort.closePort();

			} catch (Exception e) {
				publish(Messages.SerialWorker_6);
				explainSerialPortException(e);
			}
			publish(Messages.SerialWorker_9);
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
					in += serialPort.readString();

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
				publish(Messages.SerialWorker_10);
				explainSerialPortException(e);
				break;
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
		
		try {
			serialPort.openPort();
			serialPort.setParams(9600, 8, 1, 0);

			int mask = SerialPort.MASK_BREAK | SerialPort.MASK_ERR;
			serialPort.setEventsMask(mask);

			serialPort.addEventListener(this);

			if( expectString("The Final Key", 1000) != null ) //$NON-NLS-1$
			{
				publish(Messages.SerialWorker_4);
			} else {
				//Try logging out.
				serialPort.writeByte( (byte)'q');

				if( expectString("The Final Key", 1000) != null ) //$NON-NLS-1$
				{
					publish(Messages.SerialWorker_4);
				} else {
					publish(Messages.SerialWorker_5);
					disconnect();
					return null;
				}
			}
			
			String res;
			String banner = ""; //$NON-NLS-1$
			if( (res = expectString( "Pass:", 0 )) != null ) //$NON-NLS-1$
			{
				publish("" ); //$NON-NLS-1$
				if( res.indexOf('{') != -1 && res.indexOf('}') != -1 )
				{
					banner = res.substring( res.indexOf('{')+1, res.lastIndexOf('}'));
					publish(Messages.SerialWorker_17 );
					FkManager.getInstance().setCurrentBanner( banner );
					publish(banner);
				}
				publish(""); //$NON-NLS-1$
				publish(Messages.SerialWorker_7);
				postStateChange(SerialState.Working);
			} else {
				publish(Messages.SerialWorker_8);
				disconnect();
				return null;
			}

			enterString(pass);

			serialPort.writeByte( (byte)13 );
			pass = ""; //$NON-NLS-1$

			Boolean granted = false;
			Boolean gotKbLayout=false;
			String keyboard = null;
			StringBuilder sb = new StringBuilder();
			int timeOut=0;
			while(true)
			{
				if( serialPort.getInputBufferBytesCount() > 0 )
				{
					sb.append( serialPort.readString());

					String s = sb.toString();

					if( s.contains( "[Denied]") ) //$NON-NLS-1$
					{
						publish(Messages.SerialWorker_12);
						publish(Messages.SerialWorker_14);
						disconnect();
						return null;
					} else if( s.contains("[Granted]") ) //$NON-NLS-1$
					{
						granted=true;
						publish(Messages.SerialWorker_11);
					}

					if( granted )
					{
						if( s.contains("[Keyboard: ") ) //$NON-NLS-1$
						{
							gotKbLayout=true;
						}

						if(gotKbLayout)
						{
							//Look for the ] after "[Keyboard: "
							String ks = s.substring( s.indexOf("[Keyboard: ") + 11 ); //$NON-NLS-1$
							if( ks.contains("]") ) //$NON-NLS-1$
							{
								keyboard = ks.substring(0, ks.indexOf("]" ) ); //$NON-NLS-1$
								//Older firmware had a dash between country and platform (US-PC) when reporting the language.
								keyboard=keyboard.replace("-", ""); //$NON-NLS-1$ //$NON-NLS-2$
								publish(Messages.SerialWorker_28 + keyboard );
								FkManager.getInstance().setCurrentLayout(keyboard);
								break;
							}
						} else {
							Thread.sleep(10);
							timeOut++;
							if(timeOut == 10 )
							{
								publish(Messages.SerialWorker_29);
								break;
							}
						}

					}

				} else {
					Thread.sleep(25);
				}
			}

			publish(Messages.SerialWorker_13);
			serialPort.writeByte( (byte)'X'); //Machine commands with uppercase X
			expectString("[auto]", 200); //$NON-NLS-1$
			serialPort.writeByte( (byte)'l'); //Full list 

			String accounts = new String();

			timeOut = 10000;
			while(true)
			{
				if( serialPort.getInputBufferBytesCount() > 0 )
				{
					accounts += serialPort.readString();
					String sub = accounts.substring( accounts.length()-3 );
					if( sub.equals("\r\n>") ) //$NON-NLS-1$
					{
						accounts = accounts.substring( 0, accounts.length()-3 );
						break;
					}
				} else {
					Thread.sleep(10);
					timeOut-=10;
					if(timeOut < 1)
					{
						publish(Messages.SerialWorker_16);
						disconnect();
						return null;
					}
				}
			}
			
			//Trim first 3
			accounts = accounts.substring(3);

			String[] lines = accounts.split( "\r\n" ); //$NON-NLS-1$

			Boolean kbList=false;
			for(String l:lines)
			{
				if( ! kbList )
				{
					if( l.compareTo("[KBL]") == 0 ) //$NON-NLS-1$
					{
						kbList=true;
						publish(""); //$NON-NLS-1$
						publish(Messages.SerialWorker_31);
					} else {
						String ac = l.substring(0,2);
						String an = l.substring(2);
						FkManager.getInstance().listAddAcc(ac, an);
					}
				} else {
					//Next entries are supported keyboard layouts
					publish(Messages.SerialWorker_3 + FkManager.getInstance().addAvailableLayout(l) );
				}
			}

		}
		catch (Exception ex){
			publish(Messages.SerialWorker_32 );

			if( ex instanceof SerialPortException )
			{
				explainSerialPortException(ex);
			} else {
				publish(Messages.SerialWorker_33);
				publish(ex.getLocalizedMessage());
			}
			
			disconnect();
		}

		if( state != SerialState.Disconnected )
		{
			publish(""); //$NON-NLS-1$
			if( FkManager.getInstance().getList().size()==1 )
			{
				publish(Messages.SerialWorker_35);
			} else {
				publish(FkManager.getInstance().getList().size()+Messages.SerialWorker_36);
			}

			state = SerialState.Connected;

			postStateChange( state );
			publish(Messages.SerialWorker_23);
		}

		return null;
	}


	private void explainSerialPortException(Exception ex) {
		String ext = ((SerialPortException)ex).getExceptionType();

		if( ext.compareTo(SerialPortException.TYPE_PORT_ALREADY_OPENED) == 0)
		{
			publish(Messages.SerialWorker_37+dev+Messages.SerialWorker_38);
			publish(Messages.SerialWorker_39);
		} else if( ext.compareTo(SerialPortException.TYPE_PORT_NOT_FOUND) == 0 )
		{
			publish(Messages.SerialWorker_40+dev+Messages.SerialWorker_41);
			publish(Messages.SerialWorker_42);

		} else if( ext.compareTo(SerialPortException.TYPE_PERMISSION_DENIED) == 0 )
		{
			publish(Messages.SerialWorker_43+dev+Messages.SerialWorker_44);
			publish(Messages.SerialWorker_45);
		} else if( ext.compareTo(SerialPortException.TYPE_PORT_NOT_OPENED) == 0 )
		{
			publish(Messages.SerialWorker_46);
			publish(Messages.SerialWorker_47);
		} else {
			publish(Messages.SerialWorker_48+ex.getMessage());
			publish(Messages.SerialWorker_49);
			publish(Messages.SerialWorker_50);
		}
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
