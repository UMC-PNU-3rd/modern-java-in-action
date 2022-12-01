# Chapter 15 - CompletableFuture & 리액티브 프로그래밍

1. 애플리케이션을 실행하는 하드웨어 관련

   멀티코어 프로세서가 발전하면 애플리케이션의 속도는 멀티코어 프로세서를 얼마다 잘 활용할 수 있도록 소프트웨어를 개발하는가에 달라질 수 있다.

2. 애플리케이션을 어떻게 구성하는가 관련

   마이크로서비스 아키텍처 선택이 증가하게되며 독립적으로만 동작하는 웹사이트가 아닌 다양한 소스의 콘텐츠를 가져와 합치는 메시업 형태를 띄게된다. 이를 위해 여러 웹 서비스에 접근해야 하는 동시에 서비스의 응답을 기다리는 동안 연산이 블록되거나 귀중한 CPU 클록 사이클 자원을 낭비하지 않아야 한다. 특히 스레드를 블록함으로 연산 자원을 낭비하는 일은 피해야 한다.

   이를 위해 자바 8에서는 Future인터페이스를 구현한 CompletableFuture 클래스를 제공하며, 자바 9에서는 발행 구독 프로코톨에 기반한 리액티브 프로그래밍 개념을 따르는 플로 API를 제공한다.


## 동시성을 구현하는 자바 지원의 진화

- 초기 자바 : Runnable과 Thread
- 자바 5 : 스레드 실행과 태스트 제출을 분리하기 위한 ExecutorService, Runnable과 Thread의 변형을 반환하는 Callable과 Future, 제네릭
- 자바 7 : 분할 정복 알고리즘의 포크/조인 구현을 지원하는 java.util.concurrent.RecursiveTask
- 자바 8 : 스트림과 새로 추가된 람다 지원에 기반한 병렬 프로세싱. Future를 조합하는 기능을 추가하며 동시성을 강화한 CompletableFuture
- 자바 9 : 분산 비동기 프로그래밍을 지원. 리액티브 프로그래밍을 위한 Flow 인터페이스 추가

### 스레드와 높은 수준의 추상화

직접 Thread를 사용하지 않고 스트림을 이용해 스레드 사용 패턴을 추상화할 수 있다. 스트림으로 추상화하는것은 디자인 패턴을 적용하는 것과 비슷하지만 대신 쓸모 없는 코드가 라이브러리 내부로 구현되면서 복잡성도 줄어든다는 장점이 더해진다.

### Executor와 스레드 풀

자바 5는 Executor 프레임워크와 스레드 풀을 통해 자바 프로그래머가 태스크 제출과 실행을 분리할 수 있는 기능을 제공했다.

***스레드의 문제***

자바 스레드는 직접 운영체제 스레드에 접근한다. 운영체제 스레드를 만들고 종료하려면 비싼 비용을 치러야 하며 운영체제의 스레드 숫자는 제한되어있다. 운영체제가 지원하는 스레드 수를 초과해 사용하면 자바 애플리케이션이 예상치 못한 방식으로 크래시될 수 있으므로 기존 스레드가 실행되는 상태에서 계속 새로운 스레드를 만드는 상황이 일어나지 않도록 주의해야한다.

보통 운영체제와 자바의 스레드 수가 하드웨어 스레드 개수보다 많다. 일부 운영체제 스레드가 블록되거나 자고 있는 상황에서 모든 하드웨어 스레드가 코드를 실행하도록 할당된 상황에 놓일 수 있다. 프로그램에서 사용할 최적의 자바 스레드 개수는 사용할 수 있는 하드웨어 코어의 개수에 따라 달라진다.

***스레드 풀 그리고 스레드 풀이 더 좋은 이유***

스레드 풀은 일정한 수의 워커 스레드를 가지고 있다. 스레드 풀에서 사용하지 않은 스레드로 제출된 태스크를 먼저 온 순서대로 실행한다. 이들 태스크 실행이 종료되면 스레드 풀로 반환한다. 이 방식의 장점은 하드웨어에 맞는 수의 태스크를 유지함과 동시에 수 천개의 태스크를 스레드 풀에 아무 오버헤드 없이 제출할 수 있다는 점이다.

***스레드 풀 그리고 스레드 풀이 나쁜 이유***

거의 모든 관점에서 스레드를 직접 사용하는 것보다 스레드 풀을 이용하는 것이 바람직 하지만 두 가지 사항을 주의해야 한다.

- k 스레드를 가진 스레드 풀은 오직 k만큼의 스레드를 동시에 실행할 수 있다. 이때 잠을 자거나 I/O를 기다리거나 네트워크 연결을 기다리는 태스크가 있다면 주의해야 한다. 이런 상황에서 스레드는 블록되며, 블록 상황에서 태스크가 워커 스레드에 할당된 상태를 유지하지만 아무 작업도 하지 않게 된다. 핵심은 **블록할 수 있는 태스크는 스레드 풀에 제출하지 말아야 한다는 것이지만 항상 이를 지킬 수 있는 것은 아니다**
- 프로그램을 종료하기 전에 모든 스레드 풀을 종료하자. 자바는 이런 상황을 위해 Thread.setDaemon 메서드를 제공한다.

### 스레드의 다른 추상화

엄격한 포크/조인 방식이 아닌 비동기 메서드로 여유로운 포크/조인을 사용할 수 있다.

- 엄격한 포크/조인 : 스레드 생성과 join()이 한 쌍처럼 중첩된 메서드 호출 방식
- 여유로운 포크/조인 : 시작된 태스크를 내부 호출이 아니라 외부 호출에서 종료하도록 기다리는 방식
    - 스레드 실행은 메서드를 호출한 다음의 코드와 동시에 실행되므로 데이터 경쟁 문제를 일으키지 않도록 주의해야함
    - 기존 실행 중이던 스레드가 종료되지 않은 상황에서 자바의 main() 메서드가 반환할 때 스레드의 행동

## 동기 API와 비동기 API

### 비동기 적용 방식

Future 형식 API : 자바 5에서 소개된 Future를 이용한다. 일회성값을 처리하는데 적합하다.

리액티브 형식 API : 콜백 형식으로 일련의 값을 처리하는데 적합하다.

### 잠자기(그리고 기타 블로킹 동작)는 해로운 것으로 간주

스레드는 잠들어도 여전히 시스템 자원을 점유한다. 스레드 풀에서 잠을 자는 태스크는 다른 태스크가 시작되지 못하게 막으므로 자원을 소비한다. 블록 동작도 이와 마찬가지다. 이런 상황을 방지하는 방법은 이상적으로 절대 태스크에서 기다리는 일을 만들지 않는것과 코드에서 예외를 일으키는 방식이 존재한다.

### 비동기 API의 예외 처리 방법

Future나 리액티브 형식의 비동기 API에서 호출된 메서드의 실제 바디는 별도의 스레드에서 호출되며 이때 발생하는 어떤 에러는 이미 호출자의 실행 범위와는 관계가 없는 상황이 된다.

자바 9 플로 API에서는 Subscriber클래스를 이용하며, 그렇지 않는 경우 예외가 발생했을 때 실행될 추가 콜백을 만들어 인터페이스를 구현해야 한다.

## 박스와 채널 모델

박스와 채널 모델(box-and-channel model)은 동시성 모델을 설계하고 개념화하기 위한 모델을 말한다.

박스와 채널 모델을 이용하면 생각과 코드를 구조화할 수 있다. 손으로 코딩한 결과보다 박스로 원하는 연산을 표현하면 더 효율적으로 시스템 구현의 추상화 수준을 높일 수 있다. 또한 박스와 채널 모델은 병렬성을 직접 프로그래밍하는 관점을 콤비네이터를 이용해 내부적으로 작업을 처리하는 관점으로 바꿔준다.

## CompletableFuture와 콤비네이터를 이용한 동시성

Future는 실행해서 `get()`으로 결과를 얻을 수 있는 Callable로 만들어진다. 하지만 CompletableFuture는 실행할 코드 없이 Future를 만들 수 있도록 허용하며 `complete()` 메서드를 이용해 나중에 어떤 값을 이용해 다른 스레드가 이를 완료할 수 있고 `get()`으로 값을 얻을 수 있도록 허용한다.

콤비네이터를 이용한다면 get()에서 블록하지 않을 수 있고 그렇게 함으로 병렬 실행의 효율성은 높이고 데드락은 피할 수 있다.

## 발행-구독 그리고 리액티브 프로그래밍

리액티브 프로그래밍은 Future같은 객체를 통해 한 번의 결과가 아니라 여러번의 결과를 제공하는 모델이다. 또한 가장 최근의 결과에 대하여 반응(react)하는 부분이 존재한다.

자바 9에서는 java.util.concurrent.Flow의 인터페이스에 발행-구독 모델을 적용해 리액티브 프로그래밍을 제공한다.

자바 9 플로 API는 다음과 같이 세 가지로 정리할 수 있다.

- **구독자(Subscriber)**가 구독할 수 있는 **발행자(Publisher)**
- 연결을 **구독(subscription)**이라한다.
- 이 연결을 이용해 **메시지**(또는 **이벤트**)를 전송한다.

### 발행-구독 모델에서의 컨테이너

- 여러 컴포넌트가 한 구독자로 구독할 수 있다.
- 한 컴포넌트는 여러 개별 스트림을 발행할 수 있다.
- 컴포넌트는 여러 구독자에 가입할 수 있다.

### Publisher

Publisher는 발행자이며 subscribe를 통해 구독자를 등록한다.

```java
public interface Publisher<T> {
    public void subscribe(Subscriber<? super T> s);
}
```

### Subscriber

Subscriber는 구독자이며, `onNext()`라는 정보를 전달할 단순 메서드를 포함하며, 구현자가 필요한대로 이 메서드를 구현할 수 있다.

```java
public interface Subscriber<T> {
		public void onNext(T t);
    public void onError(Throwable t);
    public void onComplete();
    public void onSubscribe(Subscription s);
}
```

### 업스트림과 다운스트림

데이터가 발행자(생산자)에서 구독자(소비자)로 흐름에 착안하여 이를 업스트림(upstream) 또는 다운스트림(downstream)이라 부른다.

## 역압력

발행자가 매운 빠른 속도로 이벤트를 발행한다면 구독자가 아무 문제없이 처리할 수 있을까? 에서 역압력의 개념이 출발된다. 발행자가 구독자의 onNext를 호출하여 이벤트를 전달하는 것을 **압력**이라 부른다. 이런 상황에서는 수신할 이벤트의 숫자를 제한하는 역압력기법이 필요한다. 자바 9 플로 API에서는 발행자가 무한의 속도고 아이템을 방출하는 대신 요청했을 때만 다음 아이템을 보내도록 하는 `request()` 메서드를 제공한다.

Publisher와 Subscriber 사이에 채널이 연결되면 첫 이벤트로 `Subscriber.onSubscribe(Subscription subscription)`메서드가 호출된다. Subscription 객체는 다음처럼 Subscriber와 Publisher와 통신할 수 있는 메서드를 포함한다.

```java
public interface Subscription {
	  void request(long n);
    public void cancel();
}
```

Publisher는 Subscription 객체를 만들어 Subscriber로 전달하면 Subscriber는 이를 이용해 Publisher로 정보를 보낼 수 있다.

### 실제 역압력의 간단한 형태

한 번에 한 개의 이벤트를 처리하도록 발행-구독 연결을 구성하기 위해 다음과 같은 작업이 필요하다

- Subscriber가 OnSubscribe로 전달된 Subscription 객체를 필드로 저장.
- Subscriber가 수많은 이벤트를 받지 않도록 onSubscribe, onNext, onError의 마지막 동작에 channel.request(1)을 추가해 오직 한 이벤트만 요청한다.
- 요청을 보낸 채널에만 onNext, onError 이벤트를 보내도록 Publisher의 notifyAllSubscribers 코드를 바꾼다.
- 보통 여러 Subscriber가 자신만의 속도를 유지할 수 있도록 Publisher는 새 Subscription을 만들어 각 Subscriber와 연결한다.

역압력을 구현하려만 여러가지 장단점을 생각해야 한다.

- 여러 Subscriber가 있을 때 이벤트를 가장 느린 속도로 보낼 것인가? 아니면 각 Subscriber에게 보내지 않은 데이터를 저장할 별도의 큐를 가질 것인가?
- 큐가 너무 커지면 어떻게 해야할까?
- Subscriber가 준비가 안 되었다면 큐의 데이터를 폐기할 것인가?

이런 결정은 데이터의 성격에 따라 달라진다.

## 리액티브 시스템 vs 리액티브 프로그래밍

### 리액티브 시스템

- 런타입 환경이 변화에 대응하도록 전체 아키텍처가 설계된 프로그램.
- 반응성(responsive), 회복성(resilient), 탄력성(elastic)으로 세 가지 속성을 가진다.
- **반응성**은 리액티브 시스템이 큰 작업을 처리하느라 간단한 질의의 응답을 지연하지 않고 실시간으로 입력에 반응하는 것이다.
- **회복성**은 한 컴포넌트의 실패로 전체 시스템이 실패하지 않음을 의미한다.
- **탄력성**은 시스템이 잣긴의 작업 부하에 맞게 적응하며 작업을 효율적으로 처리함을 의미

### 리액티브 프로그래밍

- 리액티브 시스템이 가지는 속성을 구현하기 위한 프로그래밍 형식을 의미한다.
- java.util.concurrent.Flow 관련된 자바 인터페이스에서 제공하는 리액티브 프로그래밍 형식.
- 이들 인터페이스 설계는 메시지 주도(message-driven) 속성을 반영한다.

정리하자면 리액티브 시스템은 전체적인 리액티브 환경 아키텍처를 의미하며 리액티브 프로그래밍은 리액티브 환경을 위해 사용하는 프로그래밍 기법이다. 리액티브 프로그래밍을 이용해 리액티브 시스템을 구현할 수 있다.

---

# Chapter 16 - CompletableFuture : 안정적 비동기 프로그래밍

## Future의 단순 활용

자바 5부터는 미래의 어느 시점에 결과를 얻는 모델에 활용할 수 있도록 Future 인터페이스를 제공하고 있다. 다른 작업을 처리하다가 시간이 오래 걸리는 작업의 결과가 필요한 시점이 되었을 때 Future의 get 메서드로 결과를 가져올 수 있다. get 메서드를 호출했을 때 이미 계산이 완료되어 결과가 준비되었다면 즉시 결과를 반환하지만 결과가 준비되지 않았다면 작업이 완료될 때까지 스레드를 블록 시킨다.

Future 인터페이스로는 간결한 동시 실행 코드를 구현하기에 충분하지 않다. 또한 복잡한 의존을 갖는 동작을 구현하는 것은 쉽지 않다. 자바 8에서는 새로 제공하는 CompletableFuture 클래스를 통해 Stream과 비슷한 패턴, 즉 람다 표현식과 파이프라이닝을 활용하여 간편하게 비동기 동작을 구현할 수 있도록 한다.

### 동기 API와 비동기 API

- **동기 API** : 메서드를 호출한 다음에 메서드가 계산을 완료할 때까지 기다렸다가 메서드가 반환되면 호출자는 반환된 값으로 계속 다른 동작을 수행. 블록 호출(blocking call)이라 한다.
- **비동기 API** : 메서드가 즉시 반환되며 끝내지 못한 나머지 작업을 호출자 스레드와 동기적으로 실행될 수 있도록 다른 스레드에 할당한다. 비블록 호출(non-blocking call)이라 한다

## 비동기 API 구현

동기 메서드를 CompletableFuture를 통해 비동기 메서드로 변환할 수 있다. 비동기 계산과 완료 결과를 포함하는 CompletableFuture 인스턴스를 만들고 완료 결과를 complete 메서드로 전달하여 CompletableFuture를 종료할 수 있다.

```java
public Future<Integer> getPriceAsync(String product) {
	CompletableFuture<Integer> futurePrice = new CompletableFuture<>();
	new Thread(() -> {
			int price = calculatePrice(product); // 다른 스레드에서 비동기적으로 계산을 수행
			futurePrice.complete(price); // 오랜 시간이 걸리는 계산이 완료되면 Future에 값을 설정
	}).start();
	return futurePrice; // 기다리지 않고 Future 반환
}
```

### 에러 처리 방법

비동기 작업을 하는 중 에러가 발생하면 해당 스레드에만 영향을 미친다. 에러가 발생해도 메인 스레드의 작업 흐름은 계속 진행되며 순서가 중요한 일이 있을 경우 일이 꼬여버린다.

클라이언트는 타임아웃값을 받는 get메서드와 try/catch문을 통해 이 문제를 해결할 수 있다. 그래야 문제가 발생했을 때 클라이언트가 영원히 블록되지 않을 수 있고 타임아웃 시간이 지나면 **TimeoutException**을 받을 수 있다.

```java
// Future.get시 반환할 value를 전달한다.
public boolean complete(T value)
// Future.get시 반환할 value와 Timeout 시간을 설정한다.
public T get(long timeout, TimeUnit unit)
```

catch한 TimeoutException에 대하여 catch 블록 내 `completExecptionally` 메서드를 이용해 CompletableFuture 내부에서 발생한 예외를 클라이언트로 전달할 수 있다.

```java
// 원래 코드에서 try-catch문으로 잡음
public boolean completeExceptionally(Throwable ex)
```

### 팩토리 메서드 supplyAsync로 CompletableFuture 만들기

supplyAsync 메서드를 Supplier를 인수로 받아서 CompletableFuture를 반환한다. CompletableFuture는 Supplier를 실행해서 비동기적으로 결과를 생성한다. ForkJoinPool의 Executor 중 하나가 Supplier를 실행하며, 오버로드된 메서드를 이용하면 다른 Executor를 지정할 수 있다.

```java
// ForkJoinPool의 Executor 중 하나가 Supplier를 실행
public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier) {
    return asyncSupplyStage(asyncPool, supplier);
}

// 사용될 Executor 지정 가능
public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier, Executor executor) {
    return asyncSupplyStage(screenExecutor(executor), supplier);
}
```

## 비블록 코드

parallel 메서드를 통한 병렬 스트림 API이나 CompletableFuture를 사용하면 비블록 코드를 만들 수 있다. 둘 다 내부적으로 `Runtime.getRuntime().availableProcess()` (Java Virtual Machine에서 사용 가능한 프로세스 수를 판별 할 수 있다.)가 반환하는 스레드 수를 사용하면 비슷한 성능을 낸다. 결과적으로는 비슷하지만 CompletableFuture는 병렬 스트림 버전에 비해 작업에 이용할 수 있는 다양한 Executor를 지정할 수 있다는 장점이 있다. 따라서 Executor로 스레드 풀의 크기를 조절하는 등 애플리케이션에 맞는 최적화된 설정을 만들 수 있다.

<aside>
💡 Issue 한번 생각해보기

</aside>

### 커스텀 Executor 사용하기

- **스레드 풀 크기 조절**

*자바 병렬 프로그래밍(Java Concurrency in Practice)*에서 스레드 풀의 최적값을 찾는 방법을 제안한다. 스레드 풀이 너무 크면 CPI와 메모리 자원을 서로 경쟁하느라 시간을 낭비할 수 있다. 반면 스레드 풀이 너무 작으면 CPU의 일부 코어는 활용되지 않을 수 있다.

[https://camo.githubusercontent.com/586c8d6fefa35f03a45fc525fe72058341392fb567fbcbc45e60091cd723b414/68747470733a2f2f696d67312e6461756d63646e2e6e65742f7468756d622f523132383078302f3f73636f64653d6d746973746f72793226666e616d653d6874747073253341253246253246626c6f672e6b616b616f63646e2e6e6574253246646e2532466373336c545525324662747152416a4249726f452532464b64544f73487165554c594f7547326f314c456d484b253246696d672e706e67](https://camo.githubusercontent.com/586c8d6fefa35f03a45fc525fe72058341392fb567fbcbc45e60091cd723b414/68747470733a2f2f696d67312e6461756d63646e2e6e65742f7468756d622f523132383078302f3f73636f64653d6d746973746f72793226666e616d653d6874747073253341253246253246626c6f672e6b616b616f63646e2e6e6574253246646e2532466373336c545525324662747152416a4249726f452532464b64544f73487165554c594f7547326f314c456d484b253246696d672e706e67)

애플리케이션의 특성에 맞는 Executor를 만들어 CompletableFuture를 활용하는것이 바람직하다.

### 스트림 병렬화와 CompletableFuture 병렬화

- I/O가 포함되지 않은 계산 중심의 동작을 실행할 때는 스트림 인터페이스가 가장 구현하기 간단하며 효율적일 수 있다.
- 작업이 I/O를 기다리는 작업을 병렬로 실행할 때는 CompletableFuture가 더 많은 유연성을 제공하며 대기/계산(W/C)의 비율에 적합한 스레드 수를 설정할 수 있다.

## 비동기 작업 파이프라인 만들기

Stream API의 map 메서드와 CompletableFuture의 메서드들을 이용하여 비동기 작업 파이프라인을 만들 수 있다.

- supplyAsync : 전달받은 람다 표현식을 비동기적으로 수행한다.
- thenApply : CompletableFuture가 동작을 완전히 완료한 다음에 thenApply로 전달된 람다 표현식을 적용한다.
- thenCompose : 첫 번째 연산의 결과를 두 번째 연산으로 전달한다.
- thenCombine : 독립적으로 실행된 두 개의 CompletableFuture 결과를 이용하여 연산한다. 두 결과를 어떻게 합칠지 정의된 BiFunction을 두 번째 인수로 받는다.
- thenCombineAsync : 두 개의 CompletableFuture 결과를 반환하는 새로운 Future를 반환한다.

> FROM Java 9
>
- orTimeout : 지정된 시간이 지난 후 CompletableFuture를 TimeoutException으로 완료하게한다.
- completeOnTimeout : 지정된 시간이 지난 후 지정한 기본 값을 이용해 연산을 이어가게한다.

### 비동기 작업 파이프라인 예시

```java
// 예제 16-16 CompletableFuture로 비동기 작업 조합하기
public List<String> findPrices(String product) {
	List<CompletableFuture<String>> priceFutures = 
		shops.stream()
			.map(shop -> CompletableFuture.supplyAsync(() -> shop.getPrice(product), executor))
			.map(future -> future.thenApply(Quote::parse))
			.map(future -> future.thenCompose(quote -> 
				CompletableFuture.supplyAsync(() -> Discount.applyDiscount(quote, executor)))
			.collect(toList());

	return priceFutures.stream()
		.map(CompletableFuture::join)
		.collect(toList());
}
```

```java
Future<Double> futurePriceInUSD = 
	CompletableFuture.supplyAsync(() -> shop.getPrice(product))
	.thenCombine(
		CompletableFuture.supplyAsync(() -> exchangeService.getRange(Money.EUR, Money.USD)),
		(price, rate) -> price * rate
	))
	.orTimeout(3, TimeUnit.SECONDS);
```

## CompletableFuture의 종료에 대응하는 방법

실제 원격 서비스들은 얼마나 지연될지 예측하기 어렵다.

- thenAccept : CompletableFuture가 생성한 결과를 어떻게 소비할지 미리 지정한다.
- allOf : 전달받은 CompletableFuture 배열이 모두 완료될 때 CompletableFuture를 반환한다.
- anyOf : 전달받은 CompletableFuture 배열 중 하나라도 작업이 끝났을 때 완료한 CompletableFuture를 반환한다.

---

# Chapter 17 - 리액티브 프로그래밍

## 리액티브 프로그래밍 패러다임의 중요성이 증가하는 이유

- **빅데이터** : 보통 빅데이터는 페타바이트(Petabyte, PB, 10 bytes) 단위로 구성되며 매일 증가한다.
- **다양한 환경** : 모바일 디바이스에서 수천 개의 멀티 코어 프로세서로 실행되는 클라우드 기반 클러스터에 이르기 까지 다양한 환경에 애플리케이션이 배포된다.
- **사용 패턴** : 사용자는 1년 내내 항상 서비스를 이용할 수 있으며 밀리초 단위의 응답 시간을 기대한다.

## 리액티브 매니패스토

리액티브 애플리케이션과 시스템 개발의 핵심 원칙에 대하여 정의한다. [리액티브 선언문](https://www.reactivemanifesto.org/ko)을 직접 읽는것이 도움될 것이다.

### 리액티브 시스템 핵심 원칙

- 반응성(Responsive) : 일정하고 예상할 수 있는 반응 시간을 제공한다. to 안정적인 어플리케이션
- 회복성(Resilient) : 장애가 발생해도 시스템은 반응해야 한다
- 탄력성(Elastic) : 작업량이 변화하더라도 자동으로 관련 컴포넌트의 자원수를 조절하여 응답성을 유지한다
- 메시지 주도(Message Driven) : 비동기 메시지를 전달해 컴포넌트 끼리 느슨하게 통신한다.

### 애플리케이션 수준의 리액티브 vs 시스템 수준의 리액티브

### **애플리케이션 수준의** **리액티브 프로그래밍**

- 주로 비동기로 작업을 수행하여 최신 멀티코어 CPU 사용율을 극대화
- 개발자 입장에서는 이들 기술을 이용함으로 동시, 비동기 어플리케이션 구현의 추상 수준을 높일 수 있으므로 동기 블록, 경쟁 조건, 데드랍 같은 저 수준 멀티스레드 문제를 직접 처리할 필요가 없어지면서 비즈니스 요구사항을 구현하는데 집중할 수 있다.
- RxJava, Akka와 같은 리액티브 프레임워크는 별도로 지정된 스레드풀에서 블록 동작을 실행시켜 문제를 해결한다.
    - 문제 : 이벤트 스트림 3개 처리해야하는데 스레드풀(2개)중 하나가 IO동작으로 블록되어 있을 경우

### **시스템 수준의 리액티브**란 **리액티브 시스템**

- 리액티브 시스템을 만들려면 훌륭하게 설계된 리액티브 어플리케이션 집합이 잘 조화를 이루게 만들어야 한다.
- 리액티브 시스템이란 여러 애플리케이션이 한 개의 일관적인, 회복할 수 있는 플랫폼을 구성할 수 있게 해줄 뿐 아니라 이들 애플리케이션 중 하나가 실패해도 전체 시스템은 계속 운영될 수 있도록 도와주는 소프트웨어 아키텍쳐다.

## 리액티브 스트림과 Flow API

### 리액티브 프로그래밍이란?

리액티브 스트림을 사용하는 프로그래밍으로, 리액티브 스트림은 잠재적으로 무한의 비동기 데이터를 순서대로 그리고 블록하지 않는 역압력을 전제해 처리하는 표준 기술이다.

넷플릭스, 레드햇, 트위터, 라이트밴드 및 기타 회사들이 참여한 리액티브 스트림 프로젝트에서 모든 리액티브 스트림 구현이 제공해야 하는 최소 기능 집합을 네 개의 관련 인터페이스로 정의했다. 자바 9의 새로운 java.util.concurrent.Flow 클래스 뿐 아니라 Akka 스트림(Lightbend), 리액터(Pivotal), RxJava(Netflix), Vert.x(Redhat) 등 많은 서드 파티 라이브러리에서 이들 인터페이스를 구현한다.

[Reactive Streams](http://www.reactive-streams.org/)

## Flow

자바 9에서는 리액티브 프로그래밍을 제공하는 클래스 Flow를 추가했다. 이 클래스는 정적 컴포넌트 하나를 포함하고 있으며 인스턴스화할 수 없다. 리액티브 스트림 프로젝트 표준에 따라 프로그래밍 발행-구독 모델을 지원할 수 있도록 Flow 클래스는 중첩된 인터페이스 네 개를 포함한다.

### Publisher

함수형 인터페이스로 구독자를 등록할 수 있다.

```java
@FunctionalInterface
public interface Publisher<T> {
    void subscribe(Flow.Subscriber<? super T> s);
}
```

### Subscriber

구독자이며 프로토콜에서 정의한 순서로 지정된 메서드 호출을 통해 발행되어야 한다.

- `onSubscribe onNext* (onError | onComplete)?`

```java
public interface Subscriber<T> {
  void onSubscribe(Flow.Subscription s);
  void onNext(T t);
  void onError(Throwable t);
  void onComplete();
}
```

### Subscription

구독자와 발행자 사이 관계를 조절하기 위한 인터페이스이다. request는 처리할 수 있는 이벤트의 개수를 전달하며, cancel은 더 이상 이벤트를 받지 않음을 통지한다.

```java
public interface Subscription {
    void request(long n);
    void cancel();
}
```

### Processor

구독자이며 발행자이다. 주로 구독자로써 전달받은 이벤트를 변환하여 발행하는, 이벤트를 변환하는 역할을 수행한다.

```java
public interface Processor<T, R> extends Flow.Subscriber<T>, Flow.Publisher<R> { }
```

### Flow 클래스가 갖는 중첩 인터페이스들의 규칙

- Publisher는 반드시 Subscription의 request 메서드에 정의된 개수 이하의 요소만 Subscriber에게 전달해야 한다. 하지만 Publisher는 지정된 개수보다 적은 수의 요소를 onNext로 전달할 수 있으며 동작이 성공적으로 끝났으면 onComplete를 호출하고 문제가 발생하면 onerror를 호출해 Subscription을 종료할 수 있다.
- Subscriber는 요소를 받아 처리할 수 있음을 Publisher에게 알려야 한다. 이런 방식으로 Subscriber는 Publisher에게 역압력을 행사할 수 있고 Subscriber가 관리할 수 없이 너무 많은 요소를 받는 일을 피할 수 있다. 더욱이 onComplete나 onError 신호를 처리하는 상황에서 Subscriber는 Publisher나 Subscription의 어떤 메서드도 호출할 수 없으며 Subscription이 취소되었다고 가정해야 한다. 마지막으로 Subscriber는 Subscription.request() 메서드 호출이 없어도 언제든 종료 시그널을 받을 준비가 되어있어야 하며 Subscription.cancel()이 호출된 이후에라도 한 개 이상의 onNext를 받을 준비가 되어 있어야 한다.
- Publisher와 Subscriber는 정확하게 Suibscription을 공유해야 하며 각각이 고유한 역할을 수행해야 한다. 그러려면 onSubscribe와 onNext 메서드에서 Subscriber는 request 메서드를 동기적으로 호출할 수 있어야 한다. 표준에서는 Subscription.cancel() 메서드는 몇 번을 호출해도 한 번 호출한 것과 같은 효과를 가져야 하며, 여러 번 이 메서드를 호출해도 다른 추가 호출에 별 영향이 없도록 스레드에 안전해야 한다고 명시한다. 같은 Subscriber 객체에 다시 가입하는 것은 권장하지 않지만 이런 상황에서 예외가 발생해야 한다고 명세서가 강제하진 않는다. 이전의 취소된 가입이 영구적으로 적용되었다면 이후의 기능에 영향을 주지 않을 가능성도 있기 때문이다.

# 리액티브 라이브러리 RxJava 사용하기

RxJava는 자바로 리액티브 애플리케이션을 구현하는 데 사용하는 라이브러리다. 더 자세한 설명은 [ReactiveX](http://reactivex.io/intro.html) 공식 홈페이지를 참고하길 바란다.

### [Observable](http://reactivex.io/documentation/ko/observable.html)

Observable, Flowable 클래스는 다양한 종류의 리액티브 시스템을 편리하게 만들 수 있도록 여러 팩토리 메서드를 제공한다. just()와 interval() 팩토리 메서드를 사용하면 요소를 직접 지정해 이를 방출하도록 지정할 수 있다.

RxJava에서 Observable이 플로 API의 Publisher 역할을 하며 Observer는 Flow의 Subscriber 인터페이스 역할을 한다. RxJava의 Observer 인터페이스는 자바 9의 Subscriber와 같은 메서드를 정의하며 onSubscribe 메서드가 Subscription 대신 Disposable 인수를 갖는다는 점만 다르다. Observable은 역압력을 지원하지 않으므로 Subscription의 request 메서드를 포함하지 않는다.

### Observer

Observer 클래스는 Observable을 구독한다. 자바 9 네이티브 플로 API와 동일한 역할을 하지만 많은 오버로드된 기능을 제공하기에 더 유연하다.

```java
public interface Observer<T> {
	void onSubscribe(Disposable d);
	void onNext(T t);
	void onError(Throwable t);
	void onComplete();
}
```

네 개의 메서드를 모두 구현해야 하는 Flow.Subscriber와 달리 onNext의 시그니처에 해당하는 람다 표현식을 전달해 Observable을 구독할 수 있다.

```java
// 0에서 시작해 1초 간격으로 long형식의 값을 무한으로 증가 시키며 값을 방출하는 Observable
Observable<Long> onePerSec = Observable.interval(1 ,TimeUnit.SECONDS);

// 람다 표현식으로 onNext 메서드만 구현하여 구독
onePerSec.subscribe(i -> System.out.println(TempInfo.fetch("New York"));
```

필요한 옵저버가 있다면 Observer 인터페이스를 구현하여 쉽게 만들고 팩터리 메서드를 이용하여 만든 Observable에 쉽게 구독시킬 수 있다.

### Observable을 변환하고 합치기

자바 9 플로 API의 Flow.Processor는 한 스트림을 다른 스트림의 입력으로 사용할 수 있었다. 하지만 스트림을 합치고, 만들고, 거스는 등의 복잡한 작업을 구현하기는 매우 어려운 일이다. RxJava의 Observable 클래스는 앞서 언급한 복잡한 작업에 대하여 쉽게 처리할 수 있는 다양한 기능들을 제공한다.

이런 함수들을 documents의 설명으로만은 이해하기 상당히 어렵기에 마블 다이어그램이라는 시각적 방법을 이용해 이해를 돕는다. [rxmarbles.com](http://rxmarbles.com/) 사이트는 RxJava의 Observables의 스트림의 요소 위치를 직접 옮겨가며 결과를 확인 메서드의 동작을 마블 다이어그램으로 확인할 수 있도록 도와준다. documents와 함께 참고하자.
