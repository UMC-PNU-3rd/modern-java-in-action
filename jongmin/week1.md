# 1단원

## 자바 역사상 가장 큰 변화 : 자바8

### 자바8이 기반으로 하는 요구사항

- 간결한 코드
- 멀티코어 프로세서의 쉬운 활용

### 자바8에서 제공하는 새로운 기술

- 스트림API
- 메서드에 코드를 전달하는 기법
- 인터페이스의 디폴트 메서드

---

- 데이터베이스 질의 언어에서 표현식을 처리하는 것처럼 병렬 연산을 지원하는 스트림이라는 새로운 API를 제공한다.
- 스트림을 이용하면 에러를 자주 일으키며 멀티코어 CPU를 이용하는 것보다 비용이 훨씬 비싼 키워드 synchronized를 사용하지 않으면 된다
- 자바8에 추가된 스트림 API 덕분에 다른 두 가지 기능, 즉 메서드에 코드를 전달하는 간결기법(메서드 참조와 람다)과 인터페이스의 디폴트 메서드가 존재할 수 있음

### 스트림 처리

- 한 번에 한 개씩 만들어지는 연속적인 데이터 항목들의 모임
- 이론적으로 프로그램은 입력 스트림에서 데이터를 한개씩 읽어드리며 출력 스트림으로 데이터를 한 개씩 기록
- 기존에는 한 번에 한 항목을 처리했지만, 자바8은 작업을 ( DB 질의처럼 ) 고수준으로 추상화 해서 일련의 스트림으로 만들어 처리할 수 있음

### 동작 파라미터화로 메서드에 코드 전달하기

- 코드 일부를 API로 전달하는 기능
- 메서드를 다른 메서드의 인수로 넘겨주는 기능을 제공

### 병렬성과 공유 가변 데이터

- 안전하게 실행할 수 있는 코드를 만들려면 공유된 가변 데이터에 접근하지 않아야 한다.

자바 함수는 값처럼 취급

## 메서드 참조 `::`

- `::` ( 이 메서드를 값으로 사용하라는 의미 )
- 기존에 객체 참조 (new로 객체 참조를 생성함)를 이용해서 객체를 이리저리 주고 받았던 것처럼
- 자바8에서는 `File::isHidden` 을 이용해서 메서드 참조를 만들어 전달할 수 있게 되었다.
- 더 이상 메서드가 이급값이 아닌 일급값이라는 것

```java
// 기존 방식
File[] hiddenFiles = new File(".").listFiles(new FileFilter() {
    @Override
    public boolean accept(File file) {
        return file.isHidden(); // 숨겨진 파일 필터링 !
    }
});

// 메서드 참조
File[] hiddenFiles = new File(".").listFiles(File::isHidden);
```

## 람다 : 익명함수

- 자바8에서는 메서드를 일급값으로 취급할 뿐 아니라 람다(또는 익명함수)를 포함하여 함수도 값으로 취급 할 수 있다.

## 필터

- 특정 항목을 선택해서 반환하는 동작

### 프레디케이트

- Apple::isGreenApple 메서드를 filterApples로 넘겨주었다.
- (filterApples는 Predicate<Apple>를 파라미터로 받음.
- 수학에서는 인수로 값을 받아 true 나 false를 반환하는 함수를 프레디케이트라고 한다.

## 메서드 전달에서 람다로

- 한 두번만 사용할 메서드를 매번 정의하는 것은 귀찮은 일이다. → 익명함수 또는 람다라는 새로운 개념

```java
//isGreenApple 선언 후
fileterApples(inventory, Apple::isGreenApple);

//람다
fileterApples(inventory, (Apple a) -> GREEN.equals(a.getColor()) );

```

- 한 번만 사용할 메서드는 따로 정의를 구현할 필요가 없다. 간결하다.
- 람다가 몇 줄 이상으로 길어진다면, 메서드를 정의하고 메서드 참조를 활용해야함

### 병렬 처리

- 자바8의 스트림 API (java.util.stream)로 컬렉션을 처리하면서 발생하는 모호함과 반복적인 코드 문제
- 멀티코어 활용 어려움이라는 두 가지 문제를 해결했다.
- 컬렉션은 어떻게 데이터를 저장하고 접근할지에 중점
- 스트림은 데이터에 어떤 계산을 할 것인지 묘사하는 것에 중점을 둔다.

### 디폴트 메서드와 자바 모듈

- 자바8에서는 인터페이스를 쉽게 바꿀 수 있도록 디폴트 메서드를 지원한다.
- 특정 프로그램을 구현하는 데 도움을 주는 기능이 아니라
- 미래에 프로그램이 쉽게 변화할 수 있는 환경을 제공하는 기능이다.
- 인터페이스에 새로운 메서드를 추가한다면 인터페이스를 구현하는 모든 클래스는 새로 추가된 메서드를 구현해야한다.
- **디폴트 메서드는 구현 클래스에서 구현하지 않아도 되는 메서드를 인터페이스에 추가 할 수 있는 기능을 제공한다.**

```java
default void sort(Comparator<? super E> c) {
            Collections.sort(this, c);
}
```

### Optional<T> 클래스

- NullPointer 예외를 피할 수 있도록 도와줌
- 값을 갖거나 갖지 않을 수 있는 컨테이너 객체


# 2단원

# 동작 파라미터화 코드 전달하기

- 자주 바뀌는 요구사항에 효과적으로 대응 할 수 있다.
- 아직 어떻게 실행할 것인지 결정하지 않은 코드블록을 의미함

### 예시 ) 초록색 사과 필터에 빨간 사과도 필터링 하고 싶을 때

- filterGreenApple(List<Apple> inventory) + filterRedApple(List<Apple> inventory)
- 보다는
- filterApplesByColor(List<Apple> inventory, Color color);

### 여기서 무게도 필터링 하고 싶다면 ?

- 모든 속성을 파라미터로 추가하면 굉장히 불쾌함.
- → 동작 파라미터 화로 유연성을 얻자
- 1장에서 참 또는 거짓을 반환하는 함수를 프레디케이트라고 했다.
- 선택 조건을 결정하는 인터페이스를 정의하자.

```java
public interface ApplePredicate {
        boolean test(Apple apple);
}
```

```java
// 여러 버전의 ApplePredicate를 정의 할 수 있음

// 무거운 사과만 선택
public class AppleHeavyWeightPredicate implements ApplePredicate {
        public boolean test(Apple apple) {
                return apple.getWeight() > 150;
        }
}


```

### 추상적 조건으로 필터링

```java
// Apple Predicate를 이용한 필터 메서드
public static List<Apple> filterApples(List<Apple> inventory, ApplePredicate p) {
        List<Apple> result = new ArrayList<>();
        for(Apple apple: inventory) {
                if(p.test(apple)) {// 프레디케이트 객체로 사과 검사 조건을 캡슐화 했다.
                        result.add(apple)                 
                }
        }
        return result;
}        
```

### 코드/동작 전달하기

```java
public class AppleRedAndHeavyPredicate implements ApplePredicate {
        public boolean test(Apple apple) {
                return RED.equals(apple.getColor()) && apple.getWeight() > 150;
        }
}

List<Apple> redAndHeavyApples = filterApples(inventory, 
                                                                        new AppleRedAndHeavyPredicate());
```

## 한개의 파라미터, 다양한 동작 = `재활용`

- 컬렉션 탐색 로직과 각 항목에 적용할 동작을 분리할 수 있다는 것이 동작 파라미터화의 강점이다.

## 익명 클래스

- 자바의 지역 클래스(블록 내부에 선언된 클래스)와 비슷한 개념
- 말 그대로 이름이 없는 클래스
- 익명 클래스를 이용하면 클래스 선언과 인스턴스화를 동시에 할 수 있다.

### 익명 클래스의 단점

- 여전히 많은 공간을 차지한다.  → 람다 식 사용

```java
// 람다 식 사용
List<Apple> redAndHeavyApples = filterApples(inventory, 
                                            (Apple apple) -> RED.equals(apple.getColor())); 
```

## Comparator로 정렬하기

- Comparator 를 구현해서 sort메서드의 동작을 다양화 할 수 있다.

```java
inventory.sort(new Comparator<Apple>() {
        public int compare(Apple a1, Apple a2) {
                return a1.getWeight().compareTo(a2.getWeight());
        }
});

// 람다 
inventory.sort(Apple a1, Apple a2)->a1.getWeight().compareTo(a2.getWeight()));
```

## Runnable 코드 블록 실행하기

```java
Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("123");
            }
        });
//람다 식
Thread t = new Thread(() -> System.out.println("123"));
```

## Callable을 결과로 반환하기

- ExecutorService 인터페이스는 태스크 제출과 실행 과정의 연관성을 끊어준다.
- ExecutorService를 이용하면 태스크를 스레드 풀로 보내고 결과를 Future로 저장할 수 있다는 점이 스래드와 Runnalbe을 이용하는 방식과는 다르다.

## GUI 이벤트 처리하기
```java
Button button = new Button("Send");
button.setOnAction((ActionEvent event) -> label.setText("Sent!!!"));
```
