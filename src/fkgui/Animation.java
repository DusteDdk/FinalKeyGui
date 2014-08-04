package fkgui;

import java.util.Vector;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.wb.swt.SWTResourceManager;

public class Animation extends Composite {

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	
	public int msDelay=1000;
	public Boolean playing=false;
	public int curFrame=0;
	public Vector<Image> frames;
	Label label;
	
	public Animation(Composite parent, int style, float FPS) {
		super(parent, style);
		setLayout(new FormLayout());
		
		label = new Label(this, SWT.NONE);
		FormData fd_label = new FormData();
		fd_label.top = new FormAttachment(0);
		fd_label.left = new FormAttachment(0);
		fd_label.bottom = new FormAttachment(0, 32);
		fd_label.right = new FormAttachment(0, 32);
		label.setLayoutData(fd_label);
		frames = new Vector<Image>(0);
		setFPS(FPS);
	}
	
	public void setPlaying(Boolean state )
	{
		playing=state;
		if(playing)
		{
			animate();
		}
	}

	private void animate() {
		getDisplay().timerExec(msDelay, new Runnable() {
			
			@Override
			public void run() {

				if( !label.isDisposed() )
				{
					if( isVisible() && frames.size() > 0 )
					{
						curFrame++;

						if( curFrame > frames.size()-1 )
						{
							curFrame=0;
						}
						
						label.setImage( frames.get(curFrame) );
	
						label.getDisplay().update();
					}
					if(playing)
					{
						animate();
					}
				}
				
			}
		});
		
	}



	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public void addFrame(Image image) {
		frames.addElement(image);
	}
	
	public void setFPS( float fps )
	{
		msDelay = (int)(1000.0 / fps);
	}
}
