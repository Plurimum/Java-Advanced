package info.kgeorgiy.ja.lihanov.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ParallelMapperImpl implements ParallelMapper {

    private final SynchronizedRunnablesQueue runnablesQueue = new SynchronizedRunnablesQueue();
    private final List<Thread> threadList;
    private boolean closed;

    public ParallelMapperImpl(final int threads) throws InterruptedException {
        if (threads < 0) {
            throw new IllegalArgumentException("Count of threads should be not greater than zero");
        }

        final Runnable runnable = () -> {
            try {
                while (!Thread.interrupted()) {
                    runnablesQueue.poll().run();
                }
            } catch (final InterruptedException ignored) {
            } finally {
                Thread.currentThread().interrupt();
            }
        };
        threadList = Stream.generate(() -> new Thread(runnable)).limit(threads).collect(Collectors.toList());
        threadList.forEach(Thread::start);
    }

    @Override
    public <T, R> List<R> map(
            final Function<? super T, ? extends R> f,
            final List<? extends T> args
    ) throws InterruptedException {
        final SynchronizedResults<R> results = new SynchronizedResults<>(args.size());
        IntStream.range(0, args.size()).forEach(i -> runnablesQueue.add(() -> {
            if (closed) {
                Thread.currentThread().interrupt();
                return;
            }
            results.set(i, f.apply(args.get(i)));
        }));
        return results.getData();
    }

    // :NOTE: Оставшиеся потоки
    // :NOTE: "Подвисншие" потоки
    @Override
    public void close() {
        closed = true;
        threadList.forEach(Thread::interrupt);
        for (final Thread thread : threadList) {
            try {
                thread.join();
            } catch (InterruptedException ignored) {
            }
        }
        runnablesQueue.runnables.forEach(Runnable::run);
    }

    private static class SynchronizedResults<T> {
        private final List<T> data;
        private int remains;

        private SynchronizedResults(final int size) {
            this.data = new ArrayList<>(Collections.nCopies(size, null));
            remains = size;
        }

        public synchronized void set(final int index, final T value) {
            data.set(index, value);
            if (--remains == 0) {
                notify();
            }
        }

        public synchronized List<T> getData() throws InterruptedException {
            while (remains > 0) {
                wait();
            }
            return data;
        }
    }

    private static class SynchronizedRunnablesQueue {
        private final Queue<Runnable> runnables = new ArrayDeque<>();

        public synchronized void add(final Runnable runnable) {
            runnables.add(runnable);
            notify();
        }

        public synchronized Runnable poll() throws InterruptedException {
            while (runnables.isEmpty()) {
                wait();
            }
            return runnables.poll();
        }

    }
}
