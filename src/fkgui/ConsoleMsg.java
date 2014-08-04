package fkgui;

import java.awt.PopupMenu;

import fkgui.SerialWorker.SerialState;

public interface ConsoleMsg {
	public void log(String msg);
	public void serialEvent( SerialState state );
}
