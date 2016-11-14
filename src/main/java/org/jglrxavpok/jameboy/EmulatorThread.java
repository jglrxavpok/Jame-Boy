package org.jglrxavpok.jameboy;

import org.jglrxavpok.jameboy.debug.DebuggerFrame;
import org.jglrxavpok.jameboy.graphics.old.Screen;
import org.jglrxavpok.jameboy.input.Keyboard;
import org.jglrxavpok.jameboy.io.IOHandler;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;

public class EmulatorThread extends Thread {
    //This value would probably be stored elsewhere.
    final double GAME_HERTZ = 60;
    //Calculate how many ns each frame should take for our target game hertz.
    final double TIME_BETWEEN_UPDATES = 1000000000 / GAME_HERTZ;
    //At the very most we will update the game this many times before a new render.
    //If you're worried about visual hitches more than perfect timing, set this to 1.
    final int MAX_UPDATES_BEFORE_RENDER = 5;
    //If we are able to get as high as this FPS, don't render again.
    final double TARGET_FPS = 20;
    final double TARGET_TIME_BETWEEN_RENDERS = 1000000000 / TARGET_FPS;
    //We will need the last update time.
    double lastUpdateTime = System.nanoTime();
    //Store the last time we rendered.
    double lastRenderTime = System.nanoTime();
    //Simple way of finding FPS.
    int lastSecondTime = (int) (lastUpdateTime / 1000000000);
    boolean paused = false;
    private boolean alive = true;
    private int frame;
    private int fps;
    private Screen screen;
    private Font defaultFont;

    public EmulatorThread() {
        screen = JameBoyApp.screen;
        defaultFont = new Font(null, 1, 64);
    }

    public void run() {
        while (alive) {
            loop();
        }

        System.exit(0);
    }

    private void loop() {
        double now = System.nanoTime();
        int updateCount = 0;

        if (!paused) {
            //Do as many game updates as we need to, potentially playing catchup.
            while (now - lastUpdateTime > TIME_BETWEEN_UPDATES && updateCount < MAX_UPDATES_BEFORE_RENDER) {
                update();
                lastUpdateTime += TIME_BETWEEN_UPDATES;
                updateCount++;
            }

            //If for some reason an update takes forever, we don't want to do an insane number of catchups.
            //If you were doing some sort of game that needed to keep EXACT time, you would get rid of this.
            if (now - lastUpdateTime > TIME_BETWEEN_UPDATES) {
                lastUpdateTime = now - TIME_BETWEEN_UPDATES;
            }

            //Render. To do so, we need to calculate interpolation for a smooth render.
            float interpolation = Math.min(1.0f, (float) ((now - lastUpdateTime) / TIME_BETWEEN_UPDATES));
            BufferStrategy bs = JameBoyApp.mainFrame.getBufferStrategy();
            if (bs == null) {
                JameBoyApp.mainFrame.createBufferStrategy(2);
                return;
            }
            render(bs.getDrawGraphics(), interpolation);
            bs.show();
            lastRenderTime = now;
            //Update the frames we got.
            int thisSecond = (int) (lastUpdateTime / 1000000000);
            frame++;
            if (thisSecond > lastSecondTime) {
                fps = frame;
                if (JameBoyApp.emulator.getCore().getCurrentROM() != null)
                    JameBoyApp.mainFrame.setTitle("JameBoy - " + JameBoyApp.emulator.getCore().getCurrentROM().getHeader().getTitle() + " - " + fps + " fps");
                else
                    JameBoyApp.mainFrame.setTitle("JameBoy - " + fps + " fps");
                frame = 0;
                lastSecondTime = thisSecond;
            }

            if (!JameBoyApp.mainFrame.isVisible())
                alive = false;

            //Yield until it has been at least the target time between renders. This saves the CPU from hogging.
            while (now - lastRenderTime < TARGET_TIME_BETWEEN_RENDERS && now - lastUpdateTime < TIME_BETWEEN_UPDATES) {
                Thread.yield();

                //This stops the app from consuming all your CPU. It makes this slightly less accurate, but is worth it.
                //You can remove this line and it will still work (better), your CPU just climbs on certain OSes.
                //FYI on some OS's this can cause pretty bad stuttering. Scroll down and have a look at different peoples' solutions to this.
              /*  try {
                    Thread.sleep(1);
                } catch (Exception e) {
                }*/

                now = System.nanoTime();
            }
        }
    }

    public void update() {
       /* while (Mouse.next()) {
            int type = Mouse.getEventType();
            if (type == Mouse.RELEASED) {
                int x = Mouse.getEventX();
                int y = Mouse.getEventY();
                int button = Mouse.getEventButton();
                if (button == MouseEvent.BUTTON1) {
                }
            }
        }*/
        IOHandler handler = JameBoyApp.emulator.getCore().getIOHandler();
        handler.setLeftPressed(Keyboard.isKeyDown(KeyEvent.VK_LEFT));
        handler.setUpPressed(Keyboard.isKeyDown(KeyEvent.VK_UP));
        handler.setRightPressed(Keyboard.isKeyDown(KeyEvent.VK_RIGHT));
        handler.setDownPressed(Keyboard.isKeyDown(KeyEvent.VK_DOWN));
        handler.setBPressed(Keyboard.isKeyDown(KeyEvent.VK_A));
        handler.setAPressed(Keyboard.isKeyDown(KeyEvent.VK_S));
        handler.setStartPressed(Keyboard.isKeyDown(KeyEvent.VK_ENTER));
        handler.setSelectPressed(Keyboard.isKeyDown(KeyEvent.VK_BACK_SPACE));

        if (JameBoyApp.emulator.hasRomLoaded()) {
            if(!JameBoyApp.emulator.getCore().isPaused()) {
                int count = (int) (4194304 / GAME_HERTZ);
                JameBoyApp.emulator.doCycles(count);
            } else if(JameBoyApp.emulator.getCore().shouldStep()) {
                JameBoyApp.emulator.doCycles(1);
                JameBoyApp.emulator.getCore().stepDone();
            }
        }

        DebuggerFrame.getInstance().onUpdate();
    }

    public void render(Graphics g, float interpolation) {
        if (JameBoyApp.emulator.hasRomLoaded()) {
            g.drawImage(screen.image, JameBoyApp.mainFrame.getInsets().left, JameBoyApp.mainFrame.getInsets().top,
                    JameBoyApp.mainFrame.getContentPane().getWidth(), JameBoyApp.mainFrame.getContentPane().getHeight(), null);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, JameBoyApp.mainFrame.getWidth(), JameBoyApp.mainFrame.getHeight());
            g.setColor(Color.RED);
            g.setFont(defaultFont);
            g.drawString("No ROM loaded!", 10, 100);
        }
    }
}