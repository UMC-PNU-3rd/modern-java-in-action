

# **Chapter 1**

## **자바 8, 9, 10, 11 ： 무슨 일이 일어나고 있는가**

자바는 처음부터 많은 유용한 라이브러리를 포함하는 잘 설계된 객체지향 언어로 시작했다. 코드를 JVM 바이트 코드로 컴파일하는 특징 때문에 자바는 인터넷 애플릿 프로그램의 주요 언어가 되었다. JVM의 업데이트 덕에 JVM에서 실행되는 경쟁 언어는 더욱 부드럽게 실행될 수 있으며, 자바와 상호 동작할 수 있게 되었다. 또한 자바는 다양한 임베디드 컴퓨팅 분야도 장악하고 있었다.

하지만 프로그래밍 언어 생태계에 변화의 바람이 불었다. 프로그래머는 빅데이터라는 도전에 직면하면서 멀티코어 컴퓨터나 컴퓨팅 클러스터를 이용해서 빅 데이터를 효과적으로 처리할 필요성이 커졌다. 멀티코어 CPU의 대중화와 같이 하드웨어도 비약적으로 발전하였다.따라서 병렬 프로세싱을 활용해야 하는데 이전의 자바로는 충분히 대응할 수 없었다. 또한 새로운 언어가 등장하면서 진화하지 않은 기존 언어는 사장된다. 따라서 자바는 끊임없이 발전해야 했다.

그렇기에 자바는 변화하였다. 자바 8은 더 다양한 프로그래밍 도구 그리고 다양한 프로그래밍 문제를 더 빠르고 정확하며 쉽게 유지 보수할 수 있다는 장점을 제공한다. 자바 8에 추가된 기능은 자바에 없던 완전히 새로운 개념이지만 현 시장에 요구하는 기능을 효과적으로 제공한다.

<aside>
💡  자바 8은 간결한 코드, 멀티코어 프로세서의 쉬운 활용이라는 두 가지 요구사항을 기반으로 한다!

</aside>

## 자바 8의 **세 가지 프로그래밍 개념**

메서드와 람다를 일급 시민으로 ~> 언급하고 넘어가기

### 스트림 처리 (Stream API)

간단하게 조립 라인처럼 어떤 항목을 연속으로 제공하는 기능

기존에는 한번에 한 항목을 처리했지만 작업을(데이터베이스 질의처럼) 고수준으로 추상화해서 일련의 스트림으로 만들어 처리한다!

→  따라서 스트림을 사용하면 스레드라는 복잡한 작업을 사용하지 않으면서 공짜로 **병렬**성을 얻을 수 있다.

### 동작 파라미터화(behavior parameterization)로 메서드에 코드 전달하기

코드 일부를 API로 전달 → 동작 파라미터화(behavior parameterization)

Java 8 이전의 복사 및 붙여넣기를 하는 기법에 비해 프로그램이 짧고 간결해지며, 불필요한 에러도 줄일 수 있다.

### 병렬성과 공유 가변 데이터

기존의 멀티 코어를 이용하는 방법(스레드를 활용)은 매우 위험했다. 웬만한 전문가가 아니라면 사용하기 어려웠다.

자바 8 스트림을 이용하면 기존의 자바 스레드 API보다 쉽게 병렬성을 활용할 수있다!

<aside>
💡 고전적인 객체지향에서 벗어나 함수형 프로그래밍으로 다가섰다는 것이 자바 8의 가장 큰 변화이다.

</aside>

## **자바 함수**

자바 8에서 함수 사용법은 일반적인 프로그래밍 언어의 함수 사용법과 아주 비슷하다. 프로그래밍 언어의 핵심은 **값을 바꾸는 것**이다. 전통적으로 프로그래밍 언어에서는 이 값을 일급(first-class) 값(또는 시민)이라고 부른다. 이전까지 자바 프로그래밍 언어에서는 기본값, 인스턴스만이 일급 시민이었다. 메서드와, 클래스는 이 당시 일급 시민이 아니었는데, 런타임에 메서드를 전달할 수 있다면, 즉 메서드를 일급 시민으로 만들면 프로그래밍에 유용하게 활용될 수 있다. 자바 8 설계자들은 이급 시민을 일급 시민으로 바꿀 수 있는 기능을 추가했다.

### **메서드 참조 (method reference) - 이 메서드를 값으로 이용하라**

동작의 전달을 위해 익명 클래스를 만들고 메서드를 구현해서 넘길 필요 없이, 준비된 함수를 메서드 참조 ::를 이용해서 전달할 수 있다. 아래 예제를 통해 자바 8에서는 더 이상 메서드가 이급값이 아닌 일급값인것을 확인할 수 있다.

- 익명 클래스

```
File[] hiddenFiles = new File(".").listFiles(new FileFilter() {
    public boolean accept(File file) {
        return file.isHidden();
    }
});
```

- 메서드 참조

```
File[] hiddenFiles = new File(".").listFiles(File::isHidden);
```

### **람다 : 익명 함수**

자바 8에서는 (기명 named)메서드를 일급값으로 취급할 뿐 아니라 **람다(anonymous function)**를 포함하여 함수도 값으로 취급할 수 있다.

### 메서드 전달에서 람다로!

함수형 인터페이스 → 익명 메서드 → 람다 → 메서드 참조

이것만 잘 이해하면 된다!

## **스트림**

스트림 API 이전 컬렉션 API를 이용하여 다양한 로직을 처리하였을 것이다. 스트림 API를 이용하면 컬렉션 API와는 상당히 다른 방식으로 데이터를 처리할 수 있다.

### 외부 반복(External Iteration)

컬렉션 API를 사용하 for-each 루프를 이용하여 각 요소를 반복하면서 작업을 수행하는 방식을 외부 반복이라고 한다.

### 내부 반복(Internal Iteration)

스트림 API에서는 라이브러리 내부에서 모든 데이터가 처리된다. 이와 같은 반복을 내부 반복이라고 한다.

## **멀티스레딩**

기존 스레드 API로 멀티스레딩 코드를 구현해서 병렬성을 이용하는것은 쉽지 않다. 자바 스트림 API는 *컬렉션을 처리하면서 발생하는 모호함과 반복적인 코드 문제*, 그리고 *멀티코어 활용 어려움*이라는 두 가지 문제를 모두 해결했다. 내부적으로 fork-join 방식을 사용한다.

fork-join이란? 쉽게 병렬적으로 작업을 처리하고 합치는 방식?

## **디폴트 메서드와 자바 모듈**

기존 자바 기능으로는 대규모 컴포넌트 기반 프로그래밍 그리고 진화하는 시스템의 인터페이스를 적절히 대응하기 어려웠다. 자바 8에서 지원하는 **디폴트 메서드**를 이용해 기존 인터페이스를 구현하는 클래스를 바꾸지 않고도 인터페이스를 변경할 수 있다. → Java Collection 예시 들기

예를 들어 List/나 Collection/ 인터페이스는 이전에 stream이나 parallelStream 메서드를 지원하지 않았다. 하지만 자바 8에서 Collection 인터페이스에 stream메서드를 추가하고 이를 디폴트 메서드로 제공하여 기존 인터페이스를 쉽게 변경할 수 있었다.

# **Chapter 2 - 동작 파라미터화 코드 전달하기**

동작 파라미터화에는 메서드 내부적으로 다양한 동작을 수행할 수 있도록 코드를 메서드 인수로 전달한다. 이 동작 파라미터화를 이용하면 자주 바뀌는 요구사항에 효과적으로 대응할 수 있다.

## **동작 파라미터화 방법**

코드 전달 기법을 이용하면 동작을 메서드의 인수로 전달할 수 있다. 하지만 자바 8 이전에는 코드를 지저분하게 구현해야 했다. 익명 클래스로도 어느 정도 코드를 깔끔하게 만들 수 있지만 자바 8에서는 인터페이스를 상속받아 여러 클래스를 구현해야 하는 수고를 없앨 수 있는 방법을 제공한다.

- 선택 조건을 결정하는 (전략)인터페이스 선언(Predicate)

```java
public interface ApplePredicate {
	boolean test(Apple apple);
}
```

- 클래스를 통한 동작 파라미터화

```java
public class AppleGreenColorPredicate implements ApplePerdicate {
	public boolean test(Apple apple) {
		return GREEN.equals(apple.getColor());
	}
}

List<Apple> greenApples = filterApples(inventory, new AppleGreenColorPredicate());
```

- 익명 클래스를 통한 동작 파라미터화

```java
List<Apple> greenApples = filterApples(inventory, new AppleGreenColorPredicate() {
		public boolean test(Apple apple) {
			return GREEN.equals(apple.getColor());
		}
});
```

- 람다를 통한 동작 파라미터화

```java
List<Apple> greenApples
	 = filterApples(inventory, (Apple apple) -> GREEN.equals(apple.getColor()));
```

<aside>
💡 동작 파라미터화 방식이 변해가는 과정을 잘 이해해야 한다! 한개의 파라미터, 다양한 동작!!

</aside>

## 실전 예제 - Comparator

```java
// ex) java.util.Comparator
public interface Comparator<T> {
	int compare(T o1, T o2)l
}
```

```java
// anonymous class
inventory.sort(new Comparator<Apple>() {
	public int compare(Apple a1, Apple a2) {
		return a1.getWeight().compareTo(a2.getWeight());
	}
});
```
``` java
// lamda
inventory.sort(
	(Apple a1, Apple a2) -> a1.getWeight().compareTo(a2.getWeight()));`
```