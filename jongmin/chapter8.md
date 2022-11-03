```java
💡 배울 내용
컬렉션 팩토리 사용하기
리스트 및 집합과 사용할 새로운 관용 패턴 배우기
맵과 사용할 새로운 관용 패턴 배우기
```

## 컬렉션 팩토리

- 객체 쉽게 만드는 법

```java
List<String> friends = Arrays.asList("jongmin", "jaydee", "hihhihihi");
```

`UnsupportedOperationException` 예외 발생  → friends.add()

- 내부적으로 고정된 크기의 변환할 수 있는 배열로 구현되었기 때문에 이와 같은 일이 일어난다.
- 리스트를 인수로 받는 HashSet 생성자를 사용할 수 있지만, 불필요한 객체 할당을 필요로 함

### 리스트 팩토리

- **JAVA 9 부터 지원하는 메서드**

```java
List<String> friends = List.of("jongmin", "jaydee", "hihhihihi");
```

![스크린샷 2022-11-03 오후 1.34.37.png](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/9d9bd819-2710-4948-b0d7-95e372a3cd3c/%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA_2022-11-03_%E1%84%8B%E1%85%A9%E1%84%92%E1%85%AE_1.34.37.png)

### 집합 팩토리

```java
Set<String> friendsSet = Set.of("jongmin", "jaydee", "hihhihihi");
```

- 중복이 포함되어 있으면 `IllegalArgumentException` 발생

### 맵 팩토리

```java
Map<String,Integer> ageOfFriends = 
	Map.of("jongmin",23, "jaydee",21, "hihhihihi",26);

// 엔트리 객체 사용 (가변 인수)
Map<String,Integer> ageOfFriends2 = Map.ofEntries(Map.entry("jongmin",23),
                                    Map.entry("jaydee",21),
                                    Map.entry("hihhihihi",26));
```

- 맵에서는 `Map.Entry<K,V>` 객체를 인수로 받음
- 엔트리 객체를 사용해서 `Map.ofEntries` 팩토리 메서드를 이용하는 것이 좋다.
- `Map.ofEntries` 메서드는 키와 값을 감쌀 추가 객체 할당을 필요로 함 ! : entry()

## 리스트와 집합처리

### removeIf

- 프레디케이트를 만족하는 요소를 제거한다. List나 Set을 구현하거나 그 구현을 상속받은 모든 클래스에서 사용 가능하다.
- for-each 그냥 사용하면 반복자의 상태는 컬렉션의 상태와 서로 동기화 되지 않는다.
    
    → Iterator 객체를 명시적으로 사용하고 그 객체의 remove() 메서드를 호출함으로 이 문제를 해결할 수 있다.
    
    ```java
    for(Iterator<Transaction> iterator = transactions.iterator(); iterator.hasNext();) {
        Transaction transaction = iterator.next();
        if(Character.isDigit(transaction.getReferenceCode().charAt(0))){
            iterator.remove();
        }
    }
    ->
    transactions.removeIf(
    	transaction -> Character.isDigit(transaction.getReferenceCode().charAt(0)));
    ```
    

### replaceAll

- 리스트에서 이용할 수 있는 기능으로 UnaryOperator 함수를 이용해 요소를 바꾼다.

```java
referenceCodes.replaceAll(
		code -> Character.toUpperCase(code.charAt(0)) + code.subString(1);
```

### sort

- List 인터페이스에서 제공하는 기능으로 리스트를 정렬한다.

## 맵 처리

### forEach 메서드

```java
for(Map.Entry<String,Integer> entry : ageOfFriends2.entrySet()){
            String friend = entry.getKey();
            Integer age = entry.getValue();
            System.out.println(friend+" is "+age+" years old");
        }        
ageOfFriends2.forEach(
	(friend, age) -> System.out.println(friend+" is "+age+" years old"));
```

### 정렬 메서드

```java
ageOfFriends2.entrySet()
							.stream()
							.sorted(Map.Entry.comparingByKey())
							.forEachOrdered(System.out::println); // 알파벳 순으로 스트림 요소 처리
```

![스크린샷 2022-11-03 오후 2.17.15.png](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/f9a26b95-3465-4f79-97ff-ea4b7bf42752/%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA_2022-11-03_%E1%84%8B%E1%85%A9%E1%84%92%E1%85%AE_2.17.15.png)

### getOfDefault 메서드

- 첫번째 인수로 키, 두번째 인수로 기본값(Integer)을 받음
- 맵에 키가 존재하지 않으면 기본값 반환 → null 처리 안해도 됌 !

```java
// jongmin이 없으면 1을 반환
System.out.println(ageOfFriends2.getOrDefault("jongmin",1)); 
```

![스크린샷 2022-11-03 오후 2.27.20.png](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/bf66f8f3-6077-4044-8e3d-63fcaa8a9faa/%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA_2022-11-03_%E1%84%8B%E1%85%A9%E1%84%92%E1%85%AE_2.27.20.png)

### 계산 패턴

- 맵에 키가 존재하는지 여부에 따라 어떤 동작을 시행하고 결과를 저장해야 하는 상황이 필요한 때
    
    ### computeIfAbsent
    
    - 제공된 키에 해당하는 값이 `없거나` NULL → 키를 이용해 새 값을 계산하고 맵에 추가한다.
    
    ```java
    lines.forEach(line -> dataToHash
    	.computeIfAbsent(line, this::calculateDigest));
    									//찾을 키, 키가 존재하지 않으면 동작 실행
    ```
    
    ### computeIfPresent
    
    - 제공된 키가 `존재하면` 새 값을 계산하고 맵에 추가한다.
    
    ### compute
    
    - 제공된 키로 새 값을 계산하고 맵에 저장한다.

### 삭제 패턴

### remove

- 키가 특정한 값과 연관되어있을 때만 항목을 제거하는 오버로드 버전 메서드 제공

```java
favouriteMovies.remove(key,value); 
return -> boolean
```

![스크린샷 2022-11-03 오후 2.56.49.png](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/7af6baa7-9533-4dcb-85bc-6e62cbc5b74e/%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA_2022-11-03_%E1%84%8B%E1%85%A9%E1%84%92%E1%85%AE_2.56.49.png)

### 교체 패턴

### repleaceAll

- 각 항목의 `값을 교체`한다.    키값 아님 !!!

```java
ageOfFriends2.replaceAll((name, age) -> age+1);
```

### replace

- 키가 존재하면 맵의 값을 바꾼다. 키가 특정 값으로 매핑 되었을 때만 값을 교체하는 버전도 있다.

```java
ageOfFriends2.replace(key, value);  // key가 존재할 때 value값으로 교체
```

### 합침

### putAll

- ⚠️ 중복된 키가 없어야함
- `UnsupportedOperationException` 발생 주의

```java
ageOfFriends2.putAll(ageOfFriends);
```

→ forEach 와 merge 메서드를 이용해 충돌을 해결 할 수 있다.

```java
Map<String, Integer> everyone = new HashMap<>(ageOfFriends2);
        ageOfFriends3.forEach((k,v) -> everyone.merge(k,v,(name,age) -> name+age));
```

## 개선된 ConcurrentHashMap

- ConcurrentHashMap 클래스는 동시성 친화적이며 최신 기술을 반영한 HashMap 버전이다.
- 내부 자료구조의 특정 부분만 잠궈 동시 추가, 갱신 작업을 허용한다.
- 장점 : Map에서 상속받은 새 디폴트 메서드를 지원함과 동시에 스레드 안전성도 제공한다.

### 리듀스와 검색

 → 연산에 병렬성 기준값 (threshhold) 을 지정해야한다. (1로 지정하면 병렬성 극대화)

- 스트림에서 봤던 것과 비슷한 종류의 세 가지 새로운 연산을 지원한다.
    - forEach - 각(키, 값) 쌍에 주어진 액션을 실행
    - reduce - 모든 (키, 값) 쌍에 제공된 리듀스 함수를 이용해 결과로 합침
    - search - 널이 아닌 값을 반환할 때까지 각 (키, 값) 쌍에 함수를 적용
- 네 가지 연산 형태 지원
    - 키, 값으로 연산
    - 키로 연산
    - 값으로 연산
    - Map.Entry 객체로 연산