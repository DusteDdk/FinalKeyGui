package fkgui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Button;

import com.sun.org.apache.bcel.internal.generic.CPInstruction;

public class Backup extends Dialog implements FkActionEventListener {

	protected Object result;
	protected Shell shell;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public Backup(Shell parent, int style) {
		super(parent, style);
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	private boolean restoreBackup;
	private File backupFile;
	private String fileName;
	private Backup mySelf; 
	private Composite cmpBackupFileSelect;
	
	
	public Object open(boolean _restoreBackup) {
		restoreBackup=_restoreBackup;
		createContents();
		shell.open();
		shell.layout();
		mySelf = this;

		if( _restoreBackup )
		{
			setText("FinalKey - Restore");
		} else {
			setText("FinalKey - Backup");
		}

		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell(getParent(), SWT.DIALOG_TRIM);
		shell.setSize(510, 506);
		shell.setText(getText());
		shell.setLayout(new FormLayout());
		
		cmpBackupFileSelect = new Composite(shell, SWT.NONE);
		cmpBackupFileSelect.setLayout(new FormLayout());
		FormData fd_cmpBackupFileSelect = new FormData();
		fd_cmpBackupFileSelect.bottom = new FormAttachment(0, 481);
		fd_cmpBackupFileSelect.right = new FormAttachment(0, 506);
		fd_cmpBackupFileSelect.top = new FormAttachment(0);
		fd_cmpBackupFileSelect.left = new FormAttachment(0);
		cmpBackupFileSelect.setLayoutData(fd_cmpBackupFileSelect);
		
		Label lblNewLabel = new Label(cmpBackupFileSelect, SWT.NONE);
		FormData fd_lblNewLabel = new FormData();
		fd_lblNewLabel.top = new FormAttachment(0, 10);
		fd_lblNewLabel.left = new FormAttachment(0, 10);
		fd_lblNewLabel.bottom = new FormAttachment(0, 184);
		fd_lblNewLabel.right = new FormAttachment(0, 496);
		lblNewLabel.setLayoutData(fd_lblNewLabel);
		lblNewLabel.setText("About backup:\nIt is a good idea to backup your FinalKey so you don't\nlose all your logins if you lose your FinalKey or it breaks.\n\nSecurity:\nThe FinalKey backup files are encrypted, but you should\nnever keep them on your computer or in a place where\nsomeone else may steal them. Keep your backup on\nan offline storage medium in a safe place.\n");
		
		Button btnBrowse = new Button(cmpBackupFileSelect, SWT.NONE);
		FormData fd_btnBrowse = new FormData();
		fd_btnBrowse.right = new FormAttachment(100, -10);
		fd_btnBrowse.top = new FormAttachment(100, -53);
		fd_btnBrowse.bottom = new FormAttachment(100, -10);
		btnBrowse.setLayoutData(fd_btnBrowse);
		btnBrowse.setText("Browse ...");
		
		btnBrowse.addSelectionListener( new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog fd = new FileDialog(shell, SWT.SAVE );
				String[] ext = { "*.fkb", "*.*"  };
				String[] extN = { "FinalKey Backup Files", "All Files"};
				fd.setFilterExtensions( ext );
				fd.setFilterNames(extN);
				
				fileName = fd.open();
				System.out.println("FileName:" +fileName);
				backupFile = new File(fileName);
				if( !backupFile.exists() && !backupFile.isDirectory())
				{ 
					try {
						backupFile.createNewFile();
						
						MessageBox m = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
						m.setText("Ready to take backup?");
						m.setMessage("When you press Yes, the FinalKey will blink, and you have 5 seconds to press the button to allow backup. Save backup now ?");
						
						if( m.open() == SWT.YES )
						{
							FkManager.getInstance().backup(backupFile, mySelf);
							
						} else {
							backupFile.delete();
						}

					} catch (IOException e1) {
						MessageBox m = new MessageBox(shell, SWT.ERROR);
						m.setText("Error");
						m.setMessage("Could not create the file, do you have permissions to write there? Is there enough free space?");
						m.open();
						fileName=null;
					}
				} else {
					MessageBox m = new MessageBox(shell, SWT.ERROR);
					m.setText("Error");
					m.setMessage("Will not overwrite an existing file, select another file-name or delete the existing file first.");
					m.open();
					fileName=null;
				}
				
				System.out.println(fileName);
			}
			
		});
		
		Label lblPressbrowseAnd = new Label(cmpBackupFileSelect, SWT.NONE);
		fd_btnBrowse.left = new FormAttachment(0, 408);
		FormData fd_lblPressbrowseAnd = new FormData();
		fd_lblPressbrowseAnd.right = new FormAttachment(btnBrowse, -6);
		fd_lblPressbrowseAnd.bottom = new FormAttachment(100, -10);
		fd_lblPressbrowseAnd.left = new FormAttachment(0, 10);
		fd_lblPressbrowseAnd.top = new FormAttachment(100, -77);
		lblPressbrowseAnd.setLayoutData(fd_lblPressbrowseAnd);
		lblPressbrowseAnd.setText("Press \"Browse\" to select where to save the backup.");

	}

	@Override
	public void fkActionEvent(FkActionEvent event) {
		MessageBox m;
		System.out.println(event);
		
		//Handle errors differently when we are taking bacup
		if( !restoreBackup )
		{
			switch( event.type )
			{
			case ACTION_ABORTED:
				break;
			case ACTION_ERROR:
				m = new MessageBox(shell, SWT.ICON_ERROR| SWT.YES| SWT.NO);
				m.setText("Checksum Error");
				m.setMessage("There were one or more errors when verifying the backup data, this may indicate a bad connection or a hardware-malfunction on the FinalKey.\nThe Backup File can NOT be restored onto the FinalKey, but it may be manually repaired by someone with a hex-editor and too much free-time. Do you want to delete the corrupted file ?");
				if( m.open() == SWT.YES )
				{
					backupFile.delete();
				} else {
					backupFile.renameTo( new File(fileName+"-failed"));
					File crashLog = new File(fileName+"-errorlog.txt");
					FileOutputStream fout;
					try {
						fout = new FileOutputStream( crashLog );
						fout.write( event.data.getBytes() );
						fout.close();
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
				shell.close();
				
				break;
			case ACTION_OKAY:
				//TODO:
				m=new MessageBox(shell, SWT.ICON_INFORMATION);
				m.setText("Backup successful");
				m.setMessage("Your backup was verified and saved.");
				m.open();
				shell.close();
				break;
			case ACTION_WAITING:
				//TODO:
				//Make visible icon
				//Write text "waiting for button press"
				break;
			case ACTION_WORKING:
				//TODO:
				//Switch to status-view (writing)
				cmpBackupFileSelect.dispose();
				break;
			case PROGRESS_UPDATE:
				
				break;
			case STATE_ERROR:
				break;
			case UNEXPECTED_ACTION_RESULT_ERROR:
				break;
			default:
				break;
			
			}
		}
		
	}

}
