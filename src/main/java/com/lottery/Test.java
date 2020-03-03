package com.lottery;

public class Test {
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
