package setadokalo.alchemicalbrewing.util;

/* Homebrew Color class, because FUCK the JDK's color implementation; color-packing is NOT the behavior I want out of my Color class 
   And I want my color class to actually, you know, be useful. Like, at all.
*/

public class Color {
	public static final Color WHITE = new Color(1.0, 1.0, 1.0);
	public static final Color BLACK = new Color(0.0, 0.0, 0.0);
	public static final Color RED   = new Color(1.0, 0.0, 0.0);
	public static final Color GREEN = new Color(0.0, 1.0, 0.0);
	public static final Color BLUE  = new Color(0.0, 0.0, 1.0);


	protected final double r, g, b;
	
	public Color(double red, double green, double blue) {
		r = red;
		g = green;
		b = blue;
	}

	public Color mix(Color color, double factor) {
		double iFactor = 1.0 / factor;
		double tR = r * iFactor + color.r * factor;
		double tG = g * iFactor + color.g * factor;
		double tB = b * iFactor + color.b * factor;

		return new Color(tR, tG, tB);
	} 

}
