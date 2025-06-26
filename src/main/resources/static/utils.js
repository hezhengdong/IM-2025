// 全局配置
const CONFIG = {
    API_BASE_URL: 'http://localhost:8090',
    WS_BASE_URL: 'ws://localhost:8080'
};

// 工具函数

// 显示消息提示
function showToast(message, type = 'info') {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.className = `toast ${type}`;
    toast.style.display = 'block';
    
    setTimeout(() => {
        toast.style.display = 'none';
    }, 3000);
}

// 格式化时间
function formatTime(timeStr) {
    const date = new Date(timeStr);
    const now = new Date();
    const diff = now - date;
    
    if (diff < 60000) { // 1分钟内
        return '刚刚';
    } else if (diff < 3600000) { // 1小时内
        return Math.floor(diff / 60000) + '分钟前';
    } else if (diff < 86400000) { // 24小时内
        return Math.floor(diff / 3600000) + '小时前';
    } else {
        return date.toLocaleDateString() + ' ' + date.toLocaleTimeString().slice(0, 5);
    }
}

// HTTP请求封装
async function request(url, options = {}) {
    const userId = localStorage.getItem('userId');
    
    // 如果URL不是完整URL，则添加API前缀
    const fullUrl = url.startsWith('http') ? url : `${CONFIG.API_BASE_URL}${url}`;
    
    const defaultOptions = {
        headers: {
            'Content-Type': 'application/json',
            ...(userId && { 'userId': userId })
        }
    };
    
    const finalOptions = {
        ...defaultOptions,
        ...options,
        headers: {
            ...defaultOptions.headers,
            ...options.headers
        }
    };
    
    try {
        const response = await fetch(fullUrl, finalOptions);
        
        // 检查是否为401未授权状态
        if (response.status === 401) {
            const errorData = await response.json();
            showToast(errorData.msg || '请先登录', 'error');
            
            // 清除本地存储的用户信息
            localStorage.removeItem('userId');
            
            // 跳转到登录页面
            setTimeout(() => {
                window.location.href = '/login.html';
            }, 1500);
            
            throw new Error('未授权访问');
        }
        
        const data = await response.json();
        
        // 处理登录响应的userId
        if (url.includes('/login') && response.headers.get('userId')) {
            localStorage.setItem('userId', response.headers.get('userId'));
        }
        
        return data;
    } catch (error) {
        console.error('请求失败:', error);
        throw error;
    }
}

// 清空表单
function clearForm(formId) {
    document.getElementById(formId).reset();
}

// 转义HTML
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
