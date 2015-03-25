package fkgui;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;

public class SaveSettingsDialog extends Dialog implements FkActionEventListener {

	protected Object result;
	protected Shell shell;

	public String bannerTxt = null;
	public int Layout = 0;
	private Composite cmpSetBanner;
	public Animation animation;
	private SaveSettingsDialog mySelf;
	private Label lblK;
	private Text txtTest;
	private Label lblL;
	
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public SaveSettingsDialog(Shell parent, int style) {
		super(parent, style);
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	
	
	public Object open() {

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
		shell.setSize(638, 216);
		shell.setText(getText());
		shell.setLayout(new FormLayout());

		mySelf = this;
//		createSetLayout();
//		createSetBanner();
		if( bannerTxt != null )
		{
			createSetBanner();
		} else {
			createSetLayout();
		}
	
		
	}

	private void createSetLayout() {
		if(bannerTxt == null)
		{
			setText("Save Settings, Save the keyboard layout");
		} else {
			setText("Save Settings, step 2/2: Save the keyboard layout");
		}

		Composite cmpSetLayout = new Composite(shell, SWT.NONE);
		cmpSetLayout.setLayout(new FormLayout());
		FormData fd_cmpSetLayout = new FormData();
		fd_cmpSetLayout.bottom = new FormAttachment(0, 181);
		fd_cmpSetLayout.right = new FormAttachment(0, 626);
		fd_cmpSetLayout.top = new FormAttachment(0, 10);
		fd_cmpSetLayout.left = new FormAttachment(0, 10);
		cmpSetLayout.setLayoutData(fd_cmpSetLayout);
		
		lblL = new Label(cmpSetLayout, SWT.NONE);
		FormData fd_lblL = new FormData();
		fd_lblL.bottom = new FormAttachment(0, 90);
		fd_lblL.right = new FormAttachment(0, 544);
		fd_lblL.top = new FormAttachment(0, 10);
		fd_lblL.left = new FormAttachment(0, 10);
		lblL.setLayoutData(fd_lblL);
		lblL.setText("Ready to set the keyboard layout.\nWhen you press GO, The FinalKey will start blinking.\nPress the button on the FinalKey to proceed.");

		Button btnSaveLayout = new Button(cmpSetLayout, SWT.NONE);
		FormData fd_btnSaveLayout = new FormData();
		fd_btnSaveLayout.right = new FormAttachment(0, 606);
		fd_btnSaveLayout.top = new FormAttachment(0, 135);
		fd_btnSaveLayout.left = new FormAttachment(0, 515);
		btnSaveLayout.setLayoutData(fd_btnSaveLayout);
		btnSaveLayout.setText("GO");
		btnSaveLayout.addSelectionListener( new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				lblL.setText("Press the button now.");
				animation.setVisible(true);
				animation.setPlaying(true);
				Button btn = (Button)e.widget;
				btn.setVisible(false);
				txtTest.setEnabled(true);
				txtTest.forceFocus();
				FkManager.getInstance().saveLayout( Layout, mySelf );
			}
		});
		
		
		animation = new Animation(cmpSetLayout, SWT.NONE, 4);
		FormData fd_animation = new FormData();
		fd_animation.top = new FormAttachment(0, 26);
		fd_animation.right = new FormAttachment(btnSaveLayout, -16, SWT.RIGHT);
		animation.setLayoutData(fd_animation);


		animation.setVisible(false);
		animation.setPlaying(false);
		
		Label lblTest = new Label(cmpSetLayout, SWT.NONE);
		FormData fd_lblTest = new FormData();
		fd_lblTest.bottom = new FormAttachment(lblL, 32, SWT.BOTTOM);
		fd_lblTest.top = new FormAttachment(lblL, 6);
		fd_lblTest.left = new FormAttachment(0, 10);
		fd_lblTest.right = new FormAttachment(0, 52);
		lblTest.setLayoutData(fd_lblTest);
		lblTest.setText("Test:");
		
		txtTest = new Text(cmpSetLayout, SWT.BORDER);
		FormData fd_txtTest = new FormData();
		fd_txtTest.right = new FormAttachment(lblTest, 312, SWT.RIGHT);
		fd_txtTest.top = new FormAttachment(lblL, 6);
		fd_txtTest.left = new FormAttachment(lblTest, 6);
		txtTest.setLayoutData(fd_txtTest);
		txtTest.setEnabled(false);
		
	}

	private void createSetBanner() {
		
		if(Layout == 0)
		{
			setText("Save Settings, Save the banner text");
		} else {
			setText("Save Settings, step 1/2: Save the banner text");
		}
		
		cmpSetBanner = new Composite(shell, SWT.NONE);
		FormData fd_cmpSetBanner = new FormData();
		fd_cmpSetBanner.bottom = new FormAttachment(0, 181);
		fd_cmpSetBanner.right = new FormAttachment(0, 626);
		fd_cmpSetBanner.top = new FormAttachment(0, 10);
		fd_cmpSetBanner.left = new FormAttachment(0, 10);
		cmpSetBanner.setLayoutData(fd_cmpSetBanner);
		cmpSetBanner.setLayout(new FormLayout());
		
		lblK = new Label(cmpSetBanner, SWT.NONE);
		FormData fd_lblK = new FormData();
		fd_lblK.right = new FormAttachment(0, 564);
		fd_lblK.top = new FormAttachment(0, 10);
		fd_lblK.left = new FormAttachment(0, 10);
		lblK.setLayoutData(fd_lblK);
		lblK.setText("Ready to set the banner text.\nWhen you press GO, The FinalKey will start blinking, and you have 5 seconds\nto press the button on your FinalKey to confirm saving the new banner.\n");
		
		
		animation = new Animation(cmpSetBanner, SWT.NONE, 4);


		FormData fd_animation = new FormData();
		fd_animation.top = new FormAttachment(0, 16);

		animation.setLayoutData(fd_animation);

		animation.setVisible(false);
		animation.setPlaying(false);
		
		Button btnGoSaveBanner = new Button(cmpSetBanner, SWT.NONE);
		fd_animation.right = new FormAttachment(btnGoSaveBanner, -16, SWT.RIGHT);
		FormData fd_btnGoSaveBanner = new FormData();
		fd_btnGoSaveBanner.top = new FormAttachment(100, -56);
		fd_btnGoSaveBanner.bottom = new FormAttachment(100, -10);
		fd_btnGoSaveBanner.right = new FormAttachment(100, -10);
		fd_btnGoSaveBanner.left = new FormAttachment(100, -117);
		btnGoSaveBanner.setLayoutData(fd_btnGoSaveBanner);
		btnGoSaveBanner.setText("GO");
		
		btnGoSaveBanner.forceFocus();
		btnGoSaveBanner.addSelectionListener( new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				lblK.setText("Press the button now.");
				animation.setVisible(true);
				animation.setPlaying(true);
				Button btn = (Button)e.widget;
				btn.setVisible(false);
				FkManager.getInstance().saveBanner( bannerTxt, mySelf );
			}
		});
	}

	@Override
	public void fkActionEvent(FkActionEvent event) {
		MessageBox m;
		
		if( event.type == FkActionEventType.ACTION_WORKING )
		{
			if( animation.isVisible() )
			{
				animation.setPlaying(false);
				animation.setVisible(false);
			}
		}
		
		if( event.action == 'b' )
		{
			System.out.println("Event (B):" + event.toString() );
			
			if( event.type == FkActionEventType.ACTION_OKAY )
			{
				FkManager.getInstance().setBanner(bannerTxt);
				m = new MessageBox(shell, SWT.ICON_INFORMATION);
				m.setText("Saved");
				m.setMessage("Banner is set to "+bannerTxt);
				m.open();
				
				if(Layout!=0)
				{
					cmpSetBanner.dispose();
					createSetLayout();
					shell.layout();
				} else {
					shell.close();
				}
			}

			if( event.type == FkActionEventType.ACTION_ABORTED )
			{
				m = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO );
				m.setText("Set banner was aborted");
				m.setMessage("The banner was not saved. You did not press the button in time, or you held it down to cancel saving the banner. Do you want to try again ?");

				cmpSetBanner.dispose();
				if( m.open() == SWT.YES  )
				{
					createSetBanner();
					shell.layout();
				} else if( Layout != 0 ) {
					m = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO );
					m.setText("Continue saving layout");
					m.setMessage("You said no to save the banner, but you also wanted to change the keyboard layout. Do you want save the keyboard layout ?");
					if( m.open() == SWT.YES )
					{
						createSetLayout();
						shell.layout();
					} else {
						shell.close();
					}
				}

			}
		}

		if( event.action == 'k')
		{
			System.out.println("Event (k):" + event.toString() );
			if( event.type == FkActionEventType.ACTION_OKAY )
			{
				if( txtTest.getText().compareTo( "Supported specials:!\"#$%&@?()[]-.,+{}_/<>=|'\\;: *" ) == 0 )
				{
					m = new MessageBox(shell, SWT.ICON_INFORMATION );
					m.setText("Saved");
					m.setMessage("Layout verified.");
				} else {
					m = new MessageBox(shell, SWT.ICON_INFORMATION );
					m.setText("Saved");
					m.setMessage("The layout was saved, but it did not look right, maybe the FinalKey application runs with a different input-language setting than the application you want to use, but if your FinalKey is writing the wrong letters then try another layout.");
				}
				FkManager.getInstance().setCurrentLayout( FkManager.getInstance().getAvailableLayouts()[Layout-1] );
				m.open();

				shell.close();
			}

		}

		if( event.type == FkActionEventType.ACTION_ERROR  )
		{
			m = new MessageBox(shell, SWT.ICON_ERROR );
			m.setText("Error - All changes may not have been saved");
			m.setMessage( "Something went wrong, here is the output: "+event.data);
			m.open();
			shell.close();
			
		}
		
		if( event.type == FkActionEventType.STATE_ERROR )
		{
			m = new MessageBox(shell, SWT.ICON_ERROR );
			m.setText("State Error - All changes may not have been saved");
			m.setMessage( "Something went wrong, here is the output: "+event.data);
			m.open();
			shell.close();
		}
			

		
	}
}
