import java.io.File;
import java.io.FileFilter;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {
    public static void main(String[] args) {
        //기존 방식
        File[] hiddenFiles = new File(".").listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isHidden();
            }
        });

         // 메서드 참조
        File[] hiddenFiles1 = new File(".").listFiles(File::isHidden);

        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("123");
            }
        });

        Thread t = new Thread(() -> System.out.println("123"));

        Comparator<Apple> byWeight = new Comparator<Apple>() {
            @Override
            public int compare(Apple a1, Apple a2) {
                return a1.getWeight().compareTo(a2.getWeight());
            }
        };
        Comparator<Apple> byweight = (Apple a1, Apple a2) -> a1.getWeight().compareTo(a2.getWeight());

//        (String s) -> s.length();
//        (Apple a ) -> a.getWeight() >150;

        //  ㅁㅐ개변수     ->   반환값
        // (parameters) -> expression
        // or
        // (parameters) -> { statements; }

        Runnable r1 = () -> System.out.println("hello world"); // 람다 표현식

        Runnable r2 = new Runnable() {      // 익명클래스 사용
            @Override
            public void run() {
                System.out.println("hello world2");
            }
        };
        public static void process(Runnable r){
            r.run();
        }

        process(r1); // hello world 출력
        process(r2); // hello world2 출력
        process(() -> System.out.println("hello world3")); // 직접 전달된 람다 표현식으로 출력

        



    }
}
class Apple {
    int weight;
    public int getWeight(){
        return this.weight;
    }
}