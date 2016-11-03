package org.jglrxavpok.jameboy.graphics;

public class FontRenderer {


    public static final FontRenderer ARIAL = new FontRenderer(new SpriteSheet(Sprite.loadFromClasspath("/textures/fonts/arial.png"), 8, 16),
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ!?:,;.%���*+-/(){}[]=�&\"'#~_0123456789abcdefghijklmnopqrstuvwxyz @�$��^�<>");
    private SpriteSheet fontSheet;
    private String chars;
    private int charIndex;

    public FontRenderer(SpriteSheet sheet, String characters) {
        this.fontSheet = sheet;
        this.chars = characters;
    }

    public void drawText(Screen screen, String txt, int x, int y, int color) {
        int offset = 0;
        for (int index = 0; index < txt.length(); index++) {
            char c = txt.charAt(index);
            if (chars.indexOf(c) != -1) {
                screen.onlyColor(color);
                if (++charIndex >= fontSheet.getSprites().length)
                    charIndex = 0;
                screen.drawSprite(x + offset, y, fontSheet.getSprites()[chars.indexOf(c)]);
                screen.allColors();
                offset += fontSheet.getSpriteWidth();
            }
        }
//		screen.drawSprite(x, y, fontSheet.getSheet());
    }
}
