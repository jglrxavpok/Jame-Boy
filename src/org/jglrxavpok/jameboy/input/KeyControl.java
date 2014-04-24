package org.jglrxavpok.jameboy.input;

public class KeyControl extends AbstractControl
{

	private int key;
	public boolean pressed;

	public KeyControl(int keycode)
	{
		this.key = keycode;
	}
	
	public void tick()
	{
		this.pressed = Keyboard.isKeyDown(key);
	}
}
