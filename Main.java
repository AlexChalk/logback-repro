import ch.qos.logback.classic.LoggerContext;
import java.util.concurrent.*;
import java.util.Set;
import java.net.URLClassLoader;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

class DynamicClassLoader extends URLClassLoader {
    // Simplified to avoid importing all of clojure
    public DynamicClassLoader(ClassLoader parent) {
        super(new URL[0], parent);
    }
}

public class Main {
    public static void main(String[] args) {
        aTestMethod();
    }

    public static ClassLoader baseLoader() {
        // Simplified to avoid importing all of clojure
        System.out.println("baseLoader: " + Thread.currentThread().getContextClassLoader());
        return Thread.currentThread().getContextClassLoader();
    }

    public static ClassLoader contextClassLoader() {
        return contextClassLoader(Thread.currentThread());
    }

    public static ClassLoader contextClassLoader(Thread thread) {
        return thread.getContextClassLoader();
    }

    public static ClassLoader appLoader() {

        return ClassLoader.getSystemClassLoader();
    }

    public static DynamicClassLoader rootLoader() {
        System.out.println("rootLoader: " + baseLoader());
        return rootLoader(baseLoader());
    }

    public static DynamicClassLoader rootLoader(ClassLoader cl) {
        if (cl == null) {
            return null;
        }

        ClassLoader loader = cl;
        while (true) {
            ClassLoader parent = loader.getParent();
            System.out.println("parent-checks: " + isDynamicClassLoader(parent) + " " + isPriorityClassLoader(parent));
            System.out.println("loader-checks: " + isDynamicClassLoader(loader) + " " + isPriorityClassLoader(loader));
            if (isDynamicClassLoader(parent) || isPriorityClassLoader(parent)) {
                loader = parent;
            } else if (isDynamicClassLoader(loader) || isPriorityClassLoader(loader)) {
                return (DynamicClassLoader) loader;
            } else {
                return null;
            }
        }
    }

    public static boolean isDynamicClassLoader(ClassLoader cl) {
        return cl instanceof DynamicClassLoader;
    }

    public static boolean isPriorityClassLoader(ClassLoader cl) {
        if (cl == null) {
            return false;
        }

        String name = clName(cl);
        return name != null && name.startsWith("lambdaisland/priority-classloader");
    }

    public static String clName(ClassLoader cl) {
        return cl.getName();
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

        Thread currentThread = Thread.currentThread();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Set<Thread> threads = Thread.getAllStackTraces().keySet();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        for (Thread thread : threads) {
            if (thread == currentThread
                    || rootLoader(thread.getContextClassLoader()) != null
                    || thread.getContextClassLoader() == appLoader()) {
                System.out.println("check0: " + (thread == currentThread));
                System.out.println("check1: " + (rootLoader(thread.getContextClassLoader()) != null));
                System.out.println("check2: " + (thread.getContextClassLoader() == appLoader()));
                System.out.println("thread: " + thread.getName() + " of classloader " + rootLoader(thread.getContextClassLoader()));
                System.out.println("thread: " + currentThread.getName() + " of classloader " + rootLoader(currentThread.getContextClassLoader()));
                System.out.println("classloader: " + currentThread.getContextClassLoader());
                System.out.println("appLoader: " + appLoader());
                System.out.println("foo: " + appLoader().getName());
                System.out.println("thread: " + thread.getName() + " of class " + thread.getClass());
                thread.setContextClassLoader(classLoader);
            }
        }
    }
}
