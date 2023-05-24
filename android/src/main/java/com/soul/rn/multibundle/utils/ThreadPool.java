package com.soul.rn.multibundle.utils;

import android.content.Context;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class ThreadPool extends ThreadPoolExecutor {
    private static ThreadPool instance;

    private static final AtomicInteger mTaskId = new AtomicInteger(1);

    private ThreadPool(int corePoolSize) {
        super(corePoolSize, 50, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(10), new MyThreadFactory("RNMultiBundle"));
        instance = this;
    }

    public synchronized static ThreadPool getInstance(Context context) {
        if (instance != null) return instance;
        return new ThreadPool(Math.max(getCoreCount(), 2));
    }

    public void shutDown() {
        if (instance != null) {
            instance.shutdown();
        }
        instance = null;
    }

    public static int getCoreCount(){
        try {
            File[] files = new File("/sys/devices/system/cpu/").listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return Pattern.matches("cpu[0-9]",file.getName());
                }
            });
            if (files != null && files.length > 0) return files.length;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private static final class MyThreadFactory implements ThreadFactory {
        private final String name;
        private final AtomicInteger mCount = new AtomicInteger(1);

        MyThreadFactory(String name) {
            this.name = name;
        }

        @Override
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, name + "-" + mCount.getAndIncrement() + "-");
        }
    }


    public final Promise<ArrayList<Object>> all(final Promise ...promises) {
        Promise<ArrayList<Object>> result = new Promise() {
            @Override
            public void run() {
            }
        };
        HashSet<Integer> taskIds = new HashSet<>();
        ArrayList<Object> results = new ArrayList<>();
        if (promises.length == 0) {
            result.resolve(results);
            return result;
        }
        for (int i = 0; i < promises.length; i++) {
            results.add(null);
            final int index = i;
            Promise promise = promises[index];
            taskIds.add(promise.getId());
            promise.then(new Promise() {
                @Override
                public void run() {
                    synchronized (taskIds) {
                        try {
                            results.set(index,this.getResult());
                        } catch (Exception e) {
                            result.reject(e);
                            return;
                        }
                        taskIds.remove(promise.getId());
                        if (taskIds.size() == 0) {
                            result.resolve(results);
                        }
                    }
                }
            });
        }
        for (Promise promise : promises) {
            this.execute(promise);
        }
        return result;
    }

    public final Promise<Object> race(final Promise ...promises) {
        Promise<Object> result = new Promise() {
            @Override
            public void run() {
            }
        };
        AtomicBoolean completed = new AtomicBoolean(false);
        if (promises.length == 0) {
            result.resolve();
            return result;
        }
        for (Promise promise : promises) {
            promise.then(new Promise() {
                @Override
                public void run() {
                    synchronized (completed) {
                        if (completed.get()) return;
                        completed.set(true);
                        try {
                            result.resolve(this.getResult());
                        } catch (Exception e) {
                            result.reject(e);
                        }
                    }
                }
            });
        }
        for (Promise promise : promises) {
            this.execute(promise);
        }
        return result;
    }

    public static final Promise generateTimeoutPromise(long millsSecond) {
        return new Promise() {
            @Override
            public void run() {
                try {
                    Thread.sleep(millsSecond);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    this.resolve();
                }
            }
        };
    }

    public static final Promise generateDeferred() {
        return new Promise() {
            @Override
            public void run() {
            }
        };
    }

    private enum PromiseStatus {
        PENDING,
        FULFILLED,
        REJECTED,
    }

    public static abstract class Promise<T extends Object> implements Runnable {
        private PromiseStatus status;
        private final int taskId;
        private ArrayList<Promise> callbacks;
        private T result;
        private Exception reason;

        public Promise() {
            status = PromiseStatus.PENDING;
            taskId = mTaskId.getAndIncrement();
        }

        public final void resolve(@Nullable T data) {
            synchronized (status) {
                if (status != PromiseStatus.PENDING) return;
                status = PromiseStatus.FULFILLED;
                if (callbacks == null || callbacks.size() == 0) return;
                for (Promise promise : callbacks) {
                    promise.setResult(data);
                    promise.run();
                }
            }
        }

        public final void reject(@Nullable Exception reason) {
            synchronized (status) {
                if (status != PromiseStatus.PENDING) return;
                status = PromiseStatus.REJECTED;
                if (callbacks == null || callbacks.size() == 0) return;
                for (Promise promise : callbacks) {
                    promise.setResult(reason);
                    promise.run();
                }
            }
        }

        public final void resolve() {
            resolve(null);
        }

        public final void reject() {
            reject(null);
        }

        public final int getId() {
            return taskId;
        }

        public final Promise then(Promise promise) {
            if (this.status != PromiseStatus.PENDING) {
                try {
                    promise.setResult(this.getResult());
                } catch (Exception e) {
                    promise.setResult(e);
                }
                promise.run();
                return this;
            }
            if (callbacks == null) {
                callbacks = new ArrayList<>();
            }
            callbacks.add(promise);
            return this;
        }

        public final T getResult() throws Exception {
            if (reason != null) throw reason;
            return result;
        }

        private  void setResult(T newResult) {
            result = newResult;
        }

        private void setResult(Exception newReason) {
            reason = newReason;
        }

        @Override
        public void run() {
            resolve();
        }
    }
}
