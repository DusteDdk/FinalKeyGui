package fkgui;

import fkgui.SerialWorker.SerialState;

public interface ConsoleMsg {
	public void log(String msg);
	public void serialEvent( SerialState state );
	public void updateAccountList();
}
