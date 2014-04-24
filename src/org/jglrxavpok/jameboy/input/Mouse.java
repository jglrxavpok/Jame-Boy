package org.jglrxavpok.jameboy.input;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.HashMap;
import java.util.Stack;

import org.jglrxavpok.jameboy.JameBoy;

public class Mouse implements MouseListener, MouseMotionListener, MouseWheelListener
{

	private static int x;
	private static int y;
	private static Mouse instance;
	public static final int MOVE = 0;
	public static final int PRESSED = 1;
	public static final int RELEASED = 2;
	public static final int WHEEL = 3;
	private static final Stack<HashMap<String, Integer>> stackEvents = new Stack<HashMap<String, Integer>>();
	
	public static int getX()
	{
		int x1 = x;
		return Math.round((float)x1/JameBoy.scale);
	}
	
	public static int getY()
	{
		int y1 = y;
		return Math.round((float)y1/JameBoy.scale);
	}
	
	private static Mouse getInstance()
	{
		if(instance == null)
			instance = new Mouse();
		return instance;
	}
	
	public static void init(Component c)
	{
		c.addMouseListener(getInstance());
		c.addMouseMotionListener(getInstance());
		c.addMouseWheelListener(getInstance());
	}

	private static HashMap<String, Integer> currentEvent;

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0)
	{
		addToQueue(arg0.getX(), arg0.getY(), -1, arg0.getWheelRotation(), arg0.getScrollAmount(), WHEEL);
	}

	@Override
	public void mouseDragged(MouseEvent arg0)
	{
		x = arg0.getX();
		y = arg0.getY();
		addToQueue(arg0.getX(), arg0.getY(), -1, 0, 0, MOVE);
	}

	@Override
	public void mouseMoved(MouseEvent arg0)
	{
		x = arg0.getX();
		y = arg0.getY();
		addToQueue(arg0.getX(), arg0.getY(), -1, 0, 0, MOVE);
	}

	@Override
	public void mouseClicked(MouseEvent arg0)
	{
		x = arg0.getX();
		y = arg0.getY();
		addToQueue(arg0.getX(), arg0.getY(), -1, 0, 0, RELEASED);
	}

	@Override
	public void mouseEntered(MouseEvent arg0)
	{
		x = arg0.getX();
		y = arg0.getY();
		addToQueue(arg0.getX(), arg0.getY(), -1, 0, 0, MOVE);
	}

	@Override
	public void mouseExited(MouseEvent arg0)
	{
		x = arg0.getX();
		y = arg0.getY();
		addToQueue(arg0.getX(), arg0.getY(), -1, 0, 0, MOVE);
	}

	@Override
	public void mousePressed(MouseEvent arg0)
	{
		x = arg0.getX();
		y = arg0.getY();
		addToQueue(arg0.getX(), arg0.getY(), arg0.getButton(), 0, 0, PRESSED);
	}

	@Override
	public void mouseReleased(MouseEvent arg0)
	{
		x = arg0.getX();
		y = arg0.getY();
		addToQueue(arg0.getX(), arg0.getY(), arg0.getButton(), 0, 0, RELEASED);
	}
	
	private static void addToQueue(int x, int y, int button, int wheelpos, int dwheel, int type)
	{
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		map.put("x",x-JameBoy.mainFrame.getInsets().left);
		map.put("y",y-JameBoy.mainFrame.getInsets().top);
		map.put("button",button);
		map.put("wheelpos",wheelpos);
		map.put("dwheel",dwheel);
		map.put("type",type);
		stackEvents.push(map);
	}
	
	public static int getEventX()
	{
		if(currentEvent == null)
			return -1;
		return currentEvent.get("x");
	}
	
	public static int getEventY()
	{
		if(currentEvent == null)
			return -1;
		return currentEvent.get("y");
	}
	
	public static int getEventButton()
	{
		if(currentEvent == null)
			return -1;
		return currentEvent.get("button");
	}
	
	public static int getEventType()
	{
		if(currentEvent == null)
			return -1;
		return currentEvent.get("type");
	}
	
	public static int getEventWheel()
	{
		if(currentEvent == null)
			return 0;
		return currentEvent.get("wheelpos");
	}
	
	public static int getEventDWheel()
	{
		if(currentEvent == null)
			return 0;
		return currentEvent.get("dwheel");
	}
	
	public static boolean next()
	{
		if(stackEvents.isEmpty())
		{
			currentEvent = null;
			return false;
		}
		else
		{
			currentEvent = stackEvents.pop();
			return true;
		}
	}
}
