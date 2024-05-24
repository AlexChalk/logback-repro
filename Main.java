import ch.qos.logback.classic.LoggerContext;
import java.util.concurrent.*;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        aTestMethod();
    }

    static void aTestMethod() {
        LoggerContext context = new LoggerContext();

        Runnable aRunnable = new Runnable() {
            public void run() {
                System.out.println("hello");
            }
        };

        ScheduledExecutorService scheduledExecutorService = context.getScheduledExecutorService();
        ScheduledFuture<?> scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(aRunnable,
                0, 1000, TimeUnit.MILLISECONDS);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Set<Thread> threads = Thread.getAllStackTraces().keySet();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        for (Thread thread : threads) {
            thread.setContextClassLoader(classLoader);
        }
    }
}
