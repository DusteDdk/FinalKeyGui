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

public class MainWin implements PropertyChangeListener, ConsoleMsg {

	protected Shell shell;
	private Text txtPsw;
	public TrayIcon trayIcon;
	public PopupMenu popup;
	public MenuItem showMain;
	public MenuItem hideMain;
	public Text txtLog;
	public Button btnStart;
	private Text txtDev;
	Preferences prefs;
	
	SerialWorker fkSerial;
	private boolean sysTrayIconVisible;

	
	static final String PORT_PREF ="lastUsedPortPref";
	static final String DEFAULT_DEVICE = "/dev/FinalKey";
 
	
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
        


        
        //Add components to pop-up menu
        popup.add(hideMain);
        popup.addSeparator();


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
		

		fkSerial = new SerialWorker(this);
		fkSerial.addPropertyChangeListener(this);
		

		btnStart = new Button(shell, SWT.NONE);
		btnStart.setBounds(330, 10, 104, 52);
		btnStart.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				
				if( fkSerial.state == SerialState.Connected )
				{
					shell.setText("Final Key (Not connected)");
					fkSerial.disconnect();
					txtPsw.setVisible(true);
					txtDev.setVisible(true);
					
					btnStart.setText("Connect");
				} else {
					prefs.put(PORT_PREF, txtDev.getText() );
					fkSerial.connect(txtDev.getText(),txtPsw.getText());
					//Eat the password for security reasons.
					txtPsw.setText("");
					txtPsw.setVisible(false);
					txtDev.setVisible(false);
					btnStart.setText("Disconnect");
					shell.setText("Final Key (Connected)");
					
				}
			}
		});
		btnStart.setText("Connect");
		Label lblPassword = new Label(shell, SWT.NONE);
		lblPassword.setText("Password");
		lblPassword.setBounds(10, 39, 85, 23);
		
		txtPsw = new Text(shell, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
		txtPsw.setBounds(101, 39, 223, 23);
		
		txtLog = new Text(shell, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
		txtLog.setEditable(false);
		txtLog.setBounds(10, 68, 424, 388);
		
		Label lblPort = new Label(shell, SWT.NONE);
		lblPort.setBounds(10, 10, 76, 23);
		lblPort.setText("Port");
		
		txtDev = new Text(shell, SWT.BORDER);
		txtDev.setFont(SWTResourceManager.getFont("Cantarell", 9, SWT.NORMAL));
		txtDev.setText( prefs.get(PORT_PREF, DEFAULT_DEVICE));
		txtDev.setBounds(101, 10, 223, 23);
		shell.setTabList(new Control[]{txtPsw, btnStart});
		
		log("Welcome!\nConnect your Final Key and enter password.\nThen press connect.\nPress the button when it blinks.\n----------\n");


		createSysTrayIcon();
		
		
		
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
	public void propertyChange(PropertyChangeEvent evt) {
		
		if( evt.getPropertyName().equals("serialState") )
		{
			System.out.println( "serialState changed from "+((SerialState)evt.getOldValue())+" to "+((SerialState)evt.getNewValue()) );
		}
		
		
	}

	@Override
	public PopupMenu getPopup() {
		return this.popup;
	}
}
