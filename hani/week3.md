# **Chapter 5 - 스트림 활용**

## **1. 필터링**

- filter - Predicate를 통한 필터링

```java
Stream<T> filter(Predicate<? super T> predicate);
```

- distinct - 고유 요소만 필터링

```java
Stream<T> distinct();
```

## **2. 스트림 슬라이싱**

### **Predicate를 통한 슬라이싱**

리스가 이미 정렬되어 있다는 가정 하에 사용하면 좋다.

- takeWhile - Predicate의 결과가 true인 요소에 대한 필터링. Predicate이 처음으로 거짓이 되는 지점에 연산을 멈춘다.

```java
Stream<T> takeWhile(Predicate<? super T> predicate)
```

- dropWhile - Predicate의 결과가 false인 요소에 대한 필터링. Predicate이 처음으로 거짓이 되는 지점까지 발견된 요소를 버린다.

```java
Stream<T> dropWhile(Predicate<? super T> predicate)
```

### **스트림 축소**

- limit - 주어진 값 이하의 크기를 갖는 새로운 스트림을 반환한다.

```java
Stream<T> limit(long maxSize);
```

### **요소 건너뛰기**

- skip - 처음 n개 요소를 제외한 스트림을 반환한다.

```java
Stream<T> skip(long n);
```

## **3. 매핑**

### **스트림의 각 요소에 함수 적용하기**

- map - 함수를 인수로 받아 새로운 요소로 매핑된 스트림을 반환한다.
- 기본형 요소에 대한 mapTo*Type* 메서드도 지원한다 (mapToInt, mapToLong, mapToDouble).

```java
<R> Stream<R> map(Function<? super T, ? extends R> mapper);
```

### **스트림 평면화 - 중요! 조금 어려움**

- flatMap - 제공된 함수를 각 요소에 적용하여 새로운 하나의 스트림으로 매핑한다.
- 결과적으로 하나의 평면화된 스트림을 반환한다.

```java
<R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper);
```

## **4. 검색과 매칭 (쇼트 서킷)**

- anyMatch - 적어도 한 요소와 일치하는지 확인하는 최종 연산이다 (일치하는 순간 true 반환).

```java
boolean anyMatch(Predicate<? super T> predicate);
```

- allMatch - 모든 요소와 일치하는지 검사하는 최종 연산이다 (일치하지 않는 순간 false 반환).

```java
boolean allMatch(Predicate<? super T> predicate);
```

- noneMatch - 모든 요소가 일치하지 않는지 검사하는 최종 연산이다 (일치하는 순간 false 반환).

```java
boolean noneMatch(Predicate<? super T> predicate);
```

- findFirst - 첫 번째 요소를 찾아 반환한다. 순서가 정해져 있을 때 사용한다.

```java
Optional<T> findFirst();
```

- findAny - 요소를 찾으면 반환한다. 요소의 반환순서가 상관없을 때 findFirst 대신 사용된다.

```java
Optional<T> findAny();
```

## **5. 리듀싱**

- reduce - 모든 스트림 요소를 BinaryOperator로 처리해서 값으로 도출한다.
- 두 번째 reduce 메서드와 같은 경우 초기값(identity)가 없으므로 아무 요소가 없을 때를 위해 `Optional<T>`를 반환한다.

```java
T reduce(T identity, BinaryOperator<T> accumulator);
Optional<T> reduce(BinaryOperator<T> accumulator);
<U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner);
```

- max, min - 요소에서 최댓값과 최솟값을 반환한다.
- 마찬가지로, 빈 스트림일 수 있기에 `Optional<T>` 를 반환한다.

```java
Optional<T> max(Comparator<? super T> comparator);
Optional<T> min(Comparator<? super T> comparator);
```

## reduce 메서드의 장점과 병렬화

기존의 단계적 반복으로 합계를 구하는 것보다 reduce를 이용하면 내부 반복이 추상화되면서 내부 구현에서 병렬로 reduce를 실행할 수 있게 된다. 반복적인 합계에서는 sum 변수를 공유해야 하므로 쉽게 병렬화하기 어렵다. 스트림은 내부적으로 포크/조인 프레임워크(fork/join framework)를 통해 이를 처리한다.

---

## 스트림 연산 : 상태 없음과 상태 있음

각각의 스트림 연산은 상태를 갖는 연산과 상태를 갖지 않는 연산으로 나뉘어져 있다.

map, filter 등은 입력 스트림에서 각 요소를 받아 0 또는 결과를 출력 스트림으로 보낸다. 따라서 이들은 보통 상태가 없는, **내부 상태를 갖지 않는 연산(stateless operation)**이다.

하지만 reduce, sum, max 같은 연산은 결과를 누적할 내부 상태가 필요하다. 예제의 내부 상태는 작은 값이다. 스트림에서 처리하는 요소 수와 관계없이 내부 상태의 크기는 **한정(bounded)**되어 있다.

반면 sorted나 distinct 같은 연산은 스트림의 요소를 정렬하거나 중복을 제거하기 위해 과거의 이력을 알고 있어야 한다. 예를 들어 어떤 요소를 출력 스트림으로 추가하려면 **모든 요소가 버퍼에 추가되어 있어야 한다**. 따라서 데이터 스트림의 크기가 크거나 무한이라면 문제가 생길 수 있다. 이러한 연산을 **내부 상태를 갖는 연산(stateful operation)**이라 한다.

---

## **6. 숫자형 스트림**

스트림 API는 숫자 스트림을 효율적으로 처리할 수 있도록 기본형 특화 스트림(primitive stream specialization)을 제공한다.

### **기본형 특화 스트림**

기본형 특화 스트림으로 `IntStream`, `DoubleStream`, `LongStream`이 존재하며 각각의 인터페이스에는 숫자 스트림의 합계를 계산하는 sum, 최댓값 요소를 검색하는 max 같이 자주 사용하는 숫자 관련 리듀싱 연산 메서드를 제공한다.

### **객체 스트림으로 복원하기**

`boxed`메서드를 이용하면 특화 스트림을 일반 스트림으로 변환할 수 있다.

```java
Stream<Integer> boxed(); // in IntStream
```

Optional도 기본형에 대하여 지원한다. `OptionalInt`, `OptionalDouble`, `optionalLong` 세 가지 기본형 특화 스트림 버전의 Optional이 제공된다.

### **숫자 범위**

특정 범위의 숫자를 이용해야 할 때 `range`와 `rangeClosed` 메서드를 사용할 수 있다. 이는 IntStream, LongStream 두 기본형 특화 스트림에서 지원된다. range는 열린 구간을 의미하며, rangeClosed는 닫힌 구간을 의미한다.

## **7. 스트림 만들기**

### **값으로 스트림 만들기**

정적 메서드 `Stream.of` 을 이용하여 스트림을 만들 수 있다.

### **null이 될 수 있는 객체로 스트림 만들기**

자바 9부터 지원되며 `Stream.ofNullable` 메서드를 이용하여 null이 될 수 있는 객체를 지원하는 스트림을 만들 수 있다.

### **배열로 스트림 만들기**

배열을 인수로 받는 정적 메서드 `Arrays.stream` 을 이용하여 스트림을 만들 수 있다.

### **파일로 스트림 만들기**

파일을 처리하는 등의 I/O 연산에 사용하는 자바의 NIO API(비블록 I/O)도 스트림 API를 활용할 수 있도록 업데이트되었다. java.nio.file.Files의 많은 정적 메서드가 스트림을 반환한다. 

예를 들어 `Files.lines`는 주어진 파일의 행 스트림을 문자열로 반환한다.

### **함수로 무한 스트림 만들기**

Stream.iterate와 Stream.generate를 통해 함수를 이용하여 무한 스트림을 만들 수 있다. 

iterate와 generate에서 만든 스트림은 요청할 때마다 주어진 함수를 이용해서 값을 만든다. 

따라서 무제한으로 값을 계산할 수 있지만, 보통 무한한 값을 출력하지 않도록 limit(n) 함수를 함께 연결해서 사용한다.

- Stream.iterate

```java
public static<T> Stream<T> iterate(final T seed, final UnaryOperator<T> f)
```

- Stream.generate

`public static<T> Stream<T> generate(Supplier<T> s)`

# **Chapter 6 - 스트림으로 데이터 수집**

## **Collector (컬렉터)**

Collector 인터페이스 구현은 스트림 요소를 어떤 식으로 도출할지 지정한다. 훌륭하게 설계된 함수형 API의 장점으로는 높은 수준의 조합성과 재사용성을 꼽을 수 있다. Collector 인터페이스 메서드를 어떻게 구현하느냐에 따라 스트림에 어떤 리듀싱 연산을 수행할지 결정된다. **Collectors 유틸리티 클래스**는 자주 사용하는 컬렉터 인스턴스를 손쉽게 생성할 수 있는 정적 팩토리 메서드를 제공한다.

Collectors에서 제공하는 메서드의 기능은 크게 세 가지로 구분할 수 있다.

- 스트림 요소를 하나의 값으로 리듀스하고 요약
- 요소 그룹화
- 요소 분할

## **리듀싱과 요약**

- counting - 개수를 카운트한다
- maxBy, minBy - 최대 혹은 최소를 만족하는 요소를 찾는다
- summingInt - 객체를 int로 매핑하는 인수를 받아 합을 계산한다
- averagingInt - 객체를 int로 매핑하는 인스를 받아 평균을 계산한다
- summarizingInt - 요소 수, 합계, 평균, 최댓값, 최솟값 등을 계산한다.
- joining - 내부적으로 StringBuilder를 이용해서 문자열을 하나로 만든다.

**collect와 reduce**

collect와 reduce를 이용하면 동일한 기능을 구현할 수 있다. 하지만 의미론적인 문제와 실용성 문제등에 대하여 차이가 존재한다.

collect 메서드는 도출하려는 결과를 누적하는 컨테이너를 바꾸도록 설계된 메서드인 반면, reduce는 두 값을 하나로 도출하는 불변형 연산이라는 점에서 의미론적인 차이가 존재한다.

여러 스레드가 동시에 같은 데이터 구조체를 고치면 리스트 자체가 망가져버리므로 리듀싱 연산을 병렬로 수행할 수 없다. 이럴 때 가변 컨테이너 관련 작업이면서 병렬성을 확보하려면 collect 메서드로 리듀싱 연산을 구현하는것이 바람직하다.

## **그룹화**

그룹화 함수는 어떤 기준으로 스트림을 분류하는 속성을 가졌기에 **분류 함수(classification function)**라고 부른다.

- groupingBy

그룹핑에 핵심적인 메서드이며 많은 오버로딩된 메서드를 가진다.

```java
// 분할 함수
public static <T, K> Collector<T, ?, Map<K, List<T>>> groupingBy(
    Function<? super T, ? extends K> classifier) {

    return groupingBy(classifier, toList());
}

// 분할 함수, 감싸인 컬렉터
public static <T, K, A, D> Collector<T, ?, Map<K, D>> groupingBy(
    Function<? super T, ? extends K> classifier,
    Collector<? super T, A, D> downstream) {

    return groupingBy(classifier, HashMap::new, downstream);
}

// 분할 함수, 반환 타입, 감싸인 컬렉터
public static <T, K, D, A, M extends Map<K, D>> Collector<T, ?, M> groupingBy(
    Function<? super T, ? extends K> classifier,
    Supplier<M> mapFactory,
    Collector<? super T, A, D> downstream) {
  // ...
}
```

### **컬렉터 중첩**

```java
Map<Dish.Type, Dish> mostCaloricByType =
    menu.stream()
        .collect(groupingBy(Dish::Type, // 분류 함수
                collectingAndThen(
                    maxBy(comparingInt(Dish::getCalories)), // 감싸인 컬렉터
                Optional::get)); // 변환 함수
```

컬렉터를 중첩할 시 가장 외부 계층에서 안쪽으로 다음과 같은 작업이 수행된다.

1. 가장 바깥쪽에 위치한 groupingBy에서 분류하는 요소(Dish.Type)에 따라 서브스트림으로 그룹화 한다
2. groupingBy 컬렉터는 collectingAndThen으로 컬렉터를 감싼다. 따라서 두 번째 컬렉터는 그룹화된 서브스트림에 적용된다.
3. collectingAndThen 컬렉터는 세번째 컬렉터인 maxBy를 감싼다.
4. 리듀싱 컬렉터(maxBy)가 서브스트림에 연산을 수행한 결과에 Opional::get 변환 함수가 적용된다.
5. groupingBy 컬렉터가 반환하는 맵의 분류 키에 대응하는 값이 각각의 Dish에서 가장 높은 칼로리이다.

## **분할**

분할은 분할 함수(partitioning function)라 불리는 Predicate를 분류 함수로 사용하는 특수한 그룹화 기능이다. 맵의 키 형식은 Boolean이며, 결과적으로 그룹화 맵은 참 아니면 거짓을 갖는 두 개의 그룹으로 분류된다.

분할의 장점은 참, 거짓 두 가지 요소의 스트림 리스트를 모두 유지한다는 것이 장점이다.

```java
public static <T> Collector<T, ?, Map<Boolean, List<T>>> partitioningBy(Predicate<? super T> predicate)

public static <T, D, A> Collector<T, ?, Map<Boolean, D>> partitioningBy(Predicate<? super T> predicate, Collector<? super T, A, D> downstream)
```

## **Collector 인터페이스**

Collector 인터페이스는 리듀싱 연산을 어떻게 구현할지 제공하는 메서드 집합으로 구성된다. Collector 인터페이스를 직접 구현해서 더 효율적으로 문제를 해결하는 컬렉터를 만드는 방법을 살펴보자.

```
public interface Collector<T, A, R> {
    Supplier<A> supplier();
    BiConsumer<A, T> accumulator();
    BinaryOperator<A> combiner();
    Function<A, R> finisher();
    Set<Characteristics> characteristics();
}
```

- T는 수집될 항목의 제네릭 형식이다.
- A는 누적자, 즉 수집 과정에서 중간 결과를 누적하는 객체의 형식이다.
- R은 수집 연산 결과 객체의 형식이다.

예를 들어 Stream의 모든 요소를 List로 수집하는 ToListCollector라는 클래스는 아래와 같이 만들 수 있다.

```
public class ToListCollector<T> implements Collector<T, List<T>, List<T>>
```

### **supplier 메서드 : 새로운 결과 컨테이너 만들기**

supplier 메서드는 수집 과정에서 빈 누적자 인스턴스를 만드는 파라미터가 없는 함수이다.

### **accumulator 메서드 : 결과 컨테이너에 요소 추가하기**

accumulator 메서드는 리듀싱 연산을 수행하는 함수를 반환한다. 즉 누적자(스트림의 첫 n-1개 항목을 수집한 상태)와 n번째 요소를 함수에 적용한다 (그렇기에 제네릭 형식도 `<A, T>`이다).

### **finisher 메서드 : 최종 변환값을 결과 컨테이너로 적용하기**

finisher 메서드는 스트림 탐색을 끝내고 누적자 객체를 최종 결과로 반환하면서 누적 과정을 끝낼 때 호출할 함수를 반환해야한다. ToListCollector와 같이 누적자 객체가 이미 최종 결과인 상황도 있다. 이럴경우 finisher함수는 항등 함수를 반환한다.

### **combiner 메서드 : 두 결과 컨테이너 병합**

combiner는 스트림의 서로 다른 서브파트를 병렬로 처리할 때 누적자가 이 결과를 어떻게 처리할지 정의한다 (그렇기에 BinaryOperator이다).

### **characteristics 메서드**

characteristics 메서드는 컬렉터의 연산을 정의하는 Characteristics 형식의 불변 집합을 반환한다.

```java
enum Characteristics {
    CONCURRENT,
    UNORDERED,
    IDENTITY_FINISH
}
```

- **UNORDERED** : 리듀싱 결과는 스트림 요소의 방문 순서나 누적 순서에 영향을 받지 않는다.
- **CONCURRENT** : 다중 스레드에서 accumulator 함수를 동시에 호출할 수 있으며 병렬 리듀싱을 수행할 수 있다. 컬렉터의 플래그에 UNORDERED를 함께 설정하지 않았다면 데이터 소스가 정렬되어 있지 않은 상황에서만 병렬 리듀싱을 수행할 수 있다.
- **IDENTITY_FINISH** : finisher 메서드가 반환하는 함수는 단순히 identity를 적용할 뿐이므로 이를 생략할 수 있다. 따라서 리듀싱 과정의 최종 결과로 누적자 객체를 바로 사용할 수 있다. 또한 누적자 A를 결과 R로 안전하게 형변환할 수 있다.

> ToListCollector 구현 예시
> 

```java
public class ToListCollector<T> implements Collector<T, List<T>, List<T>> {

	@Override
	public Supplier<List<T>> supplier() {
		return ArrayList::new;
	}

	@Override
	public BiConsumer<List<T>, T> accumulator() {
		return List::add;
	}

	@Override
	public BinaryOperator<List<T>> combiner() {
		return (list1, list2) -> {
				list1.addAll(list2);
				return list1;
		};
	}

	@Override
	public Function<List<T>, List<T>> finisher() {
		return Function.identity();
	}

	@Override
	public Set<Characteristics> characteristics() {
		return Collections.unmodifiableSet(EnumSet.of(
				IDENTITY_FINISH, CONCURRENT));
	}
}
```
