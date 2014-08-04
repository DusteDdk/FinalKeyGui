package fkgui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.eclipse.swt.widgets.Display;

import fkgui.UpdateChecker.AutoUpdaterResultListener.AutoUpdaterResultEvent;
import fkgui.UpdateChecker.AutoUpdaterResultListener.UpdateCheckResult;


public class UpdateChecker implements Runnable {
	
	static final String CHECK_URL="http://cyberstalker.dk/finalkey/gui/update.php";
	static final String CUR_VER="0.0";
	static final String PLATFORM="lin_x64";
	static final String LANG = "US";

	public static final String REQUEST_STRING = CHECK_URL + "?version="+CUR_VER+"&platform="+PLATFORM+"&lang="+LANG;
	
	public interface AutoUpdaterResultListener {
		public class AutoUpdaterResultEvent
		{
			public String version;
			public String message;
			public UpdateCheckResult result;
			public AutoUpdaterResultEvent( UpdateCheckResult res, String ver, String msg )
			{
				result=res;
				version=ver;
				message=msg;
			}
		}
		public enum UpdateCheckResult { NO_UPDATE, CHECK_FAILED, UPDATE_AVAILABLE };
		
		public void updateCheckFinished( AutoUpdaterResultEvent event );
	}	
	
	AutoUpdaterResultListener delegate;
	public UpdateChecker( AutoUpdaterResultListener del )
	{
		delegate=del;
	}

	public class AutoUpdaterResultTask implements Runnable
	{
		private AutoUpdaterResultEvent e;
		private AutoUpdaterResultListener d;
		public AutoUpdaterResultTask( AutoUpdaterResultEvent event, AutoUpdaterResultListener delegate )
		{
			e=event;
			d=delegate;
		}
		@Override
		public void run() {
			d.updateCheckFinished(e);			
		}
		
	}
	
	@Override
	public void run() {
		UpdateCheckResult state = UpdateCheckResult.CHECK_FAILED;
		String res="";
		String ver="No Version";
		String msg="No Update";

		try {
			URL url = new URL(REQUEST_STRING);
			BufferedReader  in = new BufferedReader ( new InputStreamReader( url.openStream() ) );
			res=in.readLine();
			in.close();
			state = UpdateCheckResult.NO_UPDATE;

			String[] lines = res.split( "," );

			if( lines[0].compareTo("1")==0 )
			{
				state = UpdateCheckResult.UPDATE_AVAILABLE;
				ver = lines[1];
				msg = lines[2];
				msg=msg.replace("<br>", "\n");
				msg.trim();

			}

		} catch (Exception e) {
			System.out.println("Trouble checking for new version: "+e.getMessage() );
		}

		Display.getDefault().asyncExec( new AutoUpdaterResultTask(new AutoUpdaterResultEvent(state , ver, msg), delegate) );
		
	}

}
