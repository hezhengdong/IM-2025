package org.example.im.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.AttributeKey;
import org.example.im.chat.WebSocketService;
import org.example.im.room.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AuthHandler extends ChannelInboundHandlerAdapter {

    private static final AttributeKey<Integer> USER_ID = AttributeKey.valueOf("userId");

    private static final Logger logger = LoggerFactory.getLogger(AuthHandler.class);
    private final WebSocketService webSocketService;

    public AuthHandler(WebSocketService webSocketService) {
        this.webSocketService = webSocketService;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        // 这里执行了
        System.out.println("测试处理节点执行顺序: AuthHandler.handlerAdded");
        super.handlerAdded(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;

            // 解析URL参数
            QueryStringDecoder decoder = new QueryStringDecoder(request.uri());

            // 获取userId参数
            List<String> userIdParams = decoder.parameters().get("userId");
            if (userIdParams != null && !userIdParams.isEmpty()) {
                try {
                    Integer userId = Integer.valueOf(userIdParams.get(0));
                    ctx.channel().attr(USER_ID).set(userId);
                    logger.info("成功为 channel 设置属性: userId={}", userId);
                    webSocketService.connect(userId, ctx.channel());
                    logger.info("用户 {} 认证信息校验成功", userId);
                } catch (NumberFormatException e) {
                    logger.error("用户ID格式错误: {}", userIdParams.get(0));
                    ctx.close();
                    return;
                }
            } else {
                logger.error("WebSocket连接缺少userId参数");
                ctx.close();
                return;
            }

            // 更新请求URI，移除查询参数
            request.setUri(decoder.path());

            // 认证完成后移除此handler
            ctx.pipeline().remove(this);

            // 继续传递消息
            ctx.fireChannelRead(request);
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}