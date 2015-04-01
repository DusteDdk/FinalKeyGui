package fkgui;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ProgressBar;

public class ChangePassPrompt extends Dialog implements FkActionEventListener {

	protected Object result;
	protected Shell shell;
	private Text txtCurrentPass;
	private Label label;
	private Label lblNewLabel_1;
	private Text txtNewPassA;
	private Text txtNewPassB;
	private Label lblRepeat;
	private Label label_1;
	private Button btnAbort;
	private Button btnGo;
	private boolean format;
	private ChangePassPrompt mySelf;
	public String newLayout;
	public String newBanner;
	
	private Composite content;
	
	//
	Label lblProgressMsg;
	Button btnClose;
	ProgressBar progressBar;
	
	//
	Animation animation;
	private Label lblWaitText;
	
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public ChangePassPrompt(Shell parent, int style) {
		super(parent, style);
		mySelf = this;
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open(boolean f) {
		this.format=f;
		createContents();
		shell.open();
		shell.layout();
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
		shell = new Shell(getParent(), getStyle());
		shell.setSize(450, 168);
		shell.setText(getText());
		shell.setLayout(new FormLayout());
		
		createPswEntry();
		//createProgress();
		//createWaiting();

	
	}
	
	
	
	private void createPswEntry()
	{
		content = new Composite(shell, SWT.NONE);
		content.setLayout(new FormLayout());
		FormData fd_content = new FormData();
		fd_content.top = new FormAttachment(0);
		fd_content.left = new FormAttachment(0);
		fd_content.right = new FormAttachment(100);
		fd_content.bottom = new FormAttachment(100);
		content.setLayoutData(fd_content);

		Label lblNewLabel = new Label(content, SWT.NONE);
		lblNewLabel.setAlignment(SWT.RIGHT);
		FormData fd_lblNewLabel = new FormData();
		fd_lblNewLabel.top = new FormAttachment(0, 10);
		fd_lblNewLabel.left = new FormAttachment(0, 10);
		lblNewLabel.setLayoutData(fd_lblNewLabel);
		lblNewLabel.setText("Current Password");

		txtCurrentPass = new Text(content, SWT.BORDER | SWT.PASSWORD);
		fd_lblNewLabel.right = new FormAttachment(txtCurrentPass, -6);
		FormData fd_txtCurrentPass = new FormData();
		fd_txtCurrentPass.left = new FormAttachment(100, -298);
		fd_txtCurrentPass.top = new FormAttachment(0, 10);
		fd_txtCurrentPass.right = new FormAttachment(100, -10);
		txtCurrentPass.setLayoutData(fd_txtCurrentPass);

		label = new Label(content, SWT.SEPARATOR | SWT.HORIZONTAL);
		FormData fd_label = new FormData();
		fd_label.top = new FormAttachment(txtCurrentPass, 6);
		fd_label.bottom = new FormAttachment(txtCurrentPass, 7, SWT.BOTTOM);
		fd_label.right = new FormAttachment(txtCurrentPass, 0, SWT.RIGHT);
		fd_label.left = new FormAttachment(0, 10);
		label.setLayoutData(fd_label);

		lblNewLabel_1 = new Label(content, SWT.NONE);
		lblNewLabel_1.setAlignment(SWT.RIGHT);
		FormData fd_lblNewLabel_1 = new FormData();
		fd_lblNewLabel_1.left = new FormAttachment(lblNewLabel, 0, SWT.LEFT);
		fd_lblNewLabel_1.top = new FormAttachment(label, 6);
		fd_lblNewLabel_1.right = new FormAttachment(lblNewLabel, 0, SWT.RIGHT);
		lblNewLabel_1.setLayoutData(fd_lblNewLabel_1);
		lblNewLabel_1.setText("New Password");

		txtNewPassA = new Text(content, SWT.BORDER | SWT.PASSWORD);
		FormData fd_txtNewPassA = new FormData();
		fd_txtNewPassA.right = new FormAttachment(100, -10);
		fd_txtNewPassA.top = new FormAttachment(label, 6);
		fd_txtNewPassA.left = new FormAttachment(txtCurrentPass, 0, SWT.LEFT);
		txtNewPassA.setLayoutData(fd_txtNewPassA);

		txtNewPassB = new Text(content, SWT.BORDER | SWT.PASSWORD);
		FormData fd_txtNewPassB = new FormData();
		fd_txtNewPassB.left = new FormAttachment(txtCurrentPass, 0, SWT.LEFT);
		fd_txtNewPassB.top = new FormAttachment(txtNewPassA, 6);
		fd_txtNewPassB.right = new FormAttachment(txtCurrentPass, 0, SWT.RIGHT);
		txtNewPassB.setLayoutData(fd_txtNewPassB);

		lblRepeat = new Label(content, SWT.NONE);
		lblRepeat.setText("Repeat");
		lblRepeat.setAlignment(SWT.RIGHT);
		FormData fd_lblRepeat = new FormData();
		fd_lblRepeat.left = new FormAttachment(0, 10);
		fd_lblRepeat.top = new FormAttachment(txtNewPassB, 0, SWT.TOP);
		fd_lblRepeat.right = new FormAttachment(lblNewLabel, 0, SWT.RIGHT);
		lblRepeat.setLayoutData(fd_lblRepeat);

		label_1 = new Label(content, SWT.SEPARATOR | SWT.HORIZONTAL);
		FormData fd_label_1 = new FormData();
		fd_label_1.top = new FormAttachment(txtNewPassB, 6);
		fd_label_1.bottom = new FormAttachment(txtNewPassB, 7, SWT.BOTTOM);
		fd_label_1.left = new FormAttachment(lblNewLabel, 0, SWT.LEFT);
		fd_label_1.right = new FormAttachment(100, -10);
		label_1.setLayoutData(fd_label_1);

		btnAbort = new Button(content, SWT.NONE);
		FormData fd_btnAbort = new FormData();
		fd_btnAbort.top = new FormAttachment(label_1, 5);
		fd_btnAbort.left = new FormAttachment(lblNewLabel, 0, SWT.LEFT);
		btnAbort.setLayoutData(fd_btnAbort);
		btnAbort.setText("Abort");
		btnAbort.addSelectionListener( new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				shell.close();
			}
		});

		btnGo = new Button(content, SWT.NONE);
		FormData fd_btnGo = new FormData();
		fd_btnGo.left = new FormAttachment(txtCurrentPass, -89);
		fd_btnGo.top = new FormAttachment(btnAbort, -22);
		fd_btnGo.bottom = new FormAttachment(btnAbort, 0, SWT.BOTTOM);
		fd_btnGo.right = new FormAttachment(txtCurrentPass, 0, SWT.RIGHT);
		btnGo.setLayoutData(fd_btnGo);
		btnGo.addSelectionListener( new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				MessageBox m;

				shell.setEnabled(false);

				if( txtNewPassA.getText().compareTo(txtNewPassB.getText()) == 0 )
				{
					if( txtNewPassA.getText().length() < 10 )
					{
						m = new MessageBox(shell, SWT.ICON_WARNING|SWT.YES|SWT.NO);
						m.setText("Short password");
						m.setMessage("Secure passwords need to be long and complex. Your password can be up to 32 characters long, do you want to continue with the short password?");
						if( m.open() == SWT.YES )
						{
							if( format )
							{
								FkManager.getInstance().format( txtCurrentPass.getText(), txtNewPassA.getText(), newLayout, newBanner, mySelf );
							} else {
								FkManager.getInstance().changePass( txtCurrentPass.getText(), txtNewPassA.getText(), mySelf );
							}
						} else {
							shell.setEnabled(true);
						}

					}
				} else {
					m = new MessageBox(shell, SWT.ICON_ERROR);
					m.setText("Passwords do not match");
					m.setMessage("The new passwords you entered do not match, try again.");
					m.open();
					shell.setEnabled(true);
				}
			}

		});
		

		if( format )
		{
			btnGo.setText("Format");
			setText("Format your FinalKey");
		} else {
			btnGo.setText("Re-Encrypt");
			setText("Change password");
		}
		
	}


	private void createProgress()
	{
		content = new Composite(shell, SWT.NONE);
		content.setLayout(new FormLayout());
		FormData fd_content = new FormData();
		fd_content.bottom = new FormAttachment(0, 143);
		fd_content.right = new FormAttachment(0, 448);
		fd_content.top = new FormAttachment(0);
		fd_content.left = new FormAttachment(0);
		content.setLayoutData(fd_content);

		lblProgressMsg = new Label(content, SWT.NONE);
		FormData fd_lblProgressMsg = new FormData();
		fd_lblProgressMsg.top = new FormAttachment(0, 10);
		fd_lblProgressMsg.left = new FormAttachment(0, 10);
		fd_lblProgressMsg.bottom = new FormAttachment(0, 36);
		fd_lblProgressMsg.right = new FormAttachment(100, -10);
		lblProgressMsg.setLayoutData(fd_lblProgressMsg);
		
		if(format )
		{
			lblProgressMsg.setText("Formatting...");
		} else {
			lblProgressMsg.setText("Re-encrypting... Do not unplug The FinalKey!");
		}

		progressBar = new ProgressBar(content, SWT.NONE);
		progressBar.setMaximum(255);
		FormData fd_progressBar = new FormData();
		fd_progressBar.top = new FormAttachment(lblProgressMsg, 6);
		fd_progressBar.left = new FormAttachment(0, 10);
		fd_progressBar.right = new FormAttachment(100, -10);
		progressBar.setLayoutData(fd_progressBar);

		btnClose = new Button(content, SWT.NONE);
		FormData fd_btnClose = new FormData();
		fd_btnClose.bottom = new FormAttachment(100, -10);
		fd_btnClose.right = new FormAttachment(lblProgressMsg, 0, SWT.RIGHT);
		btnClose.setLayoutData(fd_btnClose);
		btnClose.setText("Done");
		btnClose.setVisible(false);
		
		btnClose.addSelectionListener( new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(format)
				{
					FkManager.getInstance().disconnect();
				}
				shell.close();
			}
			
		});
		
		
	}
	
	private void createWaiting()
	{
		content = new Composite(shell, SWT.NONE);
		content.setLayout(new FormLayout());
		FormData fd_content = new FormData();
		fd_content.top = new FormAttachment(0);
		fd_content.left = new FormAttachment(0);
		fd_content.bottom = new FormAttachment(100);
		fd_content.right = new FormAttachment(100);
		content.setLayoutData(fd_content);
		
		animation = new Animation(content, SWT.NONE, 4);
		FormData fd_animation = new FormData();
		fd_animation.top = new FormAttachment(0, 10);
		fd_animation.left = new FormAttachment(0, 10);
		animation.setLayoutData(fd_animation);
		
		lblWaitText = new Label(content, SWT.NONE);
		FormData fd_lblWaitText = new FormData();
		fd_lblWaitText.bottom = new FormAttachment(0, 133);
		fd_lblWaitText.right = new FormAttachment(animation, 396, SWT.RIGHT);
		fd_lblWaitText.top = new FormAttachment(0, 10);
		fd_lblWaitText.left = new FormAttachment(animation, 6);
		lblWaitText.setLayoutData(fd_lblWaitText);
		animation.setVisible(true);
		animation.setPlaying(true);
	}

	private void createWorking()
	{
		content = new Composite(shell, SWT.NONE);
		content.setLayout(new FormLayout());
		FormData fd_content = new FormData();
		fd_content.top = new FormAttachment(0);
		fd_content.left = new FormAttachment(0);
		fd_content.bottom = new FormAttachment(100);
		fd_content.right = new FormAttachment(100);
		content.setLayoutData(fd_content);

		lblWaitText = new Label(content, SWT.NONE);
		FormData fd_lblWaitText = new FormData();
		fd_lblWaitText.bottom = new FormAttachment(0, 133);
		fd_lblWaitText.right = new FormAttachment(animation, 396, SWT.RIGHT);
		fd_lblWaitText.top = new FormAttachment(0, 10);
		fd_lblWaitText.left = new FormAttachment(animation, 6);
		lblWaitText.setLayoutData(fd_lblWaitText);
		lblWaitText.setText("Working...");

	}
	
	@Override
	public void fkActionEvent(FkActionEvent event) {
		
		MessageBox d;
		System.out.println("Event: "+event);
		
		switch( event.type )
		{
		case ACTION_ABORTED:
			d = new MessageBox(shell, SWT.ICON_WARNING);
			d.setText("Aborted");
			if(format)
			{
				d.setMessage("Formatting was aborted.\nMaybe you did not press the FinalKey button in time, or you held it down.");
			} else {
				d.setMessage("Re-encryption was aborted.\nMaybe you did not press the FinalKey button in time, or you held it down.");
			}
			d.open();
			shell.close();
			break;
		case STATE_ERROR:
		case ACTION_ERROR:
			d = new MessageBox(shell, SWT.ICON_ERROR);
			d.setText("Error");
			d.setMessage(event.data);
			d.open();
			shell.close();
			break;
		case ACTION_OKAY:
			btnClose.setVisible(true);
			shell.setEnabled(true);
			
			if( format )
			{
				lblProgressMsg.setText("Format complete.");
			} else {
				lblProgressMsg.setText("Re-encryption complete.");
			}

			break;
		case ACTION_WAITING:
			content.dispose();
			createWaiting();


			if( event.action == 'p' )
			{
				lblWaitText.setText("Press FinalKey button to re-encrypt.\nPress and hold to abort.");
			} else if( event.action == 'f' )
			{
				if( event.data.compareTo("stage1")==0 )
				{
					lblWaitText.setText("Press button to format.\nPress and hold to abort.");
				} else {
					lblWaitText.setText("Press button again.");
				}
			}
			shell.layout();

			break;
		case ACTION_WORKING:
			content.dispose();
			if( event.data.compareTo( "next" )==0 )
			{
				createProgress();
			} else {
				createWorking();
			}
			shell.layout();
			break;
		case FILE_ERROR:
			break;
		case PROGRESS_UPDATE:
			try {
				progressBar.setSelection( Integer.parseInt(event.data) );
			} catch(Exception e)
			{
				
			}
			break;
		case UNEXPECTED_ACTION_RESULT_ERROR:
			break;
		default:
			break;
		
		}
		
	}
}
