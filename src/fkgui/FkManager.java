package fkgui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import jssc.SerialPort;

import org.eclipse.swt.widgets.Display;

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

	public Vector<String> getAvailableLayouts()
	{
		return(supportedLayouts);
	}
	
	public String addAvailableLayout( String str )
	{
		//available formats are in order and of the form: '  N. Layout' so we cut
		//space space N dot and space.
		String l = str.substring(5);
		supportedLayouts.add(l);
		return(l);
	}
	
	public void setLayout(int num)
	{
		
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

	public void setCurrentBanner(String _banner) {
		banner = _banner;
		
	}
	
	public String getBanner()
	{
		return(banner);
	}


}
