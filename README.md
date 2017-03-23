# Rx로 만드는 개미수열

- RxJava 2.0.7
- Eclipse Gradle Project

[개미 수열을 푸는 10가지 방법](https://medium.com/@jooyunghan/%EA%B0%9C%EB%AF%B8-%EC%88%98%EC%97%B4-%EC%B1%85%EC%9D%84-%EC%93%B0%EB%8B%A4-31f42dbe4d31#.68qpvj9o6) 을 읽다가 Rx로 해보자는 생각으로 만들어 봤음

## 1. 개미수열

  1
  11
  12
  1121
  122111
  ...
  수열은 이전 수열에 의해 생성되는데, 이전 수열의 구성과 반복되는 개수를 조합하여 나타낸다. <br/>
  (예) 1121 다음 수열은 1이 2번 반복되고 2가 1번, 1이 한번 나오므로 122111 이 된다.


## 2. 수열생성

1121 에서 다음 수열을 만들어 내는 방법

#### 그룹화
같은 숫자가 반복되는 경우에 따라 나눈다 : 11 , 2 , 1 <br/>
숫자와 반복 회수로 구성한다. : 12 , 21 , 11 <br/>

#### 합치기
11, 21, 11 을 하나의 수열로 합친다. : 112111

#### 재귀
결과로 얻은 수열을 다음 수열 계산을 위한 입력으로 사용한다.


## 3. RxJava 로 처리

- 수열은 String 으로 처리했음

#### 그룹화

구룹화된 구성을 전달하는 Observable 을 만든다.

```java
Observable.create(emitter -> {
    // 구룹화 처리
    ...
    
    // 각 그룹 전달하기
    emitter.onNext(grouped); // 전달
    // 그룹화 끝
    emitter.onComplete(); // 끝
});
```


#### 합치기

그룹화 되어 전달되는 값을 합치는데 reduce 를 사용했다.

```java
Observable.fromArray(new String[] {"12", "21", "11"})
          .reduce((pre, post) -> pre + post)
```


#### 재귀

- 여기서 Subject를 사용했는데, subject를 subscript 한 곳에서 다음 개미수열을 생성하고
onNext 로 전달하여 subject의 다음 입력값으로 사용한다.
- 수열의 처음은 "1"로 시작하므로 기본값 "1"을 가진 BehaviorSubject 를 사용한다.

```java
BehaviorSubject.createDefault("1").subscribe(curr -> {

    group(curr).reduce((pre, post) -> pre + post)
               .subscribe(next -> {
                    // 결과의 숫자열을 다음번 연산을 위한 값으로 사용한다.
                    innerSubject.onNext(next);
                });

});
```

## 4. 전체 소스

Main.java

```java
public class Main {

	public static void main(String[] args) {

		RxAntSerial.ant().take(10).subscribe(result -> {
			System.out.println(result);
		});
	}

}
```

Result
```
1
11
12
1121
122111
112213
12221131
1123123111
12213111213113
11221131132111311231
```

**RxAntSerial.java**
```java
public class RxAntSerial {

	public static Observable<String> ant() {

		BehaviorSubject<String> innerSubject = BehaviorSubject.createDefault("1");

		Observable<String> antObservable = Observable.create(emitter -> {

			// 처음 시작은 "1"을 전
			emitter.onNext("1");

			// BehaviorSubject 의 디폴트 값에 의해 처음 "1"을 받는다.
			// "1"을 group 과 reduce 과정으로 다음 개미수열을 생성하여
			// emiiter를 통해 전달한다.
			innerSubject.subscribe(current -> {

				group(current).reduce((pre, post) -> pre + post).subscribe(next -> {
					if (!emitter.isDisposed()) {
						emitter.onNext(next); // 전달

						// 결과의 숫자열을 다음번 연산을 위한 값으로 사용한다.
						innerSubject.onNext(next);
					}
				});

			});
		});

		return antObservable;
	}

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
```
