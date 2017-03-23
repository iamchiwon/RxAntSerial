package com.iamchiwon;

public class Main {

	public static void main(String[] args) {

		RxAntSerial.ant().take(10).subscribe(result -> {
			System.out.println(result);
		});
	}

}
