// 全局变量
let currentRoomId = null;
let rooms = [];
let roomRefreshTimer = null; // 房间列表刷新定时器

// 页面初始化
document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
});

function initializeApp() {
    // 检查是否已登录
    const userId = localStorage.getItem('userId');
    if (userId) {
        showPage('chatPage');
        loadRooms();
        
        // 连接WebSocket，如果失败则自动跳转回登录页面
        wsManager.connect().then(connected => {
            if (!connected) {
                logout();
                showToast('连接服务器失败，请重新登录', 'error');
                return;
            }
            startRoomRefresh(); // 启动房间列表自动刷新
        });
    } else {
        showPage('loginPage');
    }
    
    // 绑定事件
    bindEvents();
}

function bindEvents() {
    // 页面切换
    document.getElementById('toRegister').addEventListener('click', (e) => {
        e.preventDefault();
        showPage('registerPage');
    });
    
    document.getElementById('toLogin').addEventListener('click', (e) => {
        e.preventDefault();
        showPage('loginPage');
    });
    
    // 表单提交
    document.getElementById('loginForm').addEventListener('submit', handleLogin);
    document.getElementById('registerForm').addEventListener('submit', handleRegister);
    
    // 聊天功能
    document.getElementById('sendBtn').addEventListener('click', sendMessage);
    document.getElementById('messageInput').addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            sendMessage();
        }
    });
    
    // 退出登录
    document.getElementById('logoutBtn').addEventListener('click', logout);
}

// 页面切换
function showPage(pageId) {
    document.querySelectorAll('.page').forEach(page => {
        page.classList.remove('active');
    });
    document.getElementById(pageId).classList.add('active');
}

// 登录处理
async function handleLogin(e) {
    e.preventDefault();
    
    const username = document.getElementById('loginUsername').value.trim();
    const password = document.getElementById('loginPassword').value.trim();
    
    if (!username || !password) {
        showToast('请填写完整信息', 'error');
        return;
    }
    
    try {
        const response = await request('/login', {
            method: 'POST',
            body: JSON.stringify({ username, password })
        });
        
        if (response.code === 1) {
            showToast('登录成功', 'success');
            clearForm('loginForm');
            showPage('chatPage');
            loadRooms();
            
            // 连接WebSocket，如果失败则自动跳转回登录页面
            const wsConnected = await wsManager.connect();
            if (!wsConnected) {
                logout();
                showToast('连接服务器失败，请重新登录', 'error');
                return;
            }
            
            startRoomRefresh(); // 启动房间列表自动刷新
        } else {
            showToast(response.msg || '登录失败', 'error');
        }
    } catch (error) {
        showToast('网络错误，请重试', 'error');
    }
}

// 注册处理
async function handleRegister(e) {
    e.preventDefault();
    
    const username = document.getElementById('registerUsername').value.trim();
    const password = document.getElementById('registerPassword').value.trim();
    
    if (!username || !password) {
        showToast('请填写完整信息', 'error');
        return;
    }
    
    if (password.length < 4) {
        showToast('密码至少4位', 'error');
        return;
    }
    
    try {
        const response = await request('/register', {
            method: 'POST',
            body: JSON.stringify({ username, password })
        });
        
        if (response.code === 1) {
            showToast('注册成功，请登录', 'success');
            clearForm('registerForm');
            showPage('loginPage');
        } else {
            showToast(response.msg || '注册失败', 'error');
        }
    } catch (error) {
        showToast('网络错误，请重试', 'error');
    }
}

// 加载房间列表
async function loadRooms() {
    try {
        const response = await request('/rooms');
        
        if (response.code === 1) {
            rooms = response.data;
            renderRooms();
        } else {
            showToast('加载房间失败', 'error');
        }
    } catch (error) {
        showToast('网络错误', 'error');
    }
}

// 渲染房间列表
function renderRooms() {
    const roomsList = document.getElementById('roomsList');
    roomsList.innerHTML = '';
    
    // 分离公共房间和其他房间
    const publicRooms = rooms.filter(room => room.type === 1);
    const otherRooms = rooms.filter(room => room.type !== 1);
    
    // 先渲染公共房间
    publicRooms.forEach(room => {
        const roomElement = createRoomElement(room, true);
        roomsList.appendChild(roomElement);
    });
    
    // 再渲染其他房间
    otherRooms.forEach(room => {
        const roomElement = createRoomElement(room, false);
        roomsList.appendChild(roomElement);
    });
}

// 创建房间元素
function createRoomElement(room, isPublic) {
    const roomDiv = document.createElement('div');
    roomDiv.className = `room-item ${isPublic ? 'public' : ''}`;
    roomDiv.dataset.roomId = room.id;
    
    roomDiv.innerHTML = `
        <div class="room-name">${escapeHtml(room.name)}</div>
        <div class="room-last-msg">${escapeHtml(room.lastMsg || '暂无消息')}</div>
    `;
    
    roomDiv.addEventListener('click', () => selectRoom(room.id, room.name));
    
    return roomDiv;
}

// 选择房间
async function selectRoom(roomId, roomName) {
    // 更新UI
    document.querySelectorAll('.room-item').forEach(item => {
        item.classList.remove('active');
    });
    document.querySelector(`[data-room-id="${roomId}"]`).classList.add('active');
    
    document.getElementById('currentRoomName').textContent = roomName;
    document.getElementById('chatInput').style.display = 'block';
    
    // 清空聊天区域
    const chatMessages = document.getElementById('chatMessages');
    chatMessages.innerHTML = '';
    
    currentRoomId = roomId;
    wsManager.setCurrentRoom(roomId);
    
    // 加载历史消息
    await loadMessages(roomId);
}

// 加载历史消息
async function loadMessages(roomId) {
    try {
        const response = await request(`/${roomId}/messages`);
        
        if (response.code === 1) {
            const messages = response.data;
            messages.forEach(message => {
                displayMessage(message);
            });
            scrollToBottom();
        } else {
            showToast('加载消息失败', 'error');
        }
    } catch (error) {
        showToast('网络错误', 'error');
    }
}

// 显示消息
function displayMessage(message) {
    const chatMessages = document.getElementById('chatMessages');
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${message.isSelf === 'true' ? 'self' : ''}`;
    
    const time = formatTime(message.time);
    
    // 创建头像元素
    const avatarText = message.isSelf === 'true' ? '我' : (message.senderName || 'U').charAt(0).toUpperCase();
    
    messageDiv.innerHTML = `
        <div class="message-avatar">${escapeHtml(avatarText)}</div>
        <div class="message-content">
            ${message.isSelf === 'false' ? `<div class="message-header">${escapeHtml(message.senderName)}</div>` : ''}
            <div class="message-text">${escapeHtml(message.content)}</div>
            <div class="message-time">${time}</div>
        </div>
    `;
    
    chatMessages.appendChild(messageDiv);
    scrollToBottom();
}

// 发送消息
function sendMessage() {
    const messageInput = document.getElementById('messageInput');
    const content = messageInput.value.trim();
    
    if (!content) {
        showToast('请输入消息内容', 'error');
        return;
    }
    
    if (!currentRoomId) {
        showToast('请先选择房间', 'error');
        return;
    }
    
    if (wsManager.sendMessage(currentRoomId, content)) {
        messageInput.value = '';
        
        // 立即显示自己发送的消息
        const selfMessage = {
            senderName: '我',
            content: content,
            type: 'text',
            time: new Date().toISOString(),
            isSelf: 'true'
        };
        displayMessage(selfMessage);
        
        // 更新房间列表
        updateRoomLastMessage(currentRoomId, content);
    }
}

// 更新房间最后一条消息
function updateRoomLastMessage(roomId, content) {
    const roomElement = document.querySelector(`[data-room-id="${roomId}"]`);
    if (roomElement) {
        const lastMsgElement = roomElement.querySelector('.room-last-msg');
        lastMsgElement.textContent = content;
    }
    
    // 更新rooms数组
    const room = rooms.find(r => r.id == roomId);
    if (room) {
        room.lastMsg = content;
    }
}

// 滚动到底部
function scrollToBottom() {
    const chatMessages = document.getElementById('chatMessages');
    chatMessages.scrollTop = chatMessages.scrollHeight;
}

// 启动房间列表自动刷新
function startRoomRefresh() {
    // 清除已存在的定时器
    stopRoomRefresh();
    
    // 设置每1秒刷新一次房间列表
    roomRefreshTimer = setInterval(() => {
        loadRooms();
    }, 1000);
}

// 停止房间列表自动刷新
function stopRoomRefresh() {
    if (roomRefreshTimer) {
        clearInterval(roomRefreshTimer);
        roomRefreshTimer = null;
    }
}

// 退出登录
function logout() {
    localStorage.removeItem('userId');
    wsManager.disconnect();
    stopRoomRefresh(); // 停止房间列表自动刷新
    currentRoomId = null;
    rooms = [];
    
    // 重置聊天界面
    document.getElementById('roomsList').innerHTML = '';
    document.getElementById('chatMessages').innerHTML = '<div class="welcome-message"><p>欢迎使用IM聊天系统</p><p>请从左侧选择一个房间开始聊天</p></div>';
    document.getElementById('currentRoomName').textContent = '请选择一个房间开始聊天';
    document.getElementById('chatInput').style.display = 'none';
    
    showPage('loginPage');
    showToast('已退出登录', 'success');
}
