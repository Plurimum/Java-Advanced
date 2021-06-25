package info.kgeorgiy.ja.lihanov.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HelloUDPServer implements HelloServer {

    private ExecutorService service;
    private ExecutorService mainService;
    private DatagramSocket socket;

    @Override
    public void start(int port, int threads) {
        final int bufferSize;
        try {
            socket = new DatagramSocket(port);
            socket.setSoTimeout(HelloUDPClient.TIMEOUT);
            bufferSize = socket.getReceiveBufferSize();
        } catch (SocketException e) {
            return;
        }
        service = Executors.newFixedThreadPool(threads);
        mainService = Executors.newSingleThreadExecutor();
        mainService.submit(() -> {
            while (!socket.isClosed()) {
                DatagramPacket packet = new DatagramPacket(new byte[bufferSize], bufferSize);
                try {
                    socket.receive(packet);
                    packet.setData(("Hello, " + HelloUDPClient.getStringFromPacket(packet))
                            .getBytes(StandardCharsets.UTF_8));
                    service.submit(() -> {
                        try {
                            socket.send(packet);
                        } catch (IOException ignored) {
                        }
                    });
                } catch (IOException ignored) {
                }
            }
        });
    }

    @Override
    public void close() {
        socket.close();
        HelloUDPClient.shutdownService(mainService);
        HelloUDPClient.shutdownService(service);
    }

    public static void main(String[] args) {
        if (HelloUDPClient.isArgsCorrect(args, 2, "Usage: HelloUDPServer <port> <threads>")) {
            new HelloUDPServer().start(HelloUDPClient.getArgumentValue(args[0], 0),
                    HelloUDPClient.getArgumentValue(args[1], 1));
        }
    }
}
