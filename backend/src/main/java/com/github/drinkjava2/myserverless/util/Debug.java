package com.github.drinkjava2.myserverless.util;

/**
 * Debug Util
 * 
 * @author Yong Zhu
 * @since 2.0.5
 */
@SuppressWarnings("all")
public class Debug {
	private static final boolean ALLOW_PRINT = true;

	public static void print(Object obj) {
		if (ALLOW_PRINT)
			System.out.println(obj);
	}

	public static void println(Object obj) {
		if (ALLOW_PRINT)
			System.out.println(obj);
	}

	public static void println() {
		if (ALLOW_PRINT)
			System.out.println();
	}
}
