# 람다 표현식 (Chapter 3)

## 람다란 무엇일까?

메서드로 전달할 수 있는 익명 함수를 **단순화** 한 것.

### 람다의 특징

- 익명
    
    보통의 메서드와는 달리, **이름이 없다**. 코드에 대한 걱정거리가 줄어든다.
    
- 함수
    
    메서드와는 달리, 특정 **클래스에 종속되어 있지 않아**서 함수이다. 하지만 메서드와 기능은 동일하다. (파라미터리스트, 반환형식 등…)
    
- 전달
    
    람다 표현식을 **메서드 인수로 전달**하거나, **변수로 저장**할 수 있다.
    
- 간결성
    
    익명 클래스와는 달리, **깔끔하게 코드를 작성**할 수 있다.
    

익명 클래스로 작성한 코드

```java
Comparator<Apple> byWeight = new Comparator<Apple>() {
      public int compare(Apple a1, Apple a2) {
        return a1.getWeight().compareTo(a2.getWeight());
      }
};
```

람다를 이용해 작성한 코드

```java
Comparator<Apple> byWeight = 
			(Apple a1, Apple a2) -> a1.getWeight().compareTo(a2.getWeight());
```

→ 무게를 비교하는데 필요한 **코드를 전달**하고 있다.

### 람다의 구성

- 파라미터 리스트 (메서드 파라미터)
- 화살표 ( → )
- **람다 바디 (람다의 반환값)**

## 어디에 어떻게 람다를 사용하는가?

함수형 인터페이스라는 문맥에서 사용.

### 함수형 인터페이스란

정확히 **하나의 추상 메서드**를 지정하는 인터페이스.

```java
public interface Predicate<T>{
	boolean test(T t);
}
public interface Runnable{
	void run();
}
```

람다 표현식으로 **함수형 인터페이스의 추상 메서드 구현을 직접 전달**하는 것이 가능 = **전체 표현식을 함수형 인터페이스의 인스턴스**로 취급할 수 있다.

예시는 아래와 같다.

```java
Runnable r1 = () -> System.out.print("Hello World!");
```

### 함수 디스크립터

함수형 인터페이스의 **추상 메서드 시그니처** = **람다 표현식의 시그니처 (둘은 일치해야한다.)**

람다 표현식의 시그니처를 서술하는 메서드 = 함수 디스크립터

- 위 `Runnable` 인터페이스의 추상 메서드 `run`은 **인수와 반환값이 없**으므로, `Runnable` 인터페이스는 **인수와 반환값이 없는 시그니처**이다.

## 실행 어라운드 패턴(람다 활용)

고정된 **설정**과 **정리 과정**을 둔다.

실제 자원을 처리하는 코드를 설정과, 정리 두 과정이 둘러싸는 형태를 갖는다.

## 함수형 인터페이스 사용

`java.util.function` 패키지로 여러가지 새로운 함수형 인터페이스를 제공

### Predicate

`test` 라는 추상 메서드를 정의한다. `test` 는 제네릭 형식의 `T`의 객체를 인수로 받아 `boolean`을 반환한다. T 형식의 객체를 사용하는 `불리언 표현식`이 필요한 상황에서 사용 가능하다.

### Consumer

`accept`라는  추상 메서드를 정의한다. 제네릭 형식 `T` 객체를 받아서 `void`를 반환한다.

 `T` 형식의 객체를 인수로 받아, 어떤 동작을 수행하고 싶을 때 사용 가능하다.

### Function

`apply`라는 추상 메서드를 정의한다. 제네릭 형식 `T` 를 인수로 받아서 제네릭 형식의 `R` 객체를 반환한다. 입력을 출력으로 매핑하는 람다를 정의할 때 사용 가능하다.

### 기본형 특화

제네릭은 **참조형 타입**만 지정할 수 있다.

- 박싱
    
    기본형(int,double,byte,char..)을 참조형(Integer,Byte,Object,List…)로 변환하는 기능
    
- 언박싱
    
    박싱의 반대.
    
- 오토박싱
    
    박싱과 언박싱이 자동으로 이루어지는 기능
    
    **비용이 매우 크다.**
    

자바 8에 추가된 함수형 인터페이스는 기본형을 입출력으로 사용하는 상황에서 **오토박싱 동작을 피할 수 있도록** **기본형에 특화된 버전의 함수형 인터페이스**를 제공한다.

Ex) IntPredicate, LongConsumer, BooleanSupplier …

## 람다의 형식 검사, 형식 추론, 제약

### 형식 검사

**대상 형식** 이란?

- 람다가 전달될 메서드 파라미터나 람다가 할당되는 변수 등.. 에서 기대되는 **람다 표현식의 형식**

람다의 **형식 검사 과정**

```java
List<Apple> heavierThan150g = filter(inventory,(Apple apple) -> apple.getWeight() > 150);
```

- 메서드의 선언 확인
- 두 번째 파라미터로 대상 형식을 기대한다.
- 대상 형식이 Predicate<Apple> 이라면, test 라는 한 개의 추상 메서드를 정의할 것이다.
- test 메서드는 Apple 을 받아 boolean을 반환하는 함수 디스크립터를 묘사한다.
- 메서드로 전달된 인수는 이와 같은 요구사항을 만족해야 한다.

### 같은 람다, 다른 함수형 인터페이스

**대상 형식**이라는 특징 때문에, 같은 람다 표현식이더라도 **호환되는 추상 메서드**를 가진 다른 함수형 인터페이스로 사용될 수 있다.

**하나**의 람다 표현식을 **다양한** 함수형 인터페이스에 사용할 수 있다.

```java
Callable<Integer> c = () -> 42;
PrivilegedAction<Integer> p = () -> 42;
```

### 형식 추론

자바 컴파일러는 람다 표현식이 사용된 대상 형식을 이용해서, 람다 표현식과 관련된 함수형 인터페이스를 추론한다. 즉, **함수 디스크립터**를 알 수 있으므로 **컴파일러**는 **람다의 시그니처도 추론**하는 것이 가능하다. 결과적으로 컴파일러는 **람다 표현식의 파라미터 형식에 접근**이 가능하다. 즉, **람다 파라미터 형식을 추론**할 수 있다!

```java
List<Apple> greenApples = filter(inventory, apple -> GREEN.equals(apple.getColor()));
```

위 코드는 파라미터 형식을 명시적으로 지정하지 않았다. 

하지만 때로는, 파라미터 형식을 포함하는 것이 좋을 때도 있다. 개발자 스스로 결정해야 하는 부분이다.

### 지역 변수 사용

람다 표현식에서도 자유변수(파라미터로 넘겨진 변수가 아닌 외부에서 정의된 변수) 활용 가능하다.

람다 **캡쳐링**

- 자유 변수 활용하는 것
- **한 번만** 할당할 수 있는 지역 변수를 캡처할 수 있다.( `final` 로 선언되거나, 실질적으로 `final`처럼 취급되어야 한다.)

### 지역 변수의 제약 이유

인스턴스 변수

- 힙에 저장

지역 변수

- 스택에 저장

람다가 지역 변수에 바로 접근이 가능하다면, 그리고 그 람다가 스레드에서 실행된다면 변수를 할당한 스레드가 사라져서 변수 할당이 해제되었는데도 해당 스레드에서 해당 변수에 접근하려 할 수 있다. 따라서, 자바 구현에서는 **자유 지역 변수의 복사본을 람다에게 제공**한다. 그러므로, **복사본의 값이 바뀌지 않아야** 하므로, **지역 변수에는 한 번만 값을 할당**해야 한다.

## 메서드 참조

- **기존의 메서드 정의를 재활용**해서 람다처럼 전달할 수 있다.
- 가독성을 높일 수 있다.
- 메서드 명 앞에 구분자(::)를 붙이는 방식으로 메서드 참조를 활용할 수 있다.
- 실제로 메서드를 호출하는 것은 아니므로, 괄호는 필요 없다.

**메서드 참조의 세가지 유형**

- 정적 메서드 참조
    
    Ex) Integer::parseInt
    
- 다양한 형식의 인스턴스 메서드 참조
    
    Ex) String::length
    
- 기존 객체의 인스턴스 메서드 참조
    
    Ex) Apple::getWeight
    

### 생성자 참조

`ClassName::new` 처럼 클래스 명과 `new` 키워드를 이용해서 기존 생성자의 참조를 만들 수 있다.

```java
Supplier<Apple> c1 = Apple::new;
Apple a1 = c1.get(); // 새로운 Apple 객체를 만들 수 있다.
```

## 람다, 메서드 참조 활용하기

### 1단계: 코드  전달

```java
public class AppleComparator implements Comparator<Apple>{
	public int compare(Apple a1, Apple a2){
		return a1.getWeight().compareTo(a2.getWeight());
	}
}
inventory.sort(new AppleComparator());
```

sort 의 동작은 파라미터화 되었다.

### 2단계: 익명 클래스 활용

```java
inventory.sort(new Comparator<Apple>() {
	public int compare(Apple a1, Apple a2){
		return a1.getWeight().compareTo(a2.getWeight());
	}
});
```

### 3단계: 람다 표현식 사용

```java
inventory.sort((Apple a1, Apple a2) -> a1.getWeigt().compareTo(a2.getWeight()));

invectory.sort((a1,a2) -> a1.getWeigt().compareTo(a2.getWeight()));

Comparator<Apple> c = Comparator.comparing((Apple a) -> a.getWeight());

inventory.sort(comparing(apple -> apple.getWeight());
```

### 4단계: 메서드 참조 사용

```java
inventory.sort(comparing(Apple::getWeight));
```

## 람다 표현식을 조합할 수 있는 유용한 메서드

### Comparator 조합

정적 메서드 Comparator.comparing 이용

```java
Comparator<Apple> c = Comparator.comparing(Apple::getWeight);
```

역정렬도 가능하다.

```java
inventory.sort(comparing(Apple::getWeight).reversed());
```

### Predicate 조합

복잡한 프레디케이트를 만들 수 있도록 negate, and, or 세 가지 메서드 제공

```java
Predicate<Apple> notRedApple = redApple.negate();
Predicate<Apple> redAndHeavyApple = redApple.and(apple->apple.getWeight() > 150)
																						.or(apple->GREEN.equals(a.getColor()));
```

### Function 조합

Function 인스턴스를 반환하는 두 가지 디폴트 메서드를 제공한다.

- andThen
    
    주어진 함수를 먼저 적용한 결과를 다른 함수의 입력으로 전달하는 함수
    
- compose
    
    인수로 주어진 함수를 먼저 실행한 다음에 그 결과를 외부 함수의 인수로 제공
    

```java
Function<Integer, Integer> f = x -> x + 1;
Function<Integer, Integer> g = x -> x *2;
Function<Integer, Integer> h = f.andThen(g);
```

```java
Function<Integer, Integer> f = x -> x + 1;
Function<Integer, Integer> g = x -> x *2;
Function<Integer, Integer> h = f.compose(g);
```

단순한 람다 표현식을 조합해서 더 복잡한 람다 표현식을 만들어내고 있음에도 불구하고, 코드 자체가 문제를 잘 설명한다는 점이 대단하다.
  
# 스트림 소개 (Chapter 4)
대부분의 자바 애플리케이션에는 컬렉션을 많이 사용하지만, 완벽한 컬렉션 관련 연산을 지원하려면 한참 멀었다.

- SQL 질의 언어에서는, 우리가 기대하는 것이 무엇인지 직접 표현할 수 있다.
- SQL 에서는 **질의를 어떻게 구현해야 할지 명시할 필요가 없다.**
- 컬렉션도 이와 비슷한 기능을 만들 수 있지 않을까?
- 많은 요소를 포함하는 거대한 컬렉션은 어떻게 처리해야 할까?
- **성능을 높이려면 멀티코어 아키텍처를 활용해서 병렬로 컬렉션의 요소를 처리**해야 한다.

## 스트림이란 무엇인가?

- **선언형**(데이터를 처리하는 임시 구현 코드 대신 질의로 표현)으로 컬렉션 데이터를 처리할 수 있다. (간결하고 가독성이 좋아진다.)
- 선언형 코드와, 동작 파라미터화를 활용하면 **유연성이 좋아진다.**
- **멀티스레드 코드를 구현하지 않아도** 데이터를 투명하게 **병렬로 처리**할 수 있다. (성능이 좋아진다)

## 스트림 시작하기

스트림이란 **데이터 처리 연산**을 지원하도록 **소스**에서 추출된 **연속된 요소**이다.

- 연속된 요소
    
    특정 요소 형식으로 이루어진 **연속된 값 집합의 인터페이스 제공**
    
- 소스
    
    컬렉션,배열,I/O 자원 등의 데이터 제공 소스로부터 데이터를 소비한다. **정렬된 컬렉션으로 스트림을 생성하면 정렬이 그대로 유지**된다.
    
- 데이터 처리 연산
    
    일반적으로 지원하는 연산 + **데이터베이스와 비슷한 연산**을 지원한다.
    
    Ex) filter, map, find, sort… 등으로 데이터 조작이 가능하다.
    
- 파이프라이닝
    
    스트림 연산끼리 연결해서 커다란 파이프라인을 구성할 수 있도록 자신을 반환한다.
    
- 내부 반복

예제 코드

```java
List<String> threeHighCaloricDishNames = 
	menu.stream() // 스트림을 얻는다.
		.filter(dish -> dish.getCalories() > 300) // 파이프라인 연산 만들기. 필터링한다.
		.map(Dish::getName) // 요리명 추출
		.limit(3) // 선착순 3개만 선택
		.collect(toList()); // 결과를 다른 리스트로 저장
```

## 스트림과 컬렉션

컬렉션

- 현재 자료구조가 포함하는 모든 값을 메모리에 저장하는 자료구조.
- 컬렉션의 모든 요소는 컬렉션에 추가하기 전에 계산되어야 한다.
- 적극적 생성(모든 값을 계산할 때 까지 기다린다)

스트림

- 요청할 떄만 요소를 계산하는 고정된 자료구조.
- 사용자가 요청하는 값만 스트림에서 추출한다.
- 게으르게 만들어지는 컬렉션과 같다.
- 사용자가 요청할 때만 값을 계산한다.
- 게으른 생성(필요할 때만 값을 계산한다)

### 딱 한번만 탐색할 수 있다.

스트림도 딱 한번만 탐색할 수 있다. 탐색된 스트림의 요소는 소비된다.

### 외부 반복과 내부 반복

스트림은 컬렉션과 달리, **내부반복** (반복을 알아서 처리하고 결과 스트림값 어딘가에 저장해주는)을 사용한다.

- 반복 과정을 신경쓰지 않아도 된다.
- 반복을 숨겨주는 연산 리스트(filter,map…)가 미리 정의되어 있어야 한다.

## 스트림 연산

### 중간 연산

```java
List<Stirng> names = menu.stream() 
		.filter(dish -> dish.getCalories > 300)
		.map(Dish::getname)
		.limit(3)
		.collect(toList());
```

- filter, sorted 같은 중간 연산은 다른 스트림을 반환한다. → 여러 중간 연산을 연결해서 질의를 만들 수 있다.
- 단말 연산을 스트림 파이프라인에 실행하기 전까지는 아무 연산도 수행하지 않는다( 게으르다!! )
- 중간 연산을 합친 다음에 합쳐진 중간 연산을 최종 연산으로 한번에 처리한다.

**스트림의 게으른 특성 덕분에 얻는 최적화 효과**

- **쇼트 서킷**
    - 모든 연산을 다 해보기 전에 조건을 만족하면 추가적인 불필요한 연산은 하지 않는다.
    - 위 예제에서는 limit 연산이 쇼트 서킷 연산에 해당한다.
- **루프 퓨전**
    - 서로 다른 연산 과정(filter 나 map 같은)을 한 과정으로 병합하는 기법

### 최종 연산

- 스트림 파이프라인에서 결과를 도출한다.
- 보통 최종 연산에 의해 List, Integer, void 등 **스트림 이외의 결과가 반환된다.**
