```java
💡 배울 내용
병렬 스트림으로 데이터를 병렬 처리하기
병렬 스트림의 성능 분석
포크/조인 프레임워크
Spliterator로 스트림 데이터 쪼개기
```

# 병렬 스트림

- 컬렉션에 `parallelStream` 을 호출하면 병렬 스트림이 생성된다.
- 각각의 스레드에서 처리할 수 있도록 스트림 요소를 여러 청크로 분할한 스트림

```java
// 병렬처리 1~n까지 합
public long parallelSum(Long n){
    return Stream.iterate(1L, i->i+1)
            .limit(n)
            .parallel()
            .reduce(0L, Long::sum);
}
```

- 내부적으로 parallel을 호출하면 병렬수행 Boolean이 설정 됌

### sequential

- 병렬 스트림을 순차 스트림으로 바꿔준다.

### 두 가지 모두 사용했을 땐 ?

```java
stream.parallel()
			.filter(...)
			.sequential()
			.map(...)
			.parallel()
			.reduce();
```

- `최종적으로 parallel이 호출` 되었음으로 전체 파이프라인에 영향을 미치게 된다.

## 스트림 성능 측정

- 성능을 최적화 할땐 `측정` 이 중요하다 !
- 자바 마이크로벤치마크 하니스(JMH) 라이브러리 이용
- 추가하는 방법 - 프로젝트 우클릭  → Open Modules Settings  →  +버튼 → Maven → org.openjdk.jmh 검색 → 추가

![스크린샷 2022-11-02 오후 11.17.11.png](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/a9263962-56f5-4d0c-bf51-935d6a4b4db5/%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA_2022-11-02_%E1%84%8B%E1%85%A9%E1%84%92%E1%85%AE_11.17.11.png)

```java
// 벤치마크 대상 메서드를 실행하는 데 걸린 평균 시간 측정
@BenchmarkMode(Mode.AverageTime)
// 벤치마크 결과를 밀리초 단위로 출력
@OutputTimeUnit(TimeUnit.MILLISECONDS)
// 4Gb의 힙 공간을 제공한 환경에서 두 번 벤치마크를 수행해 결과의 신뢰성 확보
@Fork(value = 2, jvmArgs = {"-Xms4G", "-Xmx4G"})    
public class ParallelStreamBenchmark {
    private static final long n = 10_000_000L;
    // 벤치마크 대상 메서드
    @Benchmark     
    public long sequentialSum(){
        return Stream.iterate(1L, i->i+1).limit(n)
                .reduce(0L, Long::sum);
    }
    
    // 매 번 벤치마크를 싱행한 다음에는 가비지 컬렉터 동작 시도
    @TearDown(Level.Invocation)     
    public void tearDown(){
        System.gc();
    }
}
->
java -jar ./target/benchmarks.jar ParallelStreamBenchmark 
```

- 병렬 버전이 다섯 배나 느린 결과 이유는?
    - 반복 결과로 박싱된 객체가 만들어지므로 숫자를 더하려면 언박싱을 해야 한다.
    - 반복 작업은 병렬로 수행할 수 있는 독립 단위로 나누기가 어렵다.

→ 병렬로 수행될 수 있는 스트림 모델 필요 !

→ 병렬 프로그래밍을 오용하면 오히려 전체 프로그램의 성능이 더 나빠질 수 있다.

→ parallel 메서드를 호출했을 때 내부적으로 어떤 일이 일어나는지 꼭 이해해야함 !

### LongStream.rangeClosed 메서드

- 기본형 long을 직접 사용하므로 박싱과 언박싱 오버헤드가 사라진다
- 쉽게 청크로 분할 할 수 있는 숫자 범위 생산

```java
@Benchmark
    public long rangedSum1(){
        return LongStream.rangeClosed(1,n)
                .reduce(0L, Long::sum);
    }
5.315 ms

    @Benchmark
    public long rangedSum2(){
        return LongStream.rangeClosed(1,n)
                .parallel()
                .reduce(0L, Long::sum);
    }
2.677 ms
```

## 병렬화가 공짜가 아니다 ?

### 병렬화를 이용하기 위한 일

- 스트림을 재귀적으로 분할
- 각 서브스트림을 서로 다른 스레드의 리듀싱 연산으로 할당
- 이들 결과를 하나의 값으로 합치기

### 멀티코어 간의 데이터 이동은 우리 생각보다 비싸다.

→ 코어 간에 데이터 전송 시간보다 `훨씬 오래 걸리는 작업만 병렬로` 다른 코어에서 수행하는 것이 바람직

### 데이터 레이스 문제

- 다수의 스레드에서 동시에 데이터에 접근하는 문제

→ 공유된 가변 상태를 피해야 한다.

## 병렬 스트림 효과적으로 사용하기

1. 확신이 서지 않으면 직접 측정하라
2. 방싱을 주의하라
3. 순차 스트림보다 병렬 스트림에서 성능이 떨어지는 연산이 있다. 
    - 순서에 의존하는 연산(findFirst, limit)
4. 스트림에서 수행하는 전체 파이프라인 연산 비용을 고려하라.
5. 소량의 데이터에서는 병렬 스트림이 도움 되지 않는다.
6. 스트림을 구성하는 자료구조가 적절한지 확인해라.
7. 스트림의 특성과 파이프라인 중간 연산이 스트림의 특성을 어떻게 바꾸는지에 따라 분해 과정의 성능이 달라질 수 있다.
8. 최종 연사의 병합과정 비용을 살펴보라.

→ 결론 모르면 직접 측정해봐라 ?

### 스트림 소스와 분해성

| ArrayList | 훌륭함 |
| --- | --- |
| LinkedList | 나쁨 |
| IntStream.range | 훌륭함 |
| Stream.iterate | 나쁨 |
| HashSet | 좋음 |
| TreeSet | 좋음 |

## 포크/조인 프레임워크

- 병렬화할 수 있는 작업을 재귀적으로 작은 작업으로 분할한 다음에 서브태스크에 각각의 결과를 합쳐서 전체 결과를 만들도록 설계되었다.
- 포크/조인 프레임워크에서는 서브태스크를 스레드 풀의 작업자 스레드에 분산 할당하는 ExecutorService 인터페이스를 구현

### Recursive Task 활용

```java
//RecursiveTask를 상속받아 포크/조인 프레임워크에서 사용할 테스크 생성
public class ForkJoinSumCalculator extends RecursiveTask<Long> {

	
		...

		@Override
	  protected Long compute() { // RecursiveTask의 추상 메서드 오버라이드
	    int length = end - start;  // 테스크에서 더할 배열의 길이
	    if (length <= THRESHOLD) {
	      return computeSequentially();     // 기준값과 같거나 작으면 순차적으로 결과를 계산한다.
	    }
      // 재귀 호출
	    ForkJoinSumCalculator leftTask = new ForkJoinSumCalculator(numbers, start, start + length / 2);
	    leftTask.fork();
	    ForkJoinSumCalculator rightTask = new ForkJoinSumCalculator(numbers, start + length / 2, end);
	    Long rightResult = rightTask.compute();
	    Long leftResult = leftTask.join();
	    return leftResult + rightResult;
	  }

		// 순차적으로 계산해주는 메서드
		private long computeSequentially() {
		    long sum = 0;
		    for (int i = start; i < end; i++) {
		      sum += numbers[i];
		    }
		    return sum;
	  }
		...

}
```

### ForkJoinSumCalculator 실행

- ForkJoinSumCalculator → ForkJoinPool로 전달 → compute 메서드 수행 → 병렬로 시행할 수 있을 만큼 쪼개다가(재귀) → 충분히 작아지면 순차계산 → (트리 역순으로) 부분 결과를 합침 → 최종 결과 도출

![Untitled](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/5873461f-15b7-4620-b153-9188427a1a3a/Untitled.png)

- RecursiveTask 요약 사진

### 주의점

- **`join()`**메소드는 결과를 반환할 때까지 블록시키기 때문에 항상 두 서브 테스크를 시작한 뒤 호출해야 한다. 그렇지 않으면, 기존의 순차 스트림보다 느리게 된다.
- **`RecursiveTask`** 내에서는 **`ForkJoinPool`**의 **`invoke()`**를 사용하면 안되고 **`compute()`**나 **`fork()`**메소드를 호출해야 한다.
- 분리된 서브테스크 중 한 작업에만 **`compute()`**작업을 호출해야 한다. 한 테스크는 같은 스레드를 재사용할 수 있어 오베헤드가 감소한다.
- 병렬 계산은 디버깅이 어렵다. 다른 스레드에서 **`compute()`**를 호출하기 때문에 stack trace가 도움이 되지 않는다.

### 작업 훔치기 (work stealing)

- 가능한 많이 분할 하는 것이 좋다.
- ForkJoinPool의 `모든 스레드를 거의 공정하게 분할`한다.
- 자신에게 할당된 이중연결 리스트를 참조하고, 작업이 끝날 때마다 큐의 헤드에서 다른 테스크를 가져와 작업을 처리한다.
- 한 스레드는 다른 스레드보다 빠르게 작업을 처리하게 되면 `다른 스레드 큐의 꼬리(tail)에서 작업을 훔쳐`온다.
- 모든 큐가 빌 때까지 과정을 반복하게 되는데 이 때문에 테스크의 크기를 작게 나누어야 스레드 간의 작업 부하를 비슷하게 유지할 수 있게 된다.

![Untitled](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/bd5d6f44-0c46-4854-b9b0-01ab9fc28f10/Untitled.png)

## Spliterator 인터페이스

- 기능 : 탐색하려는 데이터를 포함하는 스트림을 어떻게 병렬화 할 것인지 정의
- ‘분할할 수 있는 반복자’
- Iterator처럼 소스의 요소 탐색 기능을 제공하지만 `병렬 작업에 특화`되어 있다.

```java
public interface Spliterator<T> { // T : 탐색하는 요소의 형식
    boolean tryAdvance(Consumer<? super T> action); // 탐색할 요소가 남았는지 여부
    Spliterator<T> trySplit(); // 일부 요소를 분할해 두 번째 Spliterator를 생성
    long estimateSize(); // 탐색해야 할 요소 수 정보
    int characteristics(); // 특성 집합 정보
}
```

### 분할 과정

1. Spliterator에 `trySplit()`을 호출하면 두 번째 Splitarator가 생성
2. 다시 `trySplit()`을 호출하면 4개, 결과가 null이 될 때까지 반복한다
3. null이 반환되면 더 이상 분할이 불가능하다는 의미기 때문에 종료된다.

![Untitled](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/63da348b-83c7-4c6d-b5a3-ecf015a8e68e/Untitled.png)