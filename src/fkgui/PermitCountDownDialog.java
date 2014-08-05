package fkgui;

import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;

public class PermitCountDownDialog extends Dialog {

	protected Object result;
	protected Shell shell;
	public Display display;

	String msg;
	int msLeft;
	private Label lblTimeLeft;
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public PermitCountDownDialog(Shell parent, int style, String title, String m, int msL) {
		super(parent, style);
		setText(title);
		msLeft = msL;
		msg = m;
	
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
		p.x -= shell.getSize().x/2;
		p.y -= shell.getSize().y/2;
		p.x += getParent().getLocation().x;
		p.y += getParent().getLocation().y;
		shell.setLocation( p );
		shell.setLayout(null);
		
		Label lblMsg = new Label(shell, SWT.NONE);
		lblMsg.setBounds(48, 10, 461, 54);
		lblMsg.setText(msg);
		
		lblTimeLeft = new Label(shell, SWT.NONE);
		lblTimeLeft.setText("    ");
		lblTimeLeft.setBounds(48, 56, 201, 32);
		
		Animation animation = new Animation(shell, SWT.NONE, 4);
		animation.setBounds(10, 32, 32, 32);
		animation.setVisible(true);
		animation.addFrame( SWTResourceManager.getImage(MainWin.class, "/fkgui/gfx/finalkey1.png") );
		animation.addFrame( SWTResourceManager.getImage(MainWin.class, "/fkgui/gfx/finalkey2.png") );
		animation.setPlaying(true);
		
		

		shell.open();
		
		shell.layout();
		display = getParent().getDisplay();
				
		animate();
		shell.setActive();
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	private void animate() {
		display.timerExec(1000, new Runnable() {
			
			@Override
			public void run() {
				if( !shell.isDisposed() )
				{

					lblTimeLeft.setText( Math.round(msLeft/1000) + " seconds left.");

					display.update();
					msLeft -= 1000;
					if( msLeft<1)
					{
						shell.close();
						
					} else {				
						animate();
					}
					
				}
			}
		});
		
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell(getParent(), getStyle());
		shell.setSize(521, 125);
		shell.setText(getText());
	}
}
