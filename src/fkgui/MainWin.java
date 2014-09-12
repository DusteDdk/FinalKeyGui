package fkgui;


import java.util.Map;
import  java.util.prefs.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.UIManager;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Control;
import org.eclipse.wb.swt.SWTResourceManager;

import fkgui.FkManager.Account;
import fkgui.SerialWorker.SerialState;
import fkgui.UpdateChecker.UpdateCheckResultListener;

import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.List;


public class MainWin implements ConsoleMsg, UpdateCheckResultListener {

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
	
	Button btnActivateAccount;

	
	static final String PREF_PORT ="lastUsedPortPref"; //$NON-NLS-1$
	static final String PREF_DEFAULT_DEVICE = "/dev/FinalKey"; //$NON-NLS-1$
	static final String PREF_AUTOHIDE = "hideMainWinAfterConnect"; //$NON-NLS-1$
	private static final String PREF_SORT_BY_ID_KEY = Messages.MainWin_0;
	public Composite cmpConnect;
	private Composite cmpAccounts;
	ListViewer lstAccounts;
	Label lblNumFree;
	Button btnNewAccoount;
	Boolean hiddenAtStart=false;
 
	//Icons
	private org.eclipse.swt.graphics.Image iconProgramOnline  = SWTResourceManager.getImage(MainWin.class, "/fkgui/gfx/finalkey.png");  //$NON-NLS-1$
	private org.eclipse.swt.graphics.Image iconProgramOffline  = SWTResourceManager.getImage(MainWin.class, "/fkgui/gfx/finalkey-big-offline.png");  //$NON-NLS-1$
	private Image iconSystrayOnline = Toolkit.getDefaultToolkit().createImage(getClass().getResource("gfx/systray-color.png"));  //$NON-NLS-1$
	private Image iconSystrayOffline= Toolkit.getDefaultToolkit().createImage(getClass().getResource("gfx/systray-offline.png"));  //$NON-NLS-1$
	
	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			MainWin window = new MainWin();

			for(String s : args )
			{
				if( s.compareTo("--hide") == 0 )
				{
					window.hiddenAtStart=true;
				}
			}

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
		addAccountsTab();
		createSysTrayIcon();

		serialEvent(SerialState.Disconnected);
		new Thread(new UpdateChecker(this)).start();

		if( hiddenAtStart )
		{
			hideToTray();
		} else {
			shell.open();
			shell.layout();
		}

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();				
			}
		}
	}
	
	public void log( String str )
	{
		txtLog.append(str+"\n"); //$NON-NLS-1$
		if( tabFolder.getSelectionIndex() == 0 )
		{
			txtLog.redraw();
			shell.redraw();
		}
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
            log("SystemTray is not supported, app is useless"); //$NON-NLS-1$
            return;
        }
        
        try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e1) {
			e1.printStackTrace();
		} 

        popup = new PopupMenu();
        trayIcon =
                new TrayIcon( iconSystrayOffline ); //$NON-NLS-1$
        trayIcon.setToolTip("The Final Key - Hardware password manager"); //$NON-NLS-1$
        trayIcon.setImageAutoSize(true);
        final SystemTray tray = SystemTray.getSystemTray();
       
        // Create a pop-up menu components
        showMain = new MenuItem(Messages.MainWin_7);
        hideMain = new MenuItem(Messages.MainWin_8);

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
        	log("TrayIcon could not be added."); //$NON-NLS-1$
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
		shell.setImage( iconProgramOffline ); //$NON-NLS-1$
		shell.setSize(711, 655);


		
		
		prefs = Preferences.userNodeForPackage(this.getClass());
		
		FkManager.getInstance().sortById( prefs.getBoolean(PREF_SORT_BY_ID_KEY, false) );		
		

		mySelf = this;
		shell.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		tabFolder = new TabFolder(shell, SWT.NONE);
		tabFolder.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		
		TabItem tbtmConnection = new TabItem(tabFolder, SWT.NONE);
		tbtmConnection.setText(Messages.MainWin_12);
		
		cmpConnect = new Composite(tabFolder, SWT.BORDER);
		tbtmConnection.setControl(cmpConnect);
		cmpConnect.setLayout(new FormLayout());
		

		btnConnect = new Button(cmpConnect, SWT.CENTER);
		btnConnect.setImage( SWTResourceManager.getImage(MainWin.class, "/fkgui/gfx/lightning.png") ); //$NON-NLS-1$
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
					
					connect();
				}
			}

		});
		btnConnect.setText(Messages.MainWin_15);
		
		txtPsw = new Text(cmpConnect, SWT.BORDER | SWT.PASSWORD);
		fd_btnConnect.bottom = new FormAttachment(txtPsw, 0, SWT.BOTTOM);
		FormData fd_txtPsw = new FormData();
		fd_txtPsw.bottom = new FormAttachment(0, 52);
		fd_txtPsw.right = new FormAttachment(0, 501);
		fd_txtPsw.top = new FormAttachment(0, 29);
		fd_txtPsw.left = new FormAttachment(0, 102);
		txtPsw.setLayoutData(fd_txtPsw);
		txtPsw.setFocus();
		txtPsw.addKeyListener( new KeyListener() {
			
			@Override
			public void keyReleased(KeyEvent arg0) {

				
			}
			
			@Override
			public void keyPressed(KeyEvent arg0) {
				if( arg0.character==(char)13 )
				{
					connect();
				}
			}
		});
		
		txtLog = new Text(cmpConnect, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI | SWT.WRAP);
		FormData fd_txtLog = new FormData();
		fd_txtLog.top = new FormAttachment(btnConnect, 6);
		fd_txtLog.left = new FormAttachment(0, 10);
		fd_txtLog.right = new FormAttachment(100, -10);
		fd_txtLog.bottom = new FormAttachment(100, -10);
		txtLog.setLayoutData(fd_txtLog);
		txtLog.setEditable(false);
		
		
		lblPort = new Label(cmpConnect, SWT.NONE);
		lblPort.setAlignment(SWT.RIGHT);
		fd_btnConnect.top = new FormAttachment(lblPort, 0, SWT.TOP);
		FormData fd_lblPort = new FormData();
		fd_lblPort.right = new FormAttachment(0, 86);
		fd_lblPort.top = new FormAttachment(0);
		fd_lblPort.left = new FormAttachment(0, 10);
		lblPort.setLayoutData(fd_lblPort);
		lblPort.setText("Port"); //$NON-NLS-1$
		
		txtDev = new Text(cmpConnect, SWT.BORDER);
		FormData fd_txtDev = new FormData();
		fd_txtDev.bottom = new FormAttachment(0, 23);
		fd_txtDev.right = new FormAttachment(0, 256);
		fd_txtDev.top = new FormAttachment(0);
		fd_txtDev.left = new FormAttachment(0, 102);
		txtDev.setLayoutData(fd_txtDev);
		txtDev.setFont(SWTResourceManager.getFont("Cantarell", 9, SWT.NORMAL)); //$NON-NLS-1$
		txtDev.setText( prefs.get(PREF_PORT, PREF_DEFAULT_DEVICE));
		lblPassword = new Label(cmpConnect, SWT.NONE);
		lblPassword.setAlignment(SWT.RIGHT);
		FormData fd_lblPassword = new FormData();
		fd_lblPassword.right = new FormAttachment(0, 95);
		fd_lblPassword.top = new FormAttachment(0, 29);
		fd_lblPassword.left = new FormAttachment(0, 10);
		lblPassword.setLayoutData(fd_lblPassword);
		lblPassword.setText(Messages.MainWin_18);
		
		chkAutoHide = new Button(cmpConnect, SWT.CHECK);
		FormData fd_chkAutoHide = new FormData();
		fd_chkAutoHide.top = new FormAttachment(btnConnect, 0, SWT.TOP);
		fd_chkAutoHide.left = new FormAttachment(txtDev, 6);
		fd_chkAutoHide.right = new FormAttachment(0, 501);
		chkAutoHide.setLayoutData(fd_chkAutoHide);
		chkAutoHide.setText(Messages.MainWin_19);
		
		chkAutoHide.setSelection( prefs.getBoolean(PREF_AUTOHIDE, false)) ;
		
		animation = new Animation(cmpConnect, SWT.NONE, 4);

		FormData fd_animation = new FormData();
		fd_animation.right = new FormAttachment(0, 86);
		fd_animation.top = new FormAttachment(0);
		fd_animation.left = new FormAttachment(0, 10);		
		
		animation.setLayoutData(fd_animation);


		
		animation.setVisible(false);
		animation.setPlaying(false);
		cmpConnect.setTabList(new Control[]{txtPsw, btnConnect});
		
	
		
		log(Messages.MainWin_22);


		shell.addShellListener( new ShellListener() {
			
			public void shellIconified(ShellEvent e) {
			}

			public void shellDeiconified(ShellEvent e) {
			}
			
			public void shellDeactivated(ShellEvent e) {
			}
			
			public void shellClosed(ShellEvent e) {
				shutDownApp();
			}
			
			public void shellActivated(ShellEvent e) {
				
			}
		} );
		

		if( System.getProperty("os.name").compareTo("Linux") == 0 )
		{
			Boolean gtkOk=false;
	        Map<String, String> env = System.getenv();
	        for (String envName : env.keySet()) {
	        	if( envName.compareTo("SWT_GTK3") == 0 && env.get(envName).compareTo("0") == 0 )
	        	{
	        		gtkOk=true;
	        	}
	        }
	        
	        if( !gtkOk )
	        {
	        	log("Warning: Enviroment variable SWT_GTK3 is not set to 0, if FinalKey GUI looks weird or crashes after connecting, try export GTK_SWT3=0 before running.");
	        }
		}

	}

	
	private void connect() {
		prefs.putBoolean(PREF_AUTOHIDE, chkAutoHide.getSelection() );
		fkSerial = new SerialWorker(mySelf);
		prefs.put(PREF_PORT, txtDev.getText() );
		fkSerial.connect(txtDev.getText(),txtPsw.getText());
		txtPsw.setText(""); //$NON-NLS-1$
	}
	
	
	@Override
	public void updateAccountList()
	{
		int free=256;
		clearSystray();
		lstAccounts.getList().removeAll();
		
		for( FkManager.Account a : FkManager.getInstance().getList() )
		{
			free--;
			lstAccounts.add( a );
							
			Menu menu = new Menu(a.name+" ["+a.num+"]"); //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-2$
			MenuItem both = new MenuItem(Messages.MainWin_25);
			MenuItem usr = new MenuItem(Messages.MainWin_26);
			MenuItem psw = new MenuItem(Messages.MainWin_27);
			menu.add(both);
			menu.add(usr);
			menu.add(psw);


			both.addActionListener(FkManager.getInstance());
			both.setActionCommand( "%"+a.num ); //$NON-NLS-1$



			psw.addActionListener(FkManager.getInstance());	
			psw.setActionCommand( "p"+a.num); //$NON-NLS-1$


			usr.addActionListener(FkManager.getInstance());	
			usr.setActionCommand( "u"+a.num ); //$NON-NLS-1$

			popup.add(menu);
		}
		
		lblNumFree.setText(" "+free+Messages.MainWin_32); //$NON-NLS-1$
		lblNumFree.pack();
		if( free == 0 )
		{
			btnNewAccoount.setVisible(false);
		} else {
			btnNewAccoount.setVisible(true);
		}
		
	}

	@Override
	public void serialEvent(SerialState state) {
		switch(state)
		{
		case Connected:
			shell.setText(Messages.MainWin_33);
			
			animation.setVisible(false);
			animation.setPlaying(false);


			btnConnect.setText(Messages.MainWin_34);
			btnConnect.setVisible(true);
			
			//Should we hide?
			if( prefs.getBoolean(PREF_AUTOHIDE, false) == true)
			{
				hideToTray();
			}
			
			addAccountsTab();
			
			
			tabFolder.setSelection(1);
			
			updateAccountList();

			//Update icons for systray and window
			shell.setImage( iconProgramOnline );
			trayIcon.setImage( iconSystrayOnline );

			int numAccounts=FkManager.getInstance().getList().size();
			if( numAccounts>0 )
			{
				trayIcon.displayMessage("FinalKey", numAccounts + Messages.MainWin_36+(( numAccounts >1)?"s":Messages.MainWin_38)+Messages.MainWin_39,  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			            TrayIcon.MessageType.INFO);
			}

			
			log(Messages.MainWin_40);
			break;
		case Connecting:
			shell.setText(Messages.MainWin_41);
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

			shell.setText(Messages.MainWin_42);
			txtPsw.setVisible(true);
			txtDev.setVisible(true);
			btnConnect.setText(Messages.MainWin_43);
			btnConnect.setVisible(true);
			lblPort.setVisible(true);
			lblPassword.setVisible(true);
			chkAutoHide.setVisible(true);
			clearSystray();

			//Update icons for systray and window
			shell.setImage( iconProgramOffline );
			trayIcon.setImage( iconSystrayOffline );

			if(lastState != state)
			{
				log(Messages.MainWin_44);
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
		tbtmAccounts.setText(Messages.MainWin_45);
		
		cmpAccounts = new Composite(tabFolder, SWT.BORDER);
		tbtmAccounts.setControl(cmpAccounts);
		cmpAccounts.setLayout(new FormLayout());
		
		btnNewAccoount = new Button(cmpAccounts, SWT.NONE);
		btnNewAccoount.setImage(SWTResourceManager.getImage(MainWin.class, "/fkgui/gfx/new.png")); //$NON-NLS-1$
		btnNewAccoount.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				NewAccountDialog dialog = new NewAccountDialog(shell, shell.getStyle() );
				shell.setEnabled(false);
				dialog.open();
				shell.setEnabled(true);
				updateAccountList();
				
			}
		});
		FormData fd_btnNewAccoount = new FormData();
		fd_btnNewAccoount.right = new FormAttachment(100, -10);
		fd_btnNewAccoount.top = new FormAttachment(0, 549);
		fd_btnNewAccoount.bottom = new FormAttachment(100, -6);
		btnNewAccoount.setLayoutData(fd_btnNewAccoount);
		btnNewAccoount.setText(Messages.MainWin_47);
		
		lblNumFree = new Label(cmpAccounts, SWT.NONE);
		lblNumFree.setText("Hello World!"); //$NON-NLS-1$
		
		FormData fd_lblNumFree = new FormData();
		fd_lblNumFree.bottom = new FormAttachment(100, -10);
		lblNumFree.setLayoutData(fd_lblNumFree);
		
		
		lstAccounts = new ListViewer(cmpAccounts, SWT.BORDER | SWT.V_SCROLL);
		List list = lstAccounts.getList();
		fd_lblNumFree.left = new FormAttachment(list, 0, SWT.LEFT);
		list.setLayoutData(new FormData());
		FormData fd_lstAccounts = new FormData();
		fd_lstAccounts.bottom = new FormAttachment(btnNewAccoount, -6);
		
		btnActivateAccount = new Button(cmpAccounts, SWT.NONE);
		fd_btnNewAccoount.left = new FormAttachment(btnActivateAccount, 6);
		btnActivateAccount.setImage(SWTResourceManager.getImage(MainWin.class, "/fkgui/gfx/lightbulb.png")); //$NON-NLS-1$
		FormData fd_btnActivateAccount = new FormData();
		fd_btnActivateAccount.right = new FormAttachment(100, -169);
		fd_btnActivateAccount.bottom = new FormAttachment(btnNewAccoount, 0, SWT.BOTTOM);
		fd_btnActivateAccount.top = new FormAttachment(btnNewAccoount, 0, SWT.TOP);
		btnActivateAccount.setLayoutData(fd_btnActivateAccount);
		fd_lstAccounts.top = new FormAttachment(0, 10);
		fd_lstAccounts.left = new FormAttachment(0, 10);
		fd_lstAccounts.right = new FormAttachment(100, -10);
		btnActivateAccount.setVisible(false);
		
		Button btnByAccountId = new Button(cmpAccounts, SWT.CHECK);
		fd_btnActivateAccount.left = new FormAttachment(btnByAccountId, 6);
		fd_lblNumFree.right = new FormAttachment(btnByAccountId, -6);
		btnByAccountId.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button btnAccId = (Button)e.getSource();
				FkManager.getInstance().sortById( btnAccId.getSelection() );
				prefs.putBoolean( PREF_SORT_BY_ID_KEY, btnAccId.getSelection() );
				updateAccountList();
			}
		});
		FormData fd_btnByAccountId = new FormData();
		fd_btnByAccountId.left = new FormAttachment(0, 139);
		fd_btnByAccountId.right = new FormAttachment(100, -398);
		fd_btnByAccountId.bottom = new FormAttachment(btnNewAccoount, 0, SWT.BOTTOM);
		btnByAccountId.setLayoutData(fd_btnByAccountId);
		btnByAccountId.setText(Messages.MainWin_btnByAccountId_text);
		btnByAccountId.setSelection( prefs.getBoolean( PREF_SORT_BY_ID_KEY, false));
		
		
		//lstAccounts.setLayoutData(fd_lstAccounts);
		lstAccounts.getControl().setLayoutData(fd_lstAccounts);
		lstAccounts.addDoubleClickListener( new IDoubleClickListener() {
			
			@Override
			public void doubleClick(DoubleClickEvent arg0) {
				// TODO Auto-generated method stub
				StructuredSelection selection = (StructuredSelection) arg0.getSelection();
				if( !selection.isEmpty() )
				{
					Account acc = (Account)selection.getFirstElement();
					showTrigDialog(acc);
				} else {
					System.out.println("Selected nothing."); //$NON-NLS-1$
				}
				
				
			}


		});
		
		lstAccounts.addSelectionChangedListener( new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent arg0) {
				StructuredSelection selection = (StructuredSelection) arg0.getSelection();
				if( !selection.isEmpty() )
				{
					Account acc = (Account)selection.getFirstElement();
					btnActivateAccount.setVisible(true);
					btnActivateAccount.setText(acc.name);
					
					//Remove any listeners
					for( Listener s :btnActivateAccount.getListeners(SWT.Selection) )
					{
						btnActivateAccount.removeListener(SWT.Selection, s);
					}
					
					btnActivateAccount.addSelectionListener( new SelectionListener() {
						
						@Override
						public void widgetSelected(SelectionEvent arg0) {
							
							StructuredSelection selection = (StructuredSelection) lstAccounts.getSelection();
							Account acc = (Account)selection.getFirstElement();
							showTrigDialog(acc);
						}
						
						@Override
						public void widgetDefaultSelected(SelectionEvent arg0) {
							// TODO Auto-generated method stub
							
						}
					});
					
				}
			}
		});
		
				
	}

	private void showTrigDialog(Account acc) {
		TriggerDialog diag = new TriggerDialog(shell, shell.getStyle(), acc, mySelf );

		//shell.setMinimized(true);
		shell.setEnabled(false);
		if( !((Boolean)diag.open()) )
		{
			//shell.setMinimized(false);
		}
		
		shell.setEnabled(true);
		
	}

	@Override
	public void updateCheckFinished(AutoUpdaterResultEvent event) {
		switch(event.result)
		{
		case CHECK_FAILED:
			System.out.println("Trouble checking for updates."); //$NON-NLS-1$
			break;
		case NO_UPDATE:
			System.out.println("No update avaiable at this time."); //$NON-NLS-1$
			break;
		case UPDATE_AVAILABLE:
			MessageBox dialog = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
			dialog.setText(Messages.MainWin_52+event.version+Messages.MainWin_53);
			dialog.setMessage(Messages.MainWin_54+event.message);
			dialog.open();	
			break;
		}
		
	}
}
