package org.jglrxavpok.jameboy.graphics;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

public class Sprite
{

	public int pixels[];
	public int width;
	public int height;
	private int onlyColor;
	private boolean onlyColorMode;
	
	public Sprite(int w, int h)
	{
		this(w,h,new int[w*h]);
	}
	
	public Sprite(int w, int h, int[] pixels)
	{
		if(w*h==pixels.length)
		{
			this.pixels = pixels;
			this.width = w;
			this.height = h;
		}
		else
		{
			throw new RuntimeException("Invalid pixels array, length and dimensions don't match");
		}
	}
	
	public int[] getPixels()
	{
		return pixels;
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public int getHeight()
	{
		return height;
	}
	
	public Sprite getScaledInstance(int width, int height)
	{
		Sprite scaledSprite = new Sprite(width, height);
		int scaleRatioWidth = ((this.width << 16) / width);
		int scaleRatioHeight = ((this.height << 16) / height);
		int i = 0;
		for (int y = 0; y < height; y++) 
		{
			for (int x = 0; x < width; x++) 
			{
				scaledSprite.pixels[i++] = pixels[(this.width * ((y * scaleRatioHeight) >> 16)) + ((x * scaleRatioWidth) >> 16)];
			}
		}
		return scaledSprite;
	}
	
	public static Sprite loadFromClasspath(String res)
	{
		try
		{
			BufferedImage img = ImageIO.read(Sprite.class.getResourceAsStream(res));
			return new Sprite(img.getWidth(), img.getHeight(), img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth()));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public void clear(int color)
	{
		for(int i=0;i<pixels.length;i++)
		{
			pixels[i] = onlyColorMode ? onlyColor : color;
 		}
	}

	public void fillRect(int x, int y, int w, int h, int color)
	{
		for(int xo = x;xo<x+w;xo++)
		{
			for(int yo = y;yo<y+h;yo++)
			{
				if(validateCoords(xo,yo))
					pixels[xo+yo*width]= blendPixels(pixels[xo+yo*width], onlyColorMode ? onlyColor : color);
			}
		}
	}

	public void drawRect(int x, int y, int w, int h, int color)
	{
		for(int xo = x;xo<x+w;xo++)
		{
			if(validateCoords(xo,y))
				pixels[xo+y*width]= blendPixels(pixels[xo+y*width],onlyColorMode ? onlyColor : color);
			if(validateCoords(xo,y+h-1))
				pixels[xo+(y+h-1)*width]= blendPixels(pixels[xo+(y+h-1)*width],onlyColorMode ? onlyColor : color);
		}
		
		for(int yo = y;yo<y+h;yo++)
		{
			if(validateCoords(x,yo))
				pixels[x+yo*width]= blendPixels(pixels[x+yo*width],onlyColorMode ? onlyColor : color);
			if(validateCoords(x+w-1,yo))
				pixels[x+w-1+yo*width]= blendPixels(pixels[x+w-1+yo*width],onlyColorMode ? onlyColor : color);
		}
	}
	
	public void drawSprite(int x, int y, Sprite sprite)
	{
		for (int yo = y; yo < y+sprite.getHeight(); yo++) 
		{
			for (int xo = x; xo < x+sprite.getWidth(); xo++) 
			{
				int color = sprite.getPixels()[(xo-x)+(yo-y)*sprite.getWidth()];
				if((color & (1<<24)) >= 0 && (color >> 24 ) == 0)
					continue;
				if(validateCoords(xo, yo))
					pixels[xo+yo*width]= blendPixels(pixels[xo+yo*width], onlyColorMode ? onlyColor : color);
			}
		}
	}
	
	public void drawSprite(int x, int y, int w, int h, Sprite sprite)
	{
		int scaleRatioWidth = ((sprite.getWidth() << 16) / w);
		int scaleRatioHeight = ((sprite.getHeight() << 16) / h);
		for (int yo = y; yo < y+h; yo++) 
		{
			for (int xo = x; xo < x+w; xo++) 
			{
				int color = sprite.getPixels()[(sprite.getWidth() * (((yo-y) * scaleRatioHeight) >> 16)) + (((xo-x) * scaleRatioWidth) >> 16)];
				if((color & (1<<24)) >= 0 && (color >> 24 ) == 0)
					continue;
				if(validateCoords(xo, yo))
					pixels[xo+yo*width]= blendPixels(pixels[xo+yo*width], onlyColorMode ? onlyColor : color);
			}
		}
	}
	
	public void drawLine(int startX, int startY, int endX, int endY, int color)
	{
		int dx = endX - startX;
		int dy = endY - startY;
		double angle = Math.atan2(dx,dy);
		int dist = (int) Math.ceil(Math.sqrt(dx*dx+dy*dy));
		for(int i = 0;i<=dist;i++)
		{
			int x = (int) (Math.sin(angle)*i+startX);
			int y = (int) (Math.cos(angle)*i+startY);
			if(validateCoords(x,y))
				pixels[x+y*width]= blendPixels(pixels[x+y*width],onlyColorMode ? onlyColor : color);
		}
	}
	
	public void setPixel(int x, int y, int color)
	{
		pixels[x+y*width] = blendPixels(pixels[x+y*width], color);
	}
	
	public int blendPixels(int backgroundColor, int pixelToBlendColor)
	{
		int alpha_blend = (pixelToBlendColor >> 24) & 0xff;

		int alpha_background = 256 - alpha_blend;

		int rr = backgroundColor & 0xff0000;
		int gg = backgroundColor & 0xff00;
		int bb = backgroundColor & 0xff;

		int r = (pixelToBlendColor & 0xff0000);
		int g = (pixelToBlendColor & 0xff00);
		int b = (pixelToBlendColor & 0xff);

		r = ((r * alpha_blend + rr * alpha_background) >> 8) & 0xff0000;
		g = ((g * alpha_blend + gg * alpha_background) >> 8) & 0xff00;
		b = ((b * alpha_blend + bb * alpha_background) >> 8) & 0xff;

		return 0xff000000 | r | g | b;
	}

	private boolean validateCoords(int xo, int yo)
	{
		return xo >= 0 && yo >= 0 && yo < height && xo < width;
	}

	public void onlyColor(int color)
	{
		onlyColor = color;
		onlyColorMode = true;
	}
	
	public void allColors()
	{
		onlyColorMode = false;
	}

	public Sprite darker(double factor, double alphaFactor)
	{
		Sprite copy = new Sprite(width,height);
		for(int i = 0;i<pixels.length;i++)
		{
			int color = ((Math.max((int)((pixels[i] >> 16 & 0xFF) * factor), 0)<<16) | (Math.max((int)((pixels[i] >> 8 & 0xFF) * factor), 0)<<8) | (Math.max((int)((pixels[i] >> 0 & 0xFF) * factor), 0))<<0) | (Math.max((int)((pixels[i] >> 24 & 0xFF) * alphaFactor), 0)<<24);
			copy.pixels[i] = color;
		}
		return copy;
	}

}
