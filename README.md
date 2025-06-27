# IM

技术栈：Spring Boot、Spring Data JPA、Netty、MySQL、MinIO

[接口文档](https://q76vzhue63.apifox.cn)

[详细设计](doc/详细设计.md)



已实现功能

- 聊天
    - 单聊
    - 广播（项目内表现为包含所有人的公共群组）
- 文件传输

待实现功能

- 文件传输
    - 分片上传
    - 断点续传
    - 限速下载
- 聊天
    - 端到端加密
    - 消息可靠性
    - 消息时序一致性
    - （还有很多，例如推拉模式权衡、心跳机制维护在线状态等等）

不打算实现的

- 自定义群聊（更多是业务功能，一个公共群聊足够学习相关技术了）
- 音视频通话（不从事相关行业，没有太大用处）



启动 Windows 下安装的 MinIO

```bash
.\minio.exe server .\storage --console-address :9001
```

内网穿透配置

```bash
# 配置文件
C:\Users\{Windows用户名}\AppData\Local\ngrok\ngrok.yml
```

```yml
version: "2"
authtoken: {自己的token}
tunnels:
    http:
        proto: http
        addr: 8090
    websocket:
        proto: http
        addr: 8080
```

启动内网穿透

```bash
ngrok.exe start --all
```

