package info.kgeorgiy.ja.lihanov.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class WebCrawler implements Crawler {

    private final ExecutorService downloadService;
    private final ExecutorService extractService;
    private final Downloader downloader;

    private static final  int DEFAULT = 1;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.downloadService = Executors.newFixedThreadPool(downloaders);
        this.extractService = Executors.newFixedThreadPool(extractors);
    }

    public void recursiveCrawl(final String url, final int remains, final Set<String> visitedUrls,
                               final Map<String, IOException> errors, final Phaser phaser) {
        if (remains == 0) {
            return;
        }
        phaser.register();
        downloadService.submit(() -> {
            try {
                Document currentDocument = downloader.download(url);
                if (remains > 1) {
                    phaser.register();
                    extractService.submit(() -> {
                        try {
                            currentDocument.extractLinks().forEach(val -> {
                                if (visitedUrls.add(val)) {
                                    recursiveCrawl(val, remains - 1, visitedUrls, errors, phaser);
                                }
                            });
                        } catch (IOException e) {
                            errors.put(url, e);
                        } finally {
                            phaser.arrive();
                        }
                    });
                }
            } catch (IOException e) {
                errors.put(url, e);
            } finally {
                phaser.arrive();
            }
        });
    }

    @Override
    public Result download(String url, int depth) {
        final Set<String> visitedUrls = ConcurrentHashMap.newKeySet();
        final Map<String, IOException> errors = new ConcurrentHashMap<>();
        Phaser phaser = new Phaser();
        phaser.register();
        visitedUrls.add(url);
        recursiveCrawl(url, depth, visitedUrls, errors, phaser);
        phaser.arriveAndAwaitAdvance();
        visitedUrls.removeAll(errors.keySet());
        return new Result(List.copyOf(visitedUrls), errors);
    }

    // :NOTE: wait for termination
    @Override
    public void close() {
        shutdownService(downloadService);
        shutdownService(extractService);
    }

    private void shutdownService(ExecutorService service) {
        service.shutdown();
        try {
            if (!service.awaitTermination(30, TimeUnit.SECONDS)) {
                service.shutdownNow();
                if (!service.awaitTermination(30, TimeUnit.SECONDS)) {
                    System.err.println("Can't terminate download service");
                }
            }
        } catch (InterruptedException e) {
            service.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private static int getArgumentValue(String argument, int index) {
        try {
            return Integer.parseInt(argument);
        } catch (NumberFormatException e) {
            System.err.println("Wrong argument format at index " + index +
                    ". The program will use default value for it.");
            return DEFAULT;
        }
    }

    public static void main(String[] args) throws IOException {
        // :NOTE: args == null || args.size > 5
        if (args.length < 1 || args.length > 5 || args[0] == null) {
            System.err.println("Usage: WebCrawler url [depth [downloads [extractors [perHost]]]]");
            return;
        }
        ArrayList<Integer> argValues = new ArrayList<>();
        for (int i = 1; i < 5; i++) {
            if (i < args.length) {
                argValues.add(getArgumentValue(args[i], i));
            } else {
                argValues.add(DEFAULT);
            }
        }
        Downloader downloader = new CachingDownloader();
        WebCrawler webCrawler = new WebCrawler(downloader, argValues.get(1),
                argValues.get(2), argValues.get(3));
        Result result = webCrawler.download(args[0], argValues.get(0));
        System.out.println("Successfully downloaded: ");
        result.getDownloaded().forEach(System.out::println);
        System.out.println("Downloaded with errors: ");
        result.getErrors().forEach((val, error) -> System.out.println(val + " " + error));
    }
}
