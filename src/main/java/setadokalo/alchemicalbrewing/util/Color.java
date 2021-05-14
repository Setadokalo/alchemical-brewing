package setadokalo.alchemicalbrewing.util;

/* Homebrew Color class, because FUCK the JDK's color implementation; color-packing is NOT the behavior I want out of my Color class 
   And I want my color class to actually, you know, be useful. Like, at all.
*/

import org.jetbrains.annotations.Contract;

public final class Color {
	public static final Color WHITE = new Color(1.000, 1.000, 1.000);
	public static final Color BLACK = new Color(0.000, 0.000, 0.000);
	public static final Color RED   = new Color(1.000, 0.000, 0.000);
	public static final Color GREEN = new Color(0.000, 1.000, 0.000);
	public static final Color BLUE  = new Color(0.000, 0.000, 1.000);
	public static final Color WATER = new Color(0.169, 0.282, 0.600);


	protected final double r, g, b;
	
	public Color(double red, double green, double blue) {
		r = red;
		g = green;
		b = blue;
	}
	public Color(int red, int green, int blue) {
		r = ((float)red) / 255.0;
		g = ((float)green) / 255.0;
		b = ((float)blue) / 255.0;
	}
	public Color(int color) {
		int red   = (color & 0xFF0000) >> 16;
		int green = (color & 0x00FF00) >> 8;
		int blue  =  color & 0x0000FF;
		r = ((float)red) / 255.0;
		g = ((float)green) / 255.0;
		b = ((float)blue) / 255.0;
	}

	public Color mix(Color color, double factor) {
		double iFactor = 1.0 - factor;
		double tR = r * iFactor + color.r * factor;
		double tG = g * iFactor + color.g * factor;
		double tB = b * iFactor + color.b * factor;

		return new Color(tR, tG, tB);
	} 
	
	public Color mul(Color color) {
		double tR = r * color.r;
		double tG = g * color.g;
		double tB = b * color.b;

		return new Color(tR, tG, tB);
	}
	public Color mul(double factor) {
		double tR = r * factor;
		double tG = g * factor;
		double tB = b * factor;

		return new Color(tR, tG, tB);
	}

	public Color add(Color color) {
		double tR = r + color.r;
		double tG = g + color.g;
		double tB = b + color.b;

		return new Color(tR, tG, tB);
	}
	public Color add(double factor) {
		double tR = r + factor;
		double tG = g + factor;
		double tB = b + factor;

		return new Color(tR, tG, tB);
	}

	public int asInt() {
		int red = (int)Math.round(r * 255);
		int green = (int)Math.round(g * 255);
		int blue = (int)Math.round(b * 255);
		return (red << 16) + (green << 8) + (blue);
	}

	public int getRed() {
		return (int)Math.round(r * 255);
	}
	public int getGreen() {
		return (int)Math.round(g * 255);
	}
	public int getBlue() {
		return (int)Math.round(b * 255);
	}



	@Contract(pure = true)
	public static int sinU8(double input) {
		var val = (int) (Math.sin(input) * 128);
		return val + 128;
	}
}