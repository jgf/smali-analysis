package org.jf.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AnalysisUtil {
	
	private AnalysisUtil() {}
	
	public static void writeAllFieldsToBuffer(final Object obj, final StringBuilder out) {
		final Class<?> thisClass = obj.getClass();
		
		for (final java.lang.reflect.Field f : thisClass.getFields()) {
			try {
				out.append(f.getName() + " = ");
				final Class<?> fType = f.getType();

				if (fType.isPrimitive()) {
					final String  n = fType.getName();
					if (n.equals("boolean")) {
						out.append(f.getBoolean(obj) + "\n");
					} else if (n.equals("byte")) {
						out.append(f.getByte(obj) + "\n");
					} else if (n.equals("char")) {
						out.append(f.getChar(obj) + "\n");
					} else if (n.equals("double")) {
						out.append(f.getDouble(obj) + "\n");
					} else if (n.equals("float")) {
						out.append(f.getFloat(obj) + "\n");
					} else if (n.equals("int")) {
						out.append(f.getInt(obj) + "\n");
					} else if (n.equals("long")) {
						out.append(f.getLong(obj) + "\n");
					} else if (n.equals("short")) {
						out.append(f.getShort(obj) + "\n");
					} else {
						throw new IllegalStateException("unknown primitive type: " + fType.getName());
					}
				} else {
					final Object val = f.get(obj);
					out.append((val == null ? "null" : val.toString()) + "\n");
				}
			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			}
		}
	}
	
	public static String getAllFieldsAsString(final Object obj) {
		final StringBuilder sb = new StringBuilder();
		writeAllFieldsToBuffer(obj, sb);
		
		return sb.toString();
	}
	
	private static final Pattern extJarPattern = Pattern.compile("(?:^|\\\\|/)ext.(?:jar|odex)$");
	public static boolean isExtJar(String dexFilePath) {
	    Matcher m = extJarPattern.matcher(dexFilePath);
	    return m.find();
	}
	
}
