package setadokalo.alchemicalbrewing.util;

import org.apache.commons.math3.fraction.Fraction;

public final class FractionUtil {
	private FractionUtil() {}

	public static String toProperString(Fraction f) {
		int num = f.getNumerator();
		int den = f.getDenominator();
		String str = Integer.toString(num / den);
		str = str + " " + (num % den);
		str = str + "/" + den;
		
		return str;
	}
	public static Fraction fromString(String str) {
		String[] split = str.split("/");
		if (split.length == 1) {
			 try {
				  int num = Integer.parseInt(split[0].trim());
				  return new Fraction(num);
			 } catch (NumberFormatException e) {
				  return null;
			 }
		} else if (split.length == 2) {
			 String[] num = split[0].trim().split(" ");
			 if (num.length == 1) {
				  try {
						int iNum = Integer.parseInt(num[0]);
						int den = Integer.parseInt(split[1].trim());
						return new Fraction(iNum, den);
				  } catch (NumberFormatException e) {
						return null;
				  }
			 } else {
				  try {
						int iNum = Integer.parseInt(num[1]);
						int den = Integer.parseInt(split[1].trim());
						iNum = iNum + (Integer.parseInt(num[0]) * den);
						return new Fraction(iNum, den);
				  } catch (NumberFormatException e) {
						return null;
				  }

			 }
		}
		return null;
	}
}
