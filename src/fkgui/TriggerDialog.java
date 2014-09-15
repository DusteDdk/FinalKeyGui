package fkgui;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Group;
import org.eclipse.wb.swt.SWTResourceManager;

import fkgui.FkManager.Account;

public class TriggerDialog extends Dialog implements FkActionEventListener {

	protected Object result;
	protected Shell shell;
	private Account account;
	public TriggerDialog mySelf;
	private FormData fd_grpChange;
	private Group grpUseAccount;
	private Group grpChange;
	ConsoleMsg delegate;
	PermitCountDownDialog permitCountdownDialog = null;
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public TriggerDialog(Shell parent, int style, Account a, ConsoleMsg d) {
		super(parent, style);
		account = a;
		delegate = d;
		setText(account.name);
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		result = (Object)new Boolean(false);
		createContents();

		Point p = getParent().getSize();
		p.x /= 2;
		p.y /= 2;
		p.x -= shell.getSize().x/2;
		p.y -= shell.getSize().y/2;
		p.x += getParent().getLocation().x;
		p.y += getParent().getLocation().y;
		shell.setLocation( p );

		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		System.out.println(Messages.TriggerDialog_1 + (Boolean)result);
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell(getParent(), SWT.BORDER | SWT.CLOSE);
		shell.setSize(677, 250);
		shell.setText(getText());
		shell.setLayout(new FormLayout());
		mySelf = this;
		grpUseAccount = new Group(shell, SWT.NONE);
		grpUseAccount.setLayout(new FormLayout());
		FormData fd_grpMakeFinalKey = new FormData();
		fd_grpMakeFinalKey.top = new FormAttachment(0, 10);
		fd_grpMakeFinalKey.bottom = new FormAttachment(0, 88);
		fd_grpMakeFinalKey.left = new FormAttachment(0, 10);
		fd_grpMakeFinalKey.right = new FormAttachment(100, -10);
		grpUseAccount.setLayoutData(fd_grpMakeFinalKey);
		grpUseAccount.setText(Messages.TriggerDialog_2);
		
		Button btnUsernamePassword = new Button(grpUseAccount, SWT.NONE);
		btnUsernamePassword.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FkManager.getInstance().trig(account, '%', mySelf);
				permitCountdownDialog = new PermitCountDownDialog(shell,SWT.SHELL_TRIM, account.name + Messages.TriggerDialog_3, Messages.TriggerDialog_4, 30000);
				//shell.setMinimized(true);
				shell.setEnabled(false); //Fisk
				permitCountdownDialog.open();
			}
		});
		btnUsernamePassword.setImage(SWTResourceManager.getImage(MainWin.class, "/fkgui/gfx/both.png")); //$NON-NLS-1$
		FormData fd_btnUsernamePassword = new FormData();
		fd_btnUsernamePassword.bottom = new FormAttachment(0, 45);
		fd_btnUsernamePassword.top = new FormAttachment(0, 10);
		fd_btnUsernamePassword.left = new FormAttachment(0, 10);
		fd_btnUsernamePassword.right = new FormAttachment(0, 224);
		btnUsernamePassword.setLayoutData(fd_btnUsernamePassword);

		btnUsernamePassword.setText(Messages.TriggerDialog_6);
		
		Button btnUsernameOnly = new Button(grpUseAccount, SWT.CENTER);
		btnUsernameOnly.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FkManager.getInstance().trig(account, 'u', mySelf);
				permitCountdownDialog = new PermitCountDownDialog(shell,SWT.SHELL_TRIM, account.name + Messages.TriggerDialog_7, Messages.TriggerDialog_8, 30000);
				shell.setEnabled(false);
				permitCountdownDialog.open();
			}
		});
		btnUsernameOnly.setImage(SWTResourceManager.getImage(MainWin.class, "/fkgui/gfx/user.png")); //$NON-NLS-1$
		FormData fd_btnUsernameOnly = new FormData();
		fd_btnUsernameOnly.left = new FormAttachment(btnUsernamePassword, 6);
		fd_btnUsernameOnly.bottom = new FormAttachment(btnUsernamePassword, 0, SWT.BOTTOM);
		fd_btnUsernameOnly.top = new FormAttachment(btnUsernamePassword, 0, SWT.TOP);
		btnUsernameOnly.setLayoutData(fd_btnUsernameOnly);
		btnUsernameOnly.setText(Messages.TriggerDialog_10);

		
		Button btnPasswordOnly = new Button(grpUseAccount, SWT.NONE);
		fd_btnUsernameOnly.right = new FormAttachment(btnPasswordOnly, -6);
		btnPasswordOnly.setImage(SWTResourceManager.getImage(MainWin.class, "/fkgui/gfx/key-icon.png")); //$NON-NLS-1$
		FormData fd_btnPasswordOnly = new FormData();
		fd_btnPasswordOnly.top = new FormAttachment(btnUsernamePassword, 0, SWT.TOP);
		fd_btnPasswordOnly.right = new FormAttachment(100, -10);
		fd_btnPasswordOnly.left = new FormAttachment(100, -243);
		fd_btnPasswordOnly.bottom = new FormAttachment( btnUsernameOnly,0, SWT.BOTTOM);
		btnPasswordOnly.setLayoutData(fd_btnPasswordOnly);
		btnPasswordOnly.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FkManager.getInstance().trig(account, 'p', mySelf);

				permitCountdownDialog = new PermitCountDownDialog(shell,SWT.SHELL_TRIM, account.name + Messages.TriggerDialog_11, Messages.TriggerDialog_12, 30000);

				//shell.setMinimized(true);
				shell.setEnabled(false); //Fisk
				permitCountdownDialog.open();
			}
		});
		

		btnPasswordOnly.setText(Messages.TriggerDialog_14);
		//btnPasswordOnly.getShell().layout();
		
				grpChange = new Group(shell, SWT.NONE);
				grpChange.setText(Messages.TriggerDialog_15);
				grpChange.setLayout(new FormLayout());
				fd_grpChange = new FormData();
				fd_grpChange.left = new FormAttachment(grpUseAccount, 353, SWT.LEFT);
				fd_grpChange.right = new FormAttachment(grpUseAccount, 0, SWT.RIGHT);
				fd_grpChange.top = new FormAttachment(grpUseAccount, 6);
				grpChange.setLayoutData(fd_grpChange);

				Button btnEdit = new Button(grpChange, SWT.NONE);
				btnEdit.setImage(SWTResourceManager.getImage(MainWin.class, "/fkgui/gfx/gtk_edit.png")); //$NON-NLS-1$
				FormData fd_btnEdit = new FormData();
				fd_btnEdit.bottom = new FormAttachment(0, 48);
				fd_btnEdit.left = new FormAttachment(0, 10);
				fd_btnEdit.top = new FormAttachment(0, 10);
				btnEdit.setLayoutData(fd_btnEdit);
				btnEdit.setText(Messages.TriggerDialog_17);
				///TODO: Inplement edit box..
				btnEdit.setVisible(false);
				
				Button btnDelete = new Button(grpChange, SWT.NONE);
				fd_btnEdit.right = new FormAttachment(100, -138);
				btnDelete.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						MessageBox dialog = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO );
						dialog.setText(Messages.TriggerDialog_18+account.name );
						dialog.setMessage(Messages.TriggerDialog_19+account.name+Messages.TriggerDialog_20+account.num+Messages.TriggerDialog_21);
						if( dialog.open() == SWT.YES )
						{
							permitCountdownDialog = new PermitCountDownDialog(shell,SWT.SHELL_TRIM, account.name + Messages.TriggerDialog_22, Messages.TriggerDialog_23+account.name+Messages.TriggerDialog_24, 5000);
							//shell.setMinimized(true);
							FkManager.getInstance().trig(account, 'd', mySelf);
							shell.setEnabled(false); //Fisk
							permitCountdownDialog.open();
						}
					}
				});
				btnDelete.setImage(SWTResourceManager.getImage(MainWin.class, "/fkgui/gfx/trashdelete.gif")); //$NON-NLS-1$
				FormData fd_btnDelete = new FormData();
				fd_btnDelete.bottom = new FormAttachment(btnEdit, 0, SWT.BOTTOM);
				fd_btnDelete.top = new FormAttachment(btnEdit, 0, SWT.TOP);
				fd_btnDelete.right = new FormAttachment(100, -16);
				fd_btnDelete.left = new FormAttachment(btnEdit, 10);
				btnDelete.setLayoutData(fd_btnDelete);
				btnDelete.setText(Messages.TriggerDialog_26);

				Button btnCancel = new Button(shell, SWT.NONE);
				fd_grpChange.bottom = new FormAttachment(btnCancel, -6);
				btnCancel.setImage(SWTResourceManager.getImage(MainWin.class, "/fkgui/gfx/Delete.png")); //$NON-NLS-1$
				FormData fd_btnCancel = new FormData();
				fd_btnCancel.left = new FormAttachment(grpUseAccount, 0, SWT.LEFT);
				fd_btnCancel.bottom = new FormAttachment(100, -10);
				fd_btnCancel.right = new FormAttachment(grpUseAccount, 0, SWT.RIGHT);
				btnCancel.setLayoutData(fd_btnCancel);
				
				btnCancel.setText(Messages.TriggerDialog_28);
				
				btnCancel.addSelectionListener( new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						shell.close();
					}
				});				
				Group grpShow = new Group(shell, SWT.NONE);
				grpShow.setText(Messages.TriggerDialog_29);
				FormData fd_grpShow = new FormData();
				fd_grpShow.bottom = new FormAttachment(grpChange, 0, SWT.BOTTOM);
				fd_grpShow.right = new FormAttachment(grpChange, -6);
				fd_grpShow.left = new FormAttachment(grpUseAccount, 0, SWT.LEFT);
				fd_grpShow.top = new FormAttachment(grpUseAccount, 6);
				grpShow.setLayoutData(fd_grpShow);
				
				Button btnShowUsername = new Button(grpShow, SWT.NONE);
				btnShowUsername.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						FkManager.getInstance().trig(account, 's', mySelf);
						permitCountdownDialog = new PermitCountDownDialog(shell,SWT.SHELL_TRIM, account.name + Messages.TriggerDialog_30, Messages.TriggerDialog_31, 30000);
						//shell.setMinimized(true);
						shell.setEnabled(false); //Fisk
						permitCountdownDialog.open();
						
					}
				});
				btnShowUsername.setImage(SWTResourceManager.getImage(MainWin.class, "/fkgui/gfx/both.png")); //$NON-NLS-1$
				btnShowUsername.setBounds(10, 28, 327, 35);
				btnShowUsername.setText(Messages.TriggerDialog_33);				
				

		        Control[] controls = new Control[] { grpUseAccount, grpShow , btnCancel, grpChange  };
		        shell.setTabList(controls);	
				
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public void fkActionEvent(FkActionEvent event) {
		MessageBox dialog;
		
		if( shell.isDisposed() )
		{
			return;
		}
		
		if( permitCountdownDialog != null && !permitCountdownDialog.shell.isDisposed() )
		{
			permitCountdownDialog.shell.close();
		}
		
		
		switch(event.type)
		{
		case STATE_ERROR:
			dialog = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
			dialog.setText(Messages.TriggerDialog_5);
			dialog.setMessage(Messages.TriggerDialog_9);
			dialog.open();			
		break;
		case ACTION_ABORTED:
			dialog = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
			dialog.setText(event.acc.name);
			dialog.setMessage(Messages.TriggerDialog_34);
			dialog.open();
			break;
		case ACTION_ERROR:
			dialog = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
			dialog.setText(event.acc.name + Messages.TriggerDialog_35);
			dialog.setMessage(Messages.TriggerDialog_36);
			dialog.open();			
			break;
		case ACTION_OKAY:
			result = (Object)new Boolean(true);
			if( event.action == 's' )
			{
				result = (Object)new Boolean(false); //Don't close main window after "show"

				int begin = event.data.lastIndexOf("Account: "+ event.acc.num); //$NON-NLS-1$
				if( begin == -1 )
				{
					begin=0;
				}
				int end = event.data.lastIndexOf("[done]"); //$NON-NLS-1$
				String s = event.data.substring( begin,end  );
				dialog = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
				dialog.setText(Messages.TriggerDialog_39);
				dialog.setMessage(s);
				dialog.open();
			}
			
			if( event.action == 'd' )
			{
				result = (Object)new Boolean(false); //We want the mainwin to get back after deleting an account.
				dialog = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
				dialog.setText(Messages.TriggerDialog_40);
				dialog.setMessage(event.acc.name + Messages.TriggerDialog_41);
				dialog.open();
				Display.getDefault().asyncExec( new Runnable() {
					@Override
					public void run() {
						delegate.updateAccountList();
					}
					
				} );
			}
			shell.close();
			break;
		} //Switch
		
		if( !shell.isDisposed() && !shell.isEnabled())
		{
			shell.setEnabled(true); //Fisk
		}

		
	}
}
