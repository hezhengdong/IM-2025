package org.example.im.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.example.im.chat.WebSocketService;
import org.example.im.room.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WebSocketServer {

    private final int port;

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private RoomRepository roomRepository;

    public WebSocketServer() {
        this.port = 8080;
    }

    public void run() throws Exception {
        // 创建主从线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);  // 接收连接
        EventLoopGroup workerGroup = new NioEventLoopGroup(); // 处理连接

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup) // 为服务端指定两个线程组
                    .channel(NioServerSocketChannel.class) // 创建异步非阻塞的服务器端 TCP Socket 连接
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new LoggingHandler(LogLevel.INFO))  // 添加日志处理器
                    .childHandler(new ChannelInitializer<SocketChannel>() { // 设置子通道处理器
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            ChannelPipeline pipeline = socketChannel.pipeline(); // 获取管道处理器链
                            // 添加心跳检测处理器
                            // 参数: 60秒读空闲、0秒写空闲、0秒读写空闲
                            pipeline.addLast(new IdleStateHandler(0, 0, 0));
                            // 添加HTTP编解码器
                            pipeline.addLast(new HttpServerCodec());
                            // 以块方式写
                            pipeline.addLast(new ChunkedWriteHandler());
                            // 添加HTTP消息聚合器
                            pipeline.addLast(new HttpObjectAggregator(8192));
                            // 添加自定义握手处理器（在WebSocket协议处理器之前）
                            pipeline.addLast(new AuthHandler(webSocketService));
                            // 添加WebSocket协议处理器，指定访问路径/ws
                            pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
                            // 添加自定义WebSocket处理器
                            pipeline.addLast(new SimpleWebSocketHandler(webSocketService, roomRepository));
                        }
                    });

            // 绑定端口并启动服务
//            serverBootstrap.bind(port).sync();
            Channel channel = serverBootstrap.bind(port).sync().channel();
            System.out.println("WebSocket服务器已启动，访问地址：ws://127.0.0.1:" + port + "/ws");

            // 等待服务端Socket关闭
            channel.closeFuture().sync();
        } finally {
            // 优雅关闭线程组
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
