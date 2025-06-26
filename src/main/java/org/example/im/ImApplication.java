package org.example.im;

import org.example.im.websocket.WebSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ImApplication implements CommandLineRunner {

    @Autowired
    private WebSocketServer webSocketServer;

    public static void main(String[] args) {
        SpringApplication.run(ImApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // 在新线程中启动 WebSocket 服务器，避免阻塞 Spring Boot 启动
        new Thread(() -> {
            try {
                webSocketServer.run();
            } catch (Exception e) {
                System.err.println("WebSocket服务器启动失败: " + e.getMessage());
            }
        }).start();
    }
}
