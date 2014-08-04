package fkgui;


import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import  java.util.prefs.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Control;
import org.eclipse.wb.swt.SWTResourceManager;

import fkgui.SerialWorker.SerialState;
import fkgui.UpdateChecker.AutoUpdaterResultListener;

import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.List;


public class MainWin implements ConsoleMsg, AutoUpdaterResultListener {

	protected Shell shell;
	private Text txtPsw;
	public TrayIcon trayIcon;
	public PopupMenu popup;
	public MenuItem showMain;
	public MenuItem hideMain;

	public Text txtLog;
	public Button btnConnect;
	public Button chkAutoHide;
	public Label lblPort;
	public Label lblPassword;
	
	public Animation animation;
	
	public TabFolder tabFolder;

	private Text txtDev;
	Preferences prefs;
	MainWin mySelf;
	private SerialState lastState = SerialState.Disconnected;
	
	SerialWorker fkSerial;
	private boolean sysTrayIconVisible;

	
	static final String PREF_PORT ="lastUsedPortPref";
	static final String DEFAULT_DEVICE = "/dev/FinalKey";
	static final String PREF_AUTOHIDE = "hideMainWinAfterConnect";
	public Composite cmpConnect;
	private Composite cmpAccounts;
	List lstAccounts;
 
	
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
		new Thread(new UpdateChecker(this)).start();

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
        trayIcon.setToolTip("The Final Key - Hardware password manager");
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
		shell.setSize(711, 655);
		shell.setText("Final Key (Not connected)");

		
		prefs = Preferences.userNodeForPackage(this.getClass());
		

		mySelf = this;
		shell.setLayout(new FormLayout());
		
		tabFolder = new TabFolder(shell, SWT.NONE);
		FormData fd_tabFolder = new FormData();
		fd_tabFolder.bottom = new FormAttachment(0, 627);
		fd_tabFolder.right = new FormAttachment(0, 709);
		fd_tabFolder.top = new FormAttachment(0);
		fd_tabFolder.left = new FormAttachment(0);
		tabFolder.setLayoutData(fd_tabFolder);
		tabFolder.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		
		TabItem tbtmConnection = new TabItem(tabFolder, SWT.NONE);
		tbtmConnection.setText("Connection");
		
		cmpConnect = new Composite(tabFolder, SWT.BORDER);
		tbtmConnection.setControl(cmpConnect);
		cmpConnect.setLayout(new FormLayout());
		

		btnConnect = new Button(cmpConnect, SWT.CENTER);
		btnConnect.setImage(SWTResourceManager.getImage("/home/dusted/Downloads/lightning.png"));
		FormData fd_btnConnect = new FormData();
		fd_btnConnect.left = new FormAttachment(100, -125);
		fd_btnConnect.right = new FormAttachment(100, -10);
		btnConnect.setLayoutData(fd_btnConnect);

		btnConnect.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				
				if( fkSerial!=null && fkSerial.state == SerialState.Connected )
				{
					fkSerial.disconnect();
				} else {
					
					prefs.putBoolean(PREF_AUTOHIDE, chkAutoHide.getSelection() );

					fkSerial = new SerialWorker(mySelf);
					prefs.put(PREF_PORT, txtDev.getText() );
					fkSerial.connect(txtDev.getText(),txtPsw.getText());
					txtPsw.setText("");
				}
			}
		});
		btnConnect.setText("Connect");
		
		txtPsw = new Text(cmpConnect, SWT.BORDER | SWT.PASSWORD);
		fd_btnConnect.bottom = new FormAttachment(txtPsw, 0, SWT.BOTTOM);
		FormData fd_txtPsw = new FormData();
		fd_txtPsw.bottom = new FormAttachment(0, 52);
		fd_txtPsw.right = new FormAttachment(0, 501);
		fd_txtPsw.top = new FormAttachment(0, 29);
		fd_txtPsw.left = new FormAttachment(0, 102);
		txtPsw.setLayoutData(fd_txtPsw);
		
		txtLog = new Text(cmpConnect, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
		FormData fd_txtLog = new FormData();
		fd_txtLog.left = new FormAttachment(0, 10);
		fd_txtLog.right = new FormAttachment(100, -10);
		fd_txtLog.bottom = new FormAttachment(100, -10);
		fd_txtLog.top = new FormAttachment(0, 58);
		txtLog.setLayoutData(fd_txtLog);
		txtLog.setEditable(false);
		
		lblPort = new Label(cmpConnect, SWT.NONE);
		fd_btnConnect.top = new FormAttachment(lblPort, 0, SWT.TOP);
		FormData fd_lblPort = new FormData();
		fd_lblPort.right = new FormAttachment(0, 86);
		fd_lblPort.top = new FormAttachment(0);
		fd_lblPort.left = new FormAttachment(0, 10);
		lblPort.setLayoutData(fd_lblPort);
		lblPort.setText("Port");
		
		txtDev = new Text(cmpConnect, SWT.BORDER);
		FormData fd_txtDev = new FormData();
		fd_txtDev.bottom = new FormAttachment(0, 23);
		fd_txtDev.right = new FormAttachment(0, 256);
		fd_txtDev.top = new FormAttachment(0);
		fd_txtDev.left = new FormAttachment(0, 102);
		txtDev.setLayoutData(fd_txtDev);
		txtDev.setFont(SWTResourceManager.getFont("Cantarell", 9, SWT.NORMAL));
		txtDev.setText( prefs.get(PREF_PORT, DEFAULT_DEVICE));
		lblPassword = new Label(cmpConnect, SWT.NONE);
		FormData fd_lblPassword = new FormData();
		fd_lblPassword.right = new FormAttachment(0, 95);
		fd_lblPassword.top = new FormAttachment(0, 29);
		fd_lblPassword.left = new FormAttachment(0, 10);
		lblPassword.setLayoutData(fd_lblPassword);
		lblPassword.setText("Password");
		
		chkAutoHide = new Button(cmpConnect, SWT.CHECK);
		FormData fd_chkAutoHide = new FormData();
		fd_chkAutoHide.right = new FormAttachment(0, 501);
		fd_chkAutoHide.top = new FormAttachment(0);
		fd_chkAutoHide.left = new FormAttachment(0, 262);
		chkAutoHide.setLayoutData(fd_chkAutoHide);
		chkAutoHide.setText("Hide after connection");
		
		chkAutoHide.setSelection( prefs.getBoolean(PREF_AUTOHIDE, false)) ;
		
		animation = new Animation(cmpConnect, SWT.NONE, 4);

		FormData fd_animation = new FormData();
		fd_animation.right = new FormAttachment(0, 86);
		fd_animation.top = new FormAttachment(0);
		fd_animation.left = new FormAttachment(0, 10);		
		
		animation.setLayoutData(fd_animation);


		
		

		
		animation.setVisible(false);
		animation.addFrame( SWTResourceManager.getImage("/home/dusted/Downloads/finalkey1.png") );
		animation.addFrame( SWTResourceManager.getImage("/home/dusted/Downloads/finalkey2.png") );
		animation.setPlaying(false);
		cmpConnect.setTabList(new Control[]{txtPsw, btnConnect});
		
	
		
		log("Type your password and press connect.\n----------\n");


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
	public void serialEvent(SerialState state) {
		switch(state)
		{
		case Connected:
			shell.setText("Final Key (Connected)");
			
			animation.setVisible(false);
			animation.setPlaying(false);


			btnConnect.setText("Disconnect");
			btnConnect.setVisible(true);
			
			//Should we hide?
			if( prefs.getBoolean(PREF_AUTOHIDE, false) == true)
			{
				hideToTray();
			}
			
			addAccountsTab();
			
			
			tabFolder.setSelection(1);
			
			for( FkManager.Account a : FkManager.getInstance().getList() )
			{
				lstAccounts.add(a.name);
				
				Menu menu = new Menu(a.name+" ["+a.num+"]");
				MenuItem both = new MenuItem("User + Pass");
				MenuItem usr = new MenuItem("User");
				MenuItem psw = new MenuItem("Pass");
				menu.add(both);
				menu.add(usr);
				menu.add(psw);


				both.addActionListener(FkManager.getInstance());
				both.setActionCommand( "%"+a.num );



				psw.addActionListener(FkManager.getInstance());	
				psw.setActionCommand( "p"+a.num);


				usr.addActionListener(FkManager.getInstance());	
				usr.setActionCommand( "u"+a.num );

				popup.add(menu);				
				
				
			}
			
			if( lstAccounts.getItemCount() > 0 )
			{
				trayIcon.displayMessage("FinalKey", lstAccounts.getItemCount() + " account"+(( lstAccounts.getItemCount()>1)?"s":"")+" ready.", 
			            TrayIcon.MessageType.INFO);
			}

			
			log("* Connected *");
			break;
		case Connecting:
			shell.setText("Final Key (Connecting...)");
			animation.setVisible(true);
			animation.setPlaying(true);

			txtPsw.setVisible(false);
			txtDev.setVisible(false);
			btnConnect.setVisible(false);
			lblPort.setVisible(false);
			lblPassword.setVisible(false);
			chkAutoHide.setVisible(false);
			break;
		case Disconnected:
			fkSerial=null;
			animation.setVisible(false);
			animation.setPlaying(false);

			remAccountsTab();
			
			tabFolder.setSelection(0);

			shell.setText("Final Key (Not connected)");
			txtPsw.setVisible(true);
			txtDev.setVisible(true);
			btnConnect.setText("Connect");
			btnConnect.setVisible(true);
			lblPort.setVisible(true);
			lblPassword.setVisible(true);
			chkAutoHide.setVisible(true);
			clearSystray();
			if(lastState != state)
			{
				log("* Disconnected *");
			}
			break;
		
		case Working:
			animation.setPlaying(false);
			animation.setVisible(false);
			break;
			
		default:
			break;
		}
		lastState=state;
		cmpConnect.layout();
	}

	private void remAccountsTab() {
		if( tabFolder.getItemCount() > 1 )
		{
			tabFolder.getItem(1).dispose();
		}
		
	}

	private void addAccountsTab() {
		TabItem tbtmAccounts = new TabItem(tabFolder, SWT.NONE);
		tbtmAccounts.setText("Accounts");
		
		cmpAccounts = new Composite(tabFolder, SWT.BORDER);
		tbtmAccounts.setControl(cmpAccounts);
		cmpAccounts.setLayout(new FormLayout());
		
		Button btnNewAccoount = new Button(cmpAccounts, SWT.NONE);
		btnNewAccoount.setImage(SWTResourceManager.getImage("/home/dusted/Downloads/Button New-01.png"));
		btnNewAccoount.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		FormData fd_btnNewAccoount = new FormData();
		fd_btnNewAccoount.left = new FormAttachment(0, 530);
		fd_btnNewAccoount.right = new FormAttachment(100, -10);
		fd_btnNewAccoount.bottom = new FormAttachment(100, -10);
		btnNewAccoount.setLayoutData(fd_btnNewAccoount);
		btnNewAccoount.setText("New Account");
		
		lstAccounts = new List(cmpAccounts, SWT.BORDER | SWT.V_SCROLL);
		FormData fd_lstAccounts = new FormData();
		fd_lstAccounts.bottom = new FormAttachment(btnNewAccoount, -6);
		fd_lstAccounts.top = new FormAttachment(0, 10);
		fd_lstAccounts.left = new FormAttachment(0, 10);
		fd_lstAccounts.right = new FormAttachment(100, -10);

		lstAccounts.setLayoutData(fd_lstAccounts);
				
		lstAccounts.addListener(SWT.Selection, new Listener()
		{

			@Override
			public void handleEvent(Event event) {
				System.out.println( "Selected Idx:"+lstAccounts.getSelectionIndex() );
				TriggerDialog diag = new TriggerDialog(shell, shell.getStyle(), FkManager.getInstance().getList().get(lstAccounts.getSelectionIndex()) );

				shell.setMinimized(true);
				shell.setEnabled(false);
				if( !((Boolean)diag.open()) )
				{
					shell.setMinimized(false);
				}
				
				shell.setEnabled(true);
			}
			
		});
	}


	@Override
	public void updateCheckFinished(AutoUpdaterResultEvent event) {
		switch(event.result)
		{
		case CHECK_FAILED:
			System.out.println("Trouble checking for updates.");
			break;
		case NO_UPDATE:
			System.out.println("No update avaiable at this time.");
			break;
		case UPDATE_AVAILABLE:
			MessageBox dialog = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
			dialog.setText("FinalKey GUI Version "+event.version+" available.");
			dialog.setMessage("There's a new version of FinalKey GUI available.\nGo to http://cyberstalker.dk/finalkey/gui/ to download.\n\nNews:\n"+event.message);
			dialog.open();	
			break;
		}
		
	}
}
