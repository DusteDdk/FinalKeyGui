package fkgui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Comparator;
import java.util.Vector;
import org.eclipse.swt.widgets.Display;

import fkgui.FkActionEventListener.FkActionEvent;
import fkgui.FkActionEventListener.FkActionEventType;

public class FkManager implements ActionListener {
	private static FkManager instance = null;
	private SerialWorker com = null;

	private Comparator<Account> sortMethod = null;
	
	public class Account
	{
		public String name;
		public String num;
		
		public Account( String acNum, String acName )
		{
			name = acName;
			num = acNum;
		}
		
		public String toString()
		{
			return(name);
		}
	}
		
	private Vector<Account> list;
	
	protected FkManager()
	{
		list = new Vector<Account>(256);
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
			
			flushSerial();
			
			if(action != '%' )
			{
				try
				{
					
					if( action == 'd' || action == 'o' )
					{
						com.serialPort.writeByte((byte)'x');
					}
					com.serialPort.writeByte((byte)action);
				} catch( Exception ex )
				{
					ex.printStackTrace();
				}
			}
			
			try {
				com.serialPort.writeBytes(acc.num.toLowerCase().getBytes());
		
				if( action == 'd' || action == 'o' )
				{
					com.serialPort.writeByte((byte)'y');
				}				
				
				try {
					Thread.sleep(200);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				int t=0;

				String msg = "";
				while( t < 1200 )
				{
					t++;
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
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
				// TODO Auto-generated catch block
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
		
		list.sort( sortMethod );
	}
	
	public void listClear()
	{
		list.clear();
	}
	
	public Vector<Account> getList()
	{
		return(list);
	}
	
	public void getCurrentLayout()
	{
		
	}
	
	public void getAvailableLayouts()
	{
		
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

			flushSerial();
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
							Thread.sleep(50);
						} else {
							com.serialPort.writeByte( (byte)seq.charAt(p) );
							Thread.sleep(5);
							
						}
						System.out.println( "Type:" + seq.charAt(p));
						while( com.serialPort.getInputBufferBytesCount() > 0 )
						{
							String in = com.serialPort.readString();
							data += in;
							System.out.println("Read:" + in);
						}
					}
					System.out.println("All chars typed, waiting for [done]");
					
					timeOut = 30000;
					while( timeOut > 0 )
					{
						timeOut -= 50;
						Thread.sleep(50);
						if( com.serialPort.getInputBufferBytesCount() > 0 )
						{
							String in = com.serialPort.readString();
							System.out.println("Read>"+in);
							data += in;
							if( data.contains("[done]") )
							{
								int begin = data.lastIndexOf("[save entry ")+12;
								String subStr = data.substring(begin);
								int end = subStr.indexOf("]");
								subStr = subStr.substring(0,end);
								if( subStr.length()==1)
								{
									subStr = "0"+subStr;
								}
								acc.num = subStr;
								listAddAcc( acc.num, acc.name);
								System.out.println("Account: "+acc.num+" " + acc.name);
								Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_OKAY, "DONE:"+data,acc,'A') );
								break;
							} else if( data.contains("[abort]") )
							{
								Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_ABORTED, "ERROR:"+data,null,'A') );
								break;
							}
						}
					}
					if(timeOut < 1)
					{
						Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_ABORTED, "TIMEOUT1:"+data,null,'A') );
					}
				} else {
					Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_ABORTED, "TIMEOUT2:"+data,null,'A') );
				}
				
				
			} catch (Exception e) {
				Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_ERROR, "EXCEPTION",null,'A') );
			}
			
		}

		
	}

	private void flushSerial() {
		try {
		Thread.sleep(10);
		
		while( com.serialPort.getInputBufferBytesCount()!=0 )
		{
			System.out.println("Flushed "+com.serialPort.getInputBufferBytesCount()+" bytes >>>" + com.serialPort.readString() + "<<<");
		}
		} catch(Exception e)
		{
			
		}
		
	}	
	
	public void createAccount(String strAccountName, String strUserName,
			Boolean autoPassword, int autoPassLen, Boolean autoPassAllSpecials,
			String autoPassSpecials, String strPassword, Boolean seperatorTab,
			FkActionEventListener delegate) {
			
			String seq = new String();
			
			seq += strAccountName;
			seq += (char)13;
			seq	+= strUserName;
			seq += (char)13;
			
			if( autoPassword )
			{
				seq += '2';
				seq += autoPassLen;
				seq += (char)13;
				if( autoPassAllSpecials )
				{
					seq += '1';
				} else {
					if( autoPassSpecials.length() > 0 )
					{
						seq += '2';
						seq += autoPassSpecials;
						seq += (char)13;
					} else {
						seq += '3';
					}
				}
			} else {
				//Manual entered password
				seq += '1';
				seq += strPassword;
				seq += (char)13;
			}
			
			//Tab/Enter sep
			if( seperatorTab )
			{
				seq += '1';
			} else {
				seq += '2';
			}
			
			//System.out.println("Seq ["+seq+"]");
			
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
			list.sort(sortMethod);
		}
	}


}
