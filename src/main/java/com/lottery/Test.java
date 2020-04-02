package com.lottery;

public class Test {
    int a = 1;
    static int b = 2;
	public static void main(String[] args) {
		Object obj = new Object();
		new Thread() {
			@Override
			public void run() {
				System.out.println(obj);
			}
		};
	}
}
