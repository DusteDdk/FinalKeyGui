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

	static final String CHECK_URL="http://finalkey.net/gui/update.php"; //$NON-NLS-1$
	static final String CUR_VER="0.5.2"; //$NON-NLS-1$
	static final String PLATFORM=System.getProperty("os.name")+"_"+System.getProperty("os.arch"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	static final String LANG = Locale.getDefault().getLanguage();

	public static final String REQUEST_STRING = CHECK_URL + "?version="+CUR_VER+"&platform="+PLATFORM+"&lang="+LANG; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

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
		String res=""; //$NON-NLS-1$
		String ver="No Version"; //$NON-NLS-1$
		String msg="No Update"; //$NON-NLS-1$
		System.out.println(Messages.UpdateChecker_11+REQUEST_STRING);
		try {
			URL url = new URL(REQUEST_STRING);
			BufferedReader  in = new BufferedReader ( new InputStreamReader( url.openStream() ) );
			res=in.readLine();
			in.close();
			state = UpdateCheckResult.NO_UPDATE;

			String[] lines = res.split( "," ); //$NON-NLS-1$

			if( lines[0].compareTo("1")==0 ) //$NON-NLS-1$
			{
				state = UpdateCheckResult.UPDATE_AVAILABLE;
				ver = lines[1];
				msg = lines[2];
				msg=msg.replace("<br>", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
				msg.trim();

			}

			in.close();

		} catch (Exception e) {
			System.out.println(Messages.UpdateChecker_16+e.getMessage() );
		}

		Display.getDefault().asyncExec( new AutoUpdaterResultTask(new UpdateResultEvent(state , ver, msg), delegate) );

	}

}
