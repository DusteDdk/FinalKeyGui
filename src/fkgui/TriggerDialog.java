package fkgui;

import java.awt.Color;
import java.io.Closeable;

import javax.swing.JFrame;
import javax.swing.JTextField;

import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.graphics.Point;
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
	private Group grpMakeFinalKey;
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
		setText("Use "+ account.name);
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
		System.out.println("Returning result:" + (Boolean)result);
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
		grpMakeFinalKey = new Group(shell, SWT.NONE);
		grpMakeFinalKey.setLayout(new FormLayout());
		FormData fd_grpMakeFinalKey = new FormData();
		fd_grpMakeFinalKey.top = new FormAttachment(0, 10);
		fd_grpMakeFinalKey.bottom = new FormAttachment(0, 88);
		fd_grpMakeFinalKey.left = new FormAttachment(0, 10);
		fd_grpMakeFinalKey.right = new FormAttachment(100, -10);
		grpMakeFinalKey.setLayoutData(fd_grpMakeFinalKey);
		grpMakeFinalKey.setText("Use " + account.name);
		
		Button btnUsernamePassword = new Button(grpMakeFinalKey, SWT.NONE);
		btnUsernamePassword.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FkManager.getInstance().trig(account, '%', mySelf);
				permitCountdownDialog = new PermitCountDownDialog(shell,SWT.SHELL_TRIM, account.name + ": Ready with username and password", "Focus username field and press button.\nPress and hold to cancel.", 30000);
				shell.setMinimized(true);
				permitCountdownDialog.open();
			}
		});
		btnUsernamePassword.setImage(SWTResourceManager.getImage(MainWin.class, "/fkgui/gfx/both.png"));
		FormData fd_btnUsernamePassword = new FormData();
		fd_btnUsernamePassword.top = new FormAttachment(0, 10);
		fd_btnUsernamePassword.left = new FormAttachment(0, 10);
		fd_btnUsernamePassword.right = new FormAttachment(0, 224);
		btnUsernamePassword.setLayoutData(fd_btnUsernamePassword);

		btnUsernamePassword.setText("Username + Password");
		
		Button btnUsernameOnly = new Button(grpMakeFinalKey, SWT.NONE);
		btnUsernameOnly.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FkManager.getInstance().trig(account, 'u', mySelf);
				permitCountdownDialog = new PermitCountDownDialog(shell,SWT.SHELL_TRIM, account.name + ": Ready with username", "Focus username field and press button.\nPress and hold to cancel.", 30000);
				shell.setMinimized(true);
				permitCountdownDialog.open();
			}
		});
		btnUsernameOnly.setImage(SWTResourceManager.getImage(MainWin.class, "/fkgui/gfx/user.png"));
		FormData fd_btnUsernameOnly = new FormData();
		fd_btnUsernameOnly.top = new FormAttachment(btnUsernamePassword, -35);
		fd_btnUsernameOnly.bottom = new FormAttachment(btnUsernamePassword, 0, SWT.BOTTOM);
		fd_btnUsernameOnly.left = new FormAttachment(btnUsernamePassword, 6);
		fd_btnUsernameOnly.right = new FormAttachment(0, 405);
		btnUsernameOnly.setLayoutData(fd_btnUsernameOnly);

		btnUsernameOnly.setText("Username");
		
		Button btnPasswordOnly = new Button(grpMakeFinalKey, SWT.NONE);
		btnPasswordOnly.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FkManager.getInstance().trig(account, 'p', mySelf);
				
				permitCountdownDialog = new PermitCountDownDialog(shell,SWT.SHELL_TRIM, account.name + ": Ready with password", "Focus username field and press button.\nPress and hold to cancel.", 30000);

				shell.setMinimized(true);
				permitCountdownDialog.open();
			}
		});
		btnPasswordOnly.setImage(SWTResourceManager.getImage(MainWin.class, "/fkgui/gfx/key-icon.png"));
		FormData fd_btnPasswordOnly = new FormData();
		fd_btnPasswordOnly.left = new FormAttachment(btnUsernameOnly, 29);
		fd_btnPasswordOnly.top = new FormAttachment(btnUsernamePassword, 0, SWT.TOP);
		fd_btnPasswordOnly.right = new FormAttachment(100, -9);
		fd_btnPasswordOnly.bottom = new FormAttachment(0, 45);
		btnPasswordOnly.setLayoutData(fd_btnPasswordOnly);

		btnPasswordOnly.setText("Password");
				
				grpChange = new Group(shell, SWT.NONE);
				grpChange.setText("Change "+account.name);
				grpChange.setLayout(new FormLayout());
				fd_grpChange = new FormData();
				fd_grpChange.left = new FormAttachment(grpMakeFinalKey, 353, SWT.LEFT);
				fd_grpChange.right = new FormAttachment(grpMakeFinalKey, 0, SWT.RIGHT);
				fd_grpChange.top = new FormAttachment(grpMakeFinalKey, 6);
				grpChange.setLayoutData(fd_grpChange);
				
				Button btnEdit = new Button(grpChange, SWT.NONE);
				btnEdit.setImage(SWTResourceManager.getImage(MainWin.class, "/fkgui/gfx/gtk_edit.png"));
				FormData fd_btnEdit = new FormData();
				fd_btnEdit.top = new FormAttachment(0, 10);
				fd_btnEdit.left = new FormAttachment(0, 10);
				btnEdit.setLayoutData(fd_btnEdit);
				btnEdit.setText("Edit");
				///TODO: Inplement edit box..
				btnEdit.setVisible(false);
				
				Button btnDelete = new Button(grpChange, SWT.NONE);
				btnDelete.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						MessageBox dialog = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO );
						dialog.setText("WARNING! Confirm delete "+account.name );
						dialog.setMessage("Are you sure you want to delete account "+account.name+" ?\nAccount id: "+account.num+"\nIf you remove the account, it can not be recovered!");
						if( dialog.open() == SWT.YES )
						{
							permitCountdownDialog = new PermitCountDownDialog(shell,SWT.SHELL_TRIM, account.name + ": Confirm deletion", "Press button to show delete "+account.name+".\nPress and hold to cancel.", 5000);
							shell.setMinimized(true);
							FkManager.getInstance().trig(account, 'd', mySelf);
							permitCountdownDialog.open();
							

						}
					}
				});
				fd_btnEdit.right = new FormAttachment(btnDelete, -6);
				btnDelete.setImage(SWTResourceManager.getImage(MainWin.class, "/fkgui/gfx/trashdelete.gif"));
				FormData fd_btnDelete = new FormData();
				fd_btnDelete.top = new FormAttachment(0, 10);
				fd_btnDelete.left = new FormAttachment(0, 166);
				fd_btnDelete.right = new FormAttachment(100, -11);
				btnDelete.setLayoutData(fd_btnDelete);
				btnDelete.setText("Delete");

				Button btnCancel = new Button(shell, SWT.NONE);
				fd_grpChange.bottom = new FormAttachment(btnCancel, -6);
				btnCancel.setImage(SWTResourceManager.getImage(MainWin.class, "/fkgui/gfx/Delete.png"));
				FormData fd_btnCancel = new FormData();
				fd_btnCancel.left = new FormAttachment(grpMakeFinalKey, 0, SWT.LEFT);
				fd_btnCancel.bottom = new FormAttachment(100, -10);
				fd_btnCancel.right = new FormAttachment(grpMakeFinalKey, 2, SWT.RIGHT);
				btnCancel.setLayoutData(fd_btnCancel);
				
				btnCancel.setText("Do nothing");
				
				btnCancel.addSelectionListener( new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						shell.close();
					}
				});				
				Group grpShow = new Group(shell, SWT.NONE);
				grpShow.setText("Show "+account.name);
				FormData fd_grpShow = new FormData();
				fd_grpShow.bottom = new FormAttachment(grpChange, 0, SWT.BOTTOM);
				fd_grpShow.right = new FormAttachment(grpChange, -6);
				fd_grpShow.left = new FormAttachment(grpMakeFinalKey, 0, SWT.LEFT);
				fd_grpShow.top = new FormAttachment(grpMakeFinalKey, 6);
				grpShow.setLayoutData(fd_grpShow);
				
				Button btnShowUsername = new Button(grpShow, SWT.NONE);
				btnShowUsername.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						FkManager.getInstance().trig(account, 's', mySelf);
						permitCountdownDialog = new PermitCountDownDialog(shell,SWT.SHELL_TRIM, account.name + ": Ready for display", "Press button to show username and password.\nPress and hold to cancel.", 30000);
						shell.setMinimized(true);
						permitCountdownDialog.open();
					}
				});
				btnShowUsername.setImage(SWTResourceManager.getImage(MainWin.class, "/fkgui/gfx/both.png"));
				btnShowUsername.setBounds(10, 36, 327, 35);
				btnShowUsername.setText("Show Username + Password");				
				

	}

	@Override
	public void fkActionEvent(FkActionEvent event) {
		MessageBox dialog;
		
		if( permitCountdownDialog != null && !permitCountdownDialog.shell.isDisposed() )
		{
			permitCountdownDialog.shell.close();
		}
		
		switch(event.type)
		{
		case ACTION_ABORTED:
			dialog = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
			dialog.setText(event.acc.name);
			dialog.setMessage("Action aborted or timed out.");
			dialog.open();
			break;
		case ACTION_ERROR:
			dialog = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
			dialog.setText(event.acc.name + " error");
			dialog.setMessage("An error ocurred.");
			dialog.open();			
			break;
		case ACTION_OKAY:
			result = (Object)new Boolean(true);
			if( event.action == 's' )
			{
				int begin = event.data.lastIndexOf("Account: "+ event.acc.num);
				if( begin == -1 )
				{
					begin=0;
				}
				int end = event.data.lastIndexOf("[done]");
				String s = event.data.substring( begin,end  );
				dialog = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
				dialog.setText("Account information");
				dialog.setMessage(s);
				dialog.open();
			}
			
			if( event.action == 'd' )
			{
				result = (Object)new Boolean(false); //We want the mainwin to get back after deleting an account.
				dialog = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
				dialog.setText("Account deleted");
				dialog.setMessage(event.acc.name + " has been erased.");
				dialog.open();
				Display.getDefault().asyncExec( new Runnable() {
					@Override
					public void run() {
						delegate.updateAccountList();
					}
					
				} );
			}
			
			if( !shell.isDisposed() )
			{
				shell.close();
			}			
			break;
		}


		
	}
}