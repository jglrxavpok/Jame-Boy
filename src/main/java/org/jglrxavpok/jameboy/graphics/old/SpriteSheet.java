package org.jglrxavpok.jameboy.graphics.old;

public class SpriteSheet {

    private int width;
    private int height;
    private int spriteWidth;
    private int spriteHeight;
    private int[] pixels;
    private Sprite[] sprites;
    private Sprite sheet;

    public SpriteSheet(Sprite sheet, int spritesOnX, int spritesOnY) {
        this.width = sheet.getWidth();
        this.height = sheet.getHeight();
        this.pixels = sheet.getPixels();
        split(sheet, spritesOnX, spritesOnY);
    }

    public SpriteSheet(int w, int h, int[] pixels, int spritesOnX, int spritesOnY) {
        this.width = w;
        this.height = h;
        this.pixels = pixels;
        Sprite sheet = new Sprite(w, h, pixels);
        split(sheet, spritesOnX, spritesOnY);
    }

    private void split(Sprite sheet, int sx, int sy) {
        this.sheet = sheet;
        sprites = new Sprite[sx * sy];
        spriteWidth = sheet.getWidth() / sx;
        spriteHeight = sheet.getHeight() / sy;
        int spriteIndex = 0;
        for (int iy = 0; iy < sy; iy++) {
            for (int ix = 0; ix < sx; ix++) {
                int[] pixels = new int[spriteWidth * spriteHeight];
                for (int xo = ix * spriteWidth; xo < ix * spriteWidth + spriteWidth; xo++) {
                    for (int yo = iy * spriteHeight; yo < iy * spriteHeight + spriteHeight; yo++) {
                        int x = xo - ix * spriteWidth;
                        int y = yo - iy * spriteHeight;
                        pixels[x + y * spriteWidth] = sheet.getPixels()[xo + yo * width];
                    }
                }
                sprites[spriteIndex++] = new Sprite(spriteWidth, spriteHeight, pixels);
            }
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getSpriteWidth() {
        return spriteWidth;
    }

    public int getSpriteHeight() {
        return spriteHeight;
    }

    public int[] getPixels() {
        return pixels;
    }

    public Sprite[] getSprites() {
        return sprites;
    }

    public Sprite getSpriteAt(int xi, int yi) {
        return sprites[xi + yi * spriteWidth];
    }

    public Sprite getSheet() {
        return sheet;
    }
}
