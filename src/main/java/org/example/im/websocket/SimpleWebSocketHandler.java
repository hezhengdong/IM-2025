package org.example.im.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import org.example.im.chat.MessageRequest;
import org.example.im.chat.WebSocketService;
import org.example.im.room.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleWebSocketHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private static final Logger logger = LoggerFactory.getLogger(SimpleWebSocketHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebSocketService webSocketService;
    private final RoomRepository roomRepository;

    public SimpleWebSocketHandler(WebSocketService webSocketService, RoomRepository roomRepository) {
        this.webSocketService = webSocketService;
        this.roomRepository = roomRepository;
    }

    // 当WebSocket客户端连接成功时触发 @OnOpen
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Integer userId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();
        System.err.println("这里是不会获取 userId 的。测试 userId：" + userId);
        logger.info("客户端建立 WebSocket 连接: {}", ctx.channel().id().asShortText());
    }

    // 当WebSocket客户端断开连接时触发 @OnClose
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Integer userId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();
        webSocketService.disconnect(userId, ctx.channel());
        logger.info("客户端断开 WebSocket 连接: {}", ctx.channel().id().asShortText());
    }

    // 处理接收到的WebSocket消息 @OnMessage
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {

        // 握手阶段就将userId保存在了channel中！！！！！

        if (frame instanceof TextWebSocketFrame) {
            String requestString = ((TextWebSocketFrame) frame).text();
            try {
                MessageRequest request = objectMapper.readValue(requestString, MessageRequest.class);
                logger.info("WebSocket 数据解析成功: {}", requestString);
                handleMessage(ctx, request);
            } catch (Exception e) {
                logger.error("WebSocket 数据解析失败: {}", e.getMessage());
                ctx.channel().writeAndFlush(new TextWebSocketFrame("{\"type\":\"ERROR\",\"content\":\"消息格式错误\"}"));
            }
        } else {
            String message = "不支持的消息类型: " + frame.getClass().getName();
            throw new UnsupportedOperationException(message);
        }
    }

    private void handleMessage(ChannelHandlerContext ctx, MessageRequest request) {
        // 获取需要的参数
        Integer roomId = request.getRoomId();
        String content = request.getContent();
        String messageType = request.getType();
        Integer roomType = roomRepository.findTypeById(roomId);
        Integer senderId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();

        // 处理消息
        switch (roomType) {
            case 0:
                // 单聊消息
                logger.info("单聊消息: {}", request);
                boolean b1 = webSocketService.sendPrivateMessage(senderId, roomId, content, messageType);
                break;
            case 1:
                // 广播消息
                logger.info("广播消息: {}", request);
                boolean b2 = webSocketService.broadcastMessage(senderId, roomId, content, messageType);
                break;
            default:
                logger.error("未知消息类型: {}", request.getType());
        }
    }

    // 用户事件触发（主要用于心跳检测）
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                logger.info("读空闲，关闭连接: {}", ctx.channel().id().asShortText());
                ctx.close();
            } else if (event.state() == IdleState.WRITER_IDLE) {
                logger.info("写空闲，发送心跳: {}", ctx.channel().id().asShortText());
                ctx.writeAndFlush(new TextWebSocketFrame("{\"type\":\"HEARTBEAT\",\"content\":\"ping\"}"));
            }
        }
    }

    // 异常处理 @OnError
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("异常发生：" + cause.getMessage());
        ctx.close();
    }
}
