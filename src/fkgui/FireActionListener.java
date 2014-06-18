package fkgui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import jssc.SerialPort;
import jssc.SerialPortException;

public class FireActionListener implements ActionListener {
	public String name;
	public String num;
	public String action;
	public SerialPort port;
	public void actionPerformed(ActionEvent e) {
		System.out.println("Performing action " + action + " on account ("+num+") - "+name );
		if(!action.equals("%"))
		{
			System.out.println("Here");
			try
			{
				port.writeBytes(action.getBytes());
			} catch( Exception ex )
			{
				ex.printStackTrace();
			}
		}
		
		try {
			port.writeBytes(num.toLowerCase().getBytes());
	
			try {
				Thread.sleep(400);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			System.out.println( port.readString() );
			
		} catch (SerialPortException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		
	}

}
