# 3장 람다 표현식

## 3.1 람다란 무엇인가?

람다 표현식 = 익명 함수를 단순화한 것

### 람다의 특징

- **익명**: 메서드 이름이 없다
- **함수**: 특정 클래스에 종속X
- **전달**: 메서드 인수로 전달 or 변수로 저장
- **간결성**: 자질구레한 코드 구현 필요X

### 람다의 구성

```java
(Apple a1, Apple a2) -> a1.getWeight().compareTo(a2.getWeight() );
```

- 파라미터 리스트
- 화살표
- 람다 바디

### 람다 표현식 예제

```java
/* 1번 */ (String s) -> s.length()
/* 2번 */ (Apple a) -> a.getWeight() > 150
/* 3번 */ (int x, int y) -> {
	System.out.println("Result:");
	System.out.println(x + y);
}
/* 4번 */ () -> 42
/* 5번 */ (Apple a1, Apple a2) -> a1.getWeight().compareTo(a2.getWeight() )
/* 6번 */ () -> {return "Mario";}
```

## 3.2 어디에, 어떻게 람다를 사용할까?

### 람다 표현식의 활용

- **함수형 인터페이스**라는 문맥에서 사용 가능
  (**변수에 할당**, **함수형 인터페이스를 인수**로 받는 **메서드**로 전달할 수 있음)
- **함수형 인터페이스의 추상 메서드**와 **같은 시그니처**를 가짐

<aside>
💡 한 개의 void 메소드 호출은 중괄호로 감쌀 필요 없다

</aside>

### 함수형 인터페이스

- **하나의 추상 메서드**를 지정하는 **인터페이스**
  cf) **추상 메서드**: 선언부만 작성하고 구현부는 작성하지 않은 채로 남겨 둔 것

```java
/* 1번 */ public interface Comparator<T> {
	int compare(T o1, T o2);
}
/* 2번 */ public interface Adder{
	int add(int a, int b);
}
```

- **@FunctionalInterface**: “Multiple nonoverriding abstract methos found in interface”

### 함수 디스크립터(function descriptor)

- **함수형 인터페이스**의 **추상 메서드 시그니처** = **람다 표현식**의 **시그니처**
- **람다 표현식**의 **시그니처 서술**하는 메서드

## 3.3 람다 활용: 실행 어라운드 패턴

### 실행 어라운드 패턴(execute around pattern)

- 순환 패턴(recurrent pattern): 자원 열고, 처리, 닫음
- 실행 어라운드 패턴 적용 과정

```java
// 1번째
public String processFile() throws IOException{
	try (BufferedReader br = new BufferedReader(new FileReader("data.txt")) ){
		return br.readLine();
	}
}
// 2번째
public interface BufferedReaderProcessor{
	String process(BufferedReader b) throws IOException;
}
public String processFile(BufferedReaderProcessor p) thorws IOException{ ...}
// 3번째
public String processFile(BufferedReaderProcessor p) throws IOExceptionP
	try ( BufferedReader br = new BufferedReader( new FileReader("data.txt") ) ){
		return p.process(br);
	}
}
// 4번째
String oneLine = processFile( (BufferedREader br) -> br.readLine() );
String twoLines = processFile( (BufferedREader br) -> br.readLine + br.readLine() );
```

## 3.4 함수형 인터페이스 사용

### 정리

- 함수형 인터페이스 = 하나의 추상 메서드를 지정
- 함수형 인터페이스의 추상 메서드 = 람다 표현식의 시그니처 묘사함
- 함수 디스크립터 = 함수형 인터페이스의 추상 메서드 시그니처
- 람다 표현식 사용하기 위해, 공통의 함수 디스크립터를 기술하는 함수형 인터페이스 집합이 필요
- 함수형 인터페이스: **Predicate**, **Consumer**, **Function**

### Predicate 인터페이스

- T 형식의 객체를 사용하는 불리언 표현식이 필요한 상황에서 사용(T → boolean)

```java
@FunctionalInterface
public interface Predicate<T>{
	boolean test(T t);
}
Predeicate<String> nonEmptyStringPredicate = (String s) -> !s.isEmpty();
List<String> nonEmpty = filter(listOfStrings, nonEmptyStringPredicate);
```

### Consumer 인터페이스

T 형식의 객체를 인수로 받아서 어떤 동작을 수행하고 싶을 때 사용(T → void)

```java
@FunctionalInterface
public interface Consumer<T>{
	void accept(T t);
}
forEach(
	Arrays.asList(1,2,3,4,5), (Integer i) -> System.out.println(i)
);
```

### Function 인터페이스

입력을 출력으로 매핑하는 람다를 정의할 때 사용(T → R)

```java
@FunctionalInterface
public interface Function<T, R>{
	R apply(T t);
}
//[7, 2, 6]
List<Integer> l = map(
	Arrays.asList("lambdas", "in", "action"),
	(String s) -> s.length()
);
```

### 기본형 특화

- **제네릭 파라미터**에는 **참조형(Byte, Integer, List)만 사용** 가능
  **제네릭 내부 구현** 때문에
- **박싱**(기본형 → 참조형)과 **언박싱**(참조형 → 기본형)
- **예시**: IntPredicate, DoublePredicate, LongConsumer, IntFunction<R>, LongFunction<R>

### 사용 사례

| 사용 사례                     | 람다 예제                                                        | 대응하는 함수형 인터페이스                             |
| ----------------------------- | ---------------------------------------------------------------- | ------------------------------------------------------ |
| 불리언 표현                   | (List<String> list) -> list. isEmpty()                           | Predicate<List<String>>                                |
| 객체 생성                     | () ->new Apple(10)                                               | Supplier<Apple>                                        |
| 객체에서 소비                 | (Apple a) -> System.out.print1n(a,getWeight())                   | Consumer<Apple>                                        |
| 객체에서 선택/추출            | (String s) -> s.length()                                         | Function<String, Integer> 또는 ToIntFunction<String>   |
| 두 값 조합                    | (int a, int b) → a \* b                                          | IntBinaryOperator                                      |
| 두 객체 비교                  | (Apple a1, Apple a2) → a1.getWeigth().compareTo(a2.getWeight() ) | Comparator<Apple>또는BiFunction<Apple, Apple, Integer> |
| ToIntBiFunction<Apple, Apple> |

## 3.5 형식 검사, 형식 추론, 제약

### 형식 검사

람다가 사용되는 **콘텍스트(context)**를 이용해서 람다의 **형식(type)을 추론**할 수 있다.

- **대상 형식(target type)**: 기대되는 람다 표현식의 형식

## 3.6 메서드 참조

### 메서드 참조가 중요한 이유

- 가독성을 높일 수 있음

### 메서드 참조

- 기존 메서드 정의를 재활용, 람다처럼 전달 가능

```java
inventory.sort(comparing(Apple::getWeight));
```

- 메서드 참조: 특정 메서드만 호출하는 람다의 축약형
  `(Apple a) → a.getWeight()`를 축약한 것!
- 예제

| 람다                                   | 메서드 참조                       |
| -------------------------------------- | --------------------------------- |
| (Apple apple) → apple.getWeight()      | Apple::getWeight                  |
| ()→ Thread.currentThread().dumpStack() | Thread.currentThread()::dumpStack |
| (str, i) → str.substring(i)            | String::substring                 |
| (String s) → System.out.println(s)     | System.out::println               |
| (String s) → this.isValidName(s)       | this::isValidName                 |

### 메서드 참조 이용하기

1. 정적 메서드 참조
   `Integer::parseInt`
2. 다양한 형식의 인스턴스 메서드 참조
   `String::length`
3. 기존 객체의 인스턴스 메서드 참조: 비공개 헬퍼 메서드를 정의했을 때
   `expensiveTransaction::getValue`

### 생성자 참조

new 키워드를 이용해서 만들 수 있음

```java
Function<Integer, Apple> c1 = Apple::new;
//아래의 코드와 동일하다
Function<Integer, Apple> c1 = (weight) -> new Apple(weight);
```

- **인수가 3개**인 생성자의 생성자 참조를 사용하려면?
  생성자 참조와 **일치하는 시그니처**를 갖는 **함수형 인터페이스**가 **필요**함!
  그러나 이러한 함수형 인터페이스는 **제공되지X** ⇒ 우리가 **직접 만들어**야 함
      ```java
      public interface TriFunction<T,U, V, R>{
      	R apply(T t, U u, V v);
      }
      TriFunction<Integer, Integer, Integer, Color> colorFactory = Color::new;
      ```

## 3.7 람다, 메서드 참조 활용하기

### 이때까지 배운 내용 총 활용

- 동작 파라미터화 + 익명 클래스 + 람다 표현식 + 메서드 참조
  ⇒ 코드 짧아짐 + 코드의 의미 명확

```java
inventory.sort(comparing(Apple::getWeight));
```

### 동작 파라미터화

```java
public class AppleComparator implements Comparator<Apple>{
	public int compare(Apple a1, Apple a2){
		return a1.getWeight().compareTo(a2.getWeight());
	}
}
inventory.sort( new AppleComparator() );
```

### 익명 클래스

```java
inventory.sort( new Comparator<Apple>() {
	public int compare(Apple a1, Apple a2){
		return a1.getWeight().compareTo(a2.getWeight());
	}
});
```

### 람다 표현식

- 여전히 길다.
- 함수형 인터페이스를 사용하는 곳이면, 람다 표현식 사용 가능

```java
inventory.sort((Apple a1, Apple a2) ->
	a1.getWeight().compareTo(a2.getWeight())
);
```

- 람다 파라미터의 형식 추론 + comparing 메서드 사용

```java
inventory.sort(comparing(apple -> apple.getWeight()) );
```

### 메서드 참조

```java
inventory.sort(comparing(Apple::getWeight));
```

## 3.8 람다 표현식을 조합할 수 있는 유용한 메서드

- **함수형 인터페이스**는 **람다 표현식을 조합**할 수 있도록 **유틸리티 메서드 제공**
- 간단한 **여러 개**의 **람다 표현식** 조합 ⇒ **복잡한 람다 표현식**

### 디폴트 메서드

- 함수형 인터페이스에서 어떤 메서드를 제공하길래 이런 일이 가능한가?
  ⇒ **디폴트 메서드**를 제공하기 때문에 가능!

### Comparator 활용

- Comparator 조합(역정렬)

```java
inventory.sort( comparing(Apple::getWeight).reversed() );
```

- Comparator 연결

```java
inventory.sort(comparing(Apple::getWeight)
				 .reversed()
				 .thenComparing(Apple::getCountry)
);
```

### Predicate 활용

- 복잡한 predicate를 위한 **negate**(반전시킬 때), **and**, **or**

### Function 활용

- 람다 표현식 조합을 위한 **andThen**(파이프 역할), **compose**(내부 함수 실행 후, 외부 함수 사용)

## 3.9 비슷한 수학적 개념

- 적분
- 람다

# <Part2 함수형 데이터 처리>

# 4장 스트림 소개

### 스트림을 위한 고민

- 컬렉션: 데이터 그룹화 및 처리
- 질의를 어떻게 구현해야할지 명시X, 구현 자동 제공
- 커다란 컬렉션의 병렬 처리

## 4.1 스트림이란 무엇인가?

- 선언형(질의)으로 컬렉션 데이터를 처리할 수 있음
  (=데이터 컬렉션 반복을 처리하는 기능)
- 투명하게 병렬처리 가능
- 선언형 코드 + 동작 파라미터 화 ⇒ 요구사항 변경에 쉽게 대응
- 고수준 빌딩 블록(high-level building block)으로 자유롭게 사용

### 스트림의 특징

- 선언형: 더 간결하고 가독성이 좋아진다
- 조립할 수 있음: 유연성이 좋아진다
- 병렬화: 성능이 좋아진다

## 4.2 스트림 시작하기

### 스트림

**데이터 처리 연산**을 지원하도록 **소스**에서 추출된 **연속된**(순차적으로 값에 접근) **요소**

- **파이프라이닝**: 스트림 연산 연결 → 커다란 파이프라인 구성(최적화 획득)
- **내부 반복**

### 여러가지 처리 연산

```java
List<String> threeHighCaloricDishNames =
	menu.stream()
			.filter(dish -> dish.getCalories() > 300)
			.map(Dish::getName)
			.limit(3)
			.collect(toList());

System.out.println(threeHighCaloricDishNames);
```

- **filter**: 스트림에서 특정 요소 제외시킴
- **map**: 다른 요소로 변환 or 정보 추출
- **limit**: 스트림 크기 제한
- **collect**: 스트림을 다른 형식으로 변환(스트림 → 리스트)

## 4.3 스트림과 컬렉션

| 스트림           | 컬렉션                | 스트림                |
| ---------------- | --------------------- | --------------------- |
|                  | 메모리에 모든 값 저장 |                       |
| 데이터 처리 시기 | 요소 미리 계산        | 요청할 때만 요소 계산 |
| 탐색 횟수 제한   | 여러번 탐색 가능      | 한번만 탐색 가능      |
| 데이터 반복 처리 | 외부 반복             | 내부 반복             |

### 내부 반복

어떤 작업을 수행할지만 지정되면, 알아서 처리됨 ⇒ filter나 map을 사용

- 투명하게 병렬로 처리
- 더 최적화된 다양한 순서로 처리
- 데이터 표현과 하드웨어를 활용한 병렬성 구현을 자동으로 선택

## 4.4 스트림 연산

크게 연산을 2그룹으로 나눌 수 있다

### 중간 연산(intermediate operation)

서로 연결되어 파이프라인을 형성함

- filter, map, limit, sorted, distinct
- 다른 스트림을 반환함 → 연결하여 질의 생성 가능
- 최종 연산 수행 전까지는 아무 연산도 수행하지 않음
- 최적화: 쇼트서킷, 루프 퓨전

### 최종 연산(terminal operation)

파이프라인을 실행한 다음에 닫음

- forEach, count, collect

**빌더 패턴**과 비슷함!

## 4.5 로드맵
