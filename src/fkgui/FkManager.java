package fkgui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JTextField;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;

import jssc.SerialPortException;
import fkgui.FkActionEventListener.FkActionEvent;
import fkgui.FkActionEventListener.FkActionEventType;
import fkgui.FkManager.Account;
import fkgui.SerialWorker.SerialState;

public class FkManager implements ActionListener {
	private static FkManager instance = null;
	private SerialWorker com = null;

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

		private class FkActionEventMsg implements Runnable  {
			private FkActionEventListener delegate;
			private FkActionEvent event;

			private FkActionEventMsg(FkActionEventListener d, FkActionEventType t, String data, Account acc )
			{
				delegate=d;
				event=new FkActionEvent(t, data, acc) ;
			}
			@Override
			public void run() {
				if( delegate != null )
				{
					delegate.fkActionEvent(event);
				}
			}
		}
		
		@Override
		public void run() {
			if(action != '%' )
			{
				try
				{
					com.serialPort.writeByte((byte)action);
				} catch( Exception ex )
				{
					ex.printStackTrace();
				}
			}
			
			try {
				com.serialPort.writeBytes(acc.num.toLowerCase().getBytes());
		
				try {
					Thread.sleep(400);
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
						if( msg.contains("[done]") )
						{
							//System.out.println("FkManager: Action OK");;
							Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_OKAY, msg, acc) );
							return;
						} else if( msg.contains("[abort]") )
						{
							//System.out.println("FkManager: Action Abort");;
							Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_ABORTED, msg,acc) );
							
							return;
						}
					}
				}
				Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_ERROR, msg,acc) );

			} catch (Exception e1) {
				// TODO Auto-generated catch block
				System.out.println("TrigTask Exception:");
				e1.printStackTrace();
				Display.getDefault().asyncExec( new FkActionEventMsg(delegate, FkActionEventListener.FkActionEventType.ACTION_ERROR, "EXCEPTION",acc) );
			}
			
		}
		
	}
	
	public void trig(Account acc, char action, FkActionEventListener delegate)
	{
		TrigTask trigTask = new TrigTask(acc,action,delegate);
		new Thread(trigTask).start();
	}
	
	
	public void listAddAcc(String num, String name)
	{
		list.addElement( new Account(num,name) );
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

	
	
}
