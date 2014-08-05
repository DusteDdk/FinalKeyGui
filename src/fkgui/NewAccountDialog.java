package fkgui;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.wb.swt.SWTResourceManager;

public class NewAccountDialog extends Dialog implements FkActionEventListener {

	protected Object result;
	protected Shell shlNewAccount;
	private Text txtAccountName;
	private Text txtUserName;
	private Text txtManPSW;
	private Text txtSpecials;
	
	private enum FkNewAccStep { NAMES, PASSTYPE, PASS_MAN, PASS_AUT, SEPERATOR, REVIEW, CLICKBTN, SAVING };
	
	private String strAccountName = "";
	private String strUserName = "";
	private String strPassword = "";
	private String autoPassSpecials = "!@#,.-_()";
	private Boolean autoPassword = true;
	private Boolean autoPassAllSpecials = true;
	private int autoPassLen = 16;
	private Boolean seperatorTab=true;

	//NamePage
	Button btnNext0;

	
	//PassType
	Button radAutPSW;
	Button radManPSW;
	
	//ManPass
	Label lblTypeThePassword;
	FormData fd_lblTypeThePassword;	
	Label lblPassword;
	FormData fd_lblPassword;
	Button chkShowPsw;
	Button btnManPassPageNext;
	
	//Autopass
	Button radAllSymbols;
	Spinner spnLen;
	
	//Seperator
	Button radTabSep;
	
	//Saving
	Label txtBUSYMSG;
	Animation animation;
	
	NewAccountDialog mySelf;

	
	
	Composite composite;
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public NewAccountDialog(Shell parent, int style) {
		super(parent, style);
		
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		shlNewAccount.open();
		shlNewAccount.layout();
		Display display = getParent().getDisplay();
		mySelf = this;
		while (!shlNewAccount.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}
	
	private void checkNamePage()
	{
		if( txtAccountName.getText().length() > 0 && txtUserName.getText().length() > 0 )
		{
			btnNext0.setVisible(true);
		} else {
			btnNext0.setVisible(false);
		}
	}

	private void createNamePage()
	{
		composite = new Composite(shlNewAccount, SWT.NONE);
	//	tbtmStepUsername.setControl(composite);
		composite.setLayout(new FormLayout());
		
		
		
		Label lblEnterNewName = new Label(composite, SWT.NONE);
		FormData fd_lblEnterNewName = new FormData();
		fd_lblEnterNewName.bottom = new FormAttachment(0, 112);
		fd_lblEnterNewName.right = new FormAttachment(0, 603);
		fd_lblEnterNewName.top = new FormAttachment(0, 10);
		fd_lblEnterNewName.left = new FormAttachment(0, 10);
		lblEnterNewName.setLayoutData(fd_lblEnterNewName);
		lblEnterNewName.setText("This creates a new account on your FinalKey.\nTo begin, choose a name for your account, for example the\nname of the website or service you want to login to.\nThis is the name that will be visible in the list of accounts.");
		Label lblUsername = new Label(composite, SWT.NONE);
		FormData fd_lblUsername = new FormData();
		lblUsername.setLayoutData(fd_lblUsername);
		lblUsername.setText("Account Name:");
		
		txtAccountName = new Text(composite, SWT.BORDER);
		fd_lblUsername.bottom = new FormAttachment(txtAccountName, 0, SWT.BOTTOM);
		fd_lblUsername.right = new FormAttachment(txtAccountName, -6);
		FormData fd_txtAccountName = new FormData();
		fd_txtAccountName.top = new FormAttachment(lblEnterNewName, 6);
		fd_txtAccountName.right = new FormAttachment(100, -274);
		fd_txtAccountName.left = new FormAttachment(0, 180);
		txtAccountName.setLayoutData(fd_txtAccountName);
		txtAccountName.addModifyListener( new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				checkNamePage();

			}
		});
		
		Label lblNewLabel = new Label(composite, SWT.NONE);
		FormData fd_lblNewLabel = new FormData();
		fd_lblNewLabel.left = new FormAttachment(0, 10);
		fd_lblNewLabel.right = new FormAttachment(100);
		fd_lblNewLabel.top = new FormAttachment(lblUsername, 20);
		lblNewLabel.setLayoutData(fd_lblNewLabel);
		lblNewLabel.setText("The username is the name that The Final Key will\ntype into the service when logging in, it is typically an E-Mail address.");
		
		Label lblUsername_1 = new Label(composite, SWT.NONE);
		FormData fd_lblUsername_1 = new FormData();
		fd_lblUsername_1.top = new FormAttachment(lblNewLabel, 6);
		fd_lblUsername_1.right = new FormAttachment(lblUsername, 0, SWT.RIGHT);
		lblUsername_1.setLayoutData(fd_lblUsername_1);
		lblUsername_1.setText("Username:");
		
		txtUserName = new Text(composite, SWT.BORDER);
		FormData fd_txtUserName = new FormData();
		fd_txtUserName.left = new FormAttachment(txtAccountName, 0, SWT.LEFT);
		fd_txtUserName.right = new FormAttachment(txtAccountName, 0, SWT.RIGHT);
		fd_txtUserName.top = new FormAttachment(lblNewLabel, 6);
		txtUserName.setLayoutData(fd_txtUserName);
		txtUserName.addModifyListener( new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				checkNamePage();
			}
		});
		
		Button btnCancel = new Button(composite, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shlNewAccount.close();
			}
		});
		FormData fd_btnCancel = new FormData();
		fd_btnCancel.bottom = new FormAttachment(100, -10);
		fd_btnCancel.left = new FormAttachment(0, 10);
		btnCancel.setLayoutData(fd_btnCancel);
		btnCancel.setText("Cancel");
		
		btnNext0 = new Button(composite, SWT.NONE);
		btnNext0.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				strAccountName = txtAccountName.getText();
				strUserName = txtUserName.getText();
				updatePage(FkNewAccStep.PASSTYPE);
			}
		});
		btnNext0.setText("Next");
		FormData fd_btnNext0 = new FormData();
		fd_btnNext0.bottom = new FormAttachment(btnCancel, 0, SWT.BOTTOM);
		fd_btnNext0.right = new FormAttachment(lblEnterNewName, 0, SWT.RIGHT);
		btnNext0.setLayoutData(fd_btnNext0);
		btnNext0.setVisible(false);
		
		txtUserName.setText( strUserName );
		txtAccountName.setText( strAccountName );

        Control[] controls = new Control[] { txtAccountName, txtUserName, btnNext0, btnCancel };
        composite.setTabList(controls);		
		
	}
	
	
	private void createPassTypePage()
	{
		composite = new Composite(shlNewAccount, SWT.NONE);
		composite.setLayout(new FormLayout());
		
		Label lblNextUpSetting = new Label(composite, SWT.NONE);
		FormData fd_lblNextUpSetting = new FormData();
		fd_lblNextUpSetting.right = new FormAttachment(0, 603);
		fd_lblNextUpSetting.top = new FormAttachment(0, 10);
		fd_lblNextUpSetting.left = new FormAttachment(0, 10);
		lblNextUpSetting.setLayoutData(fd_lblNextUpSetting);
		lblNextUpSetting.setText("Next up: Setting a safe password!\nYou have two options: Manually enter a password or to\nhave The Final Key generate a strong and random password,\nit is strongly recommended to let The Final Key generate a\nrandom password, select the longest that is allowed by the service.");
		
		radAutPSW = new Button(composite, SWT.RADIO);
		if( autoPassword )
		{
			radAutPSW.setSelection(true);
		}
		FormData fd_radAutPSW = new FormData();
		fd_radAutPSW.top = new FormAttachment(lblNextUpSetting, 37);
		fd_radAutPSW.left = new FormAttachment(0, 213);
		radAutPSW.setLayoutData(fd_radAutPSW);
		radAutPSW.setText("Automatic");
		
		radManPSW = new Button(composite, SWT.RADIO);
		if( !autoPassword )
		{
			radManPSW.setSelection(true);
		}		
		FormData fd_radManPSW = new FormData();
		fd_radManPSW.top = new FormAttachment(radAutPSW, 6);
		fd_radManPSW.left = new FormAttachment(radAutPSW, 0, SWT.LEFT);
		radManPSW.setLayoutData(fd_radManPSW);
		radManPSW.setText("Manual Entry");
		
		Button btnNext1 = new Button(composite, SWT.NONE);
		btnNext1.setText("Next");
		FormData fd_btnNext1 = new FormData();
		fd_btnNext1.bottom = new FormAttachment(100, -10);
		fd_btnNext1.right = new FormAttachment(100, -10);
		btnNext1.setLayoutData(fd_btnNext1);
		btnNext1.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if( radAutPSW.getSelection() )
				{
					updatePage(FkNewAccStep.PASS_AUT);
					autoPassword = true;
				} else {
					updatePage(FkNewAccStep.PASS_MAN);
					autoPassword = false;
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		Button btnBack0 = new Button(composite, SWT.NONE);
		btnBack0.setText("Back");
		FormData fd_btnBack0 = new FormData();
		fd_btnBack0.bottom = new FormAttachment(btnNext1, 0, SWT.BOTTOM);
		fd_btnBack0.left = new FormAttachment(lblNextUpSetting, 0, SWT.LEFT);
		btnBack0.setLayoutData(fd_btnBack0);
		btnBack0.addSelectionListener( new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				updatePage( FkNewAccStep.NAMES );
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
        Control[] controls = new Control[] { radAutPSW, radManPSW, btnNext1, btnBack0 };
        composite.setTabList(controls);
        radAutPSW.setFocus();
}
	
	void autoPassPageSaveValues()
	{
		autoPassAllSpecials = radAllSymbols.getSelection();
		autoPassSpecials = txtSpecials.getText();
		autoPassLen = spnLen.getSelection();		
	}
	void createPassAutPage()
	{
		composite = new Composite(shlNewAccount, SWT.NONE);
		
		Label lblBeforeCreatingA = new Label(composite, SWT.NONE);
		lblBeforeCreatingA.setBounds(10, 10, 593, 104);
		lblBeforeCreatingA.setText("Before creating a password, please select how long it should be.\nA longer password is safer, but the maximum allowed length varies,\ndepending on where you're using it. For example many websites do not\nallow passwords longer than 16 characters.");
		
		Label lblPasswordLength = new Label(composite, SWT.NONE);
		lblPasswordLength.setBounds(32, 132, 127, 23);
		lblPasswordLength.setText("Password length:");
		
		spnLen = new Spinner(composite, SWT.BORDER);
		spnLen.setMaximum(128);
		spnLen.setSelection(autoPassLen);
		spnLen.setBounds(165, 120, 62, 35);
		
		Label lblASafePassword = new Label(composite, SWT.NONE);
		lblASafePassword.setBounds(10, 161, 593, 76);
		lblASafePassword.setText("A strong password contains not only uppercase and lowercase\nletters and numbers, but also other symbols.\nIt differs which symbols are allowed, some allow all ASCII symbols.");
		
		radAllSymbols = new Button(composite, SWT.RADIO);
		if( autoPassAllSpecials )
		{
			radAllSymbols.setSelection(true);
		}
		radAllSymbols.setBounds(10, 251, 120, 27);
		radAllSymbols.setText("All symbols");
		
		Button radOnlySelected = new Button(composite, SWT.RADIO);
		if( !autoPassAllSpecials )
		{
			radOnlySelected.setSelection(true);
		}
		radOnlySelected.setBounds(165, 251, 120, 27);
		radOnlySelected.setText("Only these:");
		
		txtSpecials = new Text(composite, SWT.BORDER);
		txtSpecials.setText(autoPassSpecials);
		txtSpecials.setBounds(291, 243, 312, 35);
		
		Button button_5 = new Button(composite, SWT.NONE);
		button_5.setText("Back");
		button_5.setBounds(10, 288, 48, 35);
		button_5.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				autoPassPageSaveValues();
				updatePage( FkNewAccStep.PASSTYPE );
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {				
			}
		});
		
		Button button_6 = new Button(composite, SWT.NONE);
		button_6.setText("Next");
		button_6.setBounds(555, 288, 48, 35);
		button_6.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				autoPassPageSaveValues();
				updatePage( FkNewAccStep.SEPERATOR );
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {				
			}
		});
		
        Control[] controls = new Control[] { spnLen, radAllSymbols, radOnlySelected, button_6, button_5 };
        composite.setTabList(controls);			
		spnLen.setFocus();

	}
	
	
	void makePswField(Boolean showPsw)
	{
		txtManPSW = new Text(composite, SWT.BORDER | ((showPsw)?0:SWT.PASSWORD) );
		fd_lblPassword.bottom = new FormAttachment(txtManPSW, 0, SWT.BOTTOM);
		FormData fd_txtManPSW = new FormData();
		fd_txtManPSW.right = new FormAttachment(100, -10);
		fd_txtManPSW.left = new FormAttachment(lblPassword, 6);
		fd_txtManPSW.top = new FormAttachment(lblTypeThePassword, 6);
		txtManPSW.setLayoutData(fd_txtManPSW);
		txtManPSW.setText(strPassword);
		txtManPSW.addModifyListener( new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent arg0) {
				strPassword = txtManPSW.getText();
				if( txtManPSW.getText().length() > 0 )
				{
					btnManPassPageNext.setVisible(true);
				} else {
					btnManPassPageNext.setVisible(false);
				}
				
			}
		});
		
		chkShowPsw = new Button(composite, SWT.CHECK);
		FormData fd_chkShowPsw = new FormData();
		fd_chkShowPsw.top = new FormAttachment(txtManPSW, 6);
		fd_chkShowPsw.left = new FormAttachment(txtManPSW, 0, SWT.LEFT);
		chkShowPsw.setLayoutData(fd_chkShowPsw);
		chkShowPsw.setText("Show Password");
		chkShowPsw.setSelection(showPsw);
		
		chkShowPsw.addSelectionListener( new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				Button btnChk = (Button)arg0.getSource();

				txtManPSW.dispose();
				
				
				if( btnChk.getSelection() )
				{
					chkShowPsw.dispose();
					makePswField(true);
				} else {
					chkShowPsw.dispose();
					makePswField(false);
				}
				
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});		
		composite.layout();
	}
	

	
	void createPassManPage()
	{
		composite = new Composite(shlNewAccount, SWT.NONE);
		composite.setLayout(new FormLayout());
		
		Button BtnBack1 = new Button(composite, SWT.NONE);
		FormData fd_BtnBack1 = new FormData();
		fd_BtnBack1.top = new FormAttachment(0, 288);
		fd_BtnBack1.left = new FormAttachment(0, 10);
		BtnBack1.setLayoutData(fd_BtnBack1);
		BtnBack1.setText("Back");
		BtnBack1.addSelectionListener( new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				updatePage(FkNewAccStep.PASSTYPE);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
			}
		});
		
		btnManPassPageNext = new Button(composite, SWT.NONE);
		FormData fd_button_4 = new FormData();
		fd_button_4.top = new FormAttachment(0, 288);
		fd_button_4.left = new FormAttachment(0, 555);
		btnManPassPageNext.setLayoutData(fd_button_4);
		btnManPassPageNext.setText("Next");
		btnManPassPageNext.setVisible(false);
		btnManPassPageNext.addSelectionListener( new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				updatePage(FkNewAccStep.SEPERATOR);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {				
			}
		});
		
		lblTypeThePassword = new Label(composite, SWT.NONE);
		fd_lblTypeThePassword = new FormData();
		fd_lblTypeThePassword.top = new FormAttachment(0, 10);
		fd_lblTypeThePassword.left = new FormAttachment(0, 10);
		lblTypeThePassword.setLayoutData(fd_lblTypeThePassword);
		lblTypeThePassword.setText("Type the password your wish to use for ACCOUNT\nIn the box below.");
		
		lblPassword = new Label(composite, SWT.NONE);
		fd_lblPassword = new FormData();
		fd_lblPassword.left = new FormAttachment(BtnBack1, 0, SWT.LEFT);
		lblPassword.setLayoutData(fd_lblPassword);
		lblPassword.setText("Password:");
		
		makePswField(false);
		
		
        Control[] controls = new Control[] { txtManPSW, btnManPassPageNext, BtnBack1 };
        composite.setTabList(controls);	
        txtManPSW.setFocus();
		
	}
	

	void createSeperatorPage()
	{
		composite = new Composite(shlNewAccount, SWT.NONE);
		composite.setLayout(new FormLayout());
		
		Label lblToMakeFinalkey = new Label(composite, SWT.NONE);
		FormData fd_lblToMakeFinalkey = new FormData();
		fd_lblToMakeFinalkey.top = new FormAttachment(0, 10);
		fd_lblToMakeFinalkey.left = new FormAttachment(0, 10);
		lblToMakeFinalkey.setLayoutData(fd_lblToMakeFinalkey);
		lblToMakeFinalkey.setText("When you trigger the Final Key to type an account (Username + Password), it\nwill also type a \"seperation\" key, to jump from the Username input to the\nPassword input. On most websites, the \"tab\" key is used for this.\nHowever, in some applications, the \"enter\" key is used.\nHere you can select which key can be used between the username and password\nfor this account.");
		
		radTabSep = new Button(composite, SWT.RADIO);
		if( seperatorTab )
		{
			radTabSep.setSelection(true);
		}
		FormData fd_radTabSep = new FormData();
		fd_radTabSep.top = new FormAttachment(lblToMakeFinalkey, 24);
		fd_radTabSep.left = new FormAttachment(0, 231);
		radTabSep.setLayoutData(fd_radTabSep);
		radTabSep.setText("Tab");
		
		Button radEnterSep = new Button(composite, SWT.RADIO);
		if( !seperatorTab )
		{
			radEnterSep.setSelection(true);
		}
		FormData fd_radEnterSep = new FormData();
		fd_radEnterSep.top = new FormAttachment(radTabSep, 6);
		fd_radEnterSep.left = new FormAttachment(radTabSep, 0, SWT.LEFT);
		radEnterSep.setLayoutData(fd_radEnterSep);
		radEnterSep.setText("Enter");
		
		Button button_7 = new Button(composite, SWT.NONE);
		button_7.setText("Back");
		FormData fd_button_7 = new FormData();
		fd_button_7.bottom = new FormAttachment(100, -10);
		fd_button_7.left = new FormAttachment(0, 10);
		button_7.setLayoutData(fd_button_7);
		button_7.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				seperatorTab = radTabSep.getSelection();
				if( autoPassword )
				{
					updatePage(FkNewAccStep.PASS_AUT);
				} else {
					updatePage(FkNewAccStep.PASS_MAN);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
		
		Button button_8 = new Button(composite, SWT.NONE);
		button_8.setText("Next");
		FormData fd_button_8 = new FormData();
		fd_button_8.bottom = new FormAttachment(button_7, 0, SWT.BOTTOM);
		fd_button_8.right = new FormAttachment(lblToMakeFinalkey, 0, SWT.RIGHT);
		button_8.setLayoutData(fd_button_8);
		button_8.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				seperatorTab = radTabSep.getSelection();
				updatePage(FkNewAccStep.REVIEW);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
		

        Control[] controls = new Control[] { radTabSep, radEnterSep, button_8, button_7 };
        composite.setTabList(controls);		
        radTabSep.setFocus();
		
	}
	

	void createReviewPage()
	{
		composite = new Composite(shlNewAccount, SWT.NONE);
		composite.setLayout(new FormLayout());
		
		Label lblHeresAnOverview = new Label(composite, SWT.NONE);
		FormData fd_lblHeresAnOverview = new FormData();
		fd_lblHeresAnOverview.right = new FormAttachment(0, 603);
		fd_lblHeresAnOverview.top = new FormAttachment(0, 10);
		fd_lblHeresAnOverview.left = new FormAttachment(0, 10);
		lblHeresAnOverview.setLayoutData(fd_lblHeresAnOverview);
		lblHeresAnOverview.setText("Here's an overview of the account information");
		
		Label lblAccountName = new Label(composite, SWT.NONE);
		FormData fd_lblAccountName = new FormData();
		fd_lblAccountName.left = new FormAttachment(0, 120);
		fd_lblAccountName.top = new FormAttachment(lblHeresAnOverview, 18);
		lblAccountName.setLayoutData(fd_lblAccountName);
		lblAccountName.setText("Account Name:");
		
		Label lblPasswordType = new Label(composite, SWT.NONE);
		FormData fd_lblPasswordType = new FormData();
		fd_lblPasswordType.right = new FormAttachment(lblAccountName, 0, SWT.RIGHT);
		lblPasswordType.setLayoutData(fd_lblPasswordType);
		lblPasswordType.setText("Password:");
		
		Label lblUserName = new Label(composite, SWT.NONE);
		fd_lblPasswordType.top = new FormAttachment(lblUserName, 6);
		FormData fd_lblUserName = new FormData();
		fd_lblUserName.top = new FormAttachment(lblAccountName, 6);
		fd_lblUserName.right = new FormAttachment(lblAccountName, 0, SWT.RIGHT);
		lblUserName.setLayoutData(fd_lblUserName);
		lblUserName.setText("User Name:");
		
		Label lblIfEverythingLooks = new Label(composite, SWT.NONE);
		FormData fd_lblIfEverythingLooks = new FormData();
		fd_lblIfEverythingLooks.top = new FormAttachment(lblPasswordType, 49);
		fd_lblIfEverythingLooks.left = new FormAttachment(lblHeresAnOverview, 0, SWT.LEFT);
		lblIfEverythingLooks.setLayoutData(fd_lblIfEverythingLooks);
		lblIfEverythingLooks.setText("If everything looks okay, press save and wait until the Final Key blinks,\nwhen it blinks, press the button to allow the account to be saved.");
		
		Label lblSeperator = new Label(composite, SWT.NONE);
		FormData fd_lblSeperator = new FormData();
		fd_lblSeperator.top = new FormAttachment(lblPasswordType, 6);
		fd_lblSeperator.right = new FormAttachment(lblAccountName, 0, SWT.RIGHT);
		lblSeperator.setLayoutData(fd_lblSeperator);
		lblSeperator.setText("Seperator:");
		
		Button button_9 = new Button(composite, SWT.NONE);
		button_9.setText("Back");
		FormData fd_button_9 = new FormData();
		fd_button_9.bottom = new FormAttachment(100, -10);
		fd_button_9.left = new FormAttachment(lblHeresAnOverview, 0, SWT.LEFT);
		button_9.setLayoutData(fd_button_9);
		button_9.addSelectionListener( new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				updatePage(FkNewAccStep.SEPERATOR);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {		
			}
		});
		
		Button btnSave = new Button(composite, SWT.NONE);
		FormData fd_btnSave = new FormData();
		fd_btnSave.bottom = new FormAttachment(button_9, 0, SWT.BOTTOM);
		fd_btnSave.right = new FormAttachment(lblHeresAnOverview, 0, SWT.RIGHT);
		btnSave.setLayoutData(fd_btnSave);
		btnSave.setText("Save");
		btnSave.setFocus();
		
		btnSave.addSelectionListener( new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				MessageBox dialog = new MessageBox(shlNewAccount, SWT.ICON_INFORMATION | SWT.OK );
				dialog.setText("Get ready");
				dialog.setMessage("When you press OK, The Final Key will start blinking, you then have 5 seconds to press the button to save the account.");
				dialog.open();
				FkManager.getInstance().createAccount( strAccountName, strUserName, autoPassword, autoPassLen, autoPassAllSpecials, autoPassSpecials, strPassword, seperatorTab, mySelf );
				updatePage(FkNewAccStep.SAVING);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
			}
		});
		
		Label lblAccName = new Label(composite, SWT.NONE);
		FormData fd_lblAccName = new FormData();
		fd_lblAccName.right = new FormAttachment(lblHeresAnOverview, 0, SWT.RIGHT);
		fd_lblAccName.top = new FormAttachment(lblHeresAnOverview, 18);
		fd_lblAccName.left = new FormAttachment(lblAccountName, 6);
		lblAccName.setLayoutData(fd_lblAccName);
		lblAccName.setText(strAccountName);
		
		Label lblUsrName = new Label(composite, SWT.NONE);
		FormData fd_lblUsrName = new FormData();
		fd_lblUsrName.top = new FormAttachment(lblAccName, 6);
		fd_lblUsrName.left = new FormAttachment(lblUserName, 6);
		fd_lblUsrName.right = new FormAttachment(100, -10);
		lblUsrName.setLayoutData(fd_lblUsrName);
		lblUsrName.setText(strUserName);
		
		Label lblPasswordInfo = new Label(composite, SWT.NONE);
		
		String passInfo;
		if( autoPassword )
		{
			passInfo = "Automatic, " + autoPassLen + " long, ";
			if( autoPassAllSpecials )
			{
				passInfo += "all specials";
			} else {
				passInfo += "{"+autoPassSpecials+ "}";
			}
			
		} else {
			passInfo ="Manual, " + strPassword.length() +" long";
		}
		
		lblPasswordInfo.setText(passInfo);
		FormData fd_lblPasswordInfo = new FormData();
		fd_lblPasswordInfo.right = new FormAttachment(lblPasswordType, 373, SWT.RIGHT);
		fd_lblPasswordInfo.top = new FormAttachment(lblUsrName, 6);
		fd_lblPasswordInfo.left = new FormAttachment(lblPasswordType, 6);
		lblPasswordInfo.setLayoutData(fd_lblPasswordInfo);
		
		Label lblSeperatorInfo = new Label(composite, SWT.NONE);
		
		String sepInfo;
		if( seperatorTab )
		{
			sepInfo = "Tab Key";
		} else {
			sepInfo = "Enter Key";
		}
		
		lblSeperatorInfo.setText(sepInfo);
		FormData fd_lblSeperatorInfo = new FormData();
		fd_lblSeperatorInfo.right = new FormAttachment(lblSeperator, 373, SWT.RIGHT);
		fd_lblSeperatorInfo.top = new FormAttachment(lblPasswordInfo, 6);
		fd_lblSeperatorInfo.left = new FormAttachment(lblSeperator, 6);
		lblSeperatorInfo.setLayoutData(fd_lblSeperatorInfo);
		
        Control[] controls = new Control[] { btnSave, button_9 };
        composite.setTabList(controls);				

	}
	
	void createSavingPage()
	{
		composite = new Composite(shlNewAccount, SWT.NONE);
		composite.setLayout(new FormLayout());
		
		txtBUSYMSG = new Label(composite, SWT.NONE);
		FormData fd_lblSaving = new FormData();
		fd_lblSaving.right = new FormAttachment(0, 603);
		fd_lblSaving.top = new FormAttachment(0, 10);
		fd_lblSaving.left = new FormAttachment(0, 10);
		txtBUSYMSG.setLayoutData(fd_lblSaving);
		txtBUSYMSG.setText("Working...");
		
		animation = new Animation(shlNewAccount, SWT.NONE, 4);
		animation.setBounds(10, 32, 32, 32);
		animation.setVisible(true);
		animation.addFrame( SWTResourceManager.getImage(MainWin.class, "/fkgui/gfx/finalkey1.png") );
		animation.addFrame( SWTResourceManager.getImage(MainWin.class, "/fkgui/gfx/finalkey2.png") );
		animation.setPlaying(true);
		FormData fdAni = new FormData();
		fdAni.top = new FormAttachment(txtBUSYMSG);
		

		
	}
	
	
	private void updatePage(FkNewAccStep curPage)
	{
		if(composite != null && !composite.isDisposed() )
		{
			composite.dispose();
		}
		System.out.println(curPage);
		
		switch(curPage)
		{
		case CLICKBTN:
			break;
		case NAMES:
			createNamePage();
			break;
		case PASSTYPE:
			createPassTypePage();
			break;
		case PASS_AUT:
			createPassAutPage();
			break;
		case PASS_MAN:
			createPassManPage();
			break;
		case REVIEW:
			createReviewPage();
			break;
		case SEPERATOR:
			createSeperatorPage();
			break;
		case SAVING:
			createSavingPage();
			break;

			
		}
		
		shlNewAccount.layout();
	}
	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlNewAccount = new Shell(getParent(), getStyle());
		shlNewAccount.setSize(625, 394);
		shlNewAccount.setText("Create New Account");
		shlNewAccount.setLayout(new FillLayout(SWT.HORIZONTAL));
		
	//	TabFolder tabFolder = new TabFolder(shell, SWT.NONE);
		
		//TabItem tbtmStepUsername = new TabItem(tabFolder, SWT.NONE);
		//tbtmStepUsername.setText("Name");
		
		updatePage(FkNewAccStep.NAMES);

	}

	@Override
	public void fkActionEvent(FkActionEvent event) {
		//System.out.println( "Event data:"+ event.data );
		MessageBox dialog;
		Boolean closeSelf=false;
		
		switch(event.type)
		{
		case ACTION_ABORTED:
			txtBUSYMSG.setText("Error.");
			dialog = new MessageBox(shlNewAccount, SWT.ICON_WARNING);
			dialog.setText("Account not created");
			dialog.setMessage("The account was not created.\nMaybe you were not quick enough to press FinalKey button?\nPlease try again.");
			dialog.open();
			updatePage(FkNewAccStep.REVIEW);			
			break;
		case ACTION_ERROR:
			txtBUSYMSG.setText("Error.");

			dialog = new MessageBox(shlNewAccount, SWT.ICON_ERROR);
			dialog.setText("Error");
			dialog.setMessage("There was an error creating the acccount, please reconnect to FinalKey and try again.");
			dialog.open();
			closeSelf=true;
			break;
		case ACTION_OKAY:
			txtBUSYMSG.setText("Account saved.");
			dialog = new MessageBox(shlNewAccount, SWT.ICON_INFORMATION);
			dialog.setText("Account created");
			dialog.setMessage("The account was created.");
			dialog.open();
			closeSelf=true;
			break;
		case ACTION_WAITING:
			txtBUSYMSG.setText("Waiting for buttonpress...");
			break;
		case ACTION_WORKING:
			txtBUSYMSG.setText("Saving...");
			animation.setVisible(false);
			break;
		}
		
		if( !shlNewAccount.isDisposed() && closeSelf )
		{
			shlNewAccount.close();
		}
	}
}
