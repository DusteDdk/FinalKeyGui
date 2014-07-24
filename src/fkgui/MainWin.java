package fkgui;


import  java.util.prefs.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Control;
import org.eclipse.wb.swt.SWTResourceManager;

import fkgui.SerialWorker.SerialState;

public class MainWin implements ConsoleMsg {

	protected Shell shell;
	private Text txtPsw;
	public TrayIcon trayIcon;
	public PopupMenu popup;
	public MenuItem showMain;
	public MenuItem hideMain;

	public Text txtLog;
	public Button btnConnect;
	public Label lblPort;
	public Label lblPassword;

	private Text txtDev;
	Preferences prefs;
	MainWin mySelf;
	private SerialState lastState = SerialState.Disconnected;
	
	SerialWorker fkSerial;
	private boolean sysTrayIconVisible;

	
	static final String PREF_PORT ="lastUsedPortPref";
	static final String DEFAULT_DEVICE = "/dev/FinalKey";
	static final String PREF_AUTOHIDE = "hideMainWinAfterConnect";
 
	
	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			MainWin window = new MainWin();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		createSysTrayIcon();
		serialEvent(SerialState.Disconnected);

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();				
			}
		}
	}
	
	public void log( String str )
	{
		txtLog.append(str+"\n");
		txtLog.redraw();
		shell.redraw();
		System.out.println(str);
	}

	/**
	 * Create a systemTray icon
	 */
	private void createSysTrayIcon()
	{
		sysTrayIconVisible=true;
        //Check the SystemTray is supported
        if (!SystemTray.isSupported()) {
            log("SystemTray is not supported, app is useless");
            return;
        }
        
        try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnsupportedLookAndFeelException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

        popup = new PopupMenu();
        trayIcon =
                new TrayIcon(Toolkit.getDefaultToolkit().createImage(getClass().getResource("finalkey.png")));
        trayIcon.setImageAutoSize(true);
        final SystemTray tray = SystemTray.getSystemTray();
       
        // Create a pop-up menu components
        showMain = new MenuItem("Show FinalKey");
        hideMain = new MenuItem("Hide FinalKey");

        showMain.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Display.getDefault().syncExec( new Runnable(){
					
					public void run()
					{
				//Remove myself and make main window visible
						showFromTray();
					}
				} );
			}
				
		});
        
        hideMain.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Display.getDefault().syncExec( new Runnable(){
					
					public void run()
					{
						hideToTray();
					}
				} );
			}
				
		});

        clearSystray();


        trayIcon.setPopupMenu(popup);
       
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
        	log("TrayIcon could not be added.");
        }	
        
	}
	
	private void hideToTray()
	{
		popup.remove(0);
		popup.insert(showMain, 0);
		shell.setVisible(false);
	}
	
	private void showFromTray()
	{
		popup.remove(0);
		popup.insert(hideMain, 0);
		shell.setVisible(true);
		shell.forceActive();
	}
	
	private void destroySysTrayIcon()
	{
		sysTrayIconVisible=false;
		SystemTray.getSystemTray().remove(trayIcon);
	}
	
	private void clearSystray() {
			popup.removeAll();
	        //Add components to pop-up menu
			if(shell.isVisible()==true)
			{
				popup.add(hideMain);
			} else {
				popup.add(showMain);
			}
	        popup.addSeparator();
	}

	public void shutDownApp()
	{
		if(fkSerial != null)
		{
			fkSerial.disconnect();
		}
		if(sysTrayIconVisible == true)
		{
			destroySysTrayIcon();
		}
		System.exit(0);
	}
	
	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setImage(SWTResourceManager.getImage(MainWin.class, "/fkgui/finalkey.png"));
		shell.setSize(450, 495);
		shell.setText("Final Key (Not connected)");
		
		shell.setLayout(null);

		
		prefs = Preferences.userNodeForPackage(this.getClass());
		

		mySelf = this;
		

		btnConnect = new Button(shell, SWT.NONE);
		btnConnect.setBounds(330, 10, 104, 52);
		btnConnect.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				
				if( fkSerial!=null && fkSerial.state == SerialState.Connected )
				{
					fkSerial.disconnect();
				} else {
					fkSerial = new SerialWorker(mySelf);
					prefs.put(PREF_PORT, txtDev.getText() );
					fkSerial.connect(txtDev.getText(),txtPsw.getText());
					txtPsw.setText("");
				}
			}
		});
		btnConnect.setText("Connect");
		lblPassword = new Label(shell, SWT.NONE);
		lblPassword.setText("Password");
		lblPassword.setBounds(10, 39, 85, 23);
		
		txtPsw = new Text(shell, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
		txtPsw.setBounds(101, 39, 223, 23);
		
		txtLog = new Text(shell, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
		txtLog.setEditable(false);
		txtLog.setBounds(10, 68, 424, 388);
		
		lblPort = new Label(shell, SWT.NONE);
		lblPort.setBounds(10, 10, 76, 23);
		lblPort.setText("Port");
		
		txtDev = new Text(shell, SWT.BORDER);
		txtDev.setFont(SWTResourceManager.getFont("Cantarell", 9, SWT.NORMAL));
		txtDev.setText( prefs.get(PREF_PORT, DEFAULT_DEVICE));
		txtDev.setBounds(101, 10, 223, 23);
		shell.setTabList(new Control[]{txtPsw, btnConnect});
		
		log("Welcome!\nConnect your Final Key and enter password.\nThen press connect.\nPress the button when it blinks.\n----------\n");


		shell.addShellListener( new ShellListener() {
			
			public void shellIconified(ShellEvent e) {

			}

			public void shellDeiconified(ShellEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			public void shellDeactivated(ShellEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			public void shellClosed(ShellEvent e) {
				shutDownApp();
				
			}
			
			public void shellActivated(ShellEvent e) {
				// TODO Auto-generated method stub
				
			}
		} );
		


	}



	@Override
	public PopupMenu getPopup() {
		return this.popup;
	}

	@Override
	public void serialEvent(SerialState state) {
		switch(state)
		{
		case Connected:
			btnConnect.setText("Disconnect");
			btnConnect.setVisible(true);
			//Should we hide?
			if( prefs.getBoolean(PREF_AUTOHIDE, false) == true)
			{
				hideToTray();
			}
			log("* Connected *");
			break;
		case Connecting:
			shell.setText("Final Key (Connecting...)");
			txtPsw.setVisible(false);
			txtDev.setVisible(false);
			btnConnect.setVisible(false);
			lblPort.setVisible(false);
			lblPassword.setVisible(false);
			break;
		case Disconnected:
			fkSerial=null;
			shell.setText("Final Key (Not connected)");
			txtPsw.setVisible(true);
			txtDev.setVisible(true);
			btnConnect.setText("Connect");
			btnConnect.setVisible(true);
			lblPort.setVisible(true);
			lblPassword.setVisible(true);
			clearSystray();
			if(lastState != state)
			{
				log("* Disconnected *");
			}
			break;
		default:
			break;
		}
		lastState=state;
		
	}


}
