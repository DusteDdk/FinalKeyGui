package fkgui;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Spinner;

public class NewAccountDialog extends Dialog implements FkActionEventListener {

	protected Object result;
	protected Shell shlNewAccount;
	private Text txtAccountName;
	private Text txtUserName;
	private Text txtManPSW;
	private Text txtSpecials;
	
	private enum FkNewAccStep { NAMES, PASSTYPE, PASS_MAN, PASS_AUT, SEPERATOR, REVIEW, CLICKBTN, SAVING };
	private enum FkNewAccAutoSpecials { ALL, SELECTED, NONE };
	
	private String strAccountName = ""; //$NON-NLS-1$
	private String strUserName = ""; //$NON-NLS-1$
	private String strPassword = ""; //$NON-NLS-1$
	private String autoPassSpecials = "!@#,.-_()"; //$NON-NLS-1$
	private FkNewAccAutoSpecials allowedAutoSpecials = FkNewAccAutoSpecials.ALL;
	private Boolean autoPassword = true;
	private int autoPassLen = 16;
	private Boolean seperatorTab=true;
	private Button btnManPassBack;

	//NamePage
	Button btnNext0;

	
	//PassType
	Button radAutPSW;
	Button radManPSW;
	
	//ManPass
	Label lblTypeThePassword;
	Label lblPassword;
	Button chkShowPsw;
	Button btnManPassPageNext;
	
	//Autopass
	Button radAllSymbols;
	Button radOnlySelected;
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
		Point p = getParent().getSize();
		p.x /= 2;
		p.y /= 2;
		p.x -= shlNewAccount.getSize().x/2;
		p.y -= shlNewAccount.getSize().y/2;
		p.x += getParent().getLocation().x;
		p.y += getParent().getLocation().y;
		shlNewAccount.setLocation( p );

		
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
		fd_lblEnterNewName.right = new FormAttachment(0, 613);
		fd_lblEnterNewName.top = new FormAttachment(0, 10);
		fd_lblEnterNewName.left = new FormAttachment(0, 10);
		lblEnterNewName.setLayoutData(fd_lblEnterNewName);
		lblEnterNewName.setText(Messages.NewAccountDialog_4);
		Label lblUsername = new Label(composite, SWT.NONE);
		FormData fd_lblUsername = new FormData();
		lblUsername.setLayoutData(fd_lblUsername);
		lblUsername.setText(Messages.NewAccountDialog_5);
		
		txtAccountName = new Text(composite, SWT.BORDER);
		fd_lblUsername.bottom = new FormAttachment(txtAccountName, 0, SWT.BOTTOM);
		fd_lblUsername.right = new FormAttachment(txtAccountName, -6);
		FormData fd_txtAccountName = new FormData();
		fd_txtAccountName.top = new FormAttachment(lblEnterNewName, 6);
		fd_txtAccountName.right = new FormAttachment(100, -274);
		fd_txtAccountName.left = new FormAttachment(0, 180);
		txtAccountName.setLayoutData(fd_txtAccountName);
		txtAccountName.setTextLimit(31);
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
		lblNewLabel.setText(Messages.NewAccountDialog_6);
		
		Label lblUsername_1 = new Label(composite, SWT.NONE);
		FormData fd_lblUsername_1 = new FormData();
		fd_lblUsername_1.top = new FormAttachment(lblNewLabel, 6);
		fd_lblUsername_1.right = new FormAttachment(lblUsername, 0, SWT.RIGHT);
		lblUsername_1.setLayoutData(fd_lblUsername_1);
		lblUsername_1.setText(Messages.NewAccountDialog_7);
		
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
		fd_btnCancel.left = new FormAttachment(lblEnterNewName, 0, SWT.LEFT);
		btnCancel.setLayoutData(fd_btnCancel);
		btnCancel.setText(Messages.NewAccountDialog_8);
		
		btnNext0 = new Button(composite, SWT.NONE);
		btnNext0.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				strAccountName = txtAccountName.getText();
				strUserName = txtUserName.getText();
				updatePage(FkNewAccStep.PASSTYPE);
			}
		});
		btnNext0.setText(Messages.NewAccountDialog_9);
		FormData fd_btnNext0 = new FormData();
		fd_btnNext0.top = new FormAttachment(btnCancel, 0, SWT.TOP);
		fd_btnNext0.right = new FormAttachment(100, -10);
		btnNext0.setLayoutData(fd_btnNext0);
		btnNext0.setVisible(false);
		
		txtUserName.setText( strUserName );
		txtAccountName.setText( strAccountName );

		Control[] controls = new Control[] { txtAccountName, txtUserName, btnNext0, btnCancel };
		composite.setTabList(controls);
		txtAccountName.setFocus();

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
		lblNextUpSetting.setText(Messages.NewAccountDialog_10);
		
		radAutPSW = new Button(composite, SWT.RADIO);
		if( autoPassword )
		{
			radAutPSW.setSelection(true);
		}
		FormData fd_radAutPSW = new FormData();
		fd_radAutPSW.top = new FormAttachment(lblNextUpSetting, 37);
		fd_radAutPSW.left = new FormAttachment(0, 213);
		radAutPSW.setLayoutData(fd_radAutPSW);
		radAutPSW.setText(Messages.NewAccountDialog_11);
		
		radManPSW = new Button(composite, SWT.RADIO);
		if( !autoPassword )
		{
			radManPSW.setSelection(true);
		}		
		FormData fd_radManPSW = new FormData();
		fd_radManPSW.top = new FormAttachment(radAutPSW, 6);
		fd_radManPSW.left = new FormAttachment(radAutPSW, 0, SWT.LEFT);
		radManPSW.setLayoutData(fd_radManPSW);
		radManPSW.setText(Messages.NewAccountDialog_12);
		
		Button btnNext1 = new Button(composite, SWT.NONE);
		btnNext1.setText(Messages.NewAccountDialog_13);
		FormData fd_btnNext1 = new FormData();
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
		fd_btnNext1.top = new FormAttachment(btnBack0, 0, SWT.TOP);
		btnBack0.setText(Messages.NewAccountDialog_14);
		FormData fd_btnBack0 = new FormData();
		fd_btnBack0.bottom = new FormAttachment(100, -10);
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
		if( radAllSymbols.getSelection() )
		{
			allowedAutoSpecials = FkNewAccAutoSpecials.ALL;
		} else if( radOnlySelected.getSelection() )
		{
			allowedAutoSpecials = FkNewAccAutoSpecials.SELECTED;
		} else {
			allowedAutoSpecials = FkNewAccAutoSpecials.NONE;
		}
		autoPassSpecials = txtSpecials.getText();
		autoPassLen = spnLen.getSelection();		
	}
	void createPassAutPage()
	{
		composite = new Composite(shlNewAccount, SWT.NONE);
		composite.setLayout(new FormLayout());
		
		Label lblBeforeCreatingA = new Label(composite, SWT.NONE);
		FormData fd_lblBeforeCreatingA = new FormData();
		fd_lblBeforeCreatingA.left = new FormAttachment(0, 10);
		fd_lblBeforeCreatingA.right = new FormAttachment(100, -10);
		fd_lblBeforeCreatingA.top = new FormAttachment(0, 10);
		lblBeforeCreatingA.setLayoutData(fd_lblBeforeCreatingA);
		
		lblBeforeCreatingA.setText(Messages.NewAccountDialog_15);
		
		Label lblPasswordLength = new Label(composite, SWT.NONE);
		fd_lblBeforeCreatingA.bottom = new FormAttachment(100, -257);
		FormData fd_lblPasswordLength = new FormData();
		lblPasswordLength.setLayoutData(fd_lblPasswordLength);
		lblPasswordLength.setText(Messages.NewAccountDialog_16);
		
		spnLen = new Spinner(composite, SWT.BORDER);
		fd_lblPasswordLength.bottom = new FormAttachment(spnLen, 0, SWT.BOTTOM);
		fd_lblPasswordLength.right = new FormAttachment(spnLen, -9);
		FormData fd_spnLen = new FormData();
		fd_spnLen.top = new FormAttachment(0, 119);
		fd_spnLen.left = new FormAttachment(0, 310);
		spnLen.setLayoutData(fd_spnLen);
		spnLen.setMaximum(128);
		spnLen.setSelection(autoPassLen);
		
		Label lblASafePassword = new Label(composite, SWT.NONE);
		FormData fd_lblASafePassword = new FormData();
		fd_lblASafePassword.right = new FormAttachment(lblBeforeCreatingA, 0, SWT.RIGHT);
		fd_lblASafePassword.top = new FormAttachment(lblPasswordLength, 47);
		fd_lblASafePassword.left = new FormAttachment(0, 10);
		lblASafePassword.setLayoutData(fd_lblASafePassword);
		lblASafePassword.setText(Messages.NewAccountDialog_17);
		
		radAllSymbols = new Button(composite, SWT.RADIO);
		fd_lblASafePassword.bottom = new FormAttachment(100, -100);
		FormData fd_radAllSymbols = new FormData();
		fd_radAllSymbols.top = new FormAttachment(lblASafePassword, 6);
		fd_radAllSymbols.left = new FormAttachment(0, 10);
		radAllSymbols.setLayoutData(fd_radAllSymbols);
		if( allowedAutoSpecials == FkNewAccAutoSpecials.ALL )
		{
			radAllSymbols.setSelection(true);
		}
		radAllSymbols.setText(Messages.NewAccountDialog_18);
		radAllSymbols.pack();
		
		radOnlySelected = new Button(composite, SWT.RADIO);
		FormData fd_radOnlySelected = new FormData();
		fd_radOnlySelected.top = new FormAttachment(lblASafePassword, 6);
		fd_radOnlySelected.left = new FormAttachment(lblPasswordLength, 0, SWT.LEFT);
		radOnlySelected.setLayoutData(fd_radOnlySelected);
		if( allowedAutoSpecials == FkNewAccAutoSpecials.SELECTED )
		{
			radOnlySelected.setSelection(true);
		}
		radOnlySelected.setText(Messages.NewAccountDialog_19);
		
		
		
		txtSpecials = new Text(composite, SWT.BORDER);
		FormData fd_txtSpecials = new FormData();
		fd_txtSpecials.bottom = new FormAttachment(radAllSymbols, 0, SWT.BOTTOM);
		fd_txtSpecials.left = new FormAttachment(radOnlySelected, 6);
		txtSpecials.setLayoutData(fd_txtSpecials);
		txtSpecials.setText(autoPassSpecials);
		txtSpecials.setBounds(291, 243, 312, 35);
		
		
		Button button_6 = new Button(composite, SWT.NONE);
		button_6.setText(Messages.NewAccountDialog_21);
		FormData fd_button_6 = new FormData();
		fd_button_6.right = new FormAttachment(lblBeforeCreatingA, 0, SWT.RIGHT);
		button_6.setLayoutData(fd_button_6);
		
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
		

		Button button_5 = new Button(composite, SWT.NONE);
		fd_button_6.top = new FormAttachment(button_5, 0, SWT.TOP);
		button_5.setText(Messages.NewAccountDialog_20);
		FormData fd_button_5 = new FormData();
		fd_button_5.bottom = new FormAttachment(100, -10);
		fd_button_5.left = new FormAttachment(0, 10);
		button_5.setLayoutData(fd_button_5);
		
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
		
		
        
        Button radNoSpecialSymbols = new Button(composite, SWT.RADIO);
        FormData fd_radNoSpecialSymbols = new FormData();
        fd_radNoSpecialSymbols.top = new FormAttachment(lblASafePassword, 6);
        fd_radNoSpecialSymbols.left = new FormAttachment(txtSpecials, 57);
        radNoSpecialSymbols.setLayoutData(fd_radNoSpecialSymbols);
        radNoSpecialSymbols.setText(Messages.NewAccountDialog_btnNoSpecialSymbols_text);
		if( allowedAutoSpecials == FkNewAccAutoSpecials.NONE )
		{
			radNoSpecialSymbols.setSelection(true);
		}        
        

		Control[] controls = new Control[] { spnLen, radAllSymbols, radOnlySelected, radNoSpecialSymbols, button_6, button_5 };
        composite.setTabList(controls);			
		spnLen.setFocus();

		
	}
	
	
	void makePswField(Boolean showPsw)
	{
		txtManPSW = new Text(composite, SWT.BORDER | ((showPsw)?0:SWT.PASSWORD) );
		FormData fd_txtManPSW = new FormData();
		fd_txtManPSW.right = new FormAttachment(0, 613);
		fd_txtManPSW.top = new FormAttachment(0, 62);
		fd_txtManPSW.left = new FormAttachment(0, 92);
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
		

		composite.layout();
	}
	

	
	void createPassManPage()
	{
		composite = new Composite(shlNewAccount, SWT.NONE);
		composite.setLayout(new FormLayout());

		btnManPassBack = new Button(composite, SWT.NONE);
		FormData fd_BtnBack1 = new FormData();
		fd_BtnBack1.bottom = new FormAttachment(100, -10);
		btnManPassBack.setLayoutData(fd_BtnBack1);
		btnManPassBack.setText(Messages.NewAccountDialog_23);
		btnManPassBack.addSelectionListener( new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				updatePage(FkNewAccStep.PASSTYPE);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
			}
		});
		
		btnManPassPageNext = new Button(composite, SWT.NONE);
		FormData fd_btnManPassPageNext = new FormData();
		fd_btnManPassPageNext.top = new FormAttachment(btnManPassBack, 0, SWT.TOP);
		fd_btnManPassPageNext.right = new FormAttachment(100, -10);
		btnManPassPageNext.setLayoutData(fd_btnManPassPageNext);
		btnManPassPageNext.setText(Messages.NewAccountDialog_24);
		
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
		fd_BtnBack1.left = new FormAttachment(lblTypeThePassword, 0, SWT.LEFT);
		FormData fd_lblTypeThePassword = new FormData();
		fd_lblTypeThePassword.right = new FormAttachment(0, 613);
		fd_lblTypeThePassword.top = new FormAttachment(0, 10);
		fd_lblTypeThePassword.left = new FormAttachment(0, 10);
		lblTypeThePassword.setLayoutData(fd_lblTypeThePassword);
		lblTypeThePassword.setText(Messages.NewAccountDialog_25);
		
		lblPassword = new Label(composite, SWT.NONE);
		FormData fd_lblPassword = new FormData();
		fd_lblPassword.top = new FormAttachment(0, 62);
		fd_lblPassword.left = new FormAttachment(0, 10);
		lblPassword.setLayoutData(fd_lblPassword);
		lblPassword.setText(Messages.NewAccountDialog_26);
		
		makePswField(false);

		chkShowPsw = new Button(composite, SWT.CHECK);
		FormData fd_chkShowPsw = new FormData();
		fd_chkShowPsw.right = new FormAttachment(0, 613);
		fd_chkShowPsw.top = new FormAttachment(0, 103);
		fd_chkShowPsw.left = new FormAttachment(0, 92);
		chkShowPsw.setLayoutData(fd_chkShowPsw);
		chkShowPsw.setText(Messages.NewAccountDialog_22);
		chkShowPsw.setSelection(false);
		
		chkShowPsw.addSelectionListener( new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				Button btnChk = (Button)arg0.getSource();

				txtManPSW.dispose();
				
				makePswField(btnChk.getSelection());
		        Control[] controls = new Control[] { txtManPSW, chkShowPsw, btnManPassPageNext, btnManPassBack };
		        composite.setTabList(controls);					
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});		
		
        Control[] controls = new Control[] { txtManPSW, chkShowPsw, btnManPassPageNext, btnManPassBack };
        composite.setTabList(controls);	
        txtManPSW.setFocus();
        btnManPassPageNext.setVisible( (txtManPSW.getText().length() > 0) );
		
	}
	

	void createSeperatorPage()
	{
		composite = new Composite(shlNewAccount, SWT.NONE);
		composite.setLayout(new FormLayout());
		
		Label lblToMakeFinalkey = new Label(composite, SWT.NONE);
		FormData fd_lblToMakeFinalkey = new FormData();
		fd_lblToMakeFinalkey.right = new FormAttachment(0, 613);
		fd_lblToMakeFinalkey.top = new FormAttachment(0, 10);
		fd_lblToMakeFinalkey.left = new FormAttachment(0, 10);
		lblToMakeFinalkey.setLayoutData(fd_lblToMakeFinalkey);
		lblToMakeFinalkey.setText(Messages.NewAccountDialog_27);
		
		radTabSep = new Button(composite, SWT.RADIO);
		FormData fd_radTabSep = new FormData();
		fd_radTabSep.right = new FormAttachment(0, 613);
		fd_radTabSep.top = new FormAttachment(0, 172);
		fd_radTabSep.left = new FormAttachment(0, 231);
		radTabSep.setLayoutData(fd_radTabSep);
		if( seperatorTab )
		{
			radTabSep.setSelection(true);
		}
		radTabSep.setText(Messages.NewAccountDialog_28);
		
		Button radEnterSep = new Button(composite, SWT.RADIO);
		FormData fd_radEnterSep = new FormData();
		fd_radEnterSep.right = new FormAttachment(0, 613);
		fd_radEnterSep.top = new FormAttachment(0, 205);
		fd_radEnterSep.left = new FormAttachment(0, 231);
		radEnterSep.setLayoutData(fd_radEnterSep);
		if( !seperatorTab )
		{
			radEnterSep.setSelection(true);
		}
		radEnterSep.setText(Messages.NewAccountDialog_29);
		
		Button button_7 = new Button(composite, SWT.NONE);
		FormData fd_button_7 = new FormData();
		fd_button_7.left = new FormAttachment(lblToMakeFinalkey, 0, SWT.LEFT);
		button_7.setLayoutData(fd_button_7);
		button_7.setText(Messages.NewAccountDialog_30);
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
		fd_button_7.top = new FormAttachment(button_8, 0, SWT.TOP);
		FormData fd_button_8 = new FormData();
		fd_button_8.bottom = new FormAttachment(100, -10);
		fd_button_8.right = new FormAttachment(100, -10);
		button_8.setLayoutData(fd_button_8);
		button_8.setText(Messages.NewAccountDialog_31);
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
		fd_lblHeresAnOverview.right = new FormAttachment(0, 613);
		fd_lblHeresAnOverview.top = new FormAttachment(0, 10);
		fd_lblHeresAnOverview.left = new FormAttachment(0, 10);
		lblHeresAnOverview.setLayoutData(fd_lblHeresAnOverview);
		lblHeresAnOverview.setText(Messages.NewAccountDialog_32);
		
		Label lblAccountName = new Label(composite, SWT.NONE);
		lblAccountName.setAlignment(SWT.RIGHT);
		FormData fd_lblAccountName = new FormData();
		fd_lblAccountName.left = new FormAttachment(0, 10);
		fd_lblAccountName.top = new FormAttachment(0, 51);
		lblAccountName.setLayoutData(fd_lblAccountName);
		lblAccountName.setText(Messages.NewAccountDialog_33);
		
		Label lblPasswordType = new Label(composite, SWT.NONE);
		lblPasswordType.setAlignment(SWT.RIGHT);
		FormData fd_lblPasswordType = new FormData();
		fd_lblPasswordType.right = new FormAttachment(lblAccountName, 0, SWT.RIGHT);
		fd_lblPasswordType.left = new FormAttachment(lblHeresAnOverview, 0, SWT.LEFT);
		fd_lblPasswordType.top = new FormAttachment(0, 109);
		lblPasswordType.setLayoutData(fd_lblPasswordType);
		lblPasswordType.setText(Messages.NewAccountDialog_34);
		
		Label lblUserName = new Label(composite, SWT.NONE);
		lblUserName.setAlignment(SWT.RIGHT);
		FormData fd_lblUserName = new FormData();
		fd_lblUserName.right = new FormAttachment(lblHeresAnOverview, 220);
		fd_lblUserName.left = new FormAttachment(lblHeresAnOverview, 0, SWT.LEFT);
		fd_lblUserName.top = new FormAttachment(0, 80);
		lblUserName.setLayoutData(fd_lblUserName);
		lblUserName.setText(Messages.NewAccountDialog_35);
		
		Label lblIfEverythingLooks = new Label(composite, SWT.NONE);
		FormData fd_lblIfEverythingLooks = new FormData();
		fd_lblIfEverythingLooks.bottom = new FormAttachment(0, 287);
		fd_lblIfEverythingLooks.right = new FormAttachment(lblHeresAnOverview, 0, SWT.RIGHT);
		fd_lblIfEverythingLooks.top = new FormAttachment(0, 181);
		fd_lblIfEverythingLooks.left = new FormAttachment(0, 10);
		lblIfEverythingLooks.setLayoutData(fd_lblIfEverythingLooks);
		lblIfEverythingLooks.setText(Messages.NewAccountDialog_36);
		
		Label lblSeperator = new Label(composite, SWT.NONE);
		lblSeperator.setAlignment(SWT.RIGHT);
		FormData fd_lblSeperator = new FormData();
		fd_lblSeperator.right = new FormAttachment(0, 230);
		fd_lblSeperator.top = new FormAttachment(0, 138);
		fd_lblSeperator.left = new FormAttachment(0, 10);
		lblSeperator.setLayoutData(fd_lblSeperator);
		lblSeperator.setText(Messages.NewAccountDialog_37);
		
		Button button_9 = new Button(composite, SWT.NONE);
		FormData fd_button_9 = new FormData();
		fd_button_9.bottom = new FormAttachment(100, -10);
		fd_button_9.left = new FormAttachment(lblHeresAnOverview, 0, SWT.LEFT);
		button_9.setLayoutData(fd_button_9);
		button_9.setText(Messages.NewAccountDialog_38);
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
		fd_btnSave.top = new FormAttachment(button_9, 0, SWT.TOP);
		fd_btnSave.right = new FormAttachment(100, -10);
		btnSave.setLayoutData(fd_btnSave);
		btnSave.setText(Messages.NewAccountDialog_39);
		btnSave.setFocus();
		
		btnSave.addSelectionListener( new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				MessageBox dialog = new MessageBox(shlNewAccount, SWT.ICON_INFORMATION | SWT.OK );
				dialog.setText(Messages.NewAccountDialog_40);
				dialog.setMessage(Messages.NewAccountDialog_41);
				dialog.open();
				
				//In case of automatic password the
				//FkManager takes a boolean after AutoPassLen telling if it should use all specials (true) or only those from the string in the next argument (autoPassSpecials) (false) 
				//If then the no-specials radiobutton is selected, remove content of that string such that no specials are selected
				if( allowedAutoSpecials == FkNewAccAutoSpecials.NONE)
				{
					autoPassSpecials = ""; //$NON-NLS-1$
				}
				
				FkManager.getInstance().createAccount( strAccountName, strUserName, autoPassword, autoPassLen, (allowedAutoSpecials == FkNewAccAutoSpecials.ALL), autoPassSpecials, strPassword, seperatorTab, mySelf );
				updatePage(FkNewAccStep.SAVING);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
			}
		});
		
		Label lblAccName = new Label(composite, SWT.NONE);
		fd_lblAccountName.right = new FormAttachment(lblAccName, -6);
		FormData fd_lblAccName = new FormData();
		fd_lblAccName.right = new FormAttachment(0, 603);
		fd_lblAccName.top = new FormAttachment(0, 51);
		fd_lblAccName.left = new FormAttachment(0, 236);
		lblAccName.setLayoutData(fd_lblAccName);
		lblAccName.setText(strAccountName);
		
		Label lblUsrName = new Label(composite, SWT.NONE);
		FormData fd_lblUsrName = new FormData();
		fd_lblUsrName.right = new FormAttachment(0, 613);
		fd_lblUsrName.top = new FormAttachment(0, 80);
		fd_lblUsrName.left = new FormAttachment(0, 236);
		lblUsrName.setLayoutData(fd_lblUsrName);
		lblUsrName.setText(strUserName);
		
		Label lblPasswordInfo = new Label(composite, SWT.NONE);
		FormData fd_lblPasswordInfo = new FormData();
		fd_lblPasswordInfo.right = new FormAttachment(0, 603);
		fd_lblPasswordInfo.top = new FormAttachment(0, 109);
		fd_lblPasswordInfo.left = new FormAttachment(0, 236);
		lblPasswordInfo.setLayoutData(fd_lblPasswordInfo);
		
		String passInfo;
		if( autoPassword )
		{
			passInfo = Messages.NewAccountDialog_42 + autoPassLen + Messages.NewAccountDialog_43;
			if( allowedAutoSpecials == FkNewAccAutoSpecials.ALL )
			{
				passInfo += Messages.NewAccountDialog_44;
			} else if( allowedAutoSpecials == FkNewAccAutoSpecials.SELECTED )
			{
				passInfo += "{"+autoPassSpecials+ "}"; //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				passInfo += Messages.NewAccountDialog_45;
			}
			
		} else {
			passInfo =Messages.NewAccountDialog_47 + strPassword.length() +Messages.NewAccountDialog_48;
		}
		
		lblPasswordInfo.setText(passInfo);
		
		Label lblSeperatorInfo = new Label(composite, SWT.NONE);
		FormData fd_lblSeperatorInfo = new FormData();
		fd_lblSeperatorInfo.right = new FormAttachment(0, 603);
		fd_lblSeperatorInfo.top = new FormAttachment(0, 138);
		fd_lblSeperatorInfo.left = new FormAttachment(0, 236);
		lblSeperatorInfo.setLayoutData(fd_lblSeperatorInfo);
		
		String sepInfo;
		if( seperatorTab )
		{
			sepInfo = Messages.NewAccountDialog_49;
		} else {
			sepInfo = Messages.NewAccountDialog_50;
		}
		
		lblSeperatorInfo.setText(sepInfo);
		
        Control[] controls = new Control[] { btnSave, button_9 };
        composite.setTabList(controls);	

	}
	
	void createSavingPage()
	{
		composite = new Composite(shlNewAccount, SWT.NONE);
		composite.setLayout(new FormLayout());
		
		txtBUSYMSG = new Label(composite, SWT.NONE);
		FormData fd_txtBUSYMSG = new FormData();
		fd_txtBUSYMSG.bottom = new FormAttachment(0, 113);
		fd_txtBUSYMSG.top = new FormAttachment(0, 10);
		fd_txtBUSYMSG.right = new FormAttachment(100, -10);
		fd_txtBUSYMSG.left = new FormAttachment(0, 78);
		txtBUSYMSG.setLayoutData(fd_txtBUSYMSG);
		txtBUSYMSG.setText(Messages.NewAccountDialog_51);
		FormData fdAni = new FormData();
		fdAni.top = new FormAttachment(txtBUSYMSG);
		
		animation = new Animation(composite, SWT.NONE, 4);
		FormData fd_animation = new FormData();
		fd_animation.top = new FormAttachment(0, 10);
		fd_animation.left = new FormAttachment(0, 10);
		animation.setLayoutData(fd_animation);
		animation.setBounds(10, 32, 32, 32);
		animation.setVisible(true);
		animation.setPlaying(true);

	}
	
	
	private void updatePage(FkNewAccStep curPage)
	{
		if(composite != null && !composite.isDisposed() )
		{
			composite.dispose();
		}

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
		shlNewAccount.setText(Messages.NewAccountDialog_54);
		shlNewAccount.setLayout(new FillLayout(SWT.HORIZONTAL));
				
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
			txtBUSYMSG.setText(Messages.NewAccountDialog_55);
			dialog = new MessageBox(shlNewAccount, SWT.ICON_WARNING);
			dialog.setText(Messages.NewAccountDialog_56);
			dialog.setMessage(Messages.NewAccountDialog_57);
			dialog.open();
			updatePage(FkNewAccStep.REVIEW);			
			break;
		case ACTION_ERROR:
			txtBUSYMSG.setText(Messages.NewAccountDialog_58);
			dialog = new MessageBox(shlNewAccount, SWT.ICON_ERROR);
			dialog.setText(Messages.NewAccountDialog_59);
			dialog.setMessage(Messages.NewAccountDialog_60);
			dialog.open();
			closeSelf=true;
			break;
		case ACTION_OKAY:
			txtBUSYMSG.setText(Messages.NewAccountDialog_61);
			dialog = new MessageBox(shlNewAccount, SWT.ICON_INFORMATION);
			dialog.setText(Messages.NewAccountDialog_62);
			dialog.setMessage(Messages.NewAccountDialog_63);
			dialog.open();
			closeSelf=true;
			break;
		case ACTION_WAITING:
			txtBUSYMSG.setText(Messages.NewAccountDialog_64);
			break;
		case ACTION_WORKING:
			txtBUSYMSG.setText(Messages.NewAccountDialog_65);
			animation.setVisible(false);
			break;
		case STATE_ERROR:
			txtBUSYMSG.setText(Messages.NewAccountDialog_0);
			dialog = new MessageBox(shlNewAccount, SWT.ICON_ERROR);
			dialog.setText(Messages.NewAccountDialog_1);
			dialog.setMessage(Messages.NewAccountDialog_2);
			dialog.open();
			closeSelf=true;
			break;

		}
		
		if( !shlNewAccount.isDisposed() && closeSelf )
		{
			shlNewAccount.close();
		}
	}
}
