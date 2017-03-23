package com.iamchiwon;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class RxAntSerial {

	public static Observable<String> ant() {

		BehaviorSubject<String> innerSubject = BehaviorSubject.createDefault("1");

		Observable<String> antObservable = Observable.create(emitter -> {

			// 처음 시작은 "1"을 전
			emitter.onNext("1");

			// BehaviorSubject 의 디폴트 값에 의해 처음 "1"을 받는다.
			// "1"을 group 과 reduce 과정으로 다음 개미수열을 생성하여
			// emiiter를 통해 전달한다.
			innerSubject.subscribe(arr -> {

				group(arr).reduce((pre, post) -> {
					return pre + post;
				}).subscribe(serial -> {
					if (!emitter.isDisposed()) {
						emitter.onNext(serial); // 전달

						// 결과의 숫자열을 다음번 연산을 위한 값으로 사용한다.
						innerSubject.onNext(serial);
					}
				});

			});
		});

		return antObservable;
	}

	/**
	 * 수열에서 반복되는 숫자와 개수를 쌍으로 하여 값을 발생시키는 Observable을 만든다.<br/>
	 * 예를들어<br/>
	 * "111223" 이 파라미터로 전달되면 1이 3번 반복했으므로 "13", 다음으로 "22", "31"이 Observable을 통해
	 * 전달된다.
	 * 
	 * @param numbers
	 *            숫자
	 * @return 숫자와 개수로 이루어진 문자열을 전달하는 Observable<String>
	 */
	private static Observable<String> group(String numbers) {
		return Observable.create(emitter -> {
			char last = 0;
			int count = 0;

			char[] chars = numbers.toCharArray();
			for (char ch : chars) {
				if (last != ch) {
					if (count > 0) {
						String g = "" + last + count;
						emitter.onNext(g); // 전달
					}
					last = ch;
					count = 1;
				} else {
					count += 1;
				}
			}

			if (count > 0) {
				String g = "" + last + count;
				emitter.onNext(g); // 전달
			}

			emitter.onComplete(); // 끝
		});
	}
}
