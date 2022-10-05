# Chapter 3

## 람다

### 람다의 특징

- 익명 : 이름이 없다.
- 함수 : 특정 클래스에 종속되어 있지 않음
- 전달 : 람다 함수를 값으로 전달가능하다.
- 간결성 : 코드가 길지 않다.

아래는 코드의 예시이다.

```java
@FunctionalInterfac // 함수형 인터페이스
interface Game{
	public abstract void play(); // 추상 메소드
}

// 블럭 형태의 람다식
Game game2 = () -> { 
    System.out.println("람다 표현식 1");
}
game2.play();

// 표현식 스타일의 람다식
Game game3 = () -> System.out.println("람다 표현식 2"); // 람다식을 전달할 수 있다.
game3.play();
```

### 함수형 인터페이스

위 코드의 예시가 함수형 인터페이스로 코드를 넘기는 방법이다.

함수형 인터페이스는 하나의 추상 메소드를 지정하는 인터페이스이다.

람다식을 넘김으로서 그 추상 메소드를 쉽고 적게 정의할 수 있다.

- Runnable / run()
- Consumer<T> / accept(T t)
- Comparator<T> / compare(T t1, T t2)
- Supplier<T> / get()
- Function<T, R> / apply(T t) **/ 데이터 맵핑용, int받고 String반환같은**
- Operator<T> / 얜 종류가 여러개임 **/ 연산용도, 임의의 타입 전달하고 임의의 타입 반환**
- Predicate<T> / test(T t) **/ 임의의 타입 받고 boolean형 반환**

### 함수 디스크립터, 시그니처

시그니처는 추상 메소드나 람다 표현식을 표현하는데 사용된다.

`( ) → System.out.println(”Hello World!”)` 는 `() -> void` 처럼 표현하거나

`(Apple1 ap1, Apple2 ap2) → ap1.weight - ap2.weight` 는 `(Apple, Apple) → int` 로 표현하거나

함수형 인터페이스의 추상 메소드와 사용할 람다 표현식의 시그니처가 같을 때 사용가능하다.

### 실행 어라운드 패턴

실제 작동을 처리하는 코드 기준 앞 뒤로 설정과 정리 두 과정이 둘러싸는 형태

```java
@FunctionalInterface
public interface BufferedReaderProcessor {
    String process(BufferedReader b) throws IOException;
}

public String processFile(BufferedReaderProcessor p) throws IOException {
		// try-with-resource
    try(BufferedReader br = new BufferedReader(new FileReader("data.txt"))) { 
        return p.process(br);
	}
}

String oneLine = processFile((BufferedReader br) -> br.readLine());
String twoLine = processFile((BufferedReader br) -> br.readLine() + br.readLine());
```

`processFile` 의 인자로 들어간 람다 `(BufferedReader br) -> br.readLine()` 는

함수형 인터페이스 `BufferedReaderProcessor` 의 메소드 process를 정의했다.

실행 어라운드 패턴에서는 실제 작동을 처리하는 코드에만 변경사항이 있으므로, `return p.process(br);` 의 작업 내용만 변경되면 된다.

그렇기에 위와 같이 구현한다.

### 함수형 인터페이스 - Predicate

```java
public <T> List<T> filter(List<T> list, Predicate<T> p) {
    List<T> results = new ArrayList<>();
    for(T t: list) {
        if(p.test(t)) {
            results.add(t);
        }
    }
    return results;
}

public void testPredicatae() {
    List<String> listOfStrings = new ArrayList<>();
		Predicate<String> nonEmptyStringPredicate = (String s) -> !s.isEmpty();
		List<String> noEmpty = filter(Arrays.asList("아나콘다", "", "하리파이버"), nonEmptyStringPredicate);
}
```

Predicate<T> / (T) → boolean / boolean test(T t);

test에서는 인스턴스 t로 부터 boolean 값을 얻어낼 수 있는 로직을 작성하면 된다.

### 함수형 인터페이스 - Consumer

```java
public <T> void forEach(List<T> list, Consumer<T> c) {
    for(T t: list) {
        c.accept(t);
    }
}

public void testConsumer() {
    forEach(Arrays.asList(1,2,3,4,5), (Integer i) -> System.out.println(i));
}
```

Consumer<T> / (T) → void / void accept(T t);

accept(T t)는 인스턴스 t로 부터 작업할 수 있는 내용을 정의한다. 리턴 값은 없다.

### 함수형 인터페이스 - Function

```java
public <T, R> List<R> map(List<T> list, Function<T, R> f) {
    List<R> result = new ArrayList<>();
    for(T t: list) {
        result.add(f.apply(t));
    }
    return result;
}

public void test() {
    List<Integer> l = map(Arrays.asList("lam", "da", "joayeou"), (String s) -> s.length());
}
```

Function<T, R> / (T) → R / R apply(T t)

R apply(T t)는 함수형 인터페이스 Function의 추상 메소드이며, 인스턴스 t를 인자로, 결과는 R로 리턴한다.

### 참조형, 기본형, 박싱, 언박싱

- 참조형 : Byte, Integer, Object, List
- 기본형 : int, double, byte, char
- 박싱 : 기본형 → 참조형
- 언박싱 : 참조형 → 기본형

Consumer<T>의 T는 참조형만 가능하기 때문에(구현에 의해 그렇다함)

박싱을 통해 기본형을 참조형으로 바꾸고 해야한다.

### 오토박싱 그리고 비용

```java
List<Integer> list = new ArrayList<>();
for(int i = 300; i < 400; ++i) {
		list.add(i); // int i가 Integer로 오토박싱된다.
}
```

그러나 위와 같은 방식에는 비용이 소모된다.

그래서 Predicate<T>를 사용해서 오토박싱 대신

박싱을 방지한 채로 Predicate를 해주는 타입 Predicate를 사용한다.

```java
// import java.util.function.IntPredicate;
IntPredicate evenNumbers = (int i) -> i % 2 == 0;
evenNumbers.test(1000);  // 박싱 없음

// import java.util.function.Predicate;
Predicate<Integer> oddNumbers = (Integer i) -> i % 2 != 0;
oddNumbers.test(1000)    // int -> Integer, 박싱 있음, 비용 소모
```

이 외에도 Consumer, Function 등 많은 함수형 인터페이스가 여럿 타입을 커버하기 위해 다양한 함수형 인터페이스가 존재한다.

p.105에서 표3-2 참조

### 람다 void 호환 규칙

```java
// Predicate는 boolean 반환값을 갖는다.
// (T) -> boolean
Predicate<String> p = s -> list.add(s);
// Consummer는 void 반환값을 갖는다.
// (T) -> void
// 그래도 boolean 값을 반환하는 람다식을 넣어도 된다. 
Consumer<String> b = s -> list.add(s);
```

### 자유 변수, 람다 캡처링

```java
public class LambdaCapture {
    public void printPort() {
        int portNumber = 1337;
        Runnable r = () -> System.out.println(portNumber);
				// portNumber = 3115; <- 여렇게 적는 순간 오류
    }
}
```

람다는 원칙적으로 외부 변수(`자유 변수`)를 사용할 수 없지만, 사용은 할 수 있다. `단` 할당 후 수정 불가능

위와 같이 외부 변수를 사용하는 것을 `람다 캡처링` 이라고 한다

## 메소드 참조

메소드 참조 == 람다의 축약형

다음 3가지 유형으로 구분 가능

- 1. 정적 메소드 참조
- 2.다양한 형식의 인스턴스 메소드 참조
- 3. 기존 객체의 인스턴스 매소드 참조

`2` 같은 경우는 `클래스::메소드` 의 형태고 `3` 같은 경우는 `인스턴스::메소드` 형태다

```java
// 2
String::length

// 3
Transaction t = new Transaction();
t::getValue
```

### 생성자 참조

```java
public void test() {
    List<Integer> weights = Arrays.asList(7, 3, 4, 10);
    List<Apple> apples = map(weights, Apple::new);
}

public List<Apple> map(List<Integer> list, Function<Integer, Apple> f) {
    List<Apple> result = new ArrayList<>();
    for(Integer i: list) {
        result.add(f.apply(i));
    }
    return result;
}
```

`Function<Integer, Apple> f = (Integer i) -> new Apple(i);` 이 `Function<Integer, Apple> f = Apple::new` 의 형태로 바뀌었다.

### 람다와 메소드 참조 활용: AppleComparator

```java
public static void main(String[] args) {
		List<Apple> inventory = new ArrayList<>();
		inventory.addAll(Arrays.asList(
		        new Apple(80),
		        new Apple(155),
		        new Apple(120)
		));

		// List<Apple>의 sort의 인자로는 Comparator<? super Apple> c 가 필요함
		inventory.sort(~~)
}
```

1. Comparator<Apple> 만들기

```java
public class AppleComparator implements Comparator<Apple> {
    @Override
    public int compare(Apple a1, Apple a2) {
        return a1.getWeight().compareTo(a2.getWeight());
    }
}

inventory.sort(new AppleComparator());
```

2. 익명 클래스

```java
inventory.sort(new Comparator<Apple>() {
    @Override
    public int compare(Apple a1, Apple a2) {
        return a1.getWeight().compareTo(a2.getWeight());
    }
});
```

3. 람다식

```java
inventory.sort((Apple a1, Apple a2) -> a1.getWeight().compareTo(a2.getWeight()));
// 형식 추론
inventory.sort((a1, a2) -> a1.getWeight().compareTo(a2.getWeight()));
```

4. 메소드 참조

```java
Comparator<Apple> c = Comparator.comparing((a1) -> a1.getWeight());
// Comparator.comparing은 비교 시 키를 어떻게 추출할 것인지 지정하는 메소드.

inventory.sort(c);
// or
inventory.sort(Comparator.comparing(Apple::getWeight));
```
### Comparator 유용하게 사용하기

```java
// 정렬할 key 값 지정하기
inventory.sort(Comparator.comparing(Apple::getWeight));

// 역정렬, 무게를 내림차순으로 정렬
inventory.sort(Comparator.comparing(Apple::getWeight)
													.reversed());

// 처음 key 값이 같을 때 비교할 두번째 key 값 지정하기
inventory.sort(Comparator.comparing(Apple::getWeight)
													.thenComparing(Apple::getCountry)); 
```

### Predicate 유용하게 사용하기

```java
Predicate<Apple> redApple = Apple::isRedApple

// not 연산
Predicate<Apple> notRedApple = redApple.negate();

// and 연산
Predicate<Apple> redAppleAndHeavy = redApple.and((apple) -> apple.getWeight() > 150);

// or 연산
Predicate<Apple> redAppleOrHeavy = redApple.or((apple) -> apple.getWeight() > 150);
```

### Function 유용하게 사용하기

```java
Function<Integer, Integer> f = x -> x + 1;

// 합성함수
Function<Integer, Integer> h = f.andThen(g);

// 반대로 합성함수
Function<Integer, Integer> h = f.compose(g);
```