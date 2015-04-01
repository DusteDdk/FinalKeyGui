package fkgui;


import java.net.URI;
import java.util.Map;
import java.util.Vector;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Group;

public class MainWin implements ConsoleMsg, UpdateCheckResultListener {

	protected Shell shell;
	private Text txtPsw;
	public TrayIcon trayIcon;
	public PopupMenu popup;
	public MenuItem showMain;
	public MenuItem hideMain;
	public MenuItem exitApp;

	public Text txtLog;
	public Button btnConnect;
	public Label lblPort;
	public Label lblPassword;
	
	public Animation animation;
	
	public TabFolder tabFolder;

	private Text txtDev;
	Preferences prefs;
	MainWin mySelf;
	
	SerialWorker fkSerial;
	private boolean sysTrayIconVisible;
	
	Button btnOpenAccount;
	private Color blackColor;
	private Color grayColor;
	private Color defaultBgColor;
	private Color greenColor;
	private Color redColor;
	
	private static final String PREF_PORT ="lastUsedPortPref"; //$NON-NLS-1$
	private static final String PREF_DEFAULT_DEVICE = "/dev/FinalKey"; //$NON-NLS-1$
	private static final String PREF_AUTOHIDE = "hideMainWinAfterConnect"; //$NON-NLS-1$
	private static final String PREF_SORT_BY_ID_KEY = "sortAccountListById"; //$NON-NLS-1$
	private static final String PREF_SHOW_ACCOUNT_ID_IN_NAME_KEY = "showAccountIdInName"; //$NON-NLS-1$
	private static final String PREF_ALLOW_UPDATE_CHECK = "allowUpdateCheck"; //$NON-NLS-1$
	private static final String PREF_SHOW_ACCOUNTS_READY_NOTICE = "showAccountsReadyNotice"; //$NON-NLS-1$
	private static final String PREF_SHOW_SYSTRAY_NAME = "showSystrayName"; //$NON-NLS-1$
	private static final String PREF_HIDE_ON_CLOSE = "hideToSysTrayOnMainWinClose"; //$NON-NLS-1$

	public Composite cmpConnect;
	private Composite cmpAccounts;
	ListViewer lstAccounts;
	private List lstAccountsControl;
	Label lblNumFree;
	Button btnNewAccount;
	Button chkCheckForUpdates;
	Boolean hiddenAtStart=false;
	Button btnShowaccountsReady;
 
	//Icons
	private org.eclipse.swt.graphics.Image iconProgramOnline  = SWTResourceManager.getImage(MainWin.class, "/fkgui/gfx/finalkey.png");  //$NON-NLS-1$
	private org.eclipse.swt.graphics.Image iconProgramOffline  = SWTResourceManager.getImage(MainWin.class, "/fkgui/gfx/finalkey-big-offline.png");  //$NON-NLS-1$
	private Image iconSystrayOnline = Toolkit.getDefaultToolkit().createImage(getClass().getResource("gfx/systray-color.png"));  //$NON-NLS-1$
	private Image iconSystrayOffline= Toolkit.getDefaultToolkit().createImage(getClass().getResource("gfx/systray-offline.png"));  //$NON-NLS-1$

	private TabItem tbtmAccounts;
	private TabItem tbtmSettings;
	private Composite cmpSettings;
	private Button chkAutoHide;
	private Button chkSortByAccountId;
	private Button chkShowAccountId;
	private Thread updateCheckThread;
	private Text txtFilter;
	
	String systrayTipTxt;
	private Button btnHideToTrayOnClose;
	private TabItem tbtmThisFinalkey;
	private Composite composite;
	private Text txtBanner;	
	private Combo cmbLayout;
	private Group grpBackupAndRestore;
	private Group grpChangePassword;
	private Label lblAlwaysUseA;
	private Button btnReencrypt;
	private Group grpFormat;
	private Label lblNewLabel;
	private Button btnNewButton;
	private Button btnFormat;
	
	private Button btnFkSettingsSave;
	private Label lblMakeSureYou;

	
	
	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			MainWin window = new MainWin();

			for(String s : args )
			{
				if( s.compareTo("--hide") == 0 ) //$NON-NLS-1$
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

		serialEvent(SerialState.Disconnected);
		
		shell.getDisplay().addFilter(SWT.KeyDown, new Listener() {
			
			@Override
			public void handleEvent(Event arg0) {
				if( ( (arg0.stateMask&SWT.CTRL) == SWT.CTRL) && arg0.keyCode == 'f'  )
				{
					if( tabFolder.getItemCount() == 3 && !txtFilter.isFocusControl() )
					{
						tabFolder.setSelection(1);
						txtFilter.setFocus();
					}
				}
				
			}
		});


		checkForUpdates();

		if( hiddenAtStart )
		{
			hideToTray();
		} else {
			shell.open();
			shell.layout();
		}

		clearSystray(true);
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
	
	private void checkForUpdates() {
		if( prefs.getBoolean(PREF_ALLOW_UPDATE_CHECK, true) )
		{
			if( updateCheckThread == null )
			{
				updateCheckThread = new Thread(new UpdateChecker(this));
				updateCheckThread.start();
			} else {
				System.out.println(Messages.MainWin_0);
			}
		} else {
			System.out.println(Messages.MainWin_3);
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
			log("SystemTray is not supported."); //$NON-NLS-1$
			return;
		}

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		popup = new PopupMenu();
		trayIcon = new TrayIcon( iconSystrayOffline ); //$NON-NLS-1$

		trayIcon.setImageAutoSize(true);
		final SystemTray tray = SystemTray.getSystemTray();

		// Create a pop-up menu components
		showMain = new MenuItem(Messages.MainWin_7);
		hideMain = new MenuItem(Messages.MainWin_8);
		exitApp  = new MenuItem("Quit");

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

		exitApp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Display.getDefault().asyncExec( new Runnable(){

					public void run()
					{
						shutDownApp();
					}
				} );
			}
		});		
		
		trayIcon.setPopupMenu(popup);

		try {
			tray.add(trayIcon);
		} catch (AWTException e) {
			log("TrayIcon could not be added."); //$NON-NLS-1$
		}

	}

	private void setSystrayIconTip(String tip) {
		if( tip == null )
		{
			trayIcon.setToolTip(systrayTipTxt);
		} else {
			systrayTipTxt=tip;
			trayIcon.setToolTip(tip);
		}

		if( !prefs.getBoolean(PREF_SHOW_SYSTRAY_NAME, true) )
		{
			trayIcon.setToolTip(null);
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

	private void clearSystray(Boolean addExitBtn) {
			popup.removeAll();
			//Add components to pop-up menu
			if(shell.isVisible()==true)
			{
				popup.add(hideMain);
			} else {
				popup.add(showMain);
			}
			popup.addSeparator();

			if(addExitBtn)
			{
				popup.add(exitApp);
			}
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
	

	/*uint8_t EncryptedStorage::crc8(const uint8_t *addr, uint8_t len)
	{
	        uint8_t crc = 0;

	        while (len--) {
	                uint8_t inbyte = *addr++;
	                for (uint8_t i = 8; i; i--) {
	                        uint8_t mix = (crc ^ inbyte) & 0x01;
	                        crc >>= 1;
	                        if (mix) crc ^= 0x8C;
	                        inbyte >>= 1;
	                }
	        }
	        return crc;
	} */
	
	
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
		fd_btnConnect.top = new FormAttachment(0, 8);
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
		FormData fd_txtPsw = new FormData();

		fd_txtPsw.left = new FormAttachment(0, 102);
		txtPsw.setLayoutData(fd_txtPsw);
		txtPsw.setFocus();
		txtPsw.addKeyListener( new KeyListener() {
			@Override
			public void keyReleased(KeyEvent arg0) { }

			@Override
			public void keyPressed(KeyEvent arg0) {
				if( arg0.character==SWT.CR )
				{
					connect();
				}
			}
		});

		txtLog = new Text(cmpConnect, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI | SWT.WRAP);
		fd_btnConnect.bottom = new FormAttachment(txtPsw, 0, SWT.BOTTOM);
		FormData fd_txtLog = new FormData();
		fd_txtLog.bottom = new FormAttachment(100, -8);
		fd_txtLog.top = new FormAttachment(btnConnect, 8, SWT.BOTTOM);
		fd_txtLog.left = new FormAttachment(0, 8);
		fd_txtLog.right = new FormAttachment(btnConnect,0, SWT.RIGHT);
		txtLog.setLayoutData(fd_txtLog);
		txtLog.setEditable(false);

		lblPort = new Label(cmpConnect, SWT.NONE);
		lblPort.setAlignment(SWT.RIGHT);
		FormData fd_lblPort = new FormData();
		fd_lblPort.left = new FormAttachment(0, 10);
		lblPort.setLayoutData(fd_lblPort);
		lblPort.setText(Messages.MainWin_4);

		txtDev = new Text(cmpConnect, SWT.BORDER);
		fd_txtPsw.top = new FormAttachment(txtDev,4, SWT.BOTTOM);
		fd_lblPort.top = new FormAttachment(txtDev,0,SWT.CENTER);
		fd_lblPort.right = new FormAttachment(txtDev, -7);
		FormData fd_txtDev = new FormData();
		fd_txtDev.top = new FormAttachment(btnConnect,0,SWT.TOP);
		fd_txtDev.left = new FormAttachment(0, 102);
		txtDev.setLayoutData(fd_txtDev);
		txtDev.setText( prefs.get(PREF_PORT, PREF_DEFAULT_DEVICE));
		lblPassword = new Label(cmpConnect, SWT.NONE);
		lblPassword.setAlignment(SWT.RIGHT);
		FormData fd_lblPassword = new FormData();
		fd_lblPassword.right = new FormAttachment(0, 95);
		fd_lblPassword.top = new FormAttachment(txtPsw, 0, SWT.CENTER);
		fd_lblPassword.left = new FormAttachment(0, 10);
		lblPassword.setLayoutData(fd_lblPassword);
		lblPassword.setText(Messages.MainWin_18);

		animation = new Animation(cmpConnect, SWT.NONE, 4);
		fd_txtDev.right = new FormAttachment(animation,-16, SWT.LEFT );
		fd_txtPsw.right = new FormAttachment(animation,-16, SWT.LEFT );

		FormData fd_animation = new FormData();
		fd_animation.right = new FormAttachment(btnConnect, -16);
		fd_animation.top = new FormAttachment(0, 10);

		animation.setLayoutData(fd_animation);

		animation.setVisible(false);
		animation.setPlaying(false);
		cmpConnect.setTabList(new Control[]{txtPsw, btnConnect, txtDev});

		log(Messages.MainWin_22);


		//
		shell.addListener(SWT.Close, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				if( prefs.getBoolean(PREF_HIDE_ON_CLOSE, true))
				{
					arg0.doit=false;
					hideToTray();
				} else {
					shutDownApp();
				}
			}
		});


		if( System.getProperty("os.name").compareTo("Linux") == 0 ) //$NON-NLS-1$ //$NON-NLS-2$
		{
			Boolean gtkOk=false;
			Map<String, String> env = System.getenv();
			for (String envName : env.keySet()) {
				if( envName.compareTo("SWT_GTK3") == 0 && env.get(envName).compareTo("0") == 0 ) //$NON-NLS-1$ //$NON-NLS-2$
				{
					gtkOk=true;
				}
			}

			if( !gtkOk )
			{
				log("Warning: Enviroment variable SWT_GTK3 is not set to 0, if FinalKey GUI looks weird or crashes after connecting, try export GTK_SWT3=0 before running."); //$NON-NLS-1$
			}
		}

		Device display = Display.getCurrent();
		blackColor = display.getSystemColor(SWT.COLOR_BLACK );
		grayColor = display.getSystemColor(SWT.COLOR_GRAY );
		defaultBgColor = display.getSystemColor(SWT.COLOR_LIST_BACKGROUND );
		greenColor = display.getSystemColor(SWT.COLOR_DARK_GREEN );
		redColor = display.getSystemColor(SWT.COLOR_RED );

		
		//
		addSettingsTab();
		addThisFkTab();
		addAccountsTab();
		createSysTrayIcon();
	}


	private void connect() {
		fkSerial = new SerialWorker(mySelf);
		prefs.put(PREF_PORT, txtDev.getText() );
		fkSerial.connect(txtDev.getText(),txtPsw.getText());
		txtPsw.setText(""); //$NON-NLS-1$
	}

	@Override
	public void updateAccountList()
	{
		int free=256;
		clearSystray(false);
		lstAccounts.getList().removeAll();

		Boolean showAccountId = prefs.getBoolean( PREF_SHOW_ACCOUNT_ID_IN_NAME_KEY, false);

		for( FkManager.Account a : FkManager.getInstance().getList() )
		{
			free--;
			lstAccounts.add( a.showNumInName( showAccountId ) );

			//populate the popup
			Menu menu = new Menu(a.toString());
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
		
		//Add the exit button
		popup.addSeparator();
		popup.add(exitApp);

		lblNumFree.setText(" "+free+Messages.MainWin_32); //$NON-NLS-1$
		lblNumFree.pack();
		lblNumFree.getParent().layout();

		if( free == 0 )
		{
			btnNewAccount.setVisible(false);
		} else {
			btnNewAccount.setVisible(true);
		}

	}

	@Override
	public void serialEvent(SerialState state) {
		switch(state)
		{
		case Connected:
			shell.setText(Messages.MainWin_33 + FkManager.getInstance().getBanner());
			setSystrayIconTip(Messages.MainWin_33 + FkManager.getInstance().getBanner());

			animation.setVisible(false);
			animation.setPlaying(false);

			btnConnect.setText(Messages.MainWin_34);
			btnConnect.setEnabled(true);

			//Should we hide?
			if( prefs.getBoolean(PREF_AUTOHIDE, false) == true)
			{
				hideToTray();
			}

			addThisFkTab();
			addAccountsTab();
			

			
			tabFolder.setSelection(tbtmAccounts);

			updateAccountList();
			txtFilter.setFocus();

			//Update icons for systray and window
			shell.setImage( iconProgramOnline );
			trayIcon.setImage( iconSystrayOnline );

			int numAccounts=FkManager.getInstance().getList().size();
			if( numAccounts>0 && prefs.getBoolean( PREF_SHOW_ACCOUNTS_READY_NOTICE, true) )
			{

				class TrayIconMessageTask implements Runnable
				{
					private String msg;
					public TrayIconMessageTask(String _msg)
					{
						msg = _msg;
					}
					public void run()
					{
						trayIcon.displayMessage("FinalKey", msg, //$NON-NLS-1$ //$NON-NLS-2$
								TrayIcon.MessageType.INFO);
					}
				}

				new Thread( new TrayIconMessageTask(numAccounts +" "+ (( numAccounts >1)?Messages.MainWin_1:Messages.MainWin_2)) ).start();

			}

		//	log(Messages.MainWin_40);

			break;
		case Connecting:
			shell.setText(Messages.MainWin_41);
			animation.setVisible(true);
			animation.setPlaying(true);

			txtPsw.setEnabled(false);
			txtDev.setEnabled(false);
			btnConnect.setEnabled(false);

			lblPort.setEnabled(false);
			lblPassword.setEnabled(false);
			break;
		case Disconnected:
			setSystrayIconTip("The Final Key - Not Connected"); //$NON-NLS-1$

			fkSerial=null;
			animation.setVisible(false);
			animation.setPlaying(false);

			remAccountsTab();
			remThisFkTab();

			tabFolder.setSelection(0);

			shell.setText(Messages.MainWin_42);
			btnConnect.setText(Messages.MainWin_43);

			btnConnect.setEnabled(true);

			lblPort.setEnabled(true);
			lblPassword.setEnabled(true);
			txtPsw.setEnabled(true);
			txtPsw.setFocus();
			txtDev.setEnabled(true);

			cmpConnect.update();

			clearSystray(true);

			//Update icons for systray and window
			shell.setImage( iconProgramOffline );
			trayIcon.setImage( iconSystrayOffline );
			break;

		case Working:
			animation.setPlaying(false);
			animation.setVisible(false);
			break;

		default:
			break;
		}
		cmpConnect.layout();
	}

	private void updateSettingsSaveBtnVisibility()
	{
		boolean show=false;
		if( btnFkSettingsSave != null)
		{
			if( FkManager.getInstance().getBanner().compareTo( txtBanner.getText() ) != 0 )
			{
				show=true;
			}

			if( cmbLayout.getSelectionIndex() != -1  && cmbLayout.getItem(cmbLayout.getSelectionIndex()).compareTo(FkManager.getInstance().getCurrentLayout()) != 0 )
			{
				show=true;
			}

			if(show)
			{
				btnFkSettingsSave.setEnabled(true);
			} else {
				btnFkSettingsSave.setEnabled(false);
			}
		}
	}

	private void addThisFkTab()
	{
		tbtmThisFinalkey = new TabItem(tabFolder, SWT.NONE,1);
		tbtmThisFinalkey.setText(Messages.MainWin_tbtmThisFinalkey_text);

		composite = new Composite(tabFolder, SWT.NONE);
		tbtmThisFinalkey.setControl(composite);
		composite.setLayout(new FormLayout());

		Group group = new Group(composite, SWT.NONE);
		group.setText(Messages.MainWin_group_text);
		group.setLayout(new FormLayout());
		FormData fd_group = new FormData();
		fd_group.bottom = new FormAttachment(0, 100);
		fd_group.top = new FormAttachment(0, 10);
		fd_group.left = new FormAttachment(0, 10);
		fd_group.right = new FormAttachment(100, -10);
		group.setLayoutData(fd_group);


		Label lblBannerName = new Label(group, SWT.NONE);
		lblBannerName.setAlignment(SWT.RIGHT);
		FormData fd_lblBannerName = new FormData();
		lblBannerName.setLayoutData(fd_lblBannerName);
		lblBannerName.setText(Messages.MainWin_lblBannerName_text);

		txtBanner = new Text(group, SWT.BORDER);
		fd_lblBannerName.right = new FormAttachment(txtBanner, -6);
		fd_lblBannerName.left = new FormAttachment(txtBanner, -139, SWT.LEFT);
		fd_lblBannerName.bottom = new FormAttachment(txtBanner, 0, SWT.CENTER);
		FormData fd_txtBanner = new FormData();
		fd_txtBanner.right = new FormAttachment(0, 395);
		//fd_txtBanner.right = new FormAttachment(0, 455);
		fd_txtBanner.left = new FormAttachment(0, 149);
		txtBanner.setLayoutData(fd_txtBanner);
		txtBanner.setToolTipText(Messages.MainWin_text_1_toolTipText);
		txtBanner.setText(Messages.MainWin_text_1_text_1);
		txtBanner.addVerifyListener(new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent arg0) {
				System.out.println("Verify:"+arg0.text );
				arg0.doit=false;
				if(arg0.text.length()==0)
				{
					arg0.doit=true;
				} else if( FkManager.getInstance().isStringValidForFk(arg0.text))
				{
					if(txtBanner.getText().length()+arg0.text.length() < 32) //Banner can be a max of 31 chars long.
					{
						arg0.doit=true;
					}
				}
			}
		});
		txtBanner.addModifyListener( new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				updateSettingsSaveBtnVisibility();
			}
		});

		//Populate with data from FkManager
		txtBanner.setText( FkManager.getInstance().getBanner() );

		Label lblKeyboardLayout = new Label(group, SWT.NONE);
		lblKeyboardLayout.setAlignment(SWT.RIGHT);
		FormData fd_lblKeyboardLayout = new FormData();
		fd_lblKeyboardLayout.bottom = new FormAttachment(100, -27);
		fd_lblKeyboardLayout.right = new FormAttachment(lblBannerName, 0, SWT.RIGHT);
		fd_lblKeyboardLayout.left = new FormAttachment(0, 10);
		lblKeyboardLayout.setLayoutData(fd_lblKeyboardLayout);
		lblKeyboardLayout.setText(Messages.MainWin_lblKeyboardLayout_text);

		cmbLayout = new Combo(group, SWT.READ_ONLY);
		FormData fd_cmbLayout = new FormData();
		fd_cmbLayout.top = new FormAttachment(txtBanner, 6);
		fd_cmbLayout.bottom = new FormAttachment(100, -23);
		fd_cmbLayout.right = new FormAttachment(0, 395);
		fd_cmbLayout.left = new FormAttachment(0, 149);
		cmbLayout.setLayoutData(fd_cmbLayout);
		cmbLayout.setItems( FkManager.getInstance().getAvailableLayouts() );
		cmbLayout.addSelectionListener( new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				updateSettingsSaveBtnVisibility();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {} 
		});

		cmbLayout.select( cmbLayout.indexOf(FkManager.getInstance().getCurrentLayout() ));

		grpBackupAndRestore = new Group(composite, SWT.NONE);
		grpBackupAndRestore.setText(Messages.MainWin_grpBackupAndRestore_text);
		grpBackupAndRestore.setLayout(new FormLayout());
		FormData fd_grpBackupAndRestore = new FormData();
		fd_grpBackupAndRestore.top = new FormAttachment(group, 6);
		fd_grpBackupAndRestore.left = new FormAttachment(0, 10);
		fd_grpBackupAndRestore.bottom = new FormAttachment(0, 224);
		fd_grpBackupAndRestore.right = new FormAttachment(100, -10);

		btnFkSettingsSave = new Button(group, SWT.NONE);
		btnFkSettingsSave.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SaveSettingsDialog dialog = new SaveSettingsDialog(shell, shell.getStyle() );
				shell.setEnabled(false);
				
				if( (FkManager.getInstance().getBanner().compareTo( txtBanner.getText() ) != 0) )
				{
					dialog.bannerTxt = txtBanner.getText();
				}
				
				if( (cmbLayout.getSelectionIndex() != -1  && cmbLayout.getItem(cmbLayout.getSelectionIndex()).compareTo(FkManager.getInstance().getCurrentLayout()) != 0) )
				{
					dialog.Layout = cmbLayout.getSelectionIndex()+1; //FinalKey presents the layout choice between 1,2,3
				}
				
				dialog.open();
				shell.setEnabled(true);
				updateSettingsSaveBtnVisibility();
				
			}
		});
		FormData fd_btnSave = new FormData();
		fd_btnSave.left = new FormAttachment(cmbLayout, 218);
		fd_btnSave.right = new FormAttachment(100, -10);
		fd_btnSave.bottom = new FormAttachment(100, -3);
		btnFkSettingsSave.setLayoutData(fd_btnSave);
		btnFkSettingsSave.setText(Messages.MainWin_btnSave_text);
		grpBackupAndRestore.setLayoutData(fd_grpBackupAndRestore);
		btnFkSettingsSave.setEnabled(false);

		grpChangePassword = new Group(composite, SWT.NONE);
		grpChangePassword.setText(Messages.MainWin_grpChangePassword_text);
		grpChangePassword.setLayout(new FormLayout());
		FormData fd_grpChangePassword = new FormData();
		fd_grpChangePassword.top = new FormAttachment(grpBackupAndRestore, 6);
		fd_grpChangePassword.right = new FormAttachment(group, 0, SWT.RIGHT);

		Button btnBackup = new Button(grpBackupAndRestore, SWT.CENTER);
		btnBackup.setImage(SWTResourceManager.getImage(MainWin.class, "/fkgui/gfx/backup.png"));
		FormData fd_btnBackup = new FormData();
		fd_btnBackup.top = new FormAttachment(0, 10);
		fd_btnBackup.bottom = new FormAttachment(100, -10);
		fd_btnBackup.left = new FormAttachment(0, 10);
		btnBackup.setLayoutData(fd_btnBackup);
		btnBackup.setText(Messages.MainWin_btnBackupYourFinalkey_text);
		btnBackup.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shell.setEnabled(false);;
				Backup b = new Backup(shell, shell.getStyle() );
				b.open(false);
				shell.setEnabled(true);
			}
		});

		btnNewButton = new Button(grpBackupAndRestore, SWT.NONE);
		btnNewButton.setImage(SWTResourceManager.getImage(MainWin.class, "/fkgui/gfx/restore.png"));
		FormData fd_btnNewButton = new FormData();
		fd_btnNewButton.bottom = new FormAttachment(btnBackup, 0, SWT.BOTTOM);
		fd_btnNewButton.right = new FormAttachment(100, -10);
		fd_btnNewButton.top = new FormAttachment(0, 10);
		btnNewButton.setLayoutData(fd_btnNewButton);
		btnNewButton.setText(Messages.MainWin_btnNewButton_text);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shell.setEnabled(false);;
				Backup b = new Backup(shell, shell.getStyle() );
				b.open(true);
				shell.setEnabled(true);
			}
		});
		
		lblMakeSureYou = new Label(grpBackupAndRestore, SWT.NONE);
		fd_btnNewButton.left = new FormAttachment(lblMakeSureYou, 6);
		fd_btnBackup.right = new FormAttachment(lblMakeSureYou, -6);
		FormData fd_lblMakeSureYou = new FormData();
		fd_lblMakeSureYou.left = new FormAttachment(0, 136);
		fd_lblMakeSureYou.right = new FormAttachment(100, -144);
		fd_lblMakeSureYou.bottom = new FormAttachment(0, 92);
		fd_lblMakeSureYou.top = new FormAttachment(0, 10);
		lblMakeSureYou.setLayoutData(fd_lblMakeSureYou);
		lblMakeSureYou.setText(Messages.MainWin_lblMakeSureYou_text);
		fd_grpChangePassword.left = new FormAttachment(0, 10);
		grpChangePassword.setLayoutData(fd_grpChangePassword);

		lblAlwaysUseA = new Label(grpChangePassword, SWT.NONE);
		lblAlwaysUseA.setText(Messages.MainWin_lblAlwaysUseA_text);
		FormData fd_lblAlwaysUseA = new FormData();
		fd_lblAlwaysUseA.bottom = new FormAttachment(0, 79);
		fd_lblAlwaysUseA.top = new FormAttachment(0, 10);
		fd_lblAlwaysUseA.right = new FormAttachment(0, 675);
		fd_lblAlwaysUseA.left = new FormAttachment(0, 10);
		lblAlwaysUseA.setLayoutData(fd_lblAlwaysUseA);

		btnReencrypt = new Button(grpChangePassword, SWT.NONE);
		btnReencrypt.setText(Messages.MainWin_btnReencrypt_text);
		FormData fd_btnReencrypt = new FormData();
		fd_btnReencrypt.left = new FormAttachment(lblAlwaysUseA, -102);
		fd_btnReencrypt.bottom = new FormAttachment(100, -10);
		fd_btnReencrypt.top = new FormAttachment(lblAlwaysUseA, 6);
		fd_btnReencrypt.right = new FormAttachment(lblAlwaysUseA, 0, SWT.RIGHT);
		btnReencrypt.setLayoutData(fd_btnReencrypt);
		btnReencrypt.addSelectionListener( new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				shell.setEnabled(false);

				MessageBox d;
				d= new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO);
				d.setText("Danger ahead");
				d.setMessage("Changing your password will re-encrypt all accounts on your FinalKey.\nThis may take a while, and if the process is interrupted, data may be unaccessible.\nIt is highly recommended that you backup your FinalKey before proceeding.\nBe sure to remember the new password, your FinalKey can not be unlocked without it!\nDo you wish to continue ?");
				if( d.open() == SWT.YES )
				{
					ChangePassPrompt p = new ChangePassPrompt(shell, shell.getStyle());
					p.setText("Re-encrypt with new password");
					p.open(false);
				} else {
					d = new MessageBox(shell, SWT.ICON_INFORMATION );
					d.setText("Aborted");
					d.setMessage("Not changing the password.");
					d.open();
				}
				shell.setEnabled(true);
			}
		});

		grpFormat = new Group(composite, SWT.NONE);
		fd_grpChangePassword.bottom = new FormAttachment(100, -236);
		grpFormat.setText(Messages.MainWin_grpFormat_text);
		FormData fd_grpFormat = new FormData();
		fd_grpFormat.top = new FormAttachment(grpChangePassword, 6);
		fd_grpFormat.left = new FormAttachment(group, 0, SWT.LEFT);
		fd_grpFormat.right = new FormAttachment(group, 0, SWT.RIGHT);
		fd_grpFormat.bottom = new FormAttachment(100, -167);
		grpFormat.setLayoutData(fd_grpFormat);

		lblNewLabel = new Label(grpFormat, SWT.NONE);
		lblNewLabel.setBounds(10, 0, 366, 40);
		lblNewLabel.setText(Messages.MainWin_lblNewLabel_text_1);

		btnFormat = new Button(grpFormat, SWT.NONE);
		btnFormat.setBounds(594, 14, 81, 26);
		btnFormat.setText(Messages.MainWin_btnNewButton_1_text);
		btnFormat.addSelectionListener( new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				shell.setEnabled(false);

				MessageBox d;
				d= new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO);
				d.setText("Danger ahead");
				d.setMessage("Formatting your FinalKey overwrites all stored data, all accounts will be lost and unrecoverable. Are you sure you want to erase all data ?");
				if( d.open() == SWT.YES )
				{
					ChangePassPrompt p = new ChangePassPrompt(shell, shell.getStyle());
					p.setText("Format The FinalKey");
					p.newBanner = txtBanner.getText();
					p.newLayout = ""+(cmbLayout.getSelectionIndex()+1);
					p.open(true);
				} else {
					d = new MessageBox(shell, SWT.ICON_INFORMATION );
					d.setText("Aborted");
					d.setMessage("Format aborted.");
					d.open();
				}
				shell.setEnabled(true);
			}
		});

	}
	private void remThisFkTab() {
		if(tbtmThisFinalkey != null)
		{
			tbtmThisFinalkey.dispose();
		}
	}

	private void remAccountsTab() {
		if( tbtmAccounts != null )
		{
			tbtmAccounts.dispose();
		}

	}

	void updateFilteredList(Vector<Account> source)
	{
		lstAccounts.getList().removeAll();

		Boolean showAccountId = prefs.getBoolean( PREF_SHOW_ACCOUNT_ID_IN_NAME_KEY, false);

		for( FkManager.Account a : source )
		{
			lstAccounts.add( a.showNumInName( showAccountId ) );
		}
	}

	private void addAccountsTab() {

		tbtmAccounts = new TabItem(tabFolder, SWT.NONE, 1);//TabItem(tabFolder, SWT.NONE);
		tbtmAccounts.setText(Messages.MainWin_45);

		cmpAccounts = new Composite(tabFolder, SWT.BORDER);
		tbtmAccounts.setControl(cmpAccounts);
		cmpAccounts.setLayout(new FormLayout());

		btnNewAccount = new Button(cmpAccounts, SWT.NONE);
		btnNewAccount.setImage(SWTResourceManager.getImage(MainWin.class, "/fkgui/gfx/new.png")); //$NON-NLS-1$
		btnNewAccount.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				NewAccountDialog dialog = new NewAccountDialog(shell, shell.getStyle() );
				btnOpenAccount.setEnabled(false);
				shell.setEnabled(false);
				dialog.open();
				shell.setEnabled(true);
				updateAccountList();
			}
		});
		FormData fd_btnNewAccoount = new FormData();
		fd_btnNewAccoount.right = new FormAttachment(100, -10);
		fd_btnNewAccoount.top = new FormAttachment(100, -40);
		fd_btnNewAccoount.bottom = new FormAttachment(100, -6);
		fd_btnNewAccoount.left = new FormAttachment(100, -200);

		btnNewAccount.setLayoutData(fd_btnNewAccoount);
		btnNewAccount.setText(Messages.MainWin_47);

		lstAccounts = new ListViewer(cmpAccounts, SWT.BORDER | SWT.V_SCROLL);
		lstAccountsControl = lstAccounts.getList();
		lstAccountsControl.setLayoutData(new FormData());

		lblNumFree = new Label(cmpAccounts, SWT.NONE);
		//lblNumFree.setText("Hello World!"); //$NON-NLS-1$
		FormData fd_lblNumFree = new FormData();
		fd_lblNumFree.left = new FormAttachment(lstAccountsControl,0,SWT.LEFT);
		fd_lblNumFree.bottom = new FormAttachment(btnNewAccount, 0, SWT.CENTER);
		lblNumFree.setLayoutData(fd_lblNumFree);

		btnOpenAccount = new Button(cmpAccounts, SWT.NONE);
		//fd_lblNumFree.right = new FormAttachment(btnOpenAccount, -224);
		btnOpenAccount.setImage(SWTResourceManager.getImage(MainWin.class, "/fkgui/gfx/lightbulb.png")); //$NON-NLS-1$
		FormData fd_btnOpenAccount = new FormData();
		fd_btnOpenAccount.top = new FormAttachment(btnNewAccount, 0, SWT.TOP);
		fd_btnOpenAccount.bottom = new FormAttachment(btnNewAccount, 0, SWT.BOTTOM);
		fd_btnOpenAccount.right = new FormAttachment(btnNewAccount, -8, SWT.LEFT );
		btnOpenAccount.setLayoutData(fd_btnOpenAccount);
		btnOpenAccount.setText(Messages.MainWin_5);
		btnOpenAccount.getShell().layout();
		btnOpenAccount.setEnabled(false);


		FormData fd_lstAccounts = new FormData();
		fd_lstAccounts.top = new FormAttachment(0, 8);
		fd_lstAccounts.bottom = new FormAttachment(btnNewAccount, -6);

		txtFilter = new Text(cmpAccounts, SWT.BORDER);
		txtFilter.setText(Messages.MainWin_10);
		FormData fd_txtFilter = new FormData();
		fd_txtFilter.left = new FormAttachment(lblNumFree, 8, SWT.RIGHT);
		fd_txtFilter.bottom = new FormAttachment(btnOpenAccount,0, SWT.CENTER);
		fd_txtFilter.right = new FormAttachment(btnOpenAccount, -8, SWT.LEFT);
		txtFilter.setLayoutData(fd_txtFilter);
		txtFilter.setForeground( grayColor );
		txtFilter.addFocusListener( new FocusListener() {
			@Override
			public void focusLost(FocusEvent arg0) {
				txtFilter.setText(Messages.MainWin_10);
				txtFilter.setForeground( grayColor );
				txtFilter.setBackground( defaultBgColor );
			}

			@Override
			public void focusGained(FocusEvent arg0) {
				updateFilteredList( FkManager.getInstance().getList() );
				txtFilter.setForeground( blackColor );
				txtFilter.setText(""); //$NON-NLS-1$
				btnOpenAccount.setEnabled(false);
			}
		});

		txtFilter.addKeyListener( new KeyListener() {

			@Override
			public void keyReleased(KeyEvent arg0) {
				if( arg0.keyCode == SWT.ESC )
				{
					txtFilter.setText(""); //$NON-NLS-1$
				}

				txtFilterKeyPressEvent(arg0);
			}

			@Override
			public void keyPressed(KeyEvent arg0) {

			}
		});

		fd_lstAccounts.left = new FormAttachment(0, 10);
		fd_lstAccounts.right = new FormAttachment(100, -10);

		lstAccounts.getControl().setLayoutData(fd_lstAccounts);
		lstAccounts.addDoubleClickListener( new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent arg0) {
				StructuredSelection selection = (StructuredSelection) arg0.getSelection();
				if( !selection.isEmpty() )
				{
					Account acc = (Account)selection.getFirstElement();
					showTrigDialog(acc);
				}
			}


		});

		lstAccounts.addSelectionChangedListener( new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent arg0) {
				StructuredSelection selection = (StructuredSelection) arg0.getSelection();
				if( !selection.isEmpty() )
				{
					btnOpenAccount.setEnabled(true);

					//Remove any listeners
					for( Listener s :btnOpenAccount.getListeners(SWT.Selection) )
					{
						btnOpenAccount.removeListener(SWT.Selection, s);
					}

					btnOpenAccount.addSelectionListener( new SelectionListener() {

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


		lstAccountsControl.addKeyListener( new KeyListener() {
			@Override
			public void keyReleased(KeyEvent arg0) {
			}

			@Override
			public void keyPressed(KeyEvent arg0) {
				if( arg0.keyCode > 31 && arg0.keyCode < 126 && (arg0.stateMask&SWT.CTRL)!=SWT.CTRL )
				{
					txtFilter.setFocus();
					txtFilter.setText( ""+(char)arg0.keyCode ); //$NON-NLS-1$
					txtFilterKeyPressEvent(arg0);
					txtFilter.setSelection(1);
				} else if( arg0.keyCode == SWT.ARROW_UP)
				{
					//If selectionIndex is already 0, that means that the previous event caused that, so this time, it's changing nothing
					//and we can use it to wrap
					if( lstAccountsControl.getSelectionIndex() == 0)
					{
						lstAccountsControl.setSelection( lstAccountsControl.getItemCount()-1 );
						arg0.doit=false;
					}
				} else if( arg0.keyCode == SWT.ARROW_DOWN)
				{
					if( lstAccountsControl.getSelectionIndex() == lstAccountsControl.getItemCount()-1 )
					{
						lstAccountsControl.setSelection(0);
						arg0.doit=false;
					}
				}
			}
		});

		lstAccountsControl.addFocusListener( new FocusListener() {

			@Override
			public void focusLost(FocusEvent arg0) {

			}

			@Override
			public void focusGained(FocusEvent arg0) {
				if( lstAccountsControl.getItemCount() == 0 )
				{
					updateFilteredList(FkManager.getInstance().getList());
				}

			}
		});

		cmpAccounts.setTabList(new Control[]{txtFilter,lstAccountsControl, btnOpenAccount, btnNewAccount});
	}

	protected void txtFilterKeyPressEvent(KeyEvent arg0) {
		Vector<Account> res = null;

		if( txtFilter.getText().length() == 0 )
		{
			res=FkManager.getInstance().getList();
			txtFilter.setBackground( defaultBgColor );
		} else {
			res = FkManager.getInstance().getList(txtFilter.getText());
			if( res.size() < 1 )
			{
				txtFilter.setBackground( redColor );
			} else if( res.size() == 1 )
			{
				txtFilter.setBackground( greenColor );
				if( arg0.character== SWT.CR)
				{
					showTrigDialog(res.get(0));
				}
			} else {
				txtFilter.setBackground( defaultBgColor );
			}
		}


		if( res != null )
		{
			updateFilteredList( res );
			if( res.size() > 0 )
			{
				if( arg0.keyCode == SWT.ARROW_UP )
				{
					lstAccountsControl.setFocus();
					lstAccountsControl.setSelection( res.size()-1 );
				}

				if( arg0.keyCode == SWT.ARROW_DOWN )
				{
					lstAccountsControl.setFocus();
					lstAccountsControl.setSelection( 0 );
				}
			}
		}
	}

	private void addSettingsTab()
	{
		tbtmSettings = new TabItem(tabFolder, SWT.NONE);
		tbtmSettings.setText(Messages.MainWin_tbtmSettings_text);

		cmpSettings = new Composite(tabFolder, SWT.BORDER);
		tbtmSettings.setControl(cmpSettings);
		cmpSettings.setLayout(new FormLayout());

		chkAutoHide = new Button(cmpSettings, SWT.CHECK);
		chkAutoHide.setText(Messages.MainWin_6);
		chkAutoHide.setSelection( prefs.getBoolean( PREF_AUTOHIDE, false ) );
		FormData fd_chkAutoHide = new FormData();
		fd_chkAutoHide.top = new FormAttachment(0, 10);
		fd_chkAutoHide.left = new FormAttachment(0, 10);
		chkAutoHide.setLayoutData(fd_chkAutoHide);
		chkAutoHide.addSelectionListener( new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				prefs.putBoolean(PREF_AUTOHIDE, chkAutoHide.getSelection() );
			}
		});

		chkSortByAccountId = new Button(cmpSettings, SWT.CHECK);
		fd_chkAutoHide.right = new FormAttachment(chkSortByAccountId, 0, SWT.RIGHT);
		chkSortByAccountId.setText(Messages.MainWin_9);
		chkSortByAccountId.setSelection( prefs.getBoolean( PREF_SORT_BY_ID_KEY, false) );
		FormData fd_btnSortByAccountId = new FormData();
		fd_btnSortByAccountId.top = new FormAttachment(chkAutoHide, 6);
		fd_btnSortByAccountId.left = new FormAttachment(0, 10);
		fd_btnSortByAccountId.right = new FormAttachment(100, -10);
		chkSortByAccountId.setLayoutData(fd_btnSortByAccountId);
		chkSortByAccountId.addSelectionListener( new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				prefs.putBoolean( PREF_SORT_BY_ID_KEY, chkSortByAccountId.getSelection() );
				FkManager.getInstance().sortById( chkSortByAccountId.getSelection() );
				updateAccountList();
			}
		});

		chkShowAccountId = new Button(cmpSettings, SWT.CHECK);
		FormData fd_chkShowAccountId = new FormData();
		fd_chkShowAccountId.left = new FormAttachment(0, 10);
		fd_chkShowAccountId.right = new FormAttachment(100, -10);
		fd_chkShowAccountId.top = new FormAttachment(chkSortByAccountId, 6);
		chkShowAccountId.setLayoutData(fd_chkShowAccountId);
		chkShowAccountId.setText(Messages.MainWin_btnShowAccountId_text);
		chkShowAccountId.setSelection( prefs.getBoolean(PREF_SHOW_ACCOUNT_ID_IN_NAME_KEY, false) );
		chkShowAccountId.addSelectionListener( new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				prefs.putBoolean( PREF_SHOW_ACCOUNT_ID_IN_NAME_KEY, chkShowAccountId.getSelection() );
				updateAccountList();
			}
		});

		chkCheckForUpdates = new Button(cmpSettings, SWT.CHECK);
		FormData fd_chkCheckForUpdates = new FormData();
		fd_chkCheckForUpdates.left = new FormAttachment(0, 10);
		fd_chkCheckForUpdates.right = new FormAttachment(100, -10);
		fd_chkCheckForUpdates.top = new FormAttachment(chkShowAccountId, 6);
		chkCheckForUpdates.setLayoutData(fd_chkCheckForUpdates);
		chkCheckForUpdates.setText(Messages.MainWin_btnCheckForUpdates_text);
		chkCheckForUpdates.setSelection( prefs.getBoolean( PREF_ALLOW_UPDATE_CHECK, true) );
		chkCheckForUpdates.addSelectionListener( new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				prefs.putBoolean( PREF_ALLOW_UPDATE_CHECK, chkCheckForUpdates.getSelection() );
				checkForUpdates();
			}
		});

		btnShowaccountsReady = new Button(cmpSettings, SWT.CHECK);
		btnShowaccountsReady.setSelection( prefs.getBoolean( PREF_SHOW_ACCOUNTS_READY_NOTICE, true) );
		FormData fd_btnShowaccountsReady = new FormData();
		fd_btnShowaccountsReady.left = new FormAttachment(0, 10);
		fd_btnShowaccountsReady.right = new FormAttachment(100, -10);
		fd_btnShowaccountsReady.top = new FormAttachment(chkCheckForUpdates, 6);
		btnShowaccountsReady.setLayoutData(fd_btnShowaccountsReady);

		btnShowaccountsReady.setText(Messages.MainWin_btnShowaccountsReady_text);
		btnShowaccountsReady.addSelectionListener( new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
				prefs.putBoolean( PREF_SHOW_ACCOUNTS_READY_NOTICE, btnShowaccountsReady.getSelection() );
			}
		});

		Button btnShowSystrayName = new Button( cmpSettings, SWT.CHECK );
		btnShowSystrayName.setSelection( prefs.getBoolean( PREF_SHOW_SYSTRAY_NAME,  true) );
		FormData fd_btnShowSystrayName = new FormData();
		fd_btnShowSystrayName.left = new FormAttachment(0, 10);
		fd_btnShowSystrayName.right = new FormAttachment(100, -10);
		fd_btnShowSystrayName.top = new FormAttachment(btnShowaccountsReady, 6);
		btnShowSystrayName.setLayoutData(fd_btnShowSystrayName);
		btnShowSystrayName.setText( Messages.MainWin_14 );
		btnShowSystrayName.addSelectionListener( new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Button b = (Button)e.widget;
				prefs.putBoolean( PREF_SHOW_SYSTRAY_NAME, b.getSelection() );
				setSystrayIconTip(null);
			}
		});

		btnHideToTrayOnClose = new Button(cmpSettings, SWT.CHECK);
		btnHideToTrayOnClose.setSelection( prefs.getBoolean( PREF_HIDE_ON_CLOSE, true) );
		btnHideToTrayOnClose.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				prefs.putBoolean( PREF_HIDE_ON_CLOSE, btnHideToTrayOnClose.getSelection() );
			}
		});
		btnHideToTrayOnClose.setToolTipText(Messages.MainWin_btnHideToTrayOnClose_toolTipText);
		FormData fd_btnHideToTrayOnClose = new FormData();
		fd_btnHideToTrayOnClose.top = new FormAttachment(btnShowSystrayName, 6);
		fd_btnHideToTrayOnClose.right = new FormAttachment(0, 695);
		fd_btnHideToTrayOnClose.left = new FormAttachment(0, 10);
		btnHideToTrayOnClose.setLayoutData(fd_btnHideToTrayOnClose);
		btnHideToTrayOnClose.setText(Messages.MainWin_btnCheckButton_text);

	}

	private void showTrigDialog(Account acc) {
		Account selectedAccount = acc;
		TriggerDialog diag = new TriggerDialog(shell, shell.getStyle(), acc,
				mySelf);

		shell.setEnabled(false);
		diag.open();

		// Check if the account still exists
		Boolean deleted = true;
		for (Account a : FkManager.getInstance().getList()) {
			if (a.equals(selectedAccount)) {
				deleted = false;
				break;
			}
		}

		if (deleted) {
			btnOpenAccount.setEnabled(false);
		}

		txtFilter.setFocus();
		shell.setEnabled(true);
	}

	@Override
	public void updateCheckFinished(UpdateResultEvent event) {
		updateCheckThread=null;
		switch(event.result)
		{
		case CHECK_FAILED:
			System.out.println("Trouble checking for updates."); //$NON-NLS-1$
			break;
		case NO_UPDATE:
			System.out.println("No update avaiable at this time."); //$NON-NLS-1$
			break;
		case UPDATE_AVAILABLE:
			String title =Messages.MainWin_52+event.version+Messages.MainWin_53;
			String text = Messages.MainWin_54+event.message;
			log(title);
			log(text);

			MessageBox dialog = new MessageBox(shell, SWT.ICON_INFORMATION |SWT.YES | SWT.NO);
			dialog.setText(title);
			dialog.setMessage(text+"\n\n\n"+Messages.MainWin_13); //$NON-NLS-1$
			int res = dialog.open();

			if( res == SWT.YES )
			{
				if(Desktop.isDesktopSupported())
				{
					try {
						URI uri = new URI("http://finalkey.net/"); //$NON-NLS-1$
						log(Messages.MainWin_16+uri.toString());
						//FIXME: For whichever reason, this causes busy-cursor on the application, even though ui works fine.
						Desktop.getDesktop().browse(uri);
						log(Messages.MainWin_17);
					} catch (Exception e) {
						log(Messages.MainWin_20);
						log(Messages.MainWin_21);
						log(e.getLocalizedMessage());
					}
				}
			} else {
				log(Messages.MainWin_11);
			}

			break;
		}

	}
}
