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
	}
	public enum FkActionEventType { ACTION_ABORTED, ACTION_OKAY, ACTION_ERROR, ACTION_WAITING, ACTION_WORKING };
	public void fkActionEvent( FkActionEvent event );
}
