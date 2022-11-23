# 7장 병렬 데이터 처리와 성능

**스트림**으로 **데이터 컬렉션** 관련 동작을 **얼마나 쉽게 병렬로 실행**할 수 있는지 설명한다.

스트림으로 **순차** 스트림 → **병렬** 스트림으로 변경가능

병렬 스트림 **내부동작**을 이해해야 **제대로 사용**할 수 있다.

**포크/조인 프레임워크**: **쉽게 병렬화**를 수행 & 에러 **최소화**

## 7.1 병렬 스트림

**병렬 스트림?** 각각의 스레드에서 처리할 수 있도록 **스트림** 요소를 **여러 청크로 분할**

⇒ **멀티코어 프로세서**가 각각의 **청크**를 **처리가능**!

컬렉션에 `parallelStream` 호출 → 병렬 스트림 `parallel stream` 생성

### 순차 스트림 → 병렬 스트림으로 변환

- **순차 스트림**

```java
public long sequentialSum(long n){
	return Stream.iterate(1L, i -> i+1 ).limit(n).reduce(0L, Long::sum);
}
```

- **병렬 스트림**: 순차 스트림 + `parallel()`

```java
public long sequentialSum(long n){
	return Stream.iterate(1L, i -> i+1).limit(n).parallel().reduce(0L, Long::sum);
}
```

**병렬 스트림**을 사용할 때, **내부적**으로는 **불리언 플래그**가 설정되어

**연산이 병렬 수행**해야함을 나타낸다.

- `sequential`로 **병렬** 스트림 → **순차** 스트림으로 변경가능하다.

### **병렬 스트림 커스터마이징**

- 병렬 스트림은 **내부**적으로 `**ForkJoinPool`을 사용\*\*함
- `ForkJoinPool`은 `Runtime.getRuntime().availableProcessors()`가 **반환하**는 값에 따른 **스레드 수(프로세서 수)**를 갖는다.
- 특별한 이유가 없다면 **기본값 그대로 사용** 권장(일반적으로 기기의 프로세서 수와 같기 때문에)

### 스트림 성능 측정

- **성능 최적화**하기 위한 황금 규칙 = **측정!**
- **JMH**(자바 마이크로벤치 하니스)로 작은 **벤치마크 구현 ⇒ 간단, 어노테이션 지원, 높아진 이식성**

```java
@BenchmarkMdoe(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(2, jvmArgs={"-Xms4G","-Xms4G"})
public class ParallelStreamBenchmark{
	private static final long N = 10_000_000L;

	@Benchmark
	public long sequentialSum() {
		return Stream.iterate(1L, i -> i+1)
			.limit(N)
			.reduce(0L, Long::sum);
	}

	@TearDown(Level.Invocation)
	public void tearDown(){
		System.gc();
	}
	@Benchmark //순차 스트림보다 4배 빠름
	public long iterativeSum(){
		long result = 0;
		for(long i = 1L; i <= N ; i++){
			result += i;
		}
		return result;
	}
	@Benchmark //순차 스트림보다 5배 느림 -> 왜?
	public long parallelSum(){
		return Stream.iterate(1L, i -> i+1 )
			.limit(n)
			.parallel()
			.reduce(0L, Long::sum);
}
// 가비지 컬렉터의 영향을 받지 않도록 **힙 크기**를 **충분히** 설정
//& 벤치마크 끝날 때마다 **가비지 컬렉터**가 **실행**되도록 강제
//그럼에도 정확하지 않을 수 있음
```

1. 해당 **코드 컴파일** → `benchmarks.jar` 파일 생성
2. 파일 실행 `java -jar ./target/benchmarks.jar ParallelStreamBenchmark`

- 위 벤치마크에서 **병렬 스트림**이 **느린** 이유?
  - 반복 결과로 박싱된 객체 → **언박싱을 해야함**
  - 반복 작업은 **병렬로 수행**하기 **어렵다**.

즉, `iterate`는 **본질적으로 순차적**이기 때문에 오히려 병렬처리하는 **오버헤드**만 **늘어나**게 되었다.

### 특화된 메서드 사용: rangeClosed

**멀티코어 프로세서**를 활용해서 **효과적**으로 **병렬 실행**하려면 어떻게 해야할까?

5장에서 설명했던 `LongStream.rangeClosed()` 메서드!

- `LongStream.rangeClosed`는 **기본형 long을 직접 사용**하므로 박싱와 언박싱 **오버헤드X**
- `LongStream.rangeClosed` 는 **청크로 분할**할 수 있는 **숫자** 범위를 **생산**함

```java
@Benchmark
public long rangedSum(){
	return LongStream.rangeClosed(1, N)
		.reduce(0L, Long::sum);
}
```

순차 버전에 비해 숫자 스트림 처리 **속도 빠름**!

**적절한 자료구조**를 선택했기 때문이다.

**특화된 스트림** 처리는 오토박싱, 언박싱 등의 **오버헤드가 없**다!

```java
@Benchmark
public long parallelRangedSum(){
	return LongStream.rangeClosed(1, N)
		.parallel()
		.reduce(0L, Long::sum);
}
```

드디어 순차 버전보다 **더 빠른 병렬 리듀싱** 완성!!

**올바른 자료구조** 병행해야 병렬 실행에서 **최적의 성능** 발휘!

⭐그러나 **병렬화 = 공짜 X,** 멀티코어 간의 **데이터 이동**은 생각보다 **비용이 높다**.

### 병렬화와 관련된 흔한 실수

- **공유된 상태**를 **바꾸는** 알고리즘을 사용할 때(**공유된 가변 상태** 사용)

```java
public long sideEffectSum(long n){
	Accumulator accumulator = new Accumulator();
	LongStream.rangeClosed(1, n).forEach(accumulator::add);
	return accumulator.totla;
}
public class Accumulator{
	public long total = 0;
	public void add(long value){ total += value;}
}
```

`total` 을 접근할 때마다 **데이터 레이스** 문제가 일어남

병렬화하여 실행해보면, **올바른 값**이 나오지 **않음** ⇒ `total+= value`가 **atomic 연산**이 **아니**기 때문

⇒ **병렬 스트림** 제대로 동작하려면, **공유된 가변 상태**를 **피해**야 한다!

### 병렬 스트림 효과적으로 사용하기

- 확신이 서지 않으면 **직접 측정**하기(벤치마크로 직접 성능 측정)
- **박싱**에 주의하기 → **기본 특화 스트림(IntStream, LongStream, DoubleStream)** 사용하기
  (**자동 박싱**과 **언박싱**은 **성능 저하** 가능성이 있음)
- 요소 **순서에 의존**하는 **연산 주의**하기 **ex)** `findFirst`, `limit`
- 스트림 **자료구조**가 **적절**한지 확인하기
  **ex)** `ArrayList`는 `LinkedList`보다 효율적으로 분할 가능,
  `range`로 기본형 스트림 쉽게 분해 가능
- **중간 연산**이 스트림 **특성을 어떻게 바꾸는지**에 따라 분해 성능 달라짐
- **최종 연산**의 **병합** **비용** 살펴보기(ex `combiner`메서드)
- 스트림 소스의 **병렬화 친밀도**
  | 소스 | 분해성 |
  | --------------- | ------ |
  | ArrayList | 훌륭함 |
  | LinkedList | 나쁨 |
  | IntStream.range | 훌륭함 |
  | Stream.iterate | 나쁨 |
  | HashSet | 좋음 |
  | TreeSet | 좋음 |
- 병렬 스트림이 수행되는 **내부 인프라구조** 살펴보기

## 7.2 포크/조인 프레임워크

병렬화할 수 있는 작업을 재귀적으로 **작은 작업으로 분할**한 다음, **전체 결과를 만들도록** 설계함

**포크/조인 프레임워크**에서는 **서브태스크**를 스레드 풀 **(ForkJoinPool)** 의 작업자 스레드에 **분산 할당**하는 **ExcutorService 인터페이스**를 구현함

### Recursive Task 활용

- 스레드 풀 이용: `RecursiveTask<R>`의 **서브클래스**를 만들어야 함
  (R: 병렬화된 태스크가 생성하는 결과가 없을 때 RecursiveAction 형식)
- `RecursiveTask`를 정의하려면, 추상 메서드 `**compute()` 구현\*\* 해야함

  ```java
  protected abstract R compute();
  ```

  `compute` 메서드 **의사코드**

  ```java
  if(태스크가 충분히 작음 or 더이상 분할 불가){
  	순차적 태스크 계산
  } else {
  	두 서브태스크로 분할
  	태스크가 다시 서브태스크가 되도록 메서드 재귀적 호출
  	모든 서브태스크가 완료될때까지 기다림
  	각 서브태스크의 결과 합침
  }
  ```

  이 과정은 사실, **분할 정복**(divide and conquer)의 **병렬화** 버전이다.

  포크/조인 프레임워크로 병렬 합계 수행

  ```java
  public class ForkJoinSumCAlculatro
  extends java.util.concurrent.RecursiveTask<Long>{ //포크/조인 프레임워크에서 사용할 태스크 생성
  	private final long[] numbers;
  	private final int start;
  	private final int end;
  	public static final long THRESHOLD = 10_000; // 이 값 이하의 서브태스크는 분할X

  	public ForkJoinSumCalculator(long[] numbers) { // 메인 태스크 생성시, 공개 생성자
  		this(numbers, 0, numbers.length);
  	}
  	private ForkJoinSumCalculator(long[] numbers, int start, int end){ //서브태스크 생성시, 비공개 생성자
  		this.numbers = numbers;
  		this.start = start;
  		this.end = end;
  	}
  	@Override
  	protected Long compute(){ //RecursiveTask의 추상 메서드 오버라이드
  		int length = end - start; //태스크에서 더할 배열 길이
  		if ( length <= THRESHOLD) {
  			return computeSequentially(); //기준값과 같거나 작으면 순차적으로 결과 계산
  		}
  		ForkJoinSumCalculator leftTask
  			= new ForkJoinSumCalculator(numbers, start, start + length/2); //배열의 첫번째 절반을 더하도록 서브태스크 생성
  		leftTask.fork(); //ForkJoinPool의 다른 스레드로 새로 생성한 태스크 비동기 실행
  		ForkJoinSumCalculator rightTask
  			= new ForkJoinSumCalculator(numbers, start + length/2, end); //배열의 나머지 절반 더하도록 서브태스크 생성
  		Long rightResult = rightTask.compute(); // 두 번째 서브태스크 동기 실행, 추가로 분할 가능O
  		Long leftResult = leftTask.join(); //첫 번쨰 서브태스크의 결과를 읽기 or  결과 없으면 기다림
  		return leftResult + rightResult; // 두 서브태스크의 결과를 조합한 값이 이 태스크 값
  	}
  	private long computeSequentially() { // 더 분할할 수 없을 때, 서브태스크 결과 계산
  		long sum = 0;
  		for(int i = start; i < end; i++){
  			sum += numbers[i];
  		}
  		return sum;
  	}
  }
  ```

- 보통, 일반적으로 애플리케이션에서 `ForkJoinPool`을 **한 번**만 **인스턴스화**
  → 정적 필드에 **싱글턴으로 저장**
- ForkJoinPool에서 **디폴트 생성자** 이용 = JVM에서 `Runtime.availableProcessors`의 **반환값**으로 풀에 사용할 **스레드 수를 결정**한다. (**실제 프로세서**와 하이퍼 스레딩 관련 **가상 프로세서**도 개수에 포함한다.)

### ForkJoinSumCalculator 실행

**위의 코드를 실행** 시, 병렬 스트림 이용시 보다 **성능이 나빠**짐!

`ForkJoinSumCalculator` 태스크에서 사용할 수 있도록 전체 **스트림을** `long[]`으로 **변환**했기 때문

### 포크/조인 프레임워크 제대로 사용하기

- **두 서브태스크**가 모두 **시작이후**, `**join` 호출\*\*하기
  → 아니면 순차 알고리즘보다 복잡해질 수 있음
- **한쪽** 작업에는 **compute를 호출**하기, 불필요한 태스크 할당하는 **오버헤드 회피**가능
- **포크/조인 프레임워크**는 **디버깅**이 어렵다
  - **RecursiveTask**내에서 `ForkJoinPool`의 `**invoke` 사용X**, 대신 `**compute`나 `fork`직접 호출\*\*가능
- 멀티코어에 포크/조인 프레임워크 사용이 **무조건 빠르지는 X**

### 작업 훔치기

- 예제: `ForkJoinSumCalculator`에서 덧셈 수행 숫자가 **만 개 이하**에서 **서브태스크 분할을 중단**함
- 기준을 찾을 방법: **기준값 바꿔가**면서 **직접 실험**하는 방법
- 실제, **코어 개수와 상관없이** **적절한 크기**로 분할된 **많은 태스크 포킹** = **바람직!**
- 그러나, 예제보다 **복잡한 시나리오** 적용될 때,
  각각의 **서브태스크 작업완료** **시간**이 **달라질 수** 있다. ⇒ 작업 훔치기로 문제 해결!

**작업 훔치기 과정**

1. ForkJoinPool의 **모든 스레드**를 **공정**하게 **분할**
2. **각 스레드**는 **이중 연결 리스트**를 참조하며, **큐 헤드**에서 작업을 가져와 **처리**함
3. **한 스레드가** 다른 스레드보다 태스크를 **빨리 처리**할 시, **다른 스레드 큐의 꼬리**에서 **작업 훔쳐**옴
4. 모든 태스크가 작업을 끝낼 때까지 **이 과정 반복**

⇒ 태스크의 **크기를 작게** 나누어야 **스레드간 작업부하**를 **비슷한 수준**으로 유지 가능!

## 7.3 Spliterator 인터페이스

**Spliterator**: **자동**으로 **스트림**을 **분할**하는 기법, **splitable iterator**(분할 가능한 반복자)

- **Iterator** 특징: 소스의 **요소 탐색** 기능 제공함
- **Spliterator** 특징: **병렬** 작업에 **특화**됨

자바8은 컬렉션 프레임워크에 포함된 모든 자료구조에서 **디폴트 Spliterator 구현**을 **제공**함

```java
public interface Spliterator<T> { //T: Spliterator에서 탐색하는 요소 형식
	boolean tryAdvance(Consumer< ? super T> action); //요소 소비하면서 탐색 요소 남아있으면, 참을 반환
	Spliterator<T> trySplit(); //일부 요소를 분할, 두번째 spliterator 생성함
	long estimateSize(); //탐색해야 할 요소 수 정보를 제공가능
	int characteristics();
}
```

### 분할 과정

스트림 **분할과정** = **재귀**적

**1단계**: 첫 번째 Spliterator에 `trySplit` 호출 → 두 번째 Spliterator 생성

**2단계**: 두개의 Spliterator에 `trySplit` 호출 → 네 개의 Spliterator 생성

… `trySplit` 결과가 **null** 일때까지 반복

**n단계**: Spliterator에 호출한 **모든 `trySplit`** 결과가 **null**이면, 재귀 분할 과정 **종료**!

⇒ 분할 과정은 `characteristics`로 정의하는 `Spliterator`의 특성에 영향받음

**Spliterator 특성**

- characteristics는 Spliterator 자체의 특성 집합을 포함하는 int를 반환함
- Spliterator 특성

| 특성       | 의미                                                                               |
| ---------- | ---------------------------------------------------------------------------------- |
| ORDERED    | 요소에 정해진 순서가 있어서 Spliterator는 요소를 탐색, 분할할 때 순서를 유의해야함 |
| DISTINCT   | x, y 두 요소를 방문했을 때 x.equals(y)는 항상 false반환                            |
| SORTED     | 탐색된 요소 = 미리 정의된 정렬 순서 따름                                           |
| SIZED      | 크기가 알려진 소스로 Spliterator를 생성 → estimatedSize()는 정확한 값 반환         |
| NON-NULL   | 모든 요소는 null이 아님                                                            |
| IMMUTABLE  | Spliterator 소스는 불변이다. 요소 탐색중에 요소 추가, 삭제, 수정이 불가하다        |
| CONCURRENT | 동기화 없이 Spliterator의 소스를 여러 스레드에서 동시에 수정 가능하다              |
| SUBSIZED   | 이 Spliterator와 분할되는 모든 Spliteratorsms SIZED 특성을 가짐                    |

### 커스텀 Spliterator 구현

…

# <PART3스트림과 람다를 이용한 효과적 프로그래밍>

# 8장 컬렉션 API 개선

거의 **모든 자바** 애플리케이션에서 **컬렉션**을 **사용**한다.

그러나 **컬렉션 API**는 **성가시**며, **에러**를 유발함.

- **컬렉션 팩토리**: 작은 리스트, 집합, 맵을 쉽게 만들수 있도록
- 리스트, 집합에서 요소 **삭제**, **변경**
- **맵**과 관련된 **편리한 작업**

## 8.1 컬렉션 팩토리

**작은 컬렉션 객체**를 **쉽게 만들 수** 있는 방법 제공

⇒ 이런 기능이 필요한 이유?

- 전: add를 통한 요소 추가

```java
List<String> friends = new ArrayList<>();
friends.add("Raphael");
friends.add("Olivia");
friends.add("Thibaut");
```

- 후(`Array.asList`): 한꺼번에 요소 추가

```java
List<String> friends = Array.asList("Raphael", "Olivia", "Thibaut" );
```

### UnsupportedOperationException: 고정 크기 객체에 요소 추가

고정된 크기의 리스트에 요소 `add` 하려면 `UnsupportedOperationException`이 발생한다.

**자바9**에서는 **작은 리스트, 집합, 맵**을 만들수 있도록 **팩토리 메서드**를 제공한다.

### 리스트 팩토리: List.of

- `List.of` **바꿀 수 없는 리스트를** 만들 수 있다.

```java
List<String> friends = List.of("Raphael", "Olivia", "Thibaut");
//friends 리스트에 요소 추가시, UnsupportedOperationException발생
friends.add("Chih-Chun");
```

- **어떤 상황**에서 **팩토리 메서드**로 리스트를 만들어야 할까?
  - 데이터 처리 **형식을 설정**하거나 데이터 **변환할 필요**가 없을 때
  - 팩토리 메서드 구현이 더 단순하고 목적을 달성하는데 충분하다!

### 집합 팩토리: Set.of

- `Set.of`로 **바꿀 수 없는 집합**을 만들 수 있다.
- **중복**된 요소 **추가**시, `IllegalArgumentException` 발생함 (= 집합은 **고유 요소만** 포함가능)

```java
Set<String> friends = Set.of("Raphael", "Olivia", "Thibaut");
```

### 맵 팩토리: Map.of

- `Map.of`로 **바꿀 수 없는 맵**을 만들 수 있다.
- **맵 초기화 방법 2가지**
  - **키와 값**을 **번갈아** 제공하는 방법(**열개 이하**의 키와 값 쌍에서 추천)
    ```java
    Map<String, Integer> ageOfFriends
    	= Map.of("Raphael", 30 "Olivia", 25, "Thibaut", 26);
    ```
  - `Map.ofEntries`, `Map.entry()` **팩토리 메서드** 이용(**열개 이상**)
    ```java
    import static java.util.Map.entry;
    Map<String, Integer> ageOfFriends
    	= Map.ofEntries(entry("Raphael", 30), ("Olivia", 25), ("Thibaut", 26));
    ```

## 8.2 리스트와 집합 처리

자바8에서 `List`, `Set` 인터페이스에 다음과 같은 **메서드를 추가**함

- `removeIf` : **List**나 **Set**에서 **프레디케이트**를 **만족**하는 요소 **제거**한다.
- `replaceAll`: **List**에서 **UnaryOperator함수**로 요소를 **바꾼**다.
- `sort`: **List**를 **정렬**한다.

위의 메소드들은 **컬렉션** 자체를 **변경**한다! → **에러** 유발 & **복잡**해짐

### removeIf 메서드

```java
for(Transaction transaction: transactions){
	if(Character.isDigit(transaction.getReferenceCode().charAt(0))) {
		transactions.remove(transaction);
	}
}
```

위의 코드는 `ConcurrentModificationException`을 일으킴

코드를 내부적으로 살펴보면 **두 개의 개별 객체(Iterator, Collection)**가 **컬렉션을 관리**해서 **동기화**되지 않기 때문!

그래서 다음과 같이 사용하면 더 쉽게 사용할 수 있다!

```java
transactions.removeIf(transaction ->
	Character.isDigit(transaction.getReferenceCode().charAt(0)));
```

### replaceAll 메서드

- **스트림 API** 사용(**새 문자열 컬렉션**을 만듦)
  ```java
  referenceCodes.stream().map(code
  	-> Character.toUpperCase(code.charAt(0)) +code.substring(1)
  	.collect(Collectors.toList())
  	.forEach(System.out::println);
  ```
- `replaceAll` 사용(기**존 컬렉션** 변경)
  ```java
  referenceCodes.replaceAll(code -> Character.toUpperCase(code.charAt(0)) +
  	code.substring(1));
  ```

## 8.3 맵 처리

### forEach 메서드

자바8부터 **Map** 인터페이스는 **BiConsumer**를 인수로 받는 **forEach메서드**를 **지원**함

- **전**
  ```java
  for(Map.Entry<String, Integer> entry: ageOfFriend.entrySet()){
  	String friend = entry.getKey();
  	Integer age = entry.getValue;
  	System.out.println(friend + " is" + age + " years old");
  }
  ```
- **후**
  ```java
  ageOfFriends.forEach((friend, age) ->
  	System.out.println(friend + " is" + age + " years old"));
  ```

### 정렬 메서드: Entry.comparingByValue, Entry.comparingByKey

- `Entry.comparingByKey()`

```java
//Map 생성
Map<String, String> favoriteMovies
	= Map.ofEntrites(entry("Raphael","Star Wars"),
		entry("Cristina","Matrix"),
		entry("Olivia","James Bond"));
//정렬
favoriteMovies.entrySet().stream()
	.sorted(Entry.comparingByKey()).forEachOrdered(System.out::println);
```

**cf) HashMap 성능**

자바8에서는 **HashMap의 내부 구조**를 **바꿔 성능을 개선**했다.

**이전**에는 많은 키를 사용될 경우, **O(n)** 시간이 걸리는 **LinkedList**를 사용했지만,

**최근**에는 버킷이 너무 커질 경우, **O(log(n))**의 시간이 소요되는 **정렬된 트리**를 **동적으로 치환**해 성능을 개선했다.

### getOrDefault 메서드

- 요청한 **키**가 맵에 **존재하지 않을**때, `getOrDefault` 메서드를 사용하면 **쉽게 해결** 가능!
- `NullPointerException` 을 방지하기 위함

```java
Map<String, String> favoriteMovies
	= Map.ofEntries(entry("Raphael", "Star Wars"),
		entry("Olivia", "James Bond");
System.out.println(favoriteMovies.getOrDefault("Olivia", "Matrix"));
System.out.println(favoriteMovies.getOrDefault("Thibaut", "Matrix"));
```

그러나! **키가 존재**하더라도, **값이 널**이면 `getOrDefault`가 **널을 반환**할 수도 있다!

### 계산 패턴: computeIfAbsent, computeIfPresent, compute

맵에서 키의 존재 여부에 따라 동작 실행 및 결과 저장해야 할 때

- `computeIfAbsent`: 키가 없으면, 키를 이용해 값 계산 + 맵에 추가
  - **전**
  ```java
  friendsToMovies.computeIfAbsent("Raphael",
  	name -> new ArrayList<>()).add("Star Wars");
  ```
  - **후**
  ```java
  friendsToMovies.computeIfAbsent("Raphael",
  	name -> new ArrayList<>()).add("Star Wars");
  ```
- `computeIfPresent`: 키가 있으면, 값 계산 + 맵에 추가
- `compute`: 제공된 키로 값 계산 + 맵에 추가

### 삭제 패턴: remove

```java
favoriteMovies.remove(key, value);
```

### 교체 패턴: replaceAll, Replace

- `replaceAll`: **BiFunction을 적용한 결과**로 항목 값 **교체**, 이 메서드는 L**ist의 replaceAll**과 비슷함
  ```java
  Map<String, String> favoriteMovies = new HashMap<>();
  favoriteMovies.put("Raphael", "Star Wars");
  favoriteMovies.put("Olivia", "james bond");
  favoriteMovies.replaceAll((friend, movie) -> movie.toUpperCase());
  System.out.println(favoriteMovies);
  ```
- `Replace`: **키가 존재**하면 **맵의 값**을 **바꿈**

지금까지 배운 `replace` 패턴은 **한 개의 맵**에만 **적용**할 수 있다.

**두 개의 맵**에서 값을 **합치**거나 **바꿔야** 한다면? ⇒ `merge` 메서드 이용

### 합침: putAll, merge

- `putAll` : 두 개의 **맵**을 **합칠 때(중복 없을 때)**
  ```java
  Map<String, String> family = Map.ofEntries( entry("Teo", "Star Wars"),
  	entry("Cristina", "James Bond"));
  Map<String, String> family = Map.ofEntries( entry("Raphael", "Star Wars");
  Map<String, String> everyone = new HashMap<>(family);
  everyone.putAll(friends);
  System.out.println(everyone);
  ```
- `merge`: (**중복 키 있을 때**) 어떻게 **합칠지 결정**하는 `BiFunction`을 인수로 받는다,
  널값과 관련된 **복잡**한 상황 **처리가능**

  ```java

  //2개의 맵에서 Cristina가 중복으로 존재함
  Map<String, String> family = Map.ofEntries(
  entry("Teo", "Star Wars"),entry("Cristina", "James Bond"));
  Map<String, String> friends = Map.ofEntries(
  entry("Raphael", "Star Wars"),entry("Cristina", "Matrix"));
  //foreach와 merge로 충돌 해결
  Map<String, String> everyone = new HashMap<>(family);
  friends.forEach((k, v) ->
  everyone.merge(k, v, (movie1, movie2) -> movie1 + " &" + movie2))
  // 중복된 키 있으면 두 값 연결
  System.out.println(everyone);
  // {Raphael=Star Wars, Cristina=James Bond & Matrix, Teo=Star Wars}

  ```

      `merge`를 이용한 초기화 검사

  ```java
  moviesToCount.merge(movieName, 1L, (key, count) -> count + 1L);
  ```

## 8.4 개선된 ConcurrentHashMap

- **동시성 친화**적, **최신** 기술이 반영된 `HashMap`
- 따라서 동기화된 `**Hashtable`에 비해** 읽기 쓰기 연산 **성능이 월등\*\*하다

### 리듀스와 검색: forEach, reduce, search

- **forEach**: 각(키, 값) 쌍에 주어진 액션을 실행
- **reduce**: 모든 (키, 값)쌍에 제공된 리듀스 함수를 이용해 결과로 합침
- **search**: 널이 아닌 값을 반환할 때까지 각 (키, 값) 쌍에 함수를 적용

**지원하는 연산 형태**

- **키, 값**으로 연산 (forEach, reduce, search)
- **키**로 연산 (forEachKey, reduceKeys, searchKeys)
- **값**으로 연산 (forEachValue, reduceValues, searchValues)
- **Map.Entry** 객체로 연산 (forEachEntry, reduceEntries, searchEntries)

```java
ConcurrentHashMap<String, Long> map = new ConcurrentHashMap<>();
long parallelismThreshold = 1;
Optional<Integer> maxValue =
	Optional.ofNullable(map.reduceValues(parallelismThreshold, Long::max));
```

### 계수: mappingCount

- **맵의 개수**를 **반환**한다.
- 기존의 size 대신 **int를 반환**하는 **mappingCount를 사용**하는 것이 좋다.
  → **매핑의 개수**가 **int 범위를 넘어**서는 이후의 **상황에 대처** 가능!

### 집합뷰: keySet, newKeySet

- `keySet`: ConcurrentMap을 집합 뷰로 반환
- `newKeySet`: ConcurrentMap로 유지되는 집합 만들 수 있음
