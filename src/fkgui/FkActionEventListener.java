package fkgui;

import fkgui.FkManager.Account;

public interface FkActionEventListener {
	public class FkActionEvent
	{
		FkActionEventType type;
		public String data;
		public Account acc;
		public char action;
		
		public FkActionEvent( FkActionEventType t, String d, Account a, char act )
		{
			data = d;
			type = t;
			acc=a;
			action=act;
		}
		
		public String toString()
		{
			return("Event {\n  Type: "+type+"\n  Data: "+data+"\n  Account: "+((acc!=null)?acc.toString():"null")+"\n  Action:"+action+"\n}");
		}
	}
	public enum FkActionEventType { ACTION_ABORTED, ACTION_OKAY, ACTION_ERROR, ACTION_WAITING, ACTION_WORKING, STATE_ERROR, UNEXPECTED_ACTION_RESULT_ERROR, PROGRESS_UPDATE };
	public void fkActionEvent( FkActionEvent event );
}
