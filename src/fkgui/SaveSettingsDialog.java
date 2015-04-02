package fkgui;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormLayout;
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
			setText(Messages.SaveSettingsDialog_0);
		} else {
			setText(Messages.SaveSettingsDialog_1);
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
		lblL.setText(Messages.SaveSettingsDialog_2);

		Button btnSaveLayout = new Button(cmpSetLayout, SWT.NONE);
		FormData fd_btnSaveLayout = new FormData();
		fd_btnSaveLayout.right = new FormAttachment(0, 606);
		fd_btnSaveLayout.top = new FormAttachment(0, 135);
		fd_btnSaveLayout.left = new FormAttachment(0, 515);
		btnSaveLayout.setLayoutData(fd_btnSaveLayout);
		btnSaveLayout.setText(Messages.SaveSettingsDialog_3);
		btnSaveLayout.addSelectionListener( new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				lblL.setText(Messages.SaveSettingsDialog_4);
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
		lblTest.setText(Messages.SaveSettingsDialog_5);
		
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
			setText(Messages.SaveSettingsDialog_6);
		} else {
			setText(Messages.SaveSettingsDialog_7);
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
		lblK.setText(Messages.SaveSettingsDialog_8);
		
		
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
		btnGoSaveBanner.setText(Messages.SaveSettingsDialog_9);
		
		btnGoSaveBanner.forceFocus();
		btnGoSaveBanner.addSelectionListener( new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				lblK.setText(Messages.SaveSettingsDialog_10);
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
			System.out.println("Event (B):" + event.toString() ); //$NON-NLS-1$
			
			if( event.type == FkActionEventType.ACTION_OKAY )
			{
				FkManager.getInstance().setBanner(bannerTxt);
				m = new MessageBox(shell, SWT.ICON_INFORMATION);
				m.setText(Messages.SaveSettingsDialog_12);
				m.setMessage(Messages.SaveSettingsDialog_13+bannerTxt);
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
				m.setText(Messages.SaveSettingsDialog_14);
				m.setMessage(Messages.SaveSettingsDialog_15);

				cmpSetBanner.dispose();
				if( m.open() == SWT.YES  )
				{
					createSetBanner();
					shell.layout();
				} else if( Layout != 0 ) {
					m = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO );
					m.setText(Messages.SaveSettingsDialog_16);
					m.setMessage(Messages.SaveSettingsDialog_17);
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
			System.out.println("Event (k):" + event.toString() ); //$NON-NLS-1$
			if( event.type == FkActionEventType.ACTION_OKAY )
			{
				if( txtTest.getText().compareTo( "Supported specials:!\"#$%&@?()[]-.,+{}_/<>=|'\\;: *" ) == 0 ) //$NON-NLS-1$
				{
					m = new MessageBox(shell, SWT.ICON_INFORMATION );
					m.setText(Messages.SaveSettingsDialog_20);
					m.setMessage(Messages.SaveSettingsDialog_21);
				} else {
					m = new MessageBox(shell, SWT.ICON_INFORMATION );
					m.setText(Messages.SaveSettingsDialog_22);
					m.setMessage(Messages.SaveSettingsDialog_23);
				}
				FkManager.getInstance().setCurrentLayout( FkManager.getInstance().getAvailableLayouts()[Layout-1] );
				m.open();

				shell.close();
			}

		}

		if( event.type == FkActionEventType.ACTION_ERROR  )
		{
			m = new MessageBox(shell, SWT.ICON_ERROR );
			m.setText(Messages.SaveSettingsDialog_24);
			m.setMessage( Messages.SaveSettingsDialog_25+event.data);
			m.open();
			shell.close();
			
		}
		
		if( event.type == FkActionEventType.STATE_ERROR )
		{
			m = new MessageBox(shell, SWT.ICON_ERROR );
			m.setText(Messages.SaveSettingsDialog_26);
			m.setMessage( Messages.SaveSettingsDialog_27+event.data);
			m.open();
			shell.close();
		}
			

		
	}
}
