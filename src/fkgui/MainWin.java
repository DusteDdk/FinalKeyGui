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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
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

import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.List;

public class MainWin implements ConsoleMsg {

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
	private Composite composite;
	private Composite cmpAccounts;
	List lstAccounts;
	private Text txtSearch;
 
	
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
		shell.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		TabFolder tabFolder = new TabFolder(shell, SWT.NONE);
		tabFolder.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		
		TabItem tbtmConnection = new TabItem(tabFolder, SWT.NONE);
		tbtmConnection.setText("Connection");
		
		cmpConnect = new Composite(tabFolder, SWT.BORDER);
		tbtmConnection.setControl(cmpConnect);
		cmpConnect.setLayout(new FormLayout());
		

		btnConnect = new Button(cmpConnect, SWT.CENTER);
		FormData fd_btnConnect = new FormData();
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
		fd_txtLog.bottom = new FormAttachment(100, -10);
		fd_txtLog.right = new FormAttachment(btnConnect, 0, SWT.RIGHT);
		fd_txtLog.top = new FormAttachment(0, 58);
		fd_txtLog.left = new FormAttachment(0, 10);
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
		
		cmpConnect.setTabList(new Control[]{txtPsw, btnConnect});
		
		
		TabItem tbtmOptions = new TabItem(tabFolder, SWT.NONE);
		tbtmOptions.setText("Options");
		
		composite = new Composite(tabFolder, SWT.BORDER);
		tbtmOptions.setControl(composite);
		
		TabItem tbtmAccounts = new TabItem(tabFolder, SWT.NONE);
		tbtmAccounts.setText("Accounts");
		
		cmpAccounts = new Composite(tabFolder, SWT.BORDER);
		tbtmAccounts.setControl(cmpAccounts);
		cmpAccounts.setLayout(new FormLayout());
		
		Button btnNewAccoount = new Button(cmpAccounts, SWT.NONE);
		FormData fd_btnNewAccoount = new FormData();
		fd_btnNewAccoount.top = new FormAttachment(0, 10);
		fd_btnNewAccoount.left = new FormAttachment(0, 10);
		btnNewAccoount.setLayoutData(fd_btnNewAccoount);
		btnNewAccoount.setText("New");
		
		Button btnEditAccount = new Button(cmpAccounts, SWT.NONE);
		FormData fd_btnEditAccount = new FormData();
		fd_btnEditAccount.top = new FormAttachment(0, 10);
		fd_btnEditAccount.left = new FormAttachment(0, 63);
		btnEditAccount.setLayoutData(fd_btnEditAccount);
		btnEditAccount.setText("Edit");
		
		Button btnDeleteAccount = new Button(cmpAccounts, SWT.NONE);
		FormData fd_btnDeleteAccount = new FormData();
		fd_btnDeleteAccount.top = new FormAttachment(btnNewAccoount, 0, SWT.TOP);
		btnDeleteAccount.setLayoutData(fd_btnDeleteAccount);
		btnDeleteAccount.setText("Delete");
		
		lstAccounts = new List(cmpAccounts, SWT.BORDER | SWT.V_SCROLL);
		fd_btnDeleteAccount.right = new FormAttachment(lstAccounts, 0, SWT.RIGHT);
		FormData fd_lstAccounts = new FormData();
		fd_lstAccounts.left = new FormAttachment(0, 10);
		fd_lstAccounts.bottom = new FormAttachment(100, -6);
		fd_lstAccounts.right = new FormAttachment(100, -10);
		fd_lstAccounts.top = new FormAttachment(btnNewAccoount, 6);

		lstAccounts.setLayoutData(fd_lstAccounts);
		
		lstAccounts.add("Facebook");
		lstAccounts.add("Twitter");
		lstAccounts.add("Krapbox Twitter");
		lstAccounts.add("Fjord");
		lstAccounts.add("Ordbogen");
		lstAccounts.add("Fisker");
		lstAccounts.add("KrapBox");
		lstAccounts.add("En to tre");
		lstAccounts.add("Item9");
		lstAccounts.add("Item0");
		lstAccounts.add("Itema");
		lstAccounts.add("Itemb");
		lstAccounts.add("Itemc");
		lstAccounts.add("Itemd");
		lstAccounts.add("Iteme");
		lstAccounts.add("Itemf");
		lstAccounts.add("Itemg");
		lstAccounts.add("Itemh");
		lstAccounts.add("Itemi");
		lstAccounts.add("Itemj");
		lstAccounts.add("Itemk");
		
		txtSearch = new Text(cmpAccounts, SWT.BORDER);
		txtSearch.setText("Search");
		FormData fd_txtSearch = new FormData();
		fd_txtSearch.right = new FormAttachment(btnDeleteAccount, -6);
		fd_txtSearch.bottom = new FormAttachment(lstAccounts, -6);
		fd_txtSearch.left = new FormAttachment(btnEditAccount, 6);
		txtSearch.setLayoutData(fd_txtSearch);
		
		lstAccounts.addListener(SWT.Selection, new Listener()
		{

			@Override
			public void handleEvent(Event event) {
				System.out.println( "Selected Idx:"+lstAccounts.getSelectionIndex() );
				//txtSearch.setText( lstAccounts.getItem(lstAccounts.getSelectionIndex() ) );
				
			}
			
		});
		
		txtSearch.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				if( txtSearch.getText().length()<1) return;

				String lst[] = lstAccounts.getItems();
				String wrds[] = txtSearch.getText().toLowerCase().split(" ");
				int idx=0;
				int scores[] = new int[lst.length];
				for( String s : lst )
				{
					s = s.toLowerCase();
					scores[idx]=0;
					for(String w: wrds )
					{
						if( s.contains(w) )
						{
							scores[idx]++;
							//System.out.println("Account: "+s+ " scores +1 for substring "+w);
						}
					}
					idx++;
				}
				
				int top=0;
				int biggest=0;
				for(int i=0; i<lst.length;i++)
				{
					if( scores[i] > biggest )
					{
						biggest=scores[i];
						top=i;
					}
				}
				
				if(biggest!=0)
				{
					lstAccounts.setSelection(top);
				}
			}
		});
		
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
			shell.setText("Final Key (Connected)");
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
			chkAutoHide.setVisible(false);
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
			chkAutoHide.setVisible(true);
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
		cmpConnect.layout();
	}
}
