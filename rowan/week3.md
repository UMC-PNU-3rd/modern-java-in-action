# 5장 스트림 활용

스트림 API가 지원하는 다양한 연산을 알아보자.

- 데이터 처리 질의: 필터링, 슬라이싱, 매핑, 검색, 매칭, 리듀싱
- 숫자 스트림
- 무한 스트림

## 5.1 필터링

스트림의 **요소를 선택**하는 방법을 배운다.

(**프레디케이트** 필터링 방법, **고유 요소**만 필터링 하는 법)

### 프레디케이트로 필터링

스트림 인터페이스는 **`filter` 메서드**를 지원한다.

filter메서드는 **프레디케이트(불리언을 반환하는 함수)를 인수로** 받아 모든 요소를 포함하는 스트림을 반환한다.

```java
List<Dish> vegetarianMenu = menu.stream().filter(Dish::isVegetrarian).collect(toList());
```

### 고유 요소 필터링

스트림은 **`distnct` 메서드**를 지원한다.

고유 여부는 스트림에서 만든 객체의 **`hashCode`, `equals`**로 결정된다.

```java
List<Integer> numbers = Arrays.asList(1,2,1,3,3,2,4);
numbers.stream()
	.filter(i -> i % 2 ==0)
	.distinct()
	.forEach(System.out::println);
```

## 5.2 스트림 슬라이싱

**스트림의 요소**를 **선택**하거나 **스킵**하는 다양한 방법이 있다.

- **프레디케이트**를 이용
- 스트림 **줄이기**
- 몇 개의 요소 **무시**

### 프레디케이트를 이용한 슬라이싱

`takeWhile`, `dropWhile` 두 가지 메서드를 지원한다.

- `takeWhile`은 **정렬된 스트림**에 **프레디케이트**를 적용해 슬라이스할 수 있다.

```java
List<Dish> slicedMenu1 = specialMenu.stream()
	.takeWhile(dish -> dish.getCalories() < 320).collect(toList());
```

- `dropWhile`은 프레디케이트가 **거짓**이 되면 그 지점에서 **작업을 중단**하고 **남은 모든 요소를 반환**한다.

```java
List<Dish> sliceMenu2 = specialMenu.stream()
	.dropWhile(dish -> dish.getCalories() > 320).collect(toList());
```

**즉,** `takeWhile`과 `dropWhile`은 **정반대의 작업**을 수행한다.

### 스트림 축소

스트림은 `limit(n)` 메서드를 지원한다.

`limit(n)`은 **주어진 값 이하의 크기**를 갖는 **스트림**을 **반환**한다.

스트림 **정렬** 시, **최대 n개**를 **반환**할 수 있다

스트림 **비정렬** 시, 결과도 **정렬되지않고 반환**된다.

```java
List<Dish> dishes = specialMenu.stream().filter(dish -> dish.getCalories() > 300 )
	.limit(3).collect(toList());
```

### 요소 건너뛰기

스트림은 `skip(n)`메서드를 지원한다.

`skip(n)`은 **처음 n개** 요소를 **제외**한 스트림을 반환한다.

```java
List<Dish> dish = menu.stream().filter(d -> d.getCalories() > 300 )
	.skip(2).collect(toList());
```

## 5.3 매핑

스트림의 `map`과 `flatMap`은 **특정 데이터**를 **선택**하는 기능을 제공한다.

### map: 스트림 요소마다 함수 적용하기

map은 **함수를 인수로** 받는다.

리스트의 **각 요소**에 **함수를 적용**할 수 있다.

```java
List<String> dishNames = menu.stream().map(Dish::getName).collect(toList());
// 요리명을 추출하는 코드
```

- map을 연결하기(chaining)

```java
List<Integer> dishNameLengths = menu.stream().map(Dish::getName)
	.map(String::length).collect(toList));
```

### flatMap: 스트림 평면화

```java
["Hello", "World"] -> ["H", "e", "l", "o", "W", "r", "d"]
```

이 문제는 앞서 배웠던 `distinct()`로 풀 수 있을 것 같지만, 그러지 못한다.

우리가 원하는 반환은 `Stream<String>`인데, `map`은 `Stream<String[]>`을 반환하기 때문이다.

- flapMap의 사용

```java
List<String> uniqueCharacters = words.stream() //Stream<String>
	.map(word -> word.split("")) //각 단어를 개별 문자를 포함하는 배열로 변환 Stream<String[]>
	.flapMap(Array::stream) //생성된 스트림을 하나의 스트림으로 평면화 Stream<String>
	.distinct().collect(toList()); //List<String>
```

flapMap은 스트림이 아닌 **스트림의 콘텐츠로 매핑**한다.

## 5.4 검색과 매칭

스트림은 `allMatch`, `anyMatch`, `noneMatch`, `findFirst`, `findAny`메서드를 지원한다.

해당 메서드들은 **특정 속성**이 **데이터 집합**에 있는지 여부를 검색한다.

### anyMatch: 적어도 한 요소와 일치

불리언을 반환한다.

```java
if(menu.stream().anyMatch(Dish::isVegetarian)){
	System.out.println("The menu is (somewhat) vegetarian friendly!!");
}
```

### allMatch: 모든 요소와 일치

```java
boolean isHealthy = menu.stream().allMAtch(dish -> dish.getCalories() < 1000);
```

### noneMatch: 일치하는 요소가 없는지 확인

```java
boolean isHealthy = menu.stream().nonMatch(d -> d.getCalories() >= 1000);
```

⭐ `anyMatch`, `allMatch`, `noneMatch` 모두 **쇼트서킷**이다. (자바의 &&와 || 같은 연산을 활용)

### findAny: 요소 검색

스트림은 `findAny` 메서드를 지원한다.

해당 메서드는 현재 스트림에서 **임의의 요소**를 **반환**한다.

```java
Optional<Dish> dish = menu.stream().filter(Dish::isVegetarian).findAny();
```

**참고) Optional이란?**

**Optional<T>**는 값의 존재나 부재 여부를 표현하는 컨테이너 클래스다.

findAny 메서드는 **null**을 반환할 수 있는데, null은 **쉽게 에러**를 일으킬 수 있으므로 만들어졌다.

### findFirst: 첫 번째 요소 찾기

**논리적인 아이템 순서**가 정해져있는 스트림에서 사용한다.

```java
List<Integer> someNumbers = Arrays.asList(1,2,3,4,5);
Optional<Integer> firstSquareDivisibleByThree =
	someNumbers.stream().map(n -> n*n).filter(n -> n % 3 == 0).findFirst();
```

⭐ `findFirst`와 `findAny`는 **병렬성** 때문에 **사용**한다.

## 5.5 리듀싱

이 장에서는 스트림 요소를 조합해서 **복잡한 질의를 표현**하는 방법에 대해 설명한다.

**리듀싱(reduce)**: 모든 **스트림 요소를 처리**해서 **값으로 도출**하는 연산

### reduce 구성

2가지로 구성되어있다

- **0**: 초깃값
- **BinaryOperator<T>**: 두 요소를 조합해서 새로운 값을 만든다.
  ex) ( a , b) → a + b

**reduce 적용 전**

```java
int sum = 0;
for ( int x: numbers){
	sum += x;
}
```

**reduce 적용 후**

```java
int sum = numbers.stream().reduce(0, Integer::sum);
```

자바 8에서는 **정적** sum **메서드**를 **제공**한다.

즉, `(a,b) → a*b` 대신, `Integer::sum` 을 사용할 수 있다.

### 초기값 없는 reduce

해당 reduce는 **Optional 객체**를 **반환**한다.

```java
Optional<Integer> sum = numbers.stream().reduce((a,b) -> (a+b));
```

### reduce의 활용: 최대&최소

- 최대

```java
Optional<Integer> max = numbers.stream().reduce(Integer::max);
```

- 최소

```java
Optional<Integer> min = numbers.stream().reduce(Integer::min);
```

cf) **맵 리듀스(map-reduce)패턴**

**map과 reduce**를 **연결**하는 기법이다. **쉽게 병렬화**하는 특징이 있다.

### 병렬화: reduce 메서드 장점

- reduce이용: 내부 반복 **추상화** → **병렬**로 reduce **실행**
- `parallelStream()`: 스트림의 **모든 요소**를 더하는 코드를 **병렬**로 만드는데, 이를 이용해서 병렬실행!
  ```java
  int sum = numbers.parallelStream().reduce(0, Integer::sum);
  ```
- **주의 사항**: reduce에 넘겨준 **람다의 상태**가 **변경**되면 **안됨**, 실행 **순서 상관이 없어**야함

### 스트림의 상태

- **상태 없음**: map, filter
- **상태 있음**: reduce, sum, max, sorted, distinct

**상태가 있는 연산**들은 연산 **결과**를 **내부에 누적**할 필요가 있는 연산들이다.

### 이때까지 배운 연산

| 연산 | 형식 | 반환 형식 | 사용된 함수형
인터페이스 형식 | 함수 디스크립터 |
| --- | --- | --- | --- | --- |
| filter | 중간 연산 | Stream<T> | Predicate<T> | T → boolean |
| distinct | 중간 연산 | Stream<T> | | |
| takeWhile | 중간 연산 | Stream<T> | Predicate<T> | T → boolean |
| dropWhile | 중간 연산 | Stream<T> | Predicate<T> | T → boolean |
| skip | 중간 연산 | Stream<T> | long | |
| limit | 중간 연산 | Stream<T> | long | |
| map | 중간 연산 | Stream<T> | Function<T, R> | T → R |
| flatmap | 중간 연산 | Stream<T> | Function<T, Stream<T>> | T → Stream<R> |
| sorted | 중간 연산 | Stream<T> | Comparator<T> | (T, T) → int |
| anyMatch | 최종 연산 | boolean | Predicate<T> | T → boolean |
| noneMatch | 최종 연산 | boolean | Predicate<T> | T → boolean |
| allMatch | 최종 연산 | boolean | Predicate<T> | T → boolean |
| findAny | 최종 연산 | Optional<T> | | |
| findFirst | 최종 연산 | Optional<T> | | |
| forEach | 최종 연산 | void | Consumer<T> | T → void |
| collect | 최종 연산 | R | Collector<T, A, R> | |
| reduce | 최종 연산 | Optional<T> | BinaryOperator<T> | (T, T) → T |
| count | 최종 연산 | long | | |

## 5.6 실전 연습

1. 2011년에 일어난 모든 트랜잭션을 찾아 값을 오름차순으로 정리하시오.
2. 거래자가 근무하는 모든 도시를 중복 없이 나열하시오.
3. 케임브리지에서 근무하는 모든 거래자를 찾아서 이름순으로 정렬하시오.
4. 모든 거래자의 이름을 알파벳순으로 정렬해서 반환하시오.
5. 밀라노에 거래자가 있는가?
6. 케임브리지에 거주하는 거래자의 모든 트랜잭션값을 출력하시오.
7. 전체 트랜잭션 중 최댓값은 얼마인가?
8. 전체 트랜잭션 중 최솟값은 얼마인가?

### 실전 연습 데이터

```java
Trader raoul = new Trader("Raoul", "Cambridge");
Trader mario= new Trader("Mario", "Milan");
Trader alan = new Trader("Alan", "Cambridge");
Trader brian = new Trader("Brian", "Cambridge");

List<Transaction> transactions = Arrays.asList(
	new Transaction(brian, 2011, 300),
	new Transaction(raoul, 2012, 1000),
	new Transaction(raoul, 2011, 400),
	new Transaction(mario, 2012, 710),
	new Transaction(mario, 2012, 700),
	new Transaction(alan, 2012, 950)
);

public class Trader{
	private final String name; private final String city;
	public Trader(String n, String c){
		this.name = n;
		this.city = c;
	}
	public String getName(){
		return this.name;
	}
	public String getCity(){
		reutnr this.city
	}
	public String toString(){
		return "Trader:" + this.name + " in " + this.city;
	}
}

public clas Transaction {
	private final Trader trader;
	private final int year;
	private final int value;

	public Transaction(Trader trader, int year, int value) {
		this.trader = trader;
		this.year = year;
		this.value = value;
	}
	public Trader getTrader(){
		return this.trader;
	}
	public int getYear(){
		return this.year;
	}
	public int getValue(){
		return this.value;
	}
	public String toString(){
		return "{" + this.trader + ", " +
		"year: " + this.year+ ", " +
		"value: " + this.value+ "}";
	}
}
```

## 5.7 숫자형 스트림

```java
int calories = menu.stream().map(Dish::getCalories).reduce(0, Integer::sum);
```

이 코드를

```java
int calories = menu.stream().map(Dish::getCalories).sum(); //에러코드
```

처럼 `sum()`으로 **좀 더 간단하게** 바꿀 수 있는 방법이 없을까?

### 기본형 특화 스트림(primitive stream specialization)

**3가지** 기본형 특화 스트림을 제공한다.

스트림 API **박싱 비용**을 피할 수 있다.

- **IntStream**
- **DoubleStream**
- **LongStream**

각 인터페이스는 sum, max같이 자주 사용하는 **숫자** 관련 **리듀싱 연산** 수행 메서드를 **제공**한다.

필요할 때 다시 **객체 스트림**으로 **복원**하는 **기능**도 제공한다.

⭐ **특화 스트림**은 오직! **박싱 과정**에서 일어나는 **효율성과 관련**이 있다. 추가 기능은 제공 안한다.

### 숫자 스트림: 스트림 → 특화 스트림

스트림을 특화 스트림으로 변환할 때 사용하는 메서드 3가지

- **mapToInt**
- **mapToDouble**
- **mapToLong**

```java
int calories = menu.stream() //Stream<Dish> 반환
	.mapToInt(Dish::getCalories) //IntStream 반환
	.sum();
```

### 객체 스트림 복원

**숫자 스트림** 만든 후, **특화되지 않은 스트림**으로 **복원**

- **boxed** : 특화 스트림 → 일반 스트림

```java
IntStream instStream = menu.stream().mapToInt(Dish::getCalories); //스트림을 숫자 스트림으로 변환
Stream<Integer> stream = intStream.boxed(); // 숫자 스트림을 스트림으로 변환
```

### OptionalInt

IntStream에서 최댓값을 찾을 때, **기본값 0**때문에 **잘못된 결과** 도출 될 가능성이 있다.

이를 위한 **3가지 기본형 특화 스트림**을 제공한다.

- **OptionalInt**
- **OptionalDouble**
- **OptionalLong**

```java
Optional maxCalories = menu.stream().mapToInt(Dish::getCalories).max();
```

### 숫자 범위

**특정 범위의 숫자**를 **이용**해야 하는 경우가 자주 발생한다.

자바 8은 `IntStream`과 `LongStream`에서

`range`와 `rangeClosed`인 두 가지 정적 메서드를 제공한다.

- 첫 번째 인수: 시작값, 두 번째 인수: 종료값
- `range(1, 100)` = 1 < x < 100
- `rangeClosed(1, 100)` = 1 ≤ x ≤ 100

```java
IntStream evenNumbers = IntStream.rangeClosed(1, 100) // [1,100] 범위를 나타낸다
	.filter(n -> n%2 ==0);
System.out.println(evenNumbers.count()); // 1부터 100
```

### 스트림의 활용: 피타고라스 수

1.  세 수 표현하기: `new int[] {3, 4, 5}`로 표현 가능
2.  좋은 필터링 조합: `filter( b → Math.sqrt( a*a + b*b ) % 1 == 0 )`
3.  집합 생성

    ```java
    stream.filter( b -> Math.sqrt( a*a + b*b ) % 1 == 0 )
    	.map(b -> new int[]{ a, b, (int) Math.sqrt( a*a + b*b )});
    ```

4.  b값 생성
    **1차 시도**
        ```java
        IntStream.rangeClose(1,100) //IntStream
        	.filter( b -> Math.sqrt( a*a + b*b ) % 1 == 0 )
        	.boxed() //Stream<Integer>
        	.map(b -> new int[]{ a, b, (int) Math.sqrt( a*a + b*b )});
        ```

        IntStream의 map메서드는 스트림의 각 요소로 int가 반환될 것을 기대 → 우리가 원하는 것X



        **2차 시도**

        ```java
        IntStream.rangeClosed(1,100)
        	.filter(b -> Match.sqrt( a*a + b*b ) % 1 ==0)
        	.mapToObj(b -> new int[]{ a, b, (int)Match,sqrt( a*a + b*b) });
        ```

        개체값 스트림을 반환하는 IntStream의 mapTpObj 메서드
5.  a값 생성 및 최종 완성

    ```java
    Stream<int[]> pythagoreanTriples =
    	**IntStream.rangeClosed(1, 100).boxed().flatMap(a ->**
    		IntStream.rangeClosed(a, 100).filter(b -> Math.sqrt(a*a + b*b) %1 == 0)
    		.mapToObj(b -> new int[]{a, b, (int)Math.sqrt(a*a + b*b)})
    	);
    ```

## 5.8 스트림 만들기

스트림은 **데이터 처리 질의**를 하는 강력한 도구이다.

### Stream.of: 값으로 스트림 만들기

```java
Stream<String> stream = Stream.of("Modern", "java", "In", "Action");
stream.map(String::toUpperCase).forEach(System.out::println);
```

### System.getProperty & Stream.ofNullavle

: null이 될 수 있는 객체로 스트림 만들기

- System.getProperty: 제공된 키에 **대응하는 속성**이 없으면 **null을 반환**함.

```java
Stream<String> homeValueStream = Stream.ofNullable(System.getProperty("home"));
```

### Arrays.stream: 배열로 스트림 만들기

```java
int[] numbers = {2, 3, 5, 7, 11, 13};
int sum = Arrays.stream(numbers).sum();
```

### Files.lines: 파일로 스트림 만들기

```java
long uniqueWords = 0;
try(Stream<String> lines
	= Files.lines(Paths.get("data.txt"), Charset.defaultCharset())) {
	uniqueWords = lines.flatMap(line -> Arrays.stream(line.split(""))) //고유 단어 수
	.distinct()
	.count(); //단어 스트림 생성
}
catch(IOException e) {
	//파일 열기 예외 발생처리
}
```

### Stream.iterate & Stream.generate: 무한 스트림

**고정된 컬렉션**에서 **고정되지 않은 스트림**을 만들 수 있다.

보통 무한한 값을 출력하지 않도록 `**limit(n)`과 함께 사용\*\*한다.

- Stream.iterate()

```java
Stream.iterate(0, n -> n+2)
	.limit(10).forEach(System.out::println);
```

피보나치 수열

```java
Stream.iterate( new int[]{0,1},	t -> new int[]{t[0], t[0]+t[1]} )
	.limit(10).map(t -> t[0]).forEach(System.out::println);
)
```

- Stream.generate()

```java
Stream.generate(Math::random).limit(5).forEach(System.out::println);
```

# 6장 스트림으로 데이터 수집

스트림 = 데이터 집합을 멋지게 처리하는 게으른 반복자

### 스트림 연산

- **중간 연산**: 스트림 요소 소비X
- **최종 연산**: 스트림 요소 소비O

cf) 컬렉션(collection) ≠ 컬렉터(collector) ≠ 컬렉트(collect)

이번 장에서는 Stream에 toList 대신,
더 범용적인 **collector 파라미터**를 **collect 메서드**에 **전달**하여
**연산을 간결**하게 하는 법을 배운다.

```java
Map<Currency, List<Tranaction>> transactionsByCurrencies =
	transactions.stream().collect(groupingBy(Transaction::getCurrency));
```

이런식으로 하면 간결하게 이해할 수 있다!

## 6.1 Collector란 무엇인가?

### 함수형 프로그래밍

- **무엇**을 원하는지 직접 **명시 가능 →** 어떤 **방법**으로 얻을지 **신경 안써도 된다.**
- 함수형 프로그래밍에서는 **필요한 컬렉터**를 **쉽게 추가**할 수 있다.

### Collector: 고급 리듀싱 기능

- 함수형 API 장점: 높은 수준의 **조합성**과 **재사용성**
- collector 장점: **collect**로 **결과 수집 과정**을 **간단& 유연**하게 정의
- `stream().collect(~)`를 하면, **스트림 요소**에 **리듀싱 연산**이 **수행**됨
  `collect`는 **리듀싱 연산** 이용 → 스트림의 **각 요소**를 **방문**해서 컬렉터가 작업을 처리함
- **Collector 인터페이스**의 메서드 구현에 따라 **스트림에 리듀싱 연산** 수행을 **결정**한다.
- 가장 **많이 사용**하는 정적 메서드로는 `toList`가 있다.

```java
List<Transaction> transactions = transactionStream.collect(Collectors.toList());
```

### 미리 정의된 Collector

이번 장에서는, **Collectors 클래스**에서 제공하는 **팩토리 메서드** 기능을 설명한다.

1. 스트림 요소를 하나의 값으로 **리듀스 & 요약(summarization)** ⇒ **계산** 수행에서 유용함
2. 요소 **그룹화** (다양한 collector 조합하기)
3. 요소 **분할**(그룹화의 특별한 연산) ⇒ 프레디케이트를 그룹화 함수로 사용함

## 6.2 리듀싱과 요약

**collector 인스턴스**로 할 수 있는 **일**을 알아보자!

- collector로 **스트림** 항목 → **컬렉션**으로 하나의 결과로 합치기 가능!

ex) 메뉴에서 요리 수를 계산

```java
long howManyDishes = menu.stream().collect(Collectors.counting());
```

더 간단하게!

```java
long howManyDishs = menu.stream().count();
```

**cf)** import하여 `import static java.util.stream.Collectors.*;` 더 간단하게 쓰자!
이러면, `Collectors.counting()` → `counting()`으로 표현할 수 있다.

### 스트림에서 min, max 검색

- `Collectors.maxBy`
- `Collectors.minBy`

두 컬렉터는 **스트림 요소를 비교**하는데 사용할 **Comparator를 인수로** 받는다.

ex) 칼로리로 요리를 비교하는 **Comparator 구현**, 그 후 **Collectors.maxBy로 전달**

```java
//Comparator 구현
Comparator<Dish> dishCaloriesComparator
	= Comparator.comparingInt(Dish::getCalories);

//Collectors.maxBy로 전달
Optional<Dish> mostCalorieDish =
	menu.stream().collect(maxBy(dishCaloriesComparator));
```

### 요약 연산: summarizingInt

**<단순 합계>**

- **Collectors.summingInt**

: 특별한 **요약** 팩토리 메서드

: **객체를 int로 매핑**하는 함수(객체를 int로 매핑한 컬렉터 반환)를 **인수**로 받음

: summingInt가 collect 메서드로 전달되면 요약 수행

```java
int totalCalories = menu.stream().collect(summingInt(Dish::getCalories));
```

- **Collectors.summingLong**
- **Collectors.summingDouble**

**<평균 계산>**

- **Collectors.averagingInt**

```java
double avgCalories = menu.stream().collect(averagingInt(Dish::getCalories));
```

- **Collectors.averagingLong**
- **Collectors.averagingDouble**

**<두 개 이상 연산 수행>**

**하나의 요약 연산**으로 메뉴의 요소 **수**, 칼로리 **합계**, **평균**, **최댓값**, **최솟값** 계산

- **Collectors.summarizingInt**

```java
IntSummaryStatistics menuStatistics =
	menu.stream().collect(summarizingInt(Dish::getCalories));

/* menuStatistics 출력시
IntSummaryStatistics{ count=9, sum=4300, min=120,
											average=477.777778, max=800}
*/
```

- **Collectors.summarizingLong** : LongSummaryStatistics
- **Collectors.summarizingDouble** : DoubleSummaryStatistics

### 문자열 연결: joining

스트림의 각 객체에 `**toString**` 메서드를 **호출**해서 추출한 모든 문자열을 **하나의 문자열로 연결**해서 **반환**함

```java
String shortMenu = menu.stream().collect(joining());
/* 출력값
porkbeefchickenfrench friesriceseason fruitpixxaprawnssalmon
// 이렇듯 알아볼 수 없게 출력됨!
*/
```

Dish 클래스가 요리명을 반환하는 `toString`메서드 포함시, `map`생략 가능

**구분 문자열 넣기**

```java
String shortMenu = menu.stream().map(Dish::getName).collect(joining(", "));
/* 출력값
pork, beef, chicken, french fries, rice, season fruit, pizza, prawns, salmon
*/
```

### 범용 리듀싱 요약 연산: reducing

**지금까지** 살펴본 **모든 collector**는 **Collectors.reducing으로 구현**할 수 있다.

(그럼에도 **특화된 collector**를 사용한 이유는 **프로그래밍 편의성** 때문이다.)

- 메뉴의 모든 칼로리 합계 계산

```java
int totalCalories = menu.stream().collect(
	reducing(	0, Dish::getCalories, (i,j) -> i+j));
```

- reducing의 3개의 인수
  1. 리듀싱 연산 **시작값**
  2. 정수로 변환하는 **변환 함수**
  3. 두 항목을 하나의 값으로 더하는 **BinaryOperator**

한 개의 인수를 갖는 reducing

```java
Optional<Dish> monstCalorieDish =
	menu.stream().collect(reducing(
		(d1, d2) -> d1.getClories() > d2.getCalories() ? d1 : d2));
//Optional 사용하는 이유는 시작값이 설정되지 않은 상황을 대비하기 위해서다
```

### collect VS reduce

- 같은 기능을 구현할 수 있음
- **collect**: 도출하려는 **결과를 누적**하는 **컨테이너를 바꾸도록** 설계됨
- **reduce**: **두 값을 하나**로 도출하는 **불변형 연산**
- ⭐ 가변 컨테이너 관련 & 병렬성 확보 ⇒ collect 메서드로 리듀싱 연산 구현!

### Collection 유연성: 같은 연산도 다양하게 표현

reducing collector의 3번째 인수를 람다 표현식((i,j) -> i+j)대신,

- Integer클래스의 sum(Integer::sum) 참조시 단순화 가능

```java
int totalCalories = menu.stream().collect(
	reducing(	0, Dish::getCalories, Integer::sum));
```

- collector이용x 다른 방법으로 같은 연산 수행 가능함

```java
int totalCalories = menu.stream().map(Dish::getCalories).reduce(Integer::sum).get
```

- 스트림을 IntStream으로 매핑후, sum 메서드 호출

```java
int totlaCalories = menu.stream().mapToInt(Dish::getCalories).sum();
```

## 6.3 그룹화

**데이터 집합**을 하나 이상의 **특성으로 분류**해서 그룹화하기

- **Collectors.groupingBy**: 음식 메뉴별로 그룹화하기, **분류 함수**라고 부름(해당 함수를 기준으로 그룹화)

```java
Map< Dish.Type, List<Dish> > dishesByType =
	menu.stream().collect(groupingBy(Dish::getType));
/* 출력
{FISH=[prawns, salmon], OTHER=[french fries, rice, season fruit, pizza],
	MEAT=[pork, beef, chicken]}
*/
```

더 **복잡**한 **분류 기준** 적용: **람다 표현식**으로 로직 구현

```java
public enum CaloricLevel {DIET, NORMAL, FAT}

Map<CaloricLevel, List<Dish>> dishesByCaloricLevel = menu.stream().collect(
	groupingBy(dish -> {
		if(dish.getCalories() <= 400) return CaloricLevel.DIET;
		else if(dish.getCalories() <= 700) return CaloricLevel.NORMAL;
		else return CaloricLevel.FAT;
	}));
```

### 그룹화된 요소 조작

- 그룹화 이후, **그룹의 요소를 조작**하는 연산이 필요함: **groupingBy 내부**에 **filtering** 사용

ex) **500칼로리가 넘는 요리**만 필터링하기

```java
Map<Dish.Type, List<Dish>> caloricDishesByTypes =
	menu.stream().collect(groupingBy(Dish::getType,
		filtering( dish->dish.getCalories() > 500, toList())));
/* 출력
{OTHER=[french fries, pizza], MEAT=[pork, beef], FISH=[]}
```

⭐ 그룹화 이전에 filter 적용해서는 해결 할 수 없음, 맵에서 키가 사라지는 경우가 생길 수 있음

- **맵핑 함수**를 이용해 **요소를 변환**하는 작업: **groupingBy** 내부에 **mapping**과 **flatMapping** 사용

```java
Map<Dish.Type, List<String>> dishNameByType =
	menu.stream().collect(groupingBy(Dish::getType, mapping(Dish::getName, toList())));
```

이전과 달리 **각 그룹**이 **문자열 리스트**다. 그러면 **flatMap변환**을 수행 가능하다

```java
Map<Dish.Type, Set<String>> dishNamesByType=
	menu.stream().collect(groupingBy(Dish::getType,
		flatMapping(dish->dishTags.get( dish.getName() ).stream(), toSet())));
```

두 수준의 리스트를 **한수준으로 평면화**하기 위해 `flatMap`을 수행한다.

그리고 **집합**으로 **그룹화**해 **중복 태그를 제거함**에 주목하자

### 다수준 그룹화: 2가지 기준으로 동시에 그룹화

**바깥쪽 groupingBy** 메서드에 **내부 groupingBy**를 전달해서 **두 수준으로 그룹화**한다.

```java
Map<Dish.Type, Map<CaloricLevel, List<Dish>>> dishesByTypeCaloricLevel=
	menu.stream().collect(
		groupingBy(Dish::getType, // 첫 번째 수준의 분류 함수
			groupingBy(dish -> { // 두 번째 수준의 분류 함수
			if(dish.getCalories() <= 400)
				return CaloricLevel.DIET;
			else if(dish.getCalories() <= 700)
				return CaloricLevel.NORMAL;
			else return CaloricLevel.FAT;
			})
		)
);
/* 출력
	{MEAT={DIET=[chicken], NORMAL=[beef], FAT=[pork]},
	FISH={DIET=[prawn], NORMAL=[salmon]},
	OTHER={DIET=[rice, seasonal fruit], NORMAL=[french fries, pizza]}}
*/
```

결과적으로 **첫 번째 분류 함수**: MEAT, FISH, OTHER을 갖고,

**두 번째 분류 함수**: DIET, NORMAL, FAT을 갖는다.

정리하자면, **n수준 그룹화**는 **n 수준 트리 구조**로 표현되는 **n수준 맵**이 된다.

### 서브그룹으로 데이터 수집

```java
Map<Dish.Type, Long> typesCount = menu.stream().collect(
	groupingBy(Dish::getType, counting()));
/* 출력
	{MEAT=3, FISH=2, OTHER=4}
*/
```

- **컬렉터 결과를 다른 형식에 적용하기**: `collectingAndThen()`

ex) 각 서브그룹에서 가장 칼로리가 높은 요리 찾기

```java
Map<Dish.Type, Dish> mostCaloricByType =
	menu.stream().collect(groupingBy(Dish::getType, //분류 함수
		collectingAndThen(
			maxBy(comparingInt(Dish::getCalories)), //감싸인 컬렉터
		Optional::get))); //변환함수
/* 출력
	{FISH=salmon, OTHER=pizza, MEAT=pork}
*/
```

cf) 리듀싱 컬렉터는 절대 Optional.empty()를 반환하지X → 안전한 코드

- **groupingBy와 함께 사용하는 다른 컬렉터 예제**

```java
Map<Dish.Type, Integer> totalCaloriesByType=
	menu.stream().collect(groupingBy(Dish::getType, summingInt(Dish::getCalories)));
```

```java
Map<Dish.Type, Set<CaloricLevel>> caloricLevelsByType =
	menu.stream().collect(
		groupingBy(Dish::getType, mapping(dish -> {
			if(dish.getCalories() <= 400) return CaloricLevel.DIET;
			else if(dish.getCalories() <= 700) return CaloricLevel.NORMAL;
			else return CaloricLevel.FAT; },
		toSet()
	)));
```

`toCollection()` : 원하는 방식으로 결과 제어가능

```java
Map<Dish.Type, Set<CaloricLevel>> caloricLevelsByType =
	menu.stream().collect(
		groupingBy(Dish::getType, mapping(dish -> {
			if(dish.getCalories() <= 400) return CaloricLevel.DIET;
			else if(dish.getCalories() <= 700) return CaloricLevel.NORMAL;
			else return CaloricLevel.FAT; },
		toCollection(HashSet::new)
	)));
```

## 6.4 분할

**분할 함수(partitioning function)**를 분류함수로 사용하는 **특수한 그룹화**

```java
Map<Boolean, List<Dish>> partitionedMenu =
	menu.stream().collect(partitioningBy(Dish::isVegetarian)); //분할함수
/*출력
{false=[pork, beef, chicken, prawns, salmon],
true=[french fries, rice, season fruit, pizza]}
*/
```

### 분할의 장점

- **참, 거짓** 두 가지 요소의 스트림 리스트를 **모두 유지**한다
- **두 수준의 맵**을 구현할 수 있다
  ```java
  Map<Boolean, Map<Dish.Type, List<Dish>>> vegetarianDishesByType =
  	menu.stream().collect(
  	partitioningBy(Dish::isVegetarian,
  		groupingBy(Dish::getType)));
  /*출력
  {false={FISH=[prawns, salmon], MEAT=[pork, beef, chicken]},
  true ={OTHER=[french fries, rice, season fruit, pizza]}}
  */
  ```

### 숫자를 소수와 비수소로 분할하기

1. 소수판별 프레디케이트 구현하기

```java
public boolean isPrime(int candidate){
	return IntStream.range(2,candidate).noneMatch(i -> candidate % i == 0);
}
```

주어진 수의 제곱근 이하 수로 제한

```java
public boolean isPrime(int candidate){
	int candidateRoot = (int) Math.sqrt((double)candidate);
	return IntStream.rangeClosed(2, candidateRoot)
	.noneMatch( i -> candidate & i == 0);
}
```

1. isPrime = 프레디케이트, partitioningBy = 컬렉터

```java
public Map<Boolean, List<Integer>> partitionPrimes (int n) {
	return IntStream.rangeClosed(2,n).boxed()
	.collect(partitioningBy(candidate -> isPrime(candidate)));
}
```

### Collectors 클래스의 정적 팩토리 메서드

| 팩토리 메서드                                                                      | 반환 형식                                    | 사용 예제                                                                                           |
| ---------------------------------------------------------------------------------- | -------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| toList                                                                             | List<T>                                      | List<Dish> dishes = menuStream.collect(toList());                                                   |
| //스트림의 모든 항목을 리스트로 수집                                               |
| toSet                                                                              | Set<T>                                       | Set<Dish> dishes = menuStream.collect(toSet());                                                     |
| //스트림의 모든 항목을 중복이 없는 집합으로 수집                                   |
| toCollection                                                                       | Collection<T>                                | Collection<Dish> dishes = menuStream.collect(toCollection(), ArrayList::new);                       |
| //스트림의 모든 항목을 발행자가 제공하는 컬렉션으로 수집                           |
| counting                                                                           | Long                                         | int totalCalories = menuStream.collect(counting());                                                 |
| //스트림 항목 수 계산                                                              |
| summingInt                                                                         | Integer                                      | int totalCalories = menuStream.collect(summingInt(Dish::getCalories));                              |
| //스트림 항목의 정수를 더함                                                        |
| averagingInt                                                                       | Double                                       | double avgCalories = menuStream.collect(averagingInt(Dish::getCalories));                           |
| //스트림 항목의 정수의 평균값 계산                                                 |
| summarizingInt                                                                     | IntSummaryStatistics                         | IntSummaryStatistics menuStatistics = menuStream.collect(summarizingInt(Dish::getCalories));        |
| //스트림 항목의 최댓값, 최솟값, 합계, 평균 등 정보 통계                            |
| joining                                                                            | String                                       | String shortMenu = menuStream.map(Dish::getName).collect(joining(”, ”));                            |
| //스트림의 각 항목에 toString 메서드를 호출한 결과 문자열 연결                     |
| maxBy                                                                              | Optional<t>                                  | Optional<Dish> fattest = menuStream. collect(maxBy(comparingInt(Dish::getCalories)));               |
| //스트림 최댓값 요소를 Optional로 감싼 값 반환                                     |
| minBy                                                                              | Optional<T>                                  | Optional<Dish> lighttest = menuStream.collect(minBy(comparingInt(Dish::getCalories)));              |
| //스트림 최솟값 요소를 Optional로 감싼 값 반환                                     |
| reducing                                                                           | The type produced by the reduction operation | int totalCalories = menuStream.collect(reducing(0, Dish::getCalories, Integer::sum));               |
| //초기값에서 BinaryOperator로 스트림 요소를 반복적으로 합쳐 스트림을 하나로 리듀싱 |
| collectingAndThen                                                                  | The type produced by the reduction operation | int howManyDishes = menuStream.collect(collectingAndThen(toList(), List::size));                    |
| //다른 컬렉터를 감싸고 그 결과에 변환 함수 적용                                    |
| groupingBy                                                                         | Map<K, List<T>>                              | Map<Dish.Type, List<Dish>> dishesByType = menuStream.collect(groupingBy(Dish::getType));            |
| //하나의 프로퍼티값을 기준으로 그룹화, 기준은 키가 됨                              |
| partitioningBy                                                                     | Map<Boolean, List<T>>                        | Map<Boolean, List<Dish>> vegetarianDishes = menuStream.collect(partitioningBy(Dish::isVegetarian)); |
| //프레디케이트를 스트림 각 항목에 적용한 결과로 분할                               |

## 6.5 Collector 인터페이스

collector 인터페이스: **리듀싱 연산(컬렉터)**을 어떻게 **구현**할지 제공하는 **메서드 집합**으로 구성된다.

### toList()

- 가장 자주 사용하게 될 컬렉터이다.
- 가장 구현하기 쉬운 컬렉터이다.
- 이를 통해 Collector에 대해서 이해할 수 있다.

**Collector 인터페이스**

```java
public interface Collector<T, A, R>{
	Supplier<A> supplier();
	BiConsumer<A, T> accumulator();
	Function<A, T> finisher();
	BinaryOperator<A> combiner();
	Set<Characteristics> characteristics();
}
// T는 수집될 스트림 항목의 제네릭 형식이다
// A는 누적자, 중간 결과를 누적하는 객체 형식이다
// R은 수집 결과 객체의 형식(대게 컬렉션)이다.
```

### Collector 인터페이스의 메서드

1.  `supplier()`: **새로운** 결과 **컨테이너** 만들기
    수집 과정에서 빈 누적자 인스턴스를 만드는 파라미터가 없는 함수
        ```java
        public Supplier<List<T>> supplier() {
        	return ArrayList::new;
        }
        ```
2.  `accumulator()`: 결과 컨테이너에 **요소 추가**하기
    리듀싱 연산을 수행하는 함수를 반환한다
        ```java
        public Biconsumer<List<T>, T> accumulator(){
        	return List::add;
        }
        ```
3.  `finisher()`: **최종** 변환값을 결과 컨테이너로 적용하기
    스트림 탐색을 끝내고 누적 과정을 끝낼 때 호출할 함수를 반환한다.
        ```java
        public Function<List<T>, List<T>> finisher(){
        	return Function.identity();
        }
        ```

        사실! **위에 3개**만 있으면 **순차** 스트림 **리듀싱** 기능 **수행가능!**!

        그러나, collect이전의 **중간 연산**, **파이프라인**, **병렬 실행**을 고려해야 한다.

        따라서 아래의 2개 메서드도 필요하다.
4.  `combiner()`: 두 **결과** 컨테이너 **병합
    병렬 처리**시, **누적자**가 **결과**를 어떻게 **처리**할지 정의한다.
        ```java
        public BinaryOperator< List<T>> combiner() {
        	return (list1, list2) -> {
        		list1.addAll(list2);
        		return list1;
        	}
        }
        ```

        - 스트림의 **병렬 리듀싱** 수행 과정
        1. 스트림을 **재귀적**으로 **분할**한다.
        2. **모든 서브스트림**의 각 요소에 **리듀싱 연산**을 순차적으로 적용하여 **병렬 처리**하기
        3. 컬렉터의 **combiner**가 부분 **결과를 합친**다.
5.  `characteristics()`
    스트림을 **병렬 리듀스**할 것인지, **어떤 최적화**를 선택해야할지 **힌트**를 제공한다. - characteristics가 포함하는 열거형 1. **UNORDERED**: 리듀싱 결과는 순서에 영향받지 않는다. 2. **CONCURRENT**: 다중 스레드에서 accumulator 동시 호출 가능 3. **IDENTITY_FINISH**: 리듀싱 과정의 최종 결과로 누적자 개체를 바로 사용 가능

### 자신만의 커스텀 ToListCollector 구현하기

```java
public class ToListCollector<T> implements Collector<T, List<T>, List<T>> {
	@Override
	public Supplier<List<T>> supplier(){
		return ArrayList::new;
	}
	@Override
	public BiConsumer<List<T>, T> accumulator(){
		return List::add;
	}
	@Override
	public Function<List<T>, T> finisher(){
		return Function.identity();
	}
	@Override
	public BinaryOperator<List<T>> combiner(){
		return (list1, list2) -> {
			list1.addAll(list2);
			return list1;
		};
	}
	@Override
	public Set<Characteristics> characteristics() {
		return Collections.unmodifiableSet(EnumSet.of( IDENTITY_FINISH, CONCURRENT));
	}
}
```

자, 이제 자바에서 제공하는 API대신 우리가 만든 컬렉터를 사용할 수 있다!

```java
List<Dish> dishes = menuStream.collect(new ToListCollector<Dish>());
```

참고로 기존 코드는 다음과 같다.

```java
List<Dish> dishes = menuStream.collect(toList());
```

### Collector 인터페이스 구현하지 않고 커스텀하기

```java
List<Dish> dishes = menuStream.collect(
	ArrayList::new, //발행
	List::add, //누적
	List::addAll); //합침
```

## 6.6 커스텀 컬렉터를 구현해서 성능 개선하기

컬렉터는 컬렉터 수집 과정에서 **부분 결과**에 **접근할 수 없으**나, **커스텀 컬렉터**로 해결 가능하다.

**중간 결과 리스트**가 있으면 된다!

```java
public static boolean isPrime(List<Integer> primes, int candidate){
	return primes.stream().noneMatch(i -> candidate % i == 0);
}
```

최적화: 제곱근보다 작은 소수만 사용하도록 함

```java
public static boolean isPrime(List<Integer< primes, int candidate){
	int candidateRoot = (int) Math.sqrt((double) candidate);
	return primes.stream()
		.takeWhile( i -> i <= candidateRoot)
		.noneMatch( i -> candidate % i == 0 );
}
```

### 커스텀 컬렉터 만들기

1.  **Collector 클래스 시그니처 정의**

    ```java
    public interface Collector<T, A, R> //T: 스트림 요소, A: 누적자, R: 최종 결과
    ```

    우리는 지금 **누적자**와 **최종 결과**가 `Map<Boolean, List<Integer>>`인 컬렉터를 구현해야 함

    ```java
    public class PrimeNumbersCollector implements Collector<Integer, //스트림 요소
    	Map< Boolean, List<Integer> >, //누적자
    	Map< Boolean, List<Integer> >  //최종 결과
    >
    ```

2.  **리듀싱 연산 구현**
    Collector 인터페이스에 선언된 다섯 메서드
    (supplier, accumulator, finisher, combiner, characteristics) 구현 - supplier()
        ```java
        public Supplier< Map<Boolean, List<Integer>> > supplier(){
        	return () -> new HashMap<Boolean, List<Integer>>(){{
        		put(true, new ArrayList<Integer>());
        		put(false, new ArrayList<Integer>());
        	}};
        }
        ```

        누적자로 사용할 맵을 만들면서 true, false 키와 빈 리스트로 초기화를 했다.
        수집과정에서 각각 소수와 비소수를 추가할 것이다.

        - accumulator()

        ```java
        public BiConsumer< Map<Boolean, List<Integer>>, Integer> accumulator() {
        	return( Map<Boolean, List<Integer>> acc, Integer candidate) -> {
        		acc.get( isPrime(acc.get(true), candidate) ) //isPrime에 따라 소수와 비소수나눔
        			.add(candidate); // candidate를 알맞은 리스트에 추가
        	};
        }
        ```
3.  **병렬 실행할 수 있는 컬렉터 만들기**
    이번에는 누적자를 합칠 수 있는 메서드를 만든다. - combiner()
        ```java
        public BinaryOperator< Map< Boolean, List<Integer> > > combiner(){
        	return( Map< Boolean, List<Integer>> map1, Map< Boolean, List<Integer>> map2 ) -> {
        		map1.get(true).addAll(map2.get(true));
        		map1.get(false).addAll(map2.get(false));
        		return map1;
        	};
        }
        ```

        참고로 알고리즘 자체가 순차적이여서 컬렉터를 실제 병렬로 사용X, 학습 목적으로 구현한 것

        따라서 combiner는 호출될 일이 없다. → **빈 구현**으로 남겨두기 or **UnsupportedOperationException** 던지도록 구현하기
4.  **finisher 메서드와 컬렉터의 characteristics 메서드**

    - finisher()

    ```java
    public Function< Map< Boolean, List<Integer>>,
    	Map< Boolean, List<Integer> > > finisher() {
    		return Function.identity();
    }
    ```

    컬렉터 결과 형식과 같으므로 변환 과정 필요 X → 항등 함수 identity를 반환하도록 만듦

    - characteristics()

    ```java
    public Set<Characteristics> characteristics() {
    	return Collections.unmodifiableSet(EnumSet.of(IDENTITY_FINISH));
    }
    ```

    커스텀 컬렉터는 CONCURRENT도 아니고, UNORDERED도 아니지만, IDENTITY_FINISH이므로, characteristics 메서드는 위와 같다.

**위에서 만든 커스텀 컬렉터 적용하기**

```java
public Map<Boolean, List<Integer>> partitionPrimeWithCustomCollector(int n) {
	return IntStream.rangeClosed(2, n).boxed()
	.collect( new PrimeNumberCollector());
}
```

cf) 자바8에서 takeWhile 흉내내기

```java
public static <A> List<A> takeWhile(List<A> list, Predicate<A> p){
	int i =0;
	for( A item: list){
		if( !p.test(item) ){ //리스트의 아이템이 프레디케이트를 만족하는가?
			return list.subList(0, i); //만족X시, 현재 검사한 항목의 이전 항목 리스트 반환
		}
		i++;
	}
	return list;
}
```

그러나 자바9 에서와는 다르게 직접구현한 takeWhile 메소드는 적극적으로 동작한다.

가능하면 noneMatch와 동작과 조화를 이루도록 자바9에서 제공하는 takeWhile을 사용하자

### 커스텀 컬렉터 성능비교

```java
public class CollectorHarness {
	public static void main(String[] args) {
		long fastest = Long.MAX_VALUE;
		for(int i = 0; i < 10; i++ ){
			long start = System.nanoTime();
			partitionPrimes(1_000_000);
			long duration = (System.nanoTime() - start) / 1_000_000;
			if( duration < fastest) fastest = duration;
		}
		System.out.println( "Fastest execution done in "+ fastest + "msecs");
	}
}
```
