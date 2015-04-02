package fkgui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import jssc.SerialPort;
import jssc.SerialPortException;

import org.eclipse.swt.widgets.Display;
import com.sun.corba.se.impl.ior.ByteBuffer;

import fkgui.FkActionEventListener.FkActionEvent;
import fkgui.FkActionEventListener.FkActionEventType;

public class FkManager implements ActionListener {
	private static FkManager instance = null;
	private SerialWorker com = null;
	static final char ENTER_KEY = (char)13;
	static final char PAUSE_CODE = (char)23; 
	static final char SPACE_KEY = (char)32;
	private Comparator<Account> sortMethod = null;
	
	public class Account
	{
		public String name;
		public String num;
		private Boolean showNumInName;
		public Account( String acNum, String acName )
		{
			name = acName;
			num = acNum;
		}

		//Used by the ListView
		public String toString()
		{
			if( showNumInName )
			{
				return( "["+num+"] "+name);
			}
			return(name);
		}

		public Account showNumInName(Boolean showAccountId) {
			showNumInName=showAccountId;
			return this;
		}
	}
		
	private Vector<Account> list;
	private String banner = "Noname";
	private String keyLayout = "USPC";
	private Vector<String> supportedLayouts;
	
	protected FkManager()
	{
		list = new Vector<Account>(256);
		supportedLayouts = new Vector<String>(4);
	}
	
	public static FkManager getInstance()
	{
		if( instance == null )
		{
			instance = new FkManager();
		}

		return( instance );
	}
	
	public void setWorker( SerialWorker sw )
	{
		com = sw;
	}
	

	private class FkActionEventMsg implements Runnable  {
		private FkActionEventListener delegate;
		private FkActionEvent event;

		private FkActionEventMsg(FkActionEventListener d, FkActionEventType t, String data, Account acc, char act )
		{
			delegate=d;
			event=new FkActionEvent(t, data, acc,act) ;
		}
		@Override
		public void run() {
			if( delegate != null )
			{
				delegate.fkActionEvent(event);
			}
		}
	}
	
	
	private Boolean checkState()
	{
		int t=0;
		String msg="";
		Boolean stateOk=false;

		try {
			com.serialPort.writeByte((byte)SPACE_KEY);
			while( t < 100 )
			{
				t++;
				Thread.sleep(5);

				if( com.serialPort.getInputBufferBytesCount() > 0 )
				{
					msg += com.serialPort.readString();
					if( msg.endsWith("\r\n>") )
					{
						stateOk=true;
						msg="";
						break;
					}
				}
				
			}
		
		} catch(Exception e)
		{
			stateOk=false;
		}
		flushSerial();
		if( !stateOk )
		{
			return(false);
		}
		return(true);
		
	}
	
	private class TrigTask implements Runnable
	{
		private Account acc;
		private char action;
		private FkActionEventListener delegate;

		public TrigTask(Account ac, char act, FkActionEventListener d )
		{
			acc=ac;
			action=act;
			delegate=d;
		}
		
		@Override
		public void run() {
			
			int t=0;
			String msg = "";


			try {
			
				//First check that we get a prompt
				if( !checkState() )
				{
					Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.STATE_ERROR, "EXCEPTION",acc,action) );
					return;
				}

				//Drain any previous input.
				while( com.serialPort.getInputBufferBytesCount() > 0 )
				{
					com.serialPort.readBytes();
				}
				
				//if it's not '%' we need to type which action.
				if(action != '%' )
				{
	
						//If the action is to delete or override, we start by deleting the account number.
						//Those commands are xd or xo so we type an x before the action.
						if( action == 'd' || action == 'o' )
						{
							com.serialPort.writeByte((byte)'x');
						}
						com.serialPort.writeByte((byte)action);
				}

				com.serialPort.writeBytes(acc.num.toLowerCase().getBytes());
				Thread.sleep(100);

				//Verify that the device asks confirmation about the account number and action we requested, else return STATE_UNEXPECTED_ACTION_RESULT
				//Wait until a ?\r\n is found
				//Since we flushed the buffer, all we should see is an echo 
				t=0;
				while( t < 2 )
				{
					t++;
					Thread.sleep(50);
					if( com.serialPort.getInputBufferBytesCount() > 0 )
					{
						msg += com.serialPort.readString();

						if( msg.contains(" ?\r\n") )
						{
							System.out.println("Got '"+msg+"'");

							String expect;

							if(action=='d'||action=='o')
							{
								expect = "x"+action+"%"+acc.num.toLowerCase();

								if(action=='d')
								{
									expect += "\r\nDel "+acc.name+" [y/n] ?\r\n";
								} else {
									expect += "\r\nOverride "+acc.name+" [y/n] ?\r\n";
								}
							} else if(action=='%')
							{
								expect=acc.num.toLowerCase()+"\r\n\r\n[U][S][P] "+acc.name+" ?\r\n";
							} else {
								expect=""+action+"%"+acc.num.toLowerCase();
								String actStr = (""+action).toUpperCase();
								if(actStr.compareTo("S")==0)
								{
									actStr="SHOW";
								}
								expect += "\r\n\r\n\r\n["+actStr+"] "+acc.name+" ?\r\n";
							}

							String cut = msg.substring(0, expect.length());

							if( cut.compareTo(expect ) != 0 )
							{
								System.out.println("Expected: '"+expect+"' Got: '"+cut+"'");
								msg="Error: unexpected reply, disconnect it and close this program, this indicates either hardware-error or another program trying to communicate with it.";
								Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.UNEXPECTED_ACTION_RESULT_ERROR, msg, acc, action) );
								com.serialPort.writeByte((byte)9); //tab is unsupported in all inputs, should cause the device to go into a state where q will lock it.
								com.serialPort.writeByte((byte)'q');
								com.serialPort.closePort();
								return;
							}
						}
					}
				}

				if( action == 'd' || action == 'o' )
				{
					com.serialPort.writeByte((byte)'y');
					Thread.sleep(100);
				}

				t=0;
				int waitfor =  ((action=='o'||action=='d')?200:700);
				while( t < waitfor )
				{
					t++;
					Thread.sleep(50);
					if( com.serialPort.getInputBufferBytesCount() > 0 )
					{
						msg += com.serialPort.readString();
						//System.out.println( msg );
						if( msg.contains("[done]") || msg.contains("[deleted]") )
						{
							if( action == 'd' )
							{
								list.remove( acc );
							}
							Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_OKAY, msg, acc, action) );
							return;
						} else if( msg.contains("[abort]") )
						{
							//System.out.println("FkManager: Action Abort");;
							Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_ABORTED, msg,acc,action) );
							return;
						}
					}
				}
				Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_ERROR, msg,acc,action) );

			} catch (Exception e1) {
				System.out.println("TrigTask Exception:");
				e1.printStackTrace();
				Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_ERROR, "EXCEPTION",acc,action) );
			}
			
		}
		
	}
	
	public void trig(Account acc, char action, FkActionEventListener delegate)
	{
		TrigTask trigTask = new TrigTask(acc,action,delegate);
		new Thread(trigTask).start();
	}
	
	
	
	private class SortByName implements Comparator<Account>
	{
		@Override
		public int compare(Account o1, Account o2) {
			return o1.name.compareTo(o2.name);
		}
	}
	
	private class SortById implements Comparator<Account>
	{
		@Override
		public int compare(Account o1, Account o2) {
			int a = Integer.valueOf(o1.num, 16);
			int b = Integer.valueOf(o2.num, 16);
			return( a - b );
		}
	}
	
	
	
	public void listAddAcc(String num, String name)
	{
		list.addElement( new Account(num,name) );
		
		Collections.sort(list, sortMethod);
	}
	
	public void listClear()
	{
		list.clear();
	}
	
	//Return the full list
	public Vector<Account> getList()
	{
		return(list);
	}
	
	//Return a vector of accounts matching the keyword
	public Vector<Account> getList(String key)
	{
		Vector<Account> res = new Vector<Account>();
		String k = key.toLowerCase();
		for( Account a : list )
		{
			if( a.name.toLowerCase().contains( k ) ) 
			{
				res.add(a);
			}
		}
		
		return(res);
	}

	public String[] getAvailableLayouts()
	{
		String[] layouts = supportedLayouts.toArray(new String[supportedLayouts.size()]);
		return(layouts);
	}
	
	public String addAvailableLayout( String str )
	{
		//available formats are in order and of the form: '  N. Layout' so we cut
		//space space N dot and space.
		String l = str.substring(5);
		supportedLayouts.add(l);
		return(l);
	}
	


	@Override
	public void actionPerformed(ActionEvent e) {
		Account f=null;
		for( Account a : list )
		{
			if( a.num.compareTo( e.getActionCommand().substring(1, 3)) == 0 )
			{
				f=a;
				break;
			}
		}
		if(f!=null)
		{
			trig(f, e.getActionCommand().charAt(0), null);
		}
	}

	public void setCurrentLayout(String str) {
		System.out.println("FkManager: Got current layout:" + str );
		keyLayout =str;
	}

	public String getCurrentLayout()
	{
		return( keyLayout );
	}

	private class NewAccountTask implements Runnable
	{
		private String seq;
		private Account acc;
		FkActionEventListener delegate;
		public NewAccountTask(String sequence, FkActionEventListener d, String accountName)
		{
			seq=sequence;
			delegate=d;
			acc = new Account( "00", accountName );
			System.out.println("NewAccountTask ctor");
		}
		@Override
		public void run() {
			System.out.println("NewAccountTask run");
			Boolean noTimeOut = false;

			//First check that we get a prompt
			if( !checkState() )
			{
				Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.STATE_ERROR, "EXCEPTION",acc,'A') );
				return;
			}
			
			String data="";
			int timeOut;
			try {			
			//First type x a and wait for account title
				
				com.serialPort.writeByte((byte)'x');
				com.serialPort.writeByte((byte)'a');
				Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_WAITING, "WAITING",acc,'A') );

				timeOut = 6000;
				while(timeOut > 0)
				{
					if( com.serialPort.getInputBufferBytesCount() > 0 )
					{
						String in = com.serialPort.readString(); 
						data += in;
						System.out.println("Datain:" +in);
						if( data.contains("Account Title, (0-31):") )
						{
							System.out.println("Found");
							break;
						}
					} else {
						timeOut -= 50;
						Thread.sleep(50);
					}
				}
				
				if(timeOut > 0 )
				{
					Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_WORKING, "WORKING",acc,'A') );

					for(int p=0; p < seq.length();p++)
					{

						if( com.serialPort.getOutputBufferBytesCount() > 1 )
						{
							Thread.sleep(100);
							System.out.println("[SLEEP]");
						} else {
							if( seq.charAt(p) == PAUSE_CODE)
							{
								Thread.sleep(100);
								System.out.println("[DELAY]");
							} else {
								if( seq.charAt(p) == ENTER_KEY )
								{
									System.out.println( "[ENTER]" );
								} else {
									System.out.println( "Type from pos ("+p+"):" + seq.charAt(p));
								}
	
								com.serialPort.writeByte( (byte)seq.charAt(p) );
								Thread.sleep(5);
							}
							
						}
						
						if(  com.serialPort.getInputBufferBytesCount() > 0 )
						{
							System.out.print("Reading "+com.serialPort.getInputBufferBytesCount()+ " bytes: ");
							data="";
							while( com.serialPort.getInputBufferBytesCount() > 0 )
							{
								String in = com.serialPort.readString();
								data += in;
								
							}
							if( data.contains("[generate]") )
							{
								noTimeOut=true;
								System.out.println("1: noTimeOut");
							}							
							System.out.println(data);
						}
					}//While typing
					System.out.println("All chars typed, waiting for [save entry ");
					
					int step=0;
					
					timeOut = 30000;
					while( timeOut > 0 || noTimeOut )
					{
						
						if( com.serialPort.getInputBufferBytesCount() > 0 )
						{
							String in = com.serialPort.readString();
							//System.out.println("Read>"+in);
							data += in;
							System.out.println("Data:"+data);
							
							//Check for abort first, in that case, we don't want to do anything.
							if( data.contains("[abort]") )
							{
								Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_ABORTED, "ERROR:"+data,null,'A') );
								return;
							}
							
							//If we're generating a password, we don't want to timeout
							if( data.contains("[generate]") )
							{
								noTimeOut=true;
								System.out.println("2: noTimeOut");
								
							}
							
							//If we're at step0 and have the saveentry string and there's a ] after it, we're good to fetch the account number.
							if( step==0 && data.contains("[save entry ") && (data.lastIndexOf(']') > data.lastIndexOf("[save entry ")) )
							{
								int begin = data.lastIndexOf("[save entry ")+12;
								String subStr = data.substring(begin);
								int end = subStr.indexOf(']');
								subStr = subStr.substring(0,end);
								if( subStr.length()==1)
								{
									subStr = "0"+subStr;
								}
								acc.num = subStr;
								listAddAcc( acc.num, acc.name);
								System.out.println("Account: "+acc.num+" " + acc.name);
								
								//Cut data, so that the [done] step1 looks for will not be the one after [generate].
								data = data.substring(begin+end);
								
								System.out.println("Found [save entry, looking for done.");
								step++;
							}
							
							if( step==1 && data.contains("[done]") )
							{
								System.out.println("Found [done] in: {"+data+"}");
								Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_OKAY, "DONE:"+data,acc,'A') );
								return;
							}

						} else {
							Thread.sleep(50);
							timeOut -= 50;
						}
					} //While !timeout 
				} //If !timeout
				
				Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_ABORTED, "TIMEOUT:"+data,null,'A') );

			} catch (Exception e) {
				Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_ERROR, "EXCEPTION",null,'A') );
			}
			
		}

		
	}

	private void flushSerial() {
		try {
			Thread.sleep(10);
			com.serialPort.purgePort(SerialPort.PURGE_RXCLEAR | SerialPort.PURGE_TXCLEAR);
			
			System.out.println("Flushed "+com.serialPort.getInputBufferBytesCount()+" bytes.");
			while( com.serialPort.getInputBufferBytesCount() > 0 )
			{
				com.serialPort.readBytes();
			}
			
		} catch(Exception e)
		{
			System.out.println("FkManager flushSerial Exception:"+e.getMessage() );
		}
		
	}	
	
	public void createAccount(String strAccountName, String strUserName,
			Boolean autoPassword, int autoPassLen, Boolean autoPassAllSpecials,
			String autoPassSpecials, String strPassword, Boolean seperatorTab,
			FkActionEventListener delegate) {
			
			StringBuilder sb = new StringBuilder(256);
		
			
			
			sb.append(strAccountName);
			sb.append(ENTER_KEY);
			sb.append(PAUSE_CODE);
			sb.append(strUserName);
			sb.append(ENTER_KEY);
			sb.append(PAUSE_CODE);
			
			if( autoPassword )
			{
				sb.append('2'); //Select automatic password
				sb.append(PAUSE_CODE);
				sb.append(autoPassLen);
				sb.append(ENTER_KEY);

				
				if( autoPassAllSpecials )
				{
					sb.append('1'); //Allow all characters
				} else {
					if( autoPassSpecials.length() > 0 )
					{
						sb.append('2'); //Allow only characters specified below
						sb.append(PAUSE_CODE);
						sb.append(autoPassSpecials);
						sb.append(ENTER_KEY);
					} else {
						sb.append('3'); // Allow no special characters
					}
				}
			} else {
				//Manual entered password
				sb.append('1'); // Select manual password
				sb.append(PAUSE_CODE);
				sb.append(strPassword);
				sb.append(ENTER_KEY);
			}
			
			sb.append(PAUSE_CODE);

			//Tab/Enter sep
			if( seperatorTab )
			{
				sb.append('1'); //Select tab seperator
			} else {
				sb.append('2'); //Select enter seperator
			}
			
			String seq = sb.toString();
			
			System.out.println("Seq ["+seq.replace(PAUSE_CODE, 'ø').replace(ENTER_KEY, 'å')+"]");
			
			NewAccountTask newTask = new NewAccountTask(seq, delegate, strAccountName);
			new Thread(newTask).start();			
		
	}

	public void sortById(boolean byId) {
		if( byId )
		{
			sortMethod = new SortById();
		} else {
			sortMethod = new SortByName();
		}
		if(list != null )
		{
			Collections.sort(list, sortMethod);
		}
	}

	public void setBanner(String _banner) {
		banner = _banner;
		
	}
	
	public String getBanner()
	{
		return(banner);
	}
	
	public boolean isStringValidForFk(String str)
	{

		char passChars[] = {
				'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E',
				'F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T',
				'U','V','W','X','Y','Z','a','b','c','d','e','f','g','h','i',
				'j','k','l','m','n','o','p','q','r','s','t','u','v','w','x',
				'y','z','!','"','#','$','%','&','@','?','(',')','[',']','-',
				'.',',','+','{','}','_','/','<','>','=','|','\'','\\', 
				';',':',' ','*'// <- 92 (idx 91)
				};


		int len = str.length();
		for(int i=0; i<len; i++)
		{
			boolean valid=false;

			for(char c : passChars)
			{
				if(str.charAt(i)==c)
				{
					valid=true;
				}
			}

			if(!valid)
			{
				System.out.println("FkManager.isStringValidForFk(); Invalid character '"+str.charAt(i)+"' at pos "+i+" in '"+str+"'");
				return false;
			}
		}

		return true;
	}

	
	
	///
	private class BannerTask implements Runnable
	{

		private FkActionEventListener delegate;
		private String bannerTxt;
		
		public BannerTask(String txt, FkActionEventListener d )
		{
			bannerTxt=txt;
			delegate=d;
		}
		
		@Override
		public void run() {
			
			int timeOut=0;
			String data="";


			try {
			
				//First check that we get a prompt
				if( !checkState() )
				{
					Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.STATE_ERROR, "EXCEPTION",null,'b') );
					return;
				}

				//Drain any previous input.
				while( com.serialPort.getInputBufferBytesCount() > 0 )
				{
					com.serialPort.readBytes();
				}
				
				com.serialPort.writeByte((byte)'x');
				com.serialPort.writeByte((byte)'b');
				Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_WAITING, "WAITING",null,'b') );

				timeOut = 6000;
				while(timeOut > 0)
				{
					if( com.serialPort.getInputBufferBytesCount() > 0 )
					{
						String in = com.serialPort.readString(); 
						data += in;
						System.out.println("Datain:" +in);
						if( data.contains("Banner (0-31):\r\n") )
						{
							System.out.println("Found");
							break;
						}
					} else {
						timeOut -= 50;
						Thread.sleep(50);
					}
				}
				

				if(timeOut > 0 )
				{
					Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_WORKING, "WORKING",null,'b') );

					for(int p=0; p < bannerTxt.length();p++)
					{
						com.serialPort.writeByte( (byte)bannerTxt.charAt(p) );
						Thread.sleep(5);
					}

					while( com.serialPort.getInputBufferBytesCount() > 0 )
					{
						com.serialPort.readBytes();
					}

					com.serialPort.writeByte( (byte)ENTER_KEY );

					timeOut = 3000;
					while(timeOut > 0)
					{
						if( com.serialPort.getInputBufferBytesCount() > 0 )
						{
							String in = com.serialPort.readString(); 
							data += in;
							System.out.println("Datain:" +in);
							if( data.contains("[done]\r\n") )
							{
								System.out.println("Found");
								break;
							}
						} else {
							timeOut -= 50;
							Thread.sleep(50);
						}
					}
				}

				if( timeOut > 0 )
				{

					Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_OKAY, "Saved:"+data,null,'b') );
				} else {
					Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_ABORTED, "TIMEOUT:"+data,null,'b') );
				}

			} catch(Exception e)
			{
				Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_ERROR, "EXCEPTION",null,'b') );
				return;
			}
		}

	}

	public void saveBanner(String bannerTxt, FkActionEventListener eventListener) {
		BannerTask bannerTask = new BannerTask(bannerTxt, eventListener);
		new Thread(bannerTask).start();
	}

	
	private class LayoutTask implements Runnable
	{

		private FkActionEventListener delegate;
		private String layout;
		
		public LayoutTask(int _Layout, FkActionEventListener d )
		{
			layout = ""+_Layout;
			delegate=d;
		}
		
		@Override
		public void run() {
			
			int timeOut=0;
			String data="";


			try {
			
				//First check that we get a prompt
				if( !checkState() )
				{
					Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.STATE_ERROR, "EXCEPTION",null,'k') );
					return;
				}

				//Drain any previous input.
				while( com.serialPort.getInputBufferBytesCount() > 0 )
				{
					com.serialPort.readBytes();
				}
				
				com.serialPort.writeByte((byte)'x');
				com.serialPort.writeByte((byte)'k');
				Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_WAITING, "WAITING",null,'k') );

				timeOut = 6000;
				while(timeOut > 0)
				{
					if( com.serialPort.getInputBufferBytesCount() > 0 )
					{
						String in = com.serialPort.readString(); 
						data += in;
						System.out.println("Datain:" +in);
						if( data.contains("Select keyboard layout:\r\n") && data.contains("\r\n% ") )
						{
							System.out.println("Found");
							break;
						}
					} else {
						timeOut -= 50;
						Thread.sleep(50);
					}
				}

				if(timeOut > 0 )
				{


					com.serialPort.writeByte( (byte)layout.charAt(0) );


					while( com.serialPort.getInputBufferBytesCount() > 0 )
					{
						com.serialPort.readBytes();
					}

					com.serialPort.writeByte( (byte)ENTER_KEY );

					timeOut = 3000;
					while(timeOut > 0)
					{
						if( com.serialPort.getInputBufferBytesCount() > 0 )
						{
							String in = com.serialPort.readString(); 
							data += in;
							System.out.println("Datain:" +in);
							if( data.contains("test.\r\n#") )
							{
								System.out.println("Found");
								break;
							}
						} else {
							timeOut -= 50;
							Thread.sleep(50);
						}
					}
				}

				timeOut = 30000;
				while(timeOut > 0)
				{
					if( com.serialPort.getInputBufferBytesCount() > 0 )
					{
						String in = com.serialPort.readString(); 
						data += in;
						System.out.println("Datain:" +in);
						if( data.contains("Correct [y/n] ?") )
						{
							Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_WORKING, "WORKING",null,'k') );
							System.out.println("Found");
							com.serialPort.writeByte((byte)'y');

							Thread.sleep(500); //sleep before returning the result, give the UI plenty of time to catch up.

							break;
						}
					} else {
						timeOut -= 50;
						Thread.sleep(50);
					}
				}				

				if( timeOut > 0 )
				{
					Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_OKAY, "Saved:"+data,null,'k') );
				} else {
					Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_ABORTED, "TIMEOUT:"+data,null,'k') );
				}

			} catch(Exception e)
			{
				Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_ERROR, "EXCEPTION",null,'k') );
				return;
			}
		}

	}
	
	public void saveLayout(int layout, FkActionEventListener eventListener) {
		LayoutTask lt = new LayoutTask(layout, eventListener);
		new Thread(lt).start();
	}
	
	
	private class BackupTask implements Runnable
	{

		private FkActionEventListener delegate;
		private File file;
		
		
		public BackupTask(File f, FkActionEventListener d )
		{
			file=f;
			delegate=d;
		}
		
		
		byte crc8( byte[] dat, int begin, int len )
		{
			int crc = 0;
			
			int idx=0;

			while( len-- != 0 )
			{
				int inbyte = dat[begin+idx];
				idx++;
				
				for(int i=8; i!=0; i--)
				{
					int mix =  (( crc ^ inbyte) & 0x01);
					crc >>=1;
					if( mix != 0 )
					{
						crc ^=0x8C;
					}
					inbyte >>=1;
				}
			}
			return((byte)crc);
		}
		
		@Override
		public void run() {
			
			int timeOut=0;
			String data="";

			int state=0;

			try {

				//First check that we get a prompt
				if( !checkState() )
				{
					Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.STATE_ERROR, "EXCEPTION",null,'e') );
					return;
				}

				//Drain any previous input.
				while( com.serialPort.getInputBufferBytesCount() > 0 )
				{
					com.serialPort.readBytes();
				}
				com.serialPort.writeByte((byte)'X');
				com.serialPort.writeByte((byte)'e');

				
				timeOut = 6000;
				
				ByteBuffer bin = new ByteBuffer( 67000 );

				while(timeOut > 0)
				{
					if( com.serialPort.getInputBufferBytesCount() > 0 )
					{
						
						int n = com.serialPort.getInputBufferBytesCount();
						while(n-->0)
						{
							bin.append( com.serialPort.readBytes(1)[0]);
						}


						data = new String(bin.toArray());

						if( state==0 && data.contains("[RDY]\r\n") )
						{
							Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_WAITING, "WAITING",null,'e') );

							state=1;
						}

						if( state==1 && data.contains("[OK]\r\n[BEGIN]" ) )
						{
							state=2;

							Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_WORKING, "Working",null,'e') );
						}

						if( state==2 )
						{
							timeOut=3000;
							if( data.contains("[END]") )
							{

								//data = data.substring(0, data.length()-5 );
								
								bin.trimToSize();
								
								byte[] arr = bin.toArray();
								
								byte[] clean = new byte[66000];
								
								state=0;
								int idx=0;
								int cc=0;
								String errors="";
								for(int i=0;i< arr.length; i++ )
								{
									//Find [BEGIN]
									if(state==0 && i+6 < arr.length)
									{
										if(arr[i] == '[' &&arr[i+1] == 'B' &&arr[i+2] == 'E' &&arr[i+3] == 'G' &&arr[i+4] == 'I' &&arr[i+5] == 'N' &&arr[i+6] == ']' )
										{
											i+=7;
											state=1;
											idx=0;
										}
									}
									
									if(state==1)
									{
										
										if( idx < 66000 )
										{
											clean[idx] = arr[i];
											cc++;
											
											if( cc==33  )
											{
												cc=0;

												byte c = crc8(clean,(idx-32),32);
												
												if( c != clean[idx] )
												{
													errors += "CRC Error: Bytes "+(idx-32)+" to "+(idx-1)+" have CRC "+c+" but the CRC sent from FinalKey in byte "+idx+" is "+clean[idx]+"\n";
												}
											}
											
											idx++;
										} else {
											if( arr[i] == '['  &&arr[i+1] == 'E' &&arr[i+2] == 'N' &&arr[i+3] == 'D' &&arr[i+4] == ']' )
											{
												FileOutputStream fout = new FileOutputStream( file );
												fout.write( clean );
												fout.close();
												
												if( errors.length() == 0 )
												{
													Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_OKAY, "Done" ,null,'e') );
												} else {
													Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_ERROR, errors ,null,'e') );
												}
												return;
											} else {
												Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.UNEXPECTED_ACTION_RESULT_ERROR, "Read past the end?" ,null,'e') );
											}
										}
									}

								}

								break;
							} else {
								Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.PROGRESS_UPDATE, ""+bin.size() ,null,'e') );
							}
						}

					} else {
						timeOut -= 50;
						Thread.sleep(50);
					}
				}
				Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_ABORTED, "Timed out" ,null,'e') );

			} catch (Exception e) { 

			}
		}
	}

	public void backup(File f, FkActionEventListener delegate) {
		BackupTask bt = new BackupTask(f, delegate );
		new Thread(bt).start();
	}

	private class RestoreTask implements Runnable
	{

		private FkActionEventListener delegate;
		private File file;

		public RestoreTask(File f, FkActionEventListener d )
		{
			file=f;
			delegate=d;
		}


		@Override
		public void run() {

			int state=0;
			long timeStamp=System.currentTimeMillis();

			String data="";

			byte[] bin = new byte[66000];

			boolean fileOk=false;
			FileInputStream in=null;
			try {
				in = new FileInputStream(file);

				int len = in.read(bin);
				if( len == 66000 )
				{
					fileOk=true;
				}

			} catch (Exception e1) {
				Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.FILE_ERROR, e1.getLocalizedMessage() ,null,'i') );
				return;
			} finally {
				try {
					if( in != null )
					{
						in.close();
					}
				} catch (IOException e) {}
			}

			String verify = new String( bin ).substring(0,9);

			if( !fileOk  || verify.compareTo("[FinalKey") != 0)
			{
				Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.FILE_ERROR, "Not a FinalKey backup file." ,null,'i') );
				fileOk=false;
			}

			if( !fileOk )
			{
				return;
			}

			try {

				//First check that we get a prompt
				if( !checkState() )
				{
					Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.STATE_ERROR, "EXCEPTION",null,'i') );
					return;
				}

				//Drain any previous input.
				while( com.serialPort.getInputBufferBytesCount() > 0 )
				{
					com.serialPort.readBytes();
				}
				com.serialPort.writeByte((byte)'X');
				com.serialPort.writeByte((byte)'i');


				int idx=0;
				while(System.currentTimeMillis() - timeStamp < 6000L)
				{
					if( com.serialPort.getInputBufferBytesCount() > 0 )
					{
						data += com.serialPort.readString();
					}
					if( state==0 && data.contains("[RDY]\r\n") )
					{
						Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_WAITING, "WAITING",null,'i') );
						data="";
						state=1;
					}

					if( state==1 && data.contains("[NO]") )
					{
						Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_ABORTED, "",null,'i') );
						return;
					}

					if( state==1 && data.contains("[OK]\r\n") )
					{
						Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_WORKING, "WORKING",null,'i') );
						state=2;

						//Don't reset data, state2 may need some of it
					}

					//Catch E = END, F=Fail, C=CRC Fail O=CRC OK
					if( data.contains("E") )
					{
						Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_OKAY, "Done.",null,'i') );
						return;
					}

					if( data.contains("F") )
					{
						Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_ERROR, "Device reported read-error.",null,'i') );
						return;
					}

					if( data.contains("C") )
					{
						Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_ERROR, "Device reported CRC-error.",null,'i') );
						return;
					}

					//Device ready for more data
					if( state==2 && data.contains("R") )
					{
						data="";
						timeStamp = System.currentTimeMillis();

						byte[] outBuf = new byte[33];

						for(int i=0;i<33;i++)
						{
							outBuf[i] = bin[idx++];
						}

						com.serialPort.writeBytes(outBuf);

						Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.PROGRESS_UPDATE, ""+idx ,null,'i') );

						state=3;
					}

					if( state == 3 && data.contains("W") && data.contains("O") )
					{
						state=2;
					}

				}
			} catch(Exception e)
			{
				Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_ERROR, "Exception: "+e.getLocalizedMessage(),null,'i') );
				return;

			}

			Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_ERROR, "No data from device.",null,'i') );

		}
	}

	public void restore(File f, FkActionEventListener delegate) {
		RestoreTask rt = new RestoreTask(f, delegate );
		new Thread(rt).start();
	}

	public void disconnect() {
		com.disconnect();
		supportedLayouts.clear();
		list.clear();
	}
	
	private class ChangePassTask implements Runnable
	{

		private FkActionEventListener delegate;
		private String curPass;
		private String newPass;

		public ChangePassTask(String _curPass, String _newPass, FkActionEventListener d )
		{
			curPass = _curPass;
			newPass = _newPass;
			delegate=d;
		}


		@Override
		public void run() {
		
			Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_WORKING, "1",null,'p') );
			
			String data="";
			int state=0;
			//First check that we get a prompt
			if( !checkState() )
			{
				Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.STATE_ERROR, "EXCEPTION",null,'p') );
				return;
			}

			//Drain any previous input.
			try {
				while( com.serialPort.getInputBufferBytesCount() > 0 )
				{
					com.serialPort.readBytes();
				}
				com.serialPort.writeByte((byte)'x');
				com.serialPort.writeByte((byte)'p');
				
				long timeStamp=System.currentTimeMillis();
								
				
				while(System.currentTimeMillis() - timeStamp < 15000L)
				{
					
					while( com.serialPort.getInputBufferBytesCount() > 0 )
					{
						data += com.serialPort.readString();
						if( state==0 && data.contains("Are you sure [y/n] ?") )
						{
							data="";
							com.serialPort.writeByte((byte)'y');
							Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_WAITING, "WAITING",null,'p') );
							state++;
						}

						if( state==1 && data.contains("Current psw:"))
						{
							timeStamp=System.currentTimeMillis();
							Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_WORKING, "WORKING",null,'p') );
							data="";
							com.serialPort.writeBytes( curPass.getBytes() );
							com.serialPort.writeByte( (byte) ENTER_KEY );
							state++;
						}
						
						if( state==2 && data.contains("New psw:") )
						{
							data="";
							com.serialPort.writeBytes( newPass.getBytes() );
							com.serialPort.writeByte( (byte) ENTER_KEY );
							state++;
						}
						if( state==3 && data.contains("Repeat:") )
						{
							data="";
							com.serialPort.writeBytes( newPass.getBytes() );
							com.serialPort.writeByte( (byte) ENTER_KEY );
							state++;

						}
						
						if( state==4 && data.contains("Changing password.")&& data.contains("Encrypting:") )
						{
							Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_WORKING, "next",null,'p') );
							data="";
							state++;
						}
						
						if( state==5 && data.contains("/255") )
						{
							timeStamp=System.currentTimeMillis();
							String p=data.substring( 1, data.indexOf('/') );
							Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.PROGRESS_UPDATE, p ,null,'p') );
							data="";
						}
						
						if( state==5 && data.contains("[done]") )
						{
							Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_OKAY, "",null,'p') );
							return;
						}
	
						if( state== 1 && data.contains("[abort]") )
						{
							Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_ABORTED, "",null,'p') );
							return;
						}

						if( state == 2 && data.contains("[abort]") )
						{
							Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_ERROR, "Current password was not accepted.",null,'p') );
							return;
						}
					}


				}

				Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_ERROR, "Timeout ",null,'p') );

			} catch (SerialPortException e) {
				Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.STATE_ERROR, "exception:"+e.getLocalizedMessage(),null,'p') );
				return;
			}

		}
	}

	public void changePass(String currentPass, String newPass, FkActionEventListener delegate) {
		ChangePassTask ct = new ChangePassTask(currentPass, newPass, delegate);
		new Thread(ct).start();
	}
	

	private class FormatTask implements Runnable
	{

		private FkActionEventListener delegate;
		private String curPass;
		private String newPass;
		private String newLayout;
		private String newBanner;

		public FormatTask(String _curPass, String _newPass, String nl, String nb, FkActionEventListener d )
		{
			newBanner=nb;
			newLayout=nl;
			curPass = _curPass;
			newPass = _newPass;
			delegate=d;
		}


		@Override
		public void run() {
		
			Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_WORKING, "1",null,'f') );
			
			String data="";
			int state=0;
			//First check that we get a prompt
			if( !checkState() )
			{
				Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.STATE_ERROR, "EXCEPTION",null,'f') );
				return;
			}

			//Drain any previous input.
			try {
				while( com.serialPort.getInputBufferBytesCount() > 0 )
				{
					com.serialPort.readBytes();
				}
				com.serialPort.writeByte((byte)'x');
				com.serialPort.writeByte((byte)'f');

				long timeStamp=System.currentTimeMillis();

				while(System.currentTimeMillis() - timeStamp < 15000L)
				{

					while( com.serialPort.getInputBufferBytesCount() > 0 )
					{
						data += com.serialPort.readString();

						if( state==0 && data.contains("Are you sure [y/n] ?") )
						{
							data="";
							com.serialPort.writeByte((byte)'y');
							Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_WAITING, "press1",null,'f') );
							state++;
						}

						if( state==1 && data.contains("Current psw:"))
						{
							timeStamp=System.currentTimeMillis();
							Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_WORKING, "WORKING",null,'f') );
							data="";
							com.serialPort.writeBytes( curPass.getBytes() );
							com.serialPort.writeByte( (byte) ENTER_KEY );
							state++;
						}

						if( state==2 && data.contains("Psw:") )
						{
							data="";
							com.serialPort.writeBytes( newPass.getBytes() );
							com.serialPort.writeByte( (byte) ENTER_KEY );
							state++;
						}

						if( state==3 && data.contains("Repeat:") )
						{
							data="";
							com.serialPort.writeBytes( newPass.getBytes() );
							com.serialPort.writeByte( (byte) ENTER_KEY );
							state++;
						}

						if( state==4 && data.contains("Name, (0-31):\r\n"))
						{
							com.serialPort.writeBytes( newBanner.getBytes() );
							com.serialPort.writeByte( (byte) ENTER_KEY );
							//
							data="";
							state++;
						}

						if( state==5 && data.contains("Select keyboard layout:\r\n") && data.contains("% ") )
						{
							timeStamp=System.currentTimeMillis()+15000;
							Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_WAITING, "WAITING",null,'f') );

							
							
							com.serialPort.writeBytes( newLayout.getBytes() );

							data="";
							state++;
						}

						if( state==6 && data.contains("Correct [y/n] ?") )
						{
							com.serialPort.writeByte( (byte) 'y' );
							state++;
						} else if( state==6 && data.contains("[skip test]") )
						{
							state++;
						}
						
						if( state==7 && data.contains("Formatting:") )
						{
							Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_WORKING, "next",null,'f') );
							data="";
							state++;
						}
						
						if( state==8 && data.contains("/255"))
						{
							timeStamp=System.currentTimeMillis();
							String p=data.substring( 1, data.indexOf('/') );
							Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.PROGRESS_UPDATE, p ,null,'f') );
							data="";
						}
						
						if( state==8 && data.contains("[Done]") )
						{
							Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_OKAY, "",null,'p') );
							return;
						}
						
						

						
						if( state== 1 && data.contains("[abort]") )
						{
							Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_ABORTED, "",null,'f') );
							return;
						}

						if( state == 2 && data.contains("[lock]") )
						{
							Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_ERROR, "Current password was not accepted.",null,'F') );
							return;
						}
					}


				}

				Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_ERROR, "Timeout ",null,'f') );

			} catch (SerialPortException e) {
				Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.STATE_ERROR, "exception:"+e.getLocalizedMessage(),null,'f') );
				return;
			}

		}
	}

	public void format(String currentPass, String newPass, String newLayout, String newBanner, FkActionEventListener delegate) {
		FormatTask ft = new FormatTask(currentPass, newPass, newLayout, newBanner, delegate);
		new Thread(ft).start();
	}

	public String getCurrentLayoutIndex(String l) {
		
		return ""+(supportedLayouts.indexOf( l )+1);
	}

}
