package setadokalo.alchemicalbrewing.util;

import java.math.BigInteger;

import org.apache.commons.math3.fraction.BigFraction;
import org.jetbrains.annotations.Nullable;

public final class BigFractionUtil {
	private BigFractionUtil() {}

	public static String toProperString(BigFraction f) {
		BigInteger num = f.getNumerator();
		if (num.equals(BigInteger.ZERO))
			return "0";
		BigInteger den = f.getDenominator();
		String str = "";
		BigInteger whole = num.divide(den);
		if (!whole.equals(BigInteger.ZERO))
			str = whole.toString() + " ";
		BigInteger properNum = num.remainder(den);
		if (!properNum.equals(BigInteger.ZERO))
			str += properNum + "/" + den;
		
		return str;
	}
	
	@Nullable
	public static BigFraction fromString(String str) {
		String[] split = str.split("/");
		if (split.length == 1) {
			 try {
				  BigInteger num = new BigInteger(split[0].trim());
				  return new BigFraction(num);
			 } catch (NumberFormatException e) {
				  return null;
			 }
		} else if (split.length == 2) {
			 String[] num = split[0].trim().split(" ");
			 if (num.length == 1) {
				  try {
						BigInteger iNum = new BigInteger(num[0]);
						BigInteger den = new BigInteger(split[1].trim());
						return new BigFraction(iNum, den);
				  } catch (NumberFormatException e) {
						return null;
				  }
			 } else {
				  try {
						int iNum = Integer.parseInt(num[1]);
						int den = Integer.parseInt(split[1].trim());
						iNum = iNum + (Integer.parseInt(num[0]) * den);
						return new BigFraction(iNum, den);
				  } catch (NumberFormatException e) {
						return null;
				  }

			 }
		}
		return null;
	}
}
