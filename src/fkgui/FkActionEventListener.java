package fkgui;

import fkgui.FkManager.Account;

public interface FkActionEventListener {
	public class FkActionEvent
	{
		FkActionEventType type;
		public String data;
		Account acc;
		public FkActionEvent( FkActionEventType t, String d, Account a )
		{
			data = d;
			type = t;
			acc=a;
		}
	}
	public enum FkActionEventType { ACTION_ABORTED, ACTION_OKAY, ACTION_ERROR };
	public void fkActionEvent( FkActionEvent event );
}
