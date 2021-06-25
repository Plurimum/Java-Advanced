package info.kgeorgiy.ja.lihanov.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class HelloUDPClient implements HelloClient {

    public static final int TIMEOUT = 500;

    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        ExecutorService service = Executors.newFixedThreadPool(threads);
        IntStream.range(0, threads).forEach(thread ->
                service.submit(() -> {
                    try (DatagramSocket socket = new DatagramSocket()) {
                        socket.setSoTimeout(TIMEOUT);
                        final int bufferSize = socket.getReceiveBufferSize();
                        DatagramPacket requestPacket = new DatagramPacket(new byte[0], 0,
                                new InetSocketAddress(InetAddress.getByName(host), port));
                        DatagramPacket responsePacket = new DatagramPacket(new byte[bufferSize], bufferSize);
                        IntStream.range(0, requests).forEach(request -> {
                            String dataString = (prefix + thread + '_' + request);
                            requestPacket.setData(dataString.getBytes(StandardCharsets.UTF_8));
                            while (!socket.isClosed()) {
                                try {
                                    socket.send(requestPacket);
                                    socket.receive(responsePacket);
                                } catch (IOException ignored) {
                                }
                                String responseString = getStringFromPacket(responsePacket);
                                System.out.println("Request: " + dataString);
                                System.out.println("Response: " + responseString + '\n');
                                if (("Hello, " + dataString).equals(responseString)) {
                                    break;
                                }
                            }
                        });
                    } catch (SocketException | UnknownHostException ignored) {
                    }
                })
        );
        shutdownService(service);
    }

    public static void shutdownService(ExecutorService service) {
        service.shutdown();
        try {
            if (!service.awaitTermination(10, TimeUnit.SECONDS)) {
                service.shutdownNow();
                if (!service.awaitTermination(10, TimeUnit.SECONDS)) {
                    System.err.println("Can't terminate download service");
                }
            }
        } catch (InterruptedException e) {
            service.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static String getStringFromPacket(DatagramPacket packet) {
        return new String(packet.getData(),
                packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
    }

    public static int getArgumentValue(String argument, int index) {
        try {
            return Integer.parseInt(argument);
        } catch (NumberFormatException e) {
            System.err.println("Wrong argument format at index " + index +
                    ". The program will use default value for it.");
            return 0;
        }
    }

    public static boolean isArgsCorrect(String[] args, int count, String usage) {
        if (args == null || args.length != count) {
            System.out.println(usage);
            return false;
        }
        for (String arg : args) {
            if (arg == null) {
                System.out.println("null is illegal argument\n" + usage);
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        if (isArgsCorrect(args, 5,
                "Usage: HelloUDPClient <host> <port> <prefix> <threads> <requests count>")) {
            new HelloUDPClient().run(args[0], getArgumentValue(args[1], 1),
                    args[2], getArgumentValue(args[3], 3), getArgumentValue(args[4], 4));
        }
    }
}
