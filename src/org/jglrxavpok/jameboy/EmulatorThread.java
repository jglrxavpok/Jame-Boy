package org.jglrxavpok.jameboy;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.image.BufferStrategy;

import org.jglrxavpok.jameboy.graphics.Screen;
import org.jglrxavpok.jameboy.input.Mouse;

public class EmulatorThread extends Thread
{
	private boolean alive = true;
	private int frame;
	private int fps;
	//This value would probably be stored elsewhere.
	final double GAME_HERTZ = 60.0;
	//Calculate how many ns each frame should take for our target game hertz.
	final double TIME_BETWEEN_UPDATES = 1000000000 / GAME_HERTZ;
	//At the very most we will update the game this many times before a new render.
	//If you're worried about visual hitches more than perfect timing, set this to 1.
	final int MAX_UPDATES_BEFORE_RENDER = 5;
	//We will need the last update time.
	double lastUpdateTime = System.nanoTime();
	//Store the last time we rendered.
	double lastRenderTime = System.nanoTime();
  
	//If we are able to get as high as this FPS, don't render again.
	final double TARGET_FPS = 25;
	final double TARGET_TIME_BETWEEN_RENDERS = 1000000000 / TARGET_FPS;
  
	//Simple way of finding FPS.
	int lastSecondTime = (int) (lastUpdateTime / 1000000000);
	
	boolean paused = false;
    private Screen screen;
	
	public EmulatorThread()
	{
	    screen = JameBoy.screen;
	}
	
	public void run()
	{
		while(alive)
		{
			loop();
		}
		
		System.exit(0);
	}
	
	private void loop()
	{
        double now = System.nanoTime();
        int updateCount = 0;
        
        if (!paused)
        {
            //Do as many game updates as we need to, potentially playing catchup.
           while( now - lastUpdateTime > TIME_BETWEEN_UPDATES && updateCount < MAX_UPDATES_BEFORE_RENDER )
           {
              update();
              lastUpdateTime += TIME_BETWEEN_UPDATES;
              updateCount++;
           }
  
           //If for some reason an update takes forever, we don't want to do an insane number of catchups.
           //If you were doing some sort of game that needed to keep EXACT time, you would get rid of this.
           if ( now - lastUpdateTime > TIME_BETWEEN_UPDATES)
           {
              lastUpdateTime = now - TIME_BETWEEN_UPDATES;
           }
        
           //Render. To do so, we need to calculate interpolation for a smooth render.
           float interpolation = Math.min(1.0f, (float) ((now - lastUpdateTime) / TIME_BETWEEN_UPDATES) );
           BufferStrategy bs = JameBoy.mainFrame.getBufferStrategy();
           if (bs == null) {
               JameBoy.mainFrame.createBufferStrategy(2);
        	   return;
           }
           render(bs.getDrawGraphics(), interpolation);
           bs.show();
           lastRenderTime = now;
           //Update the frames we got.
           int thisSecond = (int) (lastUpdateTime / 1000000000);
           frame++;
           if (thisSecond > lastSecondTime)
           {
              fps = frame;
              if(JameBoy.emulator.getMemory().getROMTitle() != null)
                  JameBoy.mainFrame.setTitle("JameBoy - "+JameBoy.emulator.getMemory().getROMTitle()+" - "+fps+" fps");
              else
                  JameBoy.mainFrame.setTitle("JameBoy - "+fps+" fps");
              frame = 0;
              lastSecondTime = thisSecond;
           }
           
           if(!JameBoy.mainFrame.isVisible())
        	   alive = false;
        
           //Yield until it has been at least the target time between renders. This saves the CPU from hogging.
           while ( now - lastRenderTime < TARGET_TIME_BETWEEN_RENDERS && now - lastUpdateTime < TIME_BETWEEN_UPDATES)
           {
              Thread.yield();
           
              //This stops the app from consuming all your CPU. It makes this slightly less accurate, but is worth it.
              //You can remove this line and it will still work (better), your CPU just climbs on certain OSes.
              //FYI on some OS's this can cause pretty bad stuttering. Scroll down and have a look at different peoples' solutions to this.
              try {Thread.sleep(1);} catch(Exception e) {} 
           
              now = System.nanoTime();
           }
        }
	}
	
	public void update()
	{
		while(Mouse.next())
		{
			int type = Mouse.getEventType();
			if(type == Mouse.RELEASED)
			{
				int x = Mouse.getEventX();
				int y = Mouse.getEventY();
				int button = Mouse.getEventButton();
				if(button == MouseEvent.BUTTON1)
				{
				}
			}
		}
		if(JameBoy.emulator.hasRomLoaded())
		    JameBoy.emulator.doCycle();
	}
	
	public void render(Graphics g, float interpolation)
	{
	    if(JameBoy.emulator.hasRomLoaded())
	    {
	        JameBoy.emulator.getGPU().render(JameBoy.emulator, screen, g, interpolation);
	        g.drawImage(screen.image, JameBoy.mainFrame.getInsets().left, JameBoy.mainFrame.getInsets().top, JameBoy.mainFrame.getWidth(), JameBoy.mainFrame.getHeight(), null);
	    }
	    else
	    {
	        g.setColor(Color.BLACK);
	        g.fillRect(0, 0, JameBoy.mainFrame.getWidth(), JameBoy.mainFrame.getHeight());
	        g.setColor(Color.RED);
	        g.setFont(new Font(null,1,64));
	        g.drawString("No ROM loaded!", 10, 100);
	    }
	}
}