package fkgui;

import java.io.File;
import java.io.FileOutputStream;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
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
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ProgressBar;

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
	private Backup mySelf; 
	private Composite cmpBackupFileSelect;
	private Composite cmpWaiting;
	private Composite cmpStatus;
	Animation animation;
	Label lblFileName;
	Button btnBackup;
	Label lblMsg;
	private Text txtFileName;
	private Button btnBrowse;
	private String fileName;
	private Label lblStatusVerified;
	private Button btnStatusOk;
	private ProgressBar progressBar=null;


	public Object open(boolean _restoreBackup) {
		restoreBackup=_restoreBackup;
		createContents();
		shell.open();
		shell.layout();
		mySelf = this;

		if( _restoreBackup )
		{
			setText(Messages.Backup_0);
		} else {
			setText(Messages.Backup_1);
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
		shell.setSize(512, 302);
		shell.setText(getText());
		shell.setLayout(new FormLayout());
		
		//createStatusView("test");
		//createWaitingScreen("Press the button now to take a backup of your FinalKey.");
		//createBackupFileSelection();

		createBackupFileSelection();
		
	}

	private void createWaitingScreen(String msg) {
		
		cmpWaiting = new Composite(shell, SWT.NONE);
		FormData fd_cmpWaiting = new FormData();
		fd_cmpWaiting.top = new FormAttachment(0);
		fd_cmpWaiting.left = new FormAttachment(0);
		fd_cmpWaiting.bottom = new FormAttachment(0, 277);
		fd_cmpWaiting.right = new FormAttachment(0, 510);
		cmpWaiting.setLayoutData(fd_cmpWaiting);
		
		Label lblMsg = new Label(cmpWaiting, SWT.NONE);
		lblMsg.setBounds(48, 10, 393, 43);
		
		lblMsg.setText(msg);
		
		animation = new Animation(cmpWaiting, SWT.NONE, 4);
		animation.setBounds(10, 10, 32, 32);

		animation.setVisible(true);
		animation.setPlaying(true);
		
	}

	private void createBackupFileSelection() {
		cmpBackupFileSelect = new Composite(shell, SWT.NONE);
		cmpBackupFileSelect.setLayout(new FormLayout());
		FormData fd_cmpBackupFileSelect = new FormData();
		fd_cmpBackupFileSelect.bottom = new FormAttachment(0, 277);
		fd_cmpBackupFileSelect.right = new FormAttachment(0, 510);
		fd_cmpBackupFileSelect.top = new FormAttachment(0);
		fd_cmpBackupFileSelect.left = new FormAttachment(0);
		cmpBackupFileSelect.setLayoutData(fd_cmpBackupFileSelect);

		lblMsg = new Label(cmpBackupFileSelect, SWT.NONE);
		FormData fd_lblNewLabel = new FormData();
		fd_lblNewLabel.right = new FormAttachment(100, -91);
		fd_lblNewLabel.left = new FormAttachment(0, 10);
		lblMsg.setLayoutData(fd_lblNewLabel);

		if(!restoreBackup)
		{
			lblMsg.setText(Messages.Backup_2);
		} else {
			lblMsg.setText(Messages.Backup_3);
		}

		btnBackup = new Button(cmpBackupFileSelect, SWT.NONE);
		if(!restoreBackup)
		{
			btnBackup.setText(Messages.Backup_4);
			btnBackup.setImage(SWTResourceManager.getImage(Backup.class, "/fkgui/gfx/backup.png")); //$NON-NLS-1$
		} else {
			btnBackup.setText(Messages.Backup_6);
			btnBackup.setImage(SWTResourceManager.getImage(Backup.class, "/fkgui/gfx/restore.png")); //$NON-NLS-1$
		}
		FormData fd_btnBackup = new FormData();
		fd_btnBackup.bottom = new FormAttachment(100, -10);
		fd_btnBackup.right = new FormAttachment(100, -10);
		fd_btnBackup.top = new FormAttachment(0, 234);
		btnBackup.setLayoutData(fd_btnBackup);
		btnBackup.setEnabled(false);


		if(!restoreBackup)
		{
			btnBackup.addSelectionListener( new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {

					fileName=txtFileName.getText();

					try {

						backupFile = new File(fileName);

						if(backupFile.createNewFile())
						{
							MessageBox m = new MessageBox(shell, SWT.ICON_INFORMATION);
							m.setText(Messages.Backup_8);
							m.setMessage(Messages.Backup_9);
							shell.setEnabled(false);
							m.open();
							FkManager.getInstance().backup(backupFile, mySelf);
							shell.setEnabled(true);

						} else {
							MessageBox m = new MessageBox(shell, SWT.ICON_ERROR);
							m.setText(Messages.Backup_10);
							m.setMessage(Messages.Backup_11);
							shell.setEnabled(false);
							m.open();
							shell.setEnabled(true);
						}

					} catch(Exception ex)
					{
						fileName=""; //$NON-NLS-1$
						txtFileName.setText(""); //$NON-NLS-1$
						btnBackup.setEnabled(false);
						MessageBox m = new MessageBox(shell, SWT.ICON_ERROR);
						m.setText(Messages.Backup_14);
						m.setMessage(Messages.Backup_15+ex.getLocalizedMessage() );
						shell.setEnabled(false);
						m.open();
						shell.setEnabled(true);
					}

				}

			});
		} else {
			btnBackup.addSelectionListener( new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {

					fileName=txtFileName.getText();


					try {

						backupFile = new File(fileName);

						if( backupFile.canRead() )
						{
							MessageBox m = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO);
							m.setText(Messages.Backup_16);
							m.setMessage(Messages.Backup_17);
							shell.setEnabled(false);
							if( m.open() == SWT.YES )
							{
								FkManager.getInstance().restore(backupFile, mySelf);
							} else {
								m = new MessageBox(shell, SWT.ICON_INFORMATION);
								m.setText(Messages.Backup_18);
								m.setMessage(Messages.Backup_19);
								m.open();
								shell.close();
							}

						} else {
							MessageBox m = new MessageBox(shell, SWT.ICON_ERROR);
							m.setText(Messages.Backup_20);
							m.setMessage(Messages.Backup_21);
							shell.setEnabled(false);
							m.open();
							shell.setEnabled(true);
						}

					} catch(Exception ex)
					{
						fileName=""; //$NON-NLS-1$
						txtFileName.setText(""); //$NON-NLS-1$
						btnBackup.setEnabled(false);
						MessageBox m = new MessageBox(shell, SWT.ICON_ERROR);
						m.setText(Messages.Backup_24);
						m.setMessage(Messages.Backup_25+ex.getLocalizedMessage() );
						shell.setEnabled(false);
						m.open();
						shell.setEnabled(true);
					}

				}

			});

		} //Restore
		fd_btnBackup.left = new FormAttachment(0, 371);

		Label lblThisSavesThe = new Label(cmpBackupFileSelect, SWT.NONE);
		fd_lblNewLabel.top = new FormAttachment(lblThisSavesThe, 18);
		lblThisSavesThe.setFont(SWTResourceManager.getFont("Sans", 10, SWT.BOLD)); //$NON-NLS-1$
		FormData fd_lblThisSavesThe = new FormData();
		fd_lblThisSavesThe.top = new FormAttachment(0, 10);
		fd_lblThisSavesThe.left = new FormAttachment(lblMsg, 0, SWT.LEFT);
		fd_lblThisSavesThe.bottom = new FormAttachment(100, -245);
		fd_lblThisSavesThe.right = new FormAttachment(100, -68);
		lblThisSavesThe.setLayoutData(fd_lblThisSavesThe);

		if(!restoreBackup)
		{
			lblThisSavesThe.setText(Messages.Backup_27);
		} else {
			lblThisSavesThe.setText(Messages.Backup_28);
		}

		lblFileName = new Label(cmpBackupFileSelect, SWT.NONE);
		fd_lblNewLabel.bottom = new FormAttachment(100, -81);
		lblFileName.setAlignment(SWT.RIGHT);
		lblFileName.setText(Messages.Backup_29);
		FormData fd_lblFileName = new FormData();
		fd_lblFileName.bottom = new FormAttachment(btnBackup,0, SWT.CENTER);
		fd_lblFileName.left = new FormAttachment(0, 10);
		lblFileName.setLayoutData(fd_lblFileName);

		txtFileName = new Text(cmpBackupFileSelect, SWT.BORDER);
		fd_lblFileName.right = new FormAttachment(100, -463);
		FormData fd_txtFileName = new FormData();
		//fd_txtFileName.top = new FormAttachment(lblMsg, 49);
		fd_txtFileName.bottom = new FormAttachment(btnBackup, 0, SWT.CENTER);
		fd_txtFileName.left = new FormAttachment(lblFileName, 6);
		txtFileName.setLayoutData(fd_txtFileName);
		txtFileName.addListener(SWT.CHANGED, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				if( txtFileName.getText().length() > 0 )
				{
					btnBackup.setEnabled(true);
				} else {
					btnBackup.setEnabled(false);
				}
			}
		});

		btnBrowse = new Button(cmpBackupFileSelect, SWT.NONE);
		fd_txtFileName.right = new FormAttachment(btnBrowse, -6);
		FormData fd_btnBrowse = new FormData();
		fd_btnBrowse.left = new FormAttachment(0, 322);
		fd_btnBrowse.right = new FormAttachment(btnBackup, -6);
		fd_btnBrowse.top = new FormAttachment(lblMsg, 38);
		fd_btnBrowse.bottom = new FormAttachment(btnBackup, 0, SWT.BOTTOM);
		btnBrowse.setLayoutData(fd_btnBrowse);
		btnBrowse.setText("..."); //$NON-NLS-1$
		btnBrowse.addSelectionListener( new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog fd;

				if(restoreBackup)
				{
					fd = new FileDialog(shell, SWT.OPEN );
				} else {
					fd = new FileDialog(shell, SWT.SAVE );
				}

				String[] ext = { "*.fkb", "*.*"  }; //$NON-NLS-1$ //$NON-NLS-2$
				String[] extN = { Messages.Backup_33, Messages.Backup_34};

				//Try to start the selection dialog in the existing information, if it is correct and exist
				fileName = txtFileName.getText();
				if( fileName.length() != 0 )
				{
					File testFile = new File(fileName);
					String dir = testFile.getParent();
					if( dir != null )
					{
						fd.setFilterPath(dir);

						if( (!restoreBackup && !testFile.isFile()) || (restoreBackup && testFile.isFile()) )
						{
							fd.setFileName(testFile.getName());
						}
					}
				}

				if(restoreBackup)
				{
					fd.setFileName(""); //$NON-NLS-1$
				}

				fd.setFilterExtensions( ext );
				fd.setFilterNames(extN);

				fileName = fd.open();

				if( fileName != null )
				{

					System.out.println("FileName: " + fileName); //$NON-NLS-1$
					txtFileName.setText(fileName);
					btnBackup.setEnabled(true);
				}
			}
		});

	}

	@Override
	public void fkActionEvent(FkActionEvent event) {
		MessageBox m;

		//Handle errors differently when we are taking bacup
		if( !restoreBackup )
		{
			switch( event.type )
			{
			case ACTION_ABORTED:
				backupFile.delete();
				m = new MessageBox(shell, SWT.ICON_WARNING| SWT.YES| SWT.NO);
				m.setText(Messages.Backup_37);
				m.setMessage(Messages.Backup_38);

				if( m.open() == SWT.YES )
				{
					cmpWaiting.dispose();
					createBackupFileSelection();
					shell.layout();
				} else {
					shell.close();
				}
				break;
			case ACTION_ERROR:
				lblStatusVerified.setText(Messages.Backup_39);

				m = new MessageBox(shell, SWT.ICON_ERROR| SWT.YES| SWT.NO);
				m.setText(Messages.Backup_40);
				m.setMessage(Messages.Backup_41);
				if( m.open() == SWT.YES )
				{
					backupFile.delete();
				} else {
					backupFile.renameTo( new File(fileName+"-failed")); //$NON-NLS-1$
					File crashLog = new File(fileName+"-errorlog.txt"); //$NON-NLS-1$
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
				lblStatusVerified.setText(Messages.Backup_44);
				progressBar.setSelection(66000);
				m=new MessageBox(shell, SWT.ICON_INFORMATION);
				m.setText(Messages.Backup_45);
				m.setMessage(Messages.Backup_46);
				m.open();
				shell.close();
				break;
			case ACTION_WAITING:
				cmpBackupFileSelect.dispose();
				createWaitingScreen(Messages.Backup_47);
				shell.layout();
				break;
			case ACTION_WORKING:
				cmpWaiting.dispose();
				createStatusView(Messages.Backup_48);
				shell.layout();
				shell.setEnabled(false);
				break;
			case PROGRESS_UPDATE:
				if(progressBar!= null )
				{
					progressBar.setSelection( Integer.parseInt( event.data ) );
				}
				break;
			case UNEXPECTED_ACTION_RESULT_ERROR:
				break;
			default:
				break;
			
			}
		} else {
			boolean disconnect=false;
			switch(event.type)
			{
			case ACTION_ABORTED:
				m = new MessageBox(shell, SWT.ICON_ERROR);
				m.setText(Messages.Backup_49);
				m.setMessage(Messages.Backup_50);
				m.open();
				shell.close();
				break;
			case ACTION_ERROR:
				disconnect=true;
				m = new MessageBox(shell, SWT.ICON_ERROR);
				m.setText(Messages.Backup_51);
				m.setMessage(Messages.Backup_52+event.data);
				m.open();
				break;
			case ACTION_OKAY:
				lblStatusVerified.setText(Messages.Backup_53);
				lblStatusVerified.setVisible(true);
				m = new MessageBox(shell, SWT.ICON_INFORMATION);
				m.setText(Messages.Backup_54);
				m.setMessage(Messages.Backup_55);
				FkManager.getInstance().disconnect();
				m.open();
				shell.close();
				break;
			case ACTION_WAITING:
				shell.setEnabled(false);
				cmpBackupFileSelect.dispose();
				createWaitingScreen(Messages.Backup_56);
				shell.layout();
				break;
			case ACTION_WORKING:
				cmpWaiting.dispose();
				createStatusView(Messages.Backup_57);
				lblStatusVerified.setVisible(false);
				shell.layout();
				break;
			case FILE_ERROR:
				m = new MessageBox(shell, SWT.ICON_ERROR);
				m.setText(Messages.Backup_58);
				m.setMessage(Messages.Backup_59+event.data);
				m.open();
				shell.setEnabled(true);
				break;
			case PROGRESS_UPDATE:
				progressBar.setSelection(Integer.parseInt(event.data));
				break;
			default:
				break;

			}

			if(event.type == FkActionEventType.STATE_ERROR )
			{
				m = new MessageBox(shell, SWT.ICON_ERROR);
				m.setText(Messages.Backup_60);
				m.setMessage(Messages.Backup_61);
				m.open();
				disconnect=true;
			}

			if(disconnect)
			{
				FkManager.getInstance().disconnect();
				shell.close();
			}

		}

	}

	private void createStatusView(String msg) {
		cmpStatus = new Composite(shell, SWT.NONE);
		cmpStatus.setLayout(new FormLayout());
		FormData fd_cmpStatus = new FormData();
		fd_cmpStatus.bottom = new FormAttachment(0, 277);
		fd_cmpStatus.right = new FormAttachment(0, 510);
		fd_cmpStatus.top = new FormAttachment(0);
		fd_cmpStatus.left = new FormAttachment(0);
		cmpStatus.setLayoutData(fd_cmpStatus);

		Label lblStatusMsg = new Label(cmpStatus, SWT.NONE);
		FormData fd_lblStatusMsg = new FormData();
		fd_lblStatusMsg.right = new FormAttachment(0, 500);
		fd_lblStatusMsg.top = new FormAttachment(0, 10);
		fd_lblStatusMsg.left = new FormAttachment(0, 10);
		lblStatusMsg.setLayoutData(fd_lblStatusMsg);
		lblStatusMsg.setText(msg);

		progressBar = new ProgressBar(cmpStatus, SWT.NONE);
		progressBar.setToolTipText(Messages.Backup_62);
		progressBar.setMaximum(66000);
		FormData fd_progressBar = new FormData();
		fd_progressBar.right = new FormAttachment(lblStatusMsg, 0, SWT.RIGHT);
		fd_progressBar.top = new FormAttachment(lblStatusMsg, 6);
		fd_progressBar.left = new FormAttachment(0, 10);
		progressBar.setLayoutData(fd_progressBar);

		lblStatusVerified = new Label(cmpStatus, SWT.NONE);
		FormData fd_lblStatusVerified = new FormData();
		fd_lblStatusVerified.right = new FormAttachment(lblStatusMsg, 0, SWT.RIGHT);
		fd_lblStatusVerified.top = new FormAttachment(progressBar, 6);
		fd_lblStatusVerified.left = new FormAttachment(lblStatusMsg, 0, SWT.LEFT);
		lblStatusVerified.setLayoutData(fd_lblStatusVerified);
		lblStatusVerified.setText(Messages.Backup_63);

		btnStatusOk = new Button(cmpStatus, SWT.NONE);
		fd_lblStatusVerified.bottom = new FormAttachment(btnStatusOk, -6);
		FormData fd_btnStatusOk = new FormData();
		fd_btnStatusOk.bottom = new FormAttachment(100, -10);
		fd_btnStatusOk.right = new FormAttachment(100, -10);
		btnStatusOk.setLayoutData(fd_btnStatusOk);
		btnStatusOk.setText(Messages.Backup_64);
		btnStatusOk.setVisible(false);

	}
}
