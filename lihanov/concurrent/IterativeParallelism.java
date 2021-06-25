package info.kgeorgiy.ja.lihanov.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class IterativeParallelism implements ListIP {

    private final ParallelMapper parallelMapper;

    public IterativeParallelism() {
        this(null);
    }

    public IterativeParallelism(final ParallelMapper parallelMapper) {
        this.parallelMapper = parallelMapper;
    }

    private <T, V> List<V> parallelApply(
            int threads,
            final List<T> values,
            final Function<Stream<T>, V> threadFunction
    ) throws InterruptedException {
        if (threads <= 0) {
            throw new IllegalArgumentException("Count of threads should be not greater than zero");
        }
        if (values.size() == 0) {
            return List.of();
        }

        threads = Math.min(threads, values.size());
        final List<Stream<T>> tasks = new ArrayList<>();
        final int fragmentSize = values.size() / threads;
        int remains = values.size() % threads;
        for (int i = 0, right = 0; i < threads; i++) {
            final int left = right;
            right = left + fragmentSize;
            if (remains > 0) {
                remains--;
                right++;
            }
            tasks.add(values.subList(left, right).stream());
        }

        return parallelMapper != null ? parallelMapper.map(threadFunction, tasks) : map(threadFunction, tasks);
    }

    private static <T, V> List<V> map(final Function<Stream<T>, V> threadFunction, final List<Stream<T>> tasks) throws InterruptedException {
        final List<V> result = new ArrayList<>(Collections.nCopies(tasks.size(), null));

        final RuntimeException runtimeException = new RuntimeException();
        final List<Thread> threadList = IntStream.range(0, tasks.size())
                .mapToObj(i -> new Thread(() -> {
                    try {
                        result.set(i, threadFunction.apply(tasks.get(i)));
                    } catch (final RuntimeException e) {
                        synchronized (runtimeException) {
                            runtimeException.addSuppressed(e);
                        }
                    }
                }))
                .collect(Collectors.toList());
        threadList.forEach(Thread::start);
        if (runtimeException.getSuppressed().length != 0) {
            // :NOTE: ??
            throw runtimeException;
        }
        for (final Thread thread : threadList) {
            thread.join();
        }
        return result;
    }

    private static <T> T maxOfStream(final Stream<T> stream, final Comparator<? super T> comparator) {
        return stream.max(comparator).orElseThrow();
    }

    @Override
    public <T> T maximum(
            final int threads,
            final List<? extends T> values,
            final Comparator<? super T> comparator) throws InterruptedException {
        return maxOfStream(parallelApply(threads, values, stream -> maxOfStream(stream, comparator)).stream(), comparator);
    }

    @Override
    public <T> T minimum(
            final int threads,
            final List<? extends T> values,
            final Comparator<? super T> comparator
    ) throws InterruptedException {
        return maximum(threads, values, comparator.reversed());
    }

    @Override
    public <T> boolean all(
            final int threads,
            final List<? extends T> values,
            final Predicate<? super T> predicate) throws InterruptedException {
        return parallelApply(threads, values, stream -> stream.allMatch(predicate))
                .stream().allMatch(Boolean::booleanValue);
    }

    @Override
    public <T> boolean any(final int threads, final List<? extends T> values, final Predicate<? super T> predicate) throws InterruptedException {
        return !all(threads, values, predicate.negate());
    }

    @Override
    public String join(final int threads, final List<?> values) throws InterruptedException {
        return String.join("", parallelApply(
                threads,
                values,
                // :NOTE: NPE
                stream -> stream.map(Object::toString).collect(Collectors.joining())
        ));
    }

    @Override
    public <T> List<T> filter(final int threads, final List<? extends T> values, final Predicate<? super T> predicate) throws InterruptedException {
        // :NOTE: Дубль
        return parallelApply(
                threads,
                values,
                stream -> stream.filter(predicate).collect(Collectors.toList())
        ).stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public <T, U> List<U> map(final int threads, final List<? extends T> values, final Function<? super T, ? extends U> function) throws InterruptedException {
        // :NOTE: Дубль
        return parallelApply(
                threads,
                values,
                stream -> stream.map(function).collect(Collectors.toList())
        ).stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
