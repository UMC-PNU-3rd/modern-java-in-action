import java.io.File;
import java.io.FileFilter;
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

        ExecutorService executorService = Executors.newCachedThreadPool();
        Future<String> threadName = ex



    }
}