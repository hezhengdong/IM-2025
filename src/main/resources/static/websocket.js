// WebSocket管理
class WebSocketManager {
    constructor() {
        this.ws = null;
        this.currentRoomId = null;
        this.reconnectTimer = null;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.isInitialConnection = true; // 标记是否为初始连接
    }
    
    connect() {
        const userId = localStorage.getItem('userId');
        if (!userId) {
            console.error('未找到用户ID，跳转到登录页面');
            showToast('请先登录', 'error');
            setTimeout(() => {
                window.location.href = '/login.html';
            }, 1500);
            return Promise.resolve(false);
        }
        
        return new Promise((resolve) => {
            try {
                this.ws = new WebSocket(`${CONFIG.WS_BASE_URL}/ws?userId=${userId}`);
                
                this.ws.onopen = () => {
                    console.log('WebSocket连接已建立');
                    this.reconnectAttempts = 0;
                    this.isInitialConnection = false;
                    showToast('连接成功', 'success');
                    resolve(true);
                };
                
                this.ws.onmessage = (event) => {
                    try {
                        const message = JSON.parse(event.data);
                        this.handleMessage(message);
                    } catch (error) {
                        console.error('解析消息失败:', error);
                    }
                };
                
                this.ws.onclose = (event) => {
                    console.log('WebSocket连接已关闭', event.code, event.reason);
                    
                    // 检查是否为认证失败导致的关闭
                    if (event.code === 1008 || event.code === 1003) {
                        showToast('身份验证失败，请重新登录', 'error');
                        localStorage.removeItem('userId');
                        setTimeout(() => {
                            window.location.href = '/login.html';
                        }, 1500);
                        resolve(false);
                        return;
                    }
                    
                    if (this.isInitialConnection) {
                        // 初始连接失败，返回false
                        resolve(false);
                    } else {
                        // 非初始连接断开，尝试重连
                        this.attemptReconnect();
                    }
                };
                
                this.ws.onerror = (error) => {
                    console.error('WebSocket错误:', error);
                    if (this.isInitialConnection) {
                        showToast('连接服务器失败', 'error');
                        resolve(false);
                    } else {
                        showToast('连接异常', 'error');
                    }
                };
                
                // 设置连接超时
                setTimeout(() => {
                    if (this.isInitialConnection && this.ws.readyState !== WebSocket.OPEN) {
                        this.ws.close();
                        resolve(false);
                    }
                }, 5000); // 5秒超时
                
            } catch (error) {
                console.error('WebSocket连接失败:', error);
                showToast('连接失败', 'error');
                resolve(false);
            }
        });
    }
    
    attemptReconnect() {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            console.log(`尝试重连 (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);
            
            this.reconnectTimer = setTimeout(() => {
                this.connect();
            }, 3000 * this.reconnectAttempts);
        } else {
            showToast('连接断开，请刷新页面重试', 'error');
        }
    }
    
    sendMessage(roomId, content) {
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
            const message = {
                type: 'text',
                roomId: roomId,
                content: content
            };
            
            this.ws.send(JSON.stringify(message));
            return true;
        } else {
            showToast('连接断开，无法发送消息', 'error');
            return false;
        }
    }
    
    handleMessage(message) {
        // 只有在已选择房间且消息属于当前房间时才显示
        if (!this.currentRoomId || !message.roomId || message.roomId !== this.currentRoomId) {
            return;
        }
        
        displayMessage(message);
        
        // 更新房间列表中的最后一条消息
        updateRoomLastMessage(message.roomId, message.content);
    }
    
    setCurrentRoom(roomId) {
        this.currentRoomId = roomId;
    }
    
    disconnect() {
        if (this.reconnectTimer) {
            clearTimeout(this.reconnectTimer);
        }
        
        if (this.ws) {
            this.ws.close();
            this.ws = null;
        }
        
        // 重置初始连接标记
        this.isInitialConnection = true;
    }
}

// 全局WebSocket管理器实例
const wsManager = new WebSocketManager();
