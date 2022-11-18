# 11장 null대신 Optional 클래스

- **null의 도입**: 알골(ALGOL)을 설계하면서 처음 등장
  → **구현**하기 **쉬워서** 도입함
  → 그러나 십억 달러짜리 **실수!**

## 11.1 값이 없는 상황을 어떻게 처리할까?

### NullPointerException 줄이기 시도

1. **null 확인 코드** 추가하기

   ```bash
   public String getCarInsuranceName(Person person){
   	if(person != null){
   		Car car = person.getCar();
   		if( car != null ) {
   			Insuracne insurance = car.getInsurance();
   			if(insurance != null){
   				return insureance.getName();
   			}
   		}
   	}
   	return "Unknown";
   }
   ```

   **의심**되는 **변수**마다 **if가 추가**되면서 코드 **들여쓰기 수준이 증가**한다.

   이와 같은 **반복 패턴 코드**(recurring pattern)를 **깊은 의심**(deep doubt)이라 부른다.

   ⇒ 코드 구조가 **엉망**이 됨 & **가독성** 떨어짐

2. **너무 많은 출구**

   ```bash
   public String getCarInsuranceName(Person person){
   	if(person == null){
   		return "Unknown";
   	}
   	Car car = person.getCar();
   	if(car == null){
   		return "Unknown";
   	}
   	Insurance insurance = car.getInsurance();
   	if(insurance == null){
   		return "Unknown";
   	}
   	return insurance.getName();
   }
   ```

   **null 변수**가 있으면 **즉시 “Unknown”반환** 하는데, 출구가 많아진다.

   출구가 많아지면 **유지보수가 어려워**진다.

위의 시도들은 **모두 좋은 방법이** 🚫**아니다!**

따라서 **값**이 있거나 없음을 표현할 수 있는 **좋은 방법**을 알아보자!

### null때문에 발생하는 문제

1. 에러의 **근원**
   `NullPointerException`은 자바에서 가장 흔한 에러임
2. 코드를 **어지럽**게
   깊은 의심처럼 **중첩된 코드**를 봐야하므로 **가독성**이 떨어짐
3. **의미**가 **없음**
   null은 아무 의미도 표현하지 않음
4. 자바 **철학**에 **위배**
   자바는 포인터를 보이지 않도록 했음 그러나 **null포인터**는 예외임!
5. 형식 시스템에 **구멍**
   null = 무형식, 정보X → **모든** 참조형식에 **할당 가능**!
   이런식으로 여러 곳에서 쓰이면 null이 **어떤 의미**로 쓰인지 **알기 어려움**

### 다른 언어의 null 대안책

- 그루비: 안전 네비게이션 연산자(safe navigation operator) `?.`
  ```groovy
  def carInsuranceName = person?.car?.insurance?.name
  ```
- 하스켈: `Maybe`(Optional value선택형 값 저장 가능)
- 스칼라: `Option[T]`(T 형식이거나 아무 값도 갖지 않음)

## 11.2 Optional 클래스 소개

자바8: 하스켈& 스칼라의 영향 → `java.util.Optional<T>` 제공

Optional Car

값이 있으면 `Optional` 클래스는 값을 감싼다.

값이 없으면 `Optional.empty` 메서드로 `Optional`을 반환한다.

- `null` VS `Optional.empty`
  - `null` 참조 = `NullPointerException`
  - `Optional.empty` 참조 = Optional객체 활용

**Before**

```java
public class Person{
	private **Car** car;
	public **Car** getCar(){
		return car;
	}
}
```

**After**

```java
public class Person {
	private **Optional<Car>** car;
	public **Optional<Car>** getCar(){
		return car;
	}
}
```

## 11.3 Optional 적용 패턴

### Optional을 사용해서 얻는 이점

- **도메인 모델**의 **의미를 명확**하게 만들 수 있다
- **null** 참조 **대신** **값이 없는 상황**을 표현할 수 있다

### Optional 객체 만들기: 3가지 방법

```java
Optional<Car> optCar1 = Optional.empty(); //1. 빈 Optional 객체 만들기
Optional<Car> optCar2 = Optional.of(car); //2. null이 아닌 값으로 Optional 만들기
//car이 null이면 NullPointerException 발생
//Optional사용하지 않았으면, car 프로퍼티 접근시 **에러** 발생)
Optional<Car> optCar3 = Optional.ofNullable(car); //3. null값으로 Optional 만들기
//car가 null이면 빈 Optional 객체가 반환
```

### Optional 사용: map으로 객체의 정보 추출

**언제 사용** 하는가? ex) 보험회사의 이름을 **추출**할 때

- Optional 사용 **전**

```java
//정보 접근 전, null인지 확인한다.
String name = null;
if(insurance != null){
	name = insurance.getName();
}
```

- Optional 사용 **후**

```java
Optional<Insurance> optInsurance = Optional.ofNullable(insurance);
**Optional**<String> name = optInsurance.**map**(Insurance::getName);
```

**Optional의 map ≈ 스트림의 map**

- 자동차 보험회사 이름 짓시(여러 메서드 호출)

```java
public String getCarInsuranceName(Person person){
	return person.getCar().getInsurance().getName();
}
```

- 스트림의 map: 스트림의 각 요소에 제공된 함수를 적용하는 연산
- Optional의 map: 스트림 map에서 최대 요소의 개수가 한 개 이하인 데이터 컬렉션

### flatMap으로 Optional 객체 연결

map을 사용해서 코드를 재구현 해보자!

```java
//컴파일 X 코드임!
Optional<Person> optPerson = Optional.of(person);
Optional<String> name = optPerson
	.map(Person::getCar) //Optional<Optional<Car>>을 반환하게 됨
	.map(Car::getInsurance) //또 다른 Optional 반환하게 됨
	.map(Insurance::getName);
```

**중첩(2차원) `Optional`**은 지원**X** → `flatMap`을 이용하여 **1차원**으로 **평준화**하자!

- 스트림 flatMap: **함수**를 **인수**로, **다른 스트림**을 **반환**함.
  즉, 함수를 적용해서 생성된 모든 스트림이 **하나의 스트림**으로 **병합**되어 평준화된다.
- 자동차 보험회사 이름 찾기 (Optional)
  - null확인하는 분기문 추가하지 않아도 됨
  - 가독성이 높아짐

```java
public String getCarInsuranceName(Optional<Person> person){
	return person.flatMap(Person::getCar)
		.flatMap(Car::getInsurance)
		.map(Insurance::getName)
		.orElse("Unknown");
}
```

예제 11-2 & 예제 11-3 (아래의 코드)와 비교해보면 좀 더 쉽게 알 수 있다.

```java
public String getCarInsuranceName(Person person){
	if(person != null){
		Car car = person.getCar();
		if( car != null ) {
			Insuracne insurance = car.getInsurance();
			if(insurance != null){
				return insureance.getName();
			}
		}
	}
	return "Unknown";
}
```

```java
public String getCarInsuranceName(Person person){
	if(person == null){
		return "Unknown";
	}
	Car car = person.getCar();
	if(car == null){
		return "Unknown";
	}
	Insurance insurance = car.getInsurance();
	if(insurance == null){
		return "Unknown";
	}
	return insurance.getName();
}

```

### Optional 스트림 조작

자바9에서 Optional에 `stream()` 메서드를 추가함

**Optional 스트림**을 값을 가진 **스트림으로 변환**할 때 **유용하게 활용** 가능

- 사람 목록을 이용해 가입한 보험 회사 이름 찾기

```java
public Set<String> getCarInsuranceNames(List<Person> persons){
	return persons.stream().map(Person::getCar) // List<Person> -> Stream<Optional<Car>>
		.map(optCar -> optCar.flatMap(Car::getInsurance)) //Optional<Car> -> Optional<Insurance>
		.map(optIns -> optIns.map(Insurance::getName)) // Optional<Insurance>-> Optional<String>
		.flatMap(Optional::stream) // Stream<Optional<String>> -> Stream<String>
		.collect(toSet()); //중복X인 집합으로 수집
}
```

Optional 덕분에 연산을 **null 걱정없이** 안전하게 **처리**가능

but! 마지막 결과를 얻으려면 **빈 Optional**을 **제거** & **값**을 **언랩**해야함 → filter,map을 활용하여 해결

```java
Stream<Optional<String>> stream = ...
Set<String> result = stream.filter(Optional::isPresent)
	.map(Optional::get)
	.collect(toSet());
```

### Optional 값을 읽는 다양한 방법

| 메서드                              | 설명                                                         | 값이 존재O              | 값이 존재X                     |
| ----------------------------------- | ------------------------------------------------------------ | ----------------------- | ------------------------------ |
| get()                               | 값을 읽는 가장 간단한 메서드 but, 안전하지 않은 메서드       | 래핑된 값 반환          | NoSuchElementException 발생    |
| orElse(T)                           | 값 존재O ⇒ 값 반환/ 값 존재X ⇒ 기본 값 반환                  |                         | 기본값을 제공                  |
| orElseGet(Supplier)                 | 값 존재O ⇒ 값 반환/ 값 존재X ⇒ Supplier에서 제공하는 값 반환 |                         | Supplier가 실행                |
| orElseThrow(Supplier)               | 값 존재O ⇒ 값 반환 /값 존재X ⇒ Supplier에서 생성한 예외 발생 |                         | 예외 발생, 예외 종류 선택 가능 |
| ifPresent(Consumer)                 | 값 존재O ⇒ 값 반환 /값 존재X ⇒ Supplier에서 생성한 예외 발생 | 인수로 넘겨준 동작 실행 | 아무 일도 안 일어남            |
| ifPresentOrElse(Consumer, Runnable) | 값 존재O ⇒ 값 반환 /값 존재X ⇒ Supplier에서 제공하는 값 반환 | 인수로 넘겨준 동작 실행 | Runnable 실행                  |

### 두 Optional 합치기

- Person과 Car를 이용해서 **가장 저렴한 보험료**를 제공하는 **보험회사**를 찾기

```java
public Optional<Insurance> nullSafeFindCheapestInsurance(
	Optional<Person> person, Optional<Car> car) {
	return person.flatMap( p -> car.map(c->findCheapestInsurance(p, c)));
}
```

### 필터로 특정값 거르기: filter

객체의 메서드를 호출해서 프로퍼티를 확인해야 할 때

ex) 보험회사 이름이 ‘CambridgeInsurance’인지 확인해야 할 때
→ Optional + filter 사용하여 간단하게 구현 가능

```java
Optional<Insurance> optInsurance = ...;
optInsurance.filter(insurance -> "CambridgeInsurance".equals(insurance.getName()))
	.ifPresent(x -> System.out.println("ok"));
```

- filter 메서드: 프레디케이트를 인수로 받음

```java
if(Optional 객체가 값 가짐){
		if( 프레디케이트 == true ){
			Optional에 변화 없고 그대로 반환
		}else if( 프레디케이트 == false){
			빈 상태의 Optional 반환
		}
} else {
		filter는 아무 동작 하지 않음
}
```

- Optional 클래스의 메서드

| 메서드          | 설명                                                                         |
| --------------- | ---------------------------------------------------------------------------- | --- | -------------------------------------- |
| empty           | 빈 Optional 반환                                                             |
| filter          | 값 존재O && 프레디케이트 일치 ⇒ 값 있는 Optional 반환/ 값 존재X              |     | 프레디케이트 불일치 ⇒ 빈 Optional 반환 |
| flatMap         | 값 존재O ⇒ 값 있는 Optional 반환 /값 존재X ⇒ 빈 Optional 반환                |
| get             | 값 존재O ⇒ 값 있는 Optional 반환 /값 존재X ⇒ NoSuchElementException 발생     |
| ifPresent       | 값 존재O ⇒ 지정 Consumer 실행 /값 존재X ⇒ 아무 일도 일어나지 않음            |
| ifPresentOrElse | 값 존재O ⇒ 지정 Consumer 실행 /값 존재X ⇒ 아무 일도 일어나지 않음            |
| isPresent       | 값 존재O ⇒ true반환 /값 존재X ⇒ false 반환                                   |
| map             | 값 존재O ⇒ 제공된 매핑 함수 적용                                             |
| of              | 값 존재O ⇒ 값 있는 Optional 반환 /null 값 ⇒ NullPointerrException 발생       |
| ofNullable      | 값 존재O ⇒ 값 있는 Optional 반환 /값 존재X ⇒ 빈 Optional 반환                |
| or              | 값 존재O ⇒ 값 있는 Optional 반환 /값 존재X ⇒ Supplier에서 만든 Optional 반환 |
| orElse          | 값 존재O ⇒ 값 반환 /값 존재X ⇒ 기본 값 반환                                  |
| orElseGet       | 값 존재O ⇒ 값 반환 /값 존재X ⇒ Supplier에서 제공하는 값 반환                 |
| orElseThrow     | 값 존재O ⇒ 값 반환 /값 존재X ⇒ Supplier에서 생성한 예외 발생                 |
| stream          | 값 존재O ⇒ 존재하는 값만 스트림 반환 /값 존재X ⇒ 빈 스트림 반환              |

## 11.4 Optional을 사용한 실용 예제

**기존 코드**와 **Optional 호환성** 유지하기

- 기존 코드에 작은 유틸리티 메서드를 추가하는 방식으로 호환성을 유지한다

### 잠재적 null 대상 Optional로 감싸기

get 메서드의 시그니처를 고칠 수 X,

but! **get 메서드**의 **반환값**을 **Optional**로 감쌀 수 있음

- 가정: `Map<String, Object>` 이 있는데, key값에 접근한다고 가정

```java
Object value = map.get("key");
```

key에 해당하는 값이 없으면 null이 반환됨 → map 반환 값을 Optional로 감싼다.(2가지 방법)

1. 기존 if-then-else를 추가
2. `Optional.ofNullable`를 이용

   ```java
   Optional<Object> value = Optional.ofNullable(map.get("key"));
   ```

### 예외와 Optional 클래스

`Integer.parseInt(String)`은 값 제공할 수 없을 때,

`null`이 아닌 `NumberFormatException`예외를 발생시킴

- **null**은 **if문**으로 여부를 확인할 수 있음
- **예외**는 **try/catch블록**을 사용해야 함

⇒ `Integer.parseInt(String)`를 **Optional을 반환**하도록 모델링할 수 있다.

```java
public static Optional<Integer> stringToInt(String s){
	try{
		return Optional.of(Integer.parseInt(s));
	} catch (NumberFormatException e){
		return Optional.empty();
	}
}
```

위의 코드와 같은 유틸리티 클래스 `OptionalUtility`를 만들면 된다.

그러면 `OptionalUtility.stringToInt`를 이용해서
번거로운 `try/catch` 대신 `Optional<Integer>`로 변환할 수 있다.

### 기본형 Optional을 사용하지 말아야 하는 이유

`Optional`도 기본형 특화 `OptionalInt`, `OptionalLong`, `OptionalDouble`등의 클래스를 제공한다.

그러나 기본형 특화 `Optional`은 호환성 등에서 **단점**이 있어 **사용을 권장하지 않는다**.

### 응용

# 12장 새로운 날짜와 시간 API

**자바8**에서는 지금까지의 날짜와 시간 문제를 개선하는 **새로운 날짜와 시간 API**를 제공한다.

## 12.1 LocalDate, LocalTime, Instant, Duration, Period 클래스

- 간단한 날짜와 시간 간격 정의(java.time 패키지):
  `LocalDate`, `LocalTime`, `LocalDateTime`, `Instant`, `Duration`, `Period`

### LocalDate 사용

- `LocalDate`: 시간을 제외한 날짜를 표현하는 **불변 객체**, 시간대 정보를 포함하지 X

```java
LocalDate date = LocalDate.of(2017, 9, 21);
int year = date.getYear(); //2017
Month month = date.getMonth(); //SEPTEMBER
int day = date.getDayOfMonth(); //21
DayOfWeek dow = date.getDayOfWeek();  //THURSDAY
int len = date.lengthOfMonth(); //31(3월의 일 수)
boolean leap = date.isLeapYear(); //fasle(윤년이 아님)
LocalDate today = LocalDate.now(); // 현재 날짜 정보
```

- `TemporalField`: **시간 관련 객체**에서 어떤 **필드 값에 접근**할지 정의함
  TemporalField 인터페이스를 정의하는 `ChronoField`의 **열거자 요소**를 이용해서 **원하는 정보**를 **쉽게 얻을 수** 있다.

```java
LocalDate date = LocalDate.of(2017, 9, 21);
int year = date.get(ChronoField.YEAR); //date.getYear();
int month = date.get(ChronoField.MONTH_OF_YEAR); //date.getMonthValue();
int day = date.get(ChronoField.DAY_OF_MONTH); //date.getDayOfMonth();
```

### LocalTime 사용

```java
LocalTime time = LocalTime.of(13, 45, 20); //13:45:20
int hour = time.getHour(); //13
int minute = time.getMinute(); //45
int second = time.getSecond(); //20
```

cf) `LocalTime`과 `LocalDate`를 또 다르게 나타내는 방법

```java
LocalDate date = LocalDate.parse("2017-09-21");
LocalTime time = LocalTime.parse("13:45:50");
```

주의 ⚠️ 문자열을 **파싱**할 수 **없을** 때, **DateTimeParseException**이 발생함

### 날짜와 시간 조합: LocalDateTime

- `LocalDateTime`: LocalDate와 LocalTime을 쌍으로 갖는 복합 클래스

```java
//2017-09-21T13:45:20
LocalDateTime dt1 = LocalDateTime.of(2017, Month.SEPTEMBER, 21, 13, 45, 20);
LocalDateTime dt2 = LocalDateTime.of(date, time); //LocalDate, LocalTime
LocalDateTime dt3 = date.atTime(13, 45, 20); //LocalDate의 atTime ->LocalDateTime
LocalDateTime dt4 = date.atTime(time);
LocalDateTime dt5 = time.atDate(date); //LocalTime의 atDate -> LocalDateTime
```

- LocalDateTime → LocalDate나 LocalTime으로 되돌리기

```java
LocalDate date1 = dt1.toLocalDate(); //2017-09-21
LocalTime time1 = dt1.toLocalTime(); //13:45:20
```

### 기계의 날짜와 시간: Instant

**Instant 클래스** : Unix epoch time을 기준으로 특정 지점까지의 시간을 초로 표현함

- `ofEpochSecond`에 초를 넘겨줘서 Instant 인스턴스 생성 가능
- 나노초(10억분의 1초)의 **정밀도** 제공함
- ofEpochSecond의 **두번 째 인수**로 나노초 단위로 **시간 보정** 가능

```java
Instant.ofEpochSecond(3);
Instant.ofEpochSecond(3, 0);
Instant.ofEpochSecond(2, 1_000_000_000); //2초 이후의 1억 나노초
Instant.ofEpochSecond(4, -1_000_000_000); //4초 이전의 1억 나노초
```

### Duration와 Period 정의

- **Duration**: **두** 시간 **객체 사이**의 **지속시간**

```java
Duration d1 = Duration.between(time1, time2); //2개의 LocalTime으로 만들기
Duration d2 = Duration.between(dateTime1, dateTime2); //2개의 LocalDateTime으로 만들기
Duration d3 = Duration.between(instant1, instant2); //2개의 Instant로 만들기
```

- Duration 사용시 **주의** ⚠️
  - `LocalDateTime`은 **사람**이 사용하도록, `Instant`는 **기계**가 사용하도록 한거라 **혼용 불가**!
  - `Duration`은 **나노초 시간** 단위를 표현하므로 `LocalDate` 전달하지 **못함**
    → **년, 월, 일**로 시간 표현할 때는 `**Period` 클래스\*\*를 사용
  ```java
  Period tenDays = Period.between(LocalDate.of(2017,9,11),LocalDate.of(2017, 9, 21);
  ```
- 두 시간 객체 사용하지 않고 Duration과 Period만들기

```java
Duration threeMinutes = Duration.ofMinutes(3);
Duration threeMinutes = Duration.of(3, ChronoUnit.MINUTES);

Peroid tenDays = Period.ofDays(10);
Period threeWeeks = Period.ofWeeks(3);
Period twoYearsSixMonthsOneDay = Period.of(2, 6, 1);
```

- Duration과 Period가 공통으로 사용하는 메서드

| 메서드       | 정적 | 설명                                                          |
| ------------ | ---- | ------------------------------------------------------------- |
| between      | O    | 두 시간 사이의 간격을 생성함                                  |
| from         | O    | 시간 단위로 간격을 생성함                                     |
| of           | O    | 주어진 구성 요소에서 간격 인스턴스를 생성함                   |
| parse        | O    | 문자열을 파싱해서 간격 인스턴스를 생성함                      |
| addTo        | X    | 현재값의 복사본을 생성한 다음에 지정된 Temporal 객체에 추가함 |
| get          | X    | 현재 간격 정보값을 읽음                                       |
| isNegative   | X    | 간격이 음수인지 확인함                                        |
| isZero       | X    | 간격이 0인지 확인함                                           |
| minus        | X    | 현재값에서 주어진 시간을 뺀 복사본을 생성함                   |
| multipliedBy | X    | 현재값에서 주어진 값을 곱한 복사본을 생성함                   |
| negated      | X    | 주어진 값의 부호를 반전한 복사본을 생성함                     |
| plus         | X    | 현재값에 주어진 시간을 더한 복사본을 생성함                   |
| subtractFrom | X    | 지정된 Temporal 객체에서 간격을 뺌                            |

## 12.2 날짜 조정, 피싱, 포매팅

### 간단한 날짜 조정

**Temporal 인터페이스**는 `LocalDate`, `LocalTime`, `LocalDateTime`, `Instant`처럼 특정 시간을 정의함

`get`과 `with` 메서드로 Temporal 객체의 필드값을 읽거나 고칠 수 있다.

```java
LocalDate date1 = LocalDate.of(2017, 9, 21); //2017-09-21
LocalDate date2 = date1.withYear(2011); //2011-09-21
LocalDate date3 = date2.withDayOfMonth(25); //2011-09-25
LocalDate date4 = date3.with(ChronoField.MONTH_OF_YEAR, 2); // 2011-02-25
```

```java
LocalDate date1 = LocalDate.of(2017, 9, 21); //2017-09-21
LocalDate date2 = date1.plusWeeks(1); //2017-09-28
LocalDate date3 = date2.minusYears(6); //2011-09-28
LocalDate date4 = date3.plus(6, ChronoUnit.MONTHS); // 2012-03-28
```

- LocalDate, LocalTime, LocalDateTime, Instant에서 공통적으로 사용가능한 메서드

| 메서드   | 정적 | 설명                                                                   |
| -------- | ---- | ---------------------------------------------------------------------- |
| from     | O    | 주어진 Temporal 객체를 이용해서 클래스의 인스턴스를 생성함             |
| now      | O    | 시스템 시계로 Temporal 객체를 생성함                                   |
| of       | O    | 주어진 구성 요소에서 Temporal 객체의 인스턴스를 생성함                 |
| parse    | O    | 문자열을 파싱해서 Temporal 객체를 생성함                               |
| atOffSet | X    | 시간대 오프셋과 Temporal객체를 합침                                    |
| atZone   | X    | 시간대 오프셋과 Temporal객체를 합침                                    |
| format   | X    | 지정된 포매터를 이용해서 Temporal 객체를 문자열로 변환(Instant 미지원) |
| get      | X    | Temporal 객체의 상태를 읽음                                            |
| minus    | X    | 특정 시간을 뺀 Temporal 객체의 복사본을 생성함                         |
| plus     | X    | 특정 시간을 더한 Temporal 객체의 복사본을 생성함                       |
| with     | X    | 일부 상태를 바꾼 Temporal 객체의 복사본을 생성함                       |

### 복잡한 날짜 조정: TemporalAdjusters 사용하기

ex) 다음 주 일요일, 돌아오는 평일, 어떤 달의 마지막날 등 **복잡한 날짜 조정**이 **필요**할 때

```java
import static java.time.temporal.TemporalAdjusters.*;
LocalDate date1 = LocalDate.of(2014, 3, 18); // 2014-03-18 (화요일)
LocalDate date2 = date1.with(nextOrSame(DayOfWeek.SUNDAY)); // 2014-03-23
LocalDate date3 = date2.with(lastDayOfMonth())// 2014-03-31
```

- TemporalAdjusters 클래스의 팩토리 메서드

| 메서드              | 설명                                                                                                     |
| ------------------- | -------------------------------------------------------------------------------------------------------- |
| dayOfWeekInMonth    | 서수 요일에 해당하는 날짜를 반환하는 TemporalAdjuster를 반환홤                                           |
| firstDayOfMonth     | 현재 달의 첫 번째 날짜를 반환하는 TemporalAdjuster를 반환홤                                              |
| firstDayOfNextMonth | 다음 달의 첫 번째 날짜를 반환하는 TemporalAdjuster를 반환홤                                              |
| firstDayOfNextYear  | 내년의 첫 번쨰 날짜를 반환하는 TemporalAdjuster를 반환홤                                                 |
| firstDayOfYear      | 올해의 첫 번째 날짜를 반환하는 TemporalAdjuster를 반환홤                                                 |
| firstInMonth        | 현재 달의 첫번째 요일에 해당하는 날짜를 반환하는 TemporalAdjuster를 반환홤                               |
| lastDayOfMonth      | 현재 달의 마지막 날짜를 반환하는 TemporalAdjuster를 반환홤                                               |
| lastDayOfNextMonth  | 다음 달의 마지막 날짜를 반환하는 TemporalAdjuster를 반환홤                                               |
| lastDayOfNextYear   | 내년의 마지막 날짜를 반환하는 TemporalAdjuster를 반환홤                                                  |
| lastDayOfYear       | 올해의 마지막 날짜를 반환하는 TemporalAdjuster를 반환홤                                                  |
| lastInMonth         | 현재 달의 마지막 요일에 해당하는 날짜를 반환하는 TemporalAdjuster를 반환홤                               |
| next                | 현재 달에서 현재 날짜 이후로 지정한 요일이 처음으로 나타나는 날짜를 반환하는 TemporalAdjuster를 반환홤   |
| previous            | 현재 달에서 현재 날짜 이전으로 지정한 요일이 처음으로 나타나는 날짜를 반환하는 TemporalAdjuster를 반환홤 |
| nextOrSame          | 현재 날짜 이후로 지정한 요일이 처음으로 날짜를 반환하는 TemporalAdjuster를 반환홤                        |
| previousOrSame      | 현재 날짜 이전으로 지정한 요일이 이전으로 날짜를 반환하는 TemporalAdjuster를 반환홤                      |

만약, 필요한 기능이 **구현되어 있지 않을 때**에는 **직접 구현**하여 사용할 수 있다

```java
@FunctionalInterface
public interface TemporalAdjuster{
	Temporal adjustInto(Temporal temporal);
}
```

⇒ `NextWorkingDay` 클래스 구현해보기( 날짜를 하루씩 바꾸는데 토요일과 일요일은 건너뛴다)

```java
public class NextWorkingDay implements TemporalAdjuster{
	@Override
	public Temporal adjustInto(Temporal temporal){
		DayOfWeek dow = DayOfWeek.of(temporal.get(ChronoField.DAY_OF_WEEK); //현재 날짜
		int dayToAdd = 1; //보통은 하루 추가
		if(dow == DayOfWeek.FRIDAY) dayToAdd = 3; //오늘이 금요일이면 3일 추가
		else if(dow == DayOfWeek.SATURDAY) dayToAdd = 2; //토요일이면 2일 추가
		return temporal.plus(dayToAdd, ChronoUnit.DAYS); //적정날 날 수만큼 추가된 날짜 반환
	}
}
```

### 날짜와 시간 객체의 출력과 파싱

**날짜와 시간 관련 작업**에서 **포매팅**과 **파싱**은 떨어질 수 없는 관계다.

가장 중요한 패키지인 `DateTimeFormatter` 클래스로 **날짜나 시간**을 **특정 형식의 문자열**로 만들 수 있다

`DateTimeFormatter` 클래스는 `BASIC_ISO_DATE`와 `ISO_LOCAL_DATE` 등 상수를 미리 정의하고 있다.

- 포매팅

```java
LocalDate date = LocalDate.of(2014, 3, 18);
String s1 = date.format(DateTimeFormatter.BASIC_ISO_DATE); // 20140318
String s2 = date.format(DateTimeFormatter.ISO_LOCAL_DATE); //2014-03-18
```

- 파싱: 문자열 파싱 → 날짜 객체

```java
LocalDate date1 = LocalDate.parse("20140318", DateTimeFormatter.BASIC_ISO_DATE);
LocalDate date2 = LocalDate.parse("2014-03-18", DateTimeFormatter.ISO_LOCAL_DATE);
```

- 특정 패턴으로 포매터 만들기

```java
DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
LocalDate date1 = LocalDate.of(2014, 3, 18);
String formattedDate = date1.format(formatter); //요청 형식의 패턴에 해당하는 문자열 생성
LocalDate date2 = LocalDate.parse(formattedDate, formatter); //문자열 파싱->다시 날짜 생성
```

- 복합적인 포매터 세부 제어 ex) 지역화된 포매터, 이탈리아

```java
DateTimeFormatter italianFormatter =
	DateTimeFormatter.ofPattern("d. MMMM yyyy", LOCAL.ITALIAN);
LocalDate date1 = LocalDate.of(2014, 3, 18);
String formattedDate = date.format(italianFormatter);
LocalDate date2 = LocalDate.parse(formattedDate, italianFormatter);
```

- 프로그램적으로 포매터 만들기: `DateTimeFormatterBuilder` 클래스 이용하기

```java
DateTimeFormatter italianFormatter = new DateTimeFormatterBuilder()
	.appendText(ChronoField.DAY_OF_MONTH)
	.appendLiteral(". ")
	.appendText(ChronoField.MONTH_OF_YEAR)
	.appendLiteral(" ")
	.appendText(ChronoField.YEAR)
	.parseCaseInsensitive()
	.toFormatter(Locale.ITALIAN);

LocalDate date = LocalDate.of(2021, 3, 25);
String italianFormattedDay = date.format(italianFormatter);
// -> 출력: 25. marzo 2021
```

## 12.3 다양한 시간대와 캘린더 활용 방법

### 시간대 사용하기

**표준 시간이 같은 지역**을 묶어서 **시간대 규칙 집합**을 정의한다.

- `ZoneRules` 클래스에는 **40개** 정도의 **시간대**가 있다
- `ZoneId`의 `getRules()`를 이용해서 시간대의 규정을 획득 가능하다
- **지역 ID**로 특정 **ZoneId**를 구분한다.
  ```java
  ZoneId romeone = ZoneId.of("Europe/Rome"); //기존의 TimeZone 객체를 Zoneid 객체를 변환
  ```
  - `’{지역}/{도시}’` 형식으로 지역 ID가 이루어진다
  - I**ANA Time Zone Database**에서 제공하는 지역 집합 정보를 사용한다.
- `ZoneId` 얻은 이후, `LocalDate`, `LocalDateTime`, `Instant`를 이용하여
  `ZonedDateTime` 인스턴스로 변환

```java
//LocalDate
LocalDate date = LocalDate.of(2014, Month.MARCH, 18);
ZonedDateTime zdt1 = date.atStartOfDay(romeZone);
//LocalDateTime
LocalDateTime dateTime = LocalDateTime.of(2014, Month.MARCH, 18, 13, 45);
ZonedDateTime zdt2 = dateTime.atZone(romeZone);
//Instant
Instant instant = Instant.now();
ZonedDateTime zdt3 = instant.atZone(romeZone);
```

![ZonedDateTime의 개념](https://user-images.githubusercontent.com/64851797/202667813-327a3ac9-47ed-467d-874d-c350b90ee66a.png)

ZonedDateTime의 개념

- `ZoneId`를 이용하여 `Instant`를 `LocalDateTime`로 바꾸기

```java
Instant instant = Instant.now();
LocalDateTime timeFromInstant = LocalDateTime.ofInstant(instant, romeZone);
```

### UTC/Greenwich 기준의 고정 오프셋

- 때로 **UTC(협정 시계시)/GMT(그리니치 표준시)**를 기준으로 시간대를 표현하기도 함

ex) 뉴욕은 런던보다 5시간 느리다

```java
ZoneOffset newYorkOffset = ZoneOffset.of("-05:00");
```

이 방법은 **서머타임**을 제대로 **처리할 수 없으**므로 권장하지 않음

(cf. 서머타임: 여름철에 표준시보다 1시간 시계를 앞당겨 생활하는 제도, 유럽과 미국을 중심을 70여개 국가에서 시행중)

- **ISO-8601캘린더**에서 정의하는 **UTC/GMT와 오프셋**으로 날짜와 시간을 표현하는 `OffsetDateTime`을 만드는 방법

```java
LocalDateTime date = LocalDateTime.of(2014, Month.MARCH, 18, 13, 45);
OffsetDateTime dateTimeInNewYork = OffsetDateTime.of(date, newYorkOffset);
```

### 대안 캘린더 시스템 사용하기

- **ISO-8601 캘린터**는 전 세계에서 **통용**된다.
- 하지만, 자바에서는 **4개의 캘린더** 시스템을 제공한다. `ThaiBuddhistDate`, `MinguoDate`, `JapaneseDate`, `HijrahDate`

프로그램 입출력을 지역화하는 상황을 제외하고는

모든 데이터 저장, 조작, 비지니스 규칙해석 등의 작업에서 LocalDate를 사용해야 한다.
