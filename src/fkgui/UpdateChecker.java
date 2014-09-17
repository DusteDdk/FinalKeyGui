package fkgui;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Locale;

import org.eclipse.swt.widgets.Display;

import fkgui.UpdateChecker.UpdateCheckResultListener.UpdateCheckResult;
import fkgui.UpdateChecker.UpdateCheckResultListener.UpdateResultEvent;


public class UpdateChecker implements Runnable {

	//Change locale with -Duser.country=DK -Duser.language=da parms for the java command.

	static final String CHECK_URL="http://cyberstalker.dk/finalkey/gui/update.php";
	static final String CUR_VER="0.1.2";
	static final String PLATFORM=System.getProperty("os.name")+"_"+System.getProperty("os.arch");
	static final String LANG = Locale.getDefault().getLanguage();

	public static final String REQUEST_STRING = CHECK_URL + "?version="+CUR_VER+"&platform="+PLATFORM+"&lang="+LANG;

	public interface UpdateCheckResultListener {
		public class UpdateResultEvent
		{
			public String version;
			public String message;
			public UpdateCheckResult result;
			public UpdateResultEvent( UpdateCheckResult res, String ver, String msg )
			{
				result=res;
				version=ver;
				message=msg;
			}
		}
		public enum UpdateCheckResult { NO_UPDATE, CHECK_FAILED, UPDATE_AVAILABLE };

		public void updateCheckFinished( UpdateResultEvent event );
	}

	UpdateCheckResultListener delegate;
	public UpdateChecker( UpdateCheckResultListener del )
	{
		delegate=del;
	}

	public class AutoUpdaterResultTask implements Runnable
	{
		private UpdateResultEvent e;
		private UpdateCheckResultListener d;
		public AutoUpdaterResultTask( UpdateResultEvent event, UpdateCheckResultListener delegate )
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
		System.out.println("Checking for new version: "+REQUEST_STRING);
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

			in.close();

		} catch (Exception e) {
			System.out.println("Trouble checking for new version: "+e.getMessage() );
		}

		Display.getDefault().asyncExec( new AutoUpdaterResultTask(new UpdateResultEvent(state , ver, msg), delegate) );

	}

}
