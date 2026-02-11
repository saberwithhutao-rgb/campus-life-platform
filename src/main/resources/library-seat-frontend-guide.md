# 图书馆座位管理 - 前端调用指南

## 接口信息

### 1. 进入座位接口

- **接口地址**: `/api/library/seat/enter`
- **请求方式**: POST
- **请求参数**: 无
- **返回格式**:
  ```json
  {
    "code": 200,
    "message": "进入座位成功",
    "data": {
      "id": 1,
      "totalSeats": 100,
      "occupiedSeats": 51
    }
  }
  ```

### 2. 离开座位接口

- **接口地址**: `/api/library/seat/leave`
- **请求方式**: POST
- **请求参数**: 无
- **返回格式**:
  ```json
  {
    "code": 200,
    "message": "离开座位成功",
    "data": {
      "id": 1,
      "totalSeats": 100,
      "occupiedSeats": 50
    }
  }
  ```

### 3. 获取座位状态接口

- **接口地址**: `/api/library/seat/status`
- **请求方式**: GET
- **请求参数**: 无
- **返回格式**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "id": 1,
      "totalSeats": 100,
      "occupiedSeats": 50
    }
  }
  ```

## 前端调用代码示例

### 1. 安装Axios（如果未安装）

```bash
# 使用npm
npm install axios

# 使用yarn
yarn add axios
```

### 2. 创建API调用模块

```javascript
// api.js
import axios from "axios";

// 创建axios实例
const api = axios.create({
  baseURL: "http://localhost:8080", // 后端服务地址
  timeout: 10000, // 请求超时时间
  headers: {
    "Content-Type": "application/json",
  },
});

// 图书馆座位管理相关API
export const librarySeatApi = {
  // 进入座位
  enterSeat: () => {
    return api.post("/api/library/seat/enter");
  },

  // 离开座位
  leaveSeat: () => {
    return api.post("/api/library/seat/leave");
  },

  // 获取座位状态
  getSeatStatus: () => {
    return api.get("/api/library/seat/status");
  },
};

export default api;
```

### 3. 组件中使用示例

#### Vue 3 组件示例

```vue
<template>
  <div class="library-seat-container">
    <h2>图书馆座位管理</h2>

    <div class="seat-status">
      <h3>当前状态</h3>
      <p>总座位数: {{ totalSeats }}</p>
      <p>已占用座位: {{ occupiedSeats }}</p>
      <p>可用座位: {{ availableSeats }}</p>
      <button @click="refreshStatus" class="btn-refresh">刷新状态</button>
    </div>

    <div class="seat-actions">
      <button
        @click="enterSeat"
        class="btn-enter"
        :disabled="isLoading || occupiedSeats >= totalSeats"
      >
        {{ isLoading ? '处理中...' : '进入座位' }}
      </button>

      <button
        @click="leaveSeat"
        class="btn-leave"
        :disabled="isLoading || occupiedSeats <= 0"
      >
        {{ isLoading ? '处理中...' : '离开座位' }}
      </button>
    </div>

    <div v-if="message" class="message" :class="messageType">
      {{ message }}
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { librarySeatApi } from './api';

const totalSeats = ref(0);
const occupiedSeats = ref(0);
const isLoading = ref(false);
const message = ref('');
const messageType = ref('info');

const availableSeats = computed(() => {
  return total
```
Seats.value - occupiedSeats.value;
});

// 刷新座位状态
const refreshStatus = async () => {
  try {
    isLoading.value = true;
    const response = await librarySeatApi.getSeatStatus();
    if (response.data.code === 200) {
      totalSeats.value = response.data.data.totalSeats;
      occupiedSeats.value = response.data.data.occupiedSeats;
      showMessage('状态刷新成功', 'success');
    } else {
      showMessage(response.data.message || '获取状态失败', 'error');
    }
  } catch (error) {
    console.error('获取座位状态失败:', error);
    showMessage('网络错误，请稍后重试', 'error');
  } finally {
    isLoading.value = false;
  }
};

// 进入座位
const enterSeat = async () => {
  try {
    isLoading.value = true;
    const response = await librarySeatApi.enterSeat();
    if (response.data.code === 200) {
      totalSeats.value = response.data.data.totalSeats;
      occupiedSeats.value = response.data.data.occupiedSeats;
      showMessage('进入座位成功', 'success');
    } else {
      showMessage(response.data.message || '进入座位失败', 'error');
    }
  } catch (error) {
    console.error('进入座位失败:', error);
    showMessage('网络错误，请稍后重试', 'error');
  } finally {
    isLoading.value = false;
  }
};

// 离开座位
const leaveSeat = async () => {
  try {
    isLoading.value = true;
    const response = await librarySeatApi.leaveSeat();
    if (response.data.code === 200) {
      totalSeats.value = response.data.data.totalSeats;
      occupiedSeats.value = response.data.data.occupiedSeats;
      showMessage('离开座位成功', 'success');
    } else {
      showMessage(response.data.message || '离开座位失败', 'error');
    }
  } catch (error) {
    console.error('离开座位失败:', error);
    showMessage('网络错误，请稍后重试', 'error');
  } finally {
    isLoading.value = false;
  }
};

// 显示消息
const showMessage = (msg, type = 'info') => {
  message.value = msg;
  messageType.value = type;
  
  // 3秒后自动清除消息
  setTimeout(() => {
    message.value = '';
  }, 3000);
};

// 组件挂载时获取初始状态
onMounted(() => {
  refreshStatus();
});
</script>

<style scoped>
.library-seat-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background-color: #f9f9f9;
}

.seat-status {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.seat-actions {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

button {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  transition: background-color 0.3s;
}

.btn-refresh {
  background-color: #f0f0f0;
}

.btn-refresh:hover {
  background-color: #e0e0e0;
}

.btn-enter {
  background-color: #4CAF50;
  color: white;
  flex: 1;
}

.btn-enter:hover:not(:disabled) {
  background-color: #45a049;
}

.btn-leave {
  background-color: #f44336;
  color: white;
  flex: 1;
}

.btn-leave:hover:not(:disabled) {
  background-color: #da190b;
}

button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.message {
  padding: 10px;
  border-radius: 4px;
  margin-top: 10px;
}

.message.success {
  background-color: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.message.error {
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.message.info {
  background-color: #d1ecf1;
  color: #0c5460;
  border: 1px solid #bee5eb;
}
</style>
```

#### React 组件示例

```javascript
import React, { useState, useEffect } from 'react';
import { librarySeatApi } from './api';

function LibrarySeatManager() {
  const [totalSeats, setTotalSeats] = useState(0);
  const [occupiedSeats, setOccupiedSeats] = useState(0);
  const [isLoading, setIsLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [messageType, setMessageType] = useState('info');

  const availableSeats = totalSeats - occupiedSeats;

  // 刷新座位状态
  const refreshStatus = async () => {
    try {
      setIsLoading(true);
      const response = await librarySeatApi.getSeatStatus();
      if (response.data.code === 200) {
        setTotalSeats(response.data.data.totalSeats);
        setOccupiedSeats(response.data.data.occupiedSeats);
        showMessage('状态刷新成功', 'success');
      } else {
        showMessage(response.data.message || '获取状态失败', 'error');
      }
    } catch (error) {
      console.error('获取座位状态失败:', error);
      showMessage('网络错误，请稍后重试', 'error');
    } finally {
      setIsLoading(false);
    }
  };

  // 进入座位
  const enterSeat = async () => {
    try {
      setIsLoading(true);
      const response = await librarySeatApi.enterSeat();
      if (response.data.code === 200) {
        setTotalSeats(response.data.data.totalSeats);
        setOccupiedSeats(response.data.data.occupiedSeats);
        showMessage('进入座位成功', 'success');
      } else {
        showMessage(response.data.message || '进入座位失败', 'error');
      }
    } catch (error) {
      console.error('进入座位失败:', error);
      showMessage('网络错误，请稍后重试', 'error');
    } finally {
      setIsLoading(false);
    }
  };

  // 离开座位
  const leaveSeat = async () => {
    try {
      setIsLoading(true);
      const response = await librarySeatApi.leaveSeat();
      if (response.data.code === 200) {
        setTotalSeats(response.data.data.totalSeats);
        setOccupiedSeats(response.data.data.occupiedSeats);
        showMessage('离开座位成功', 'success');
      } else {
        showMessage(response.data.message || '离开座位失败', 'error');
      }
    } catch (error) {
      console.error('离开座位失败:', error);
      showMessage('网络错误，请稍后重试', 'error');
    } finally {
      setIsLoading(false);
    }
  };

  // 显示消息
  const showMessage = (msg, type = 'info') => {
    setMessage(msg);
    setMessageType(type);
    
    // 3秒后自动清除消息
    setTimeout(() => {
      setMessage('');
    }, 3000);
  };

  // 组件挂载时获取初始状态
  useEffect(() => {
    refreshStatus();
  }, []);

  return (
    <div className="library-seat-container">
      <h2>图书馆座位管理</h2>
      
      <div className="seat-status">
        <h3>当前状态</h3>
        <p>总座位数: {totalSeats}</p>
        <p>已占用座位: {occupiedSeats}</p>
        <p>可用座位: {availableSeats}</p>
        <button 
          onClick={refreshStatus} 
          className="btn-refresh"
          disabled={isLoading}
        >
          {isLoading ? '刷新中...' : '刷新状态'}
        </button>
      </div>
      
      <div className="seat-actions">
        <button 
          onClick={enterSeat} 
          className="btn-enter" 
          disabled={isLoading || occupiedSeats >= totalSeats}
        >
          {isLoading ? '处理中...' : '进入座位'}
        </button>
        
        <button 
          onClick={leaveSeat} 
          className="btn-leave" 
          disabled={isLoading || occupiedSeats <= 0}
        >
          {isLoading ? '处理中...' : '离开座位'}
        </button>
      </div>
      
      {message && (
        <div className={`message ${messageType}`}>
          {message}
        </div>
      )}
    </div>
  );
}

export default LibrarySeatManager;
```

### 4. 纯JavaScript调用示例

```javascript
// 不依赖框架的纯JavaScript调用

// 获取座位状态
function getSeatStatus() {
  fetch('http://localhost:8080/api/library/seat/status')
    .then(response => response.json())
    .then(data => {
      if (data.code === 200) {
        console.log('座位状态:', data.data);
        // 更新UI显示
        document.getElementById('totalSeats').textContent = data.data.totalSeats;
        document.getElementById('occupiedSeats').textContent = data.data.occupiedSeats;
      } else {
        console.error('获取状态失败:', data.message);
      }
    })
    .catch(error => {
      console.error('网络错误:', error);
    });
}

// 进入座位
function enterSeat() {
  fetch('http://localhost:8080/api/library/seat/enter', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    }
  })
  .then(response => response.json())
  .then(data => {
    if (data.code === 200) {
      console.log('进入座位成功:', data.data);
      // 更新UI显示
      document.getElementById('occupiedSeats').textContent = data.data.occupiedSeats;
      alert('进入座位成功');
    } else {
      console.error('进入座位失败:', data.message);
      alert('进入座位失败: ' + data.message);
    }
  })
  .catch(error => {
    console.error('网络错误:', error);
    alert('网络错误，请稍后重试');
  });
}

// 离开座位
function leaveSeat() {
  fetch('http://localhost:8080/api/library/seat/leave', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    }
  })
  .then(response => response.json())
  .then(data => {
    if (data.code === 200) {
      console.log('离开座位成功:', data.data);
      // 更新UI显示
      document.getElementById('occupiedSeats').textContent = data.data.occupiedSeats;
      alert('离开座位成功');
    } else {
      console.error('离开座位失败:', data.message);
      alert('离开座位失败: ' + data.message);
    }
  })
  .catch(error => {
    console.error('网络错误:', error);
    alert('网络错误，请稍后重试');
  });
}

// 页面加载时获取初始状态
window.onload = function() {
  getSeatStatus();
  
  // 绑定按钮事件
  document.getElementById('btnEnter').addEventListener('click', enterSeat);
  document.getElementById('btnLeave').addEventListener('click', leaveSeat);
  document.getElementById('btnRefresh').addEventListener('click', getSeatStatus);
};
```

## 错误处理

### 常见错误情况

1. **座位已满**:
   ```json
   {
     "code": 500,
     "message": "座位已满，无法进入",
     "data": null
   }
   ```

2. **无占用座位**:
   ```json
   {
     "code": 500,
     "message": "无占用座位，无法离开",
     "data": null
   }
   ```

3. **座位数据不存在**:
   ```json
   {
     "code": 500,
     "message": "座位数据不存在",
     "data": null
   }
   ```

### 前端错误处理建议

- 在调用API前检查按钮状态（如座位是否已满/已空）
- 使用try-catch捕获网络错误
- 对不同的错误码和错误信息进行不同的处理
- 显示友好的错误提示给用户
- 添加加载状态，避免重复提交

## 注意事项

1. **跨域问题**:
   - 后端已配置CORS，允许前端跨域访问
   - 若仍有跨域问题，请检查后端CORS配置

2. **用户ID**:
   - 当前实现中，所有操作记录的user_id固定为1
   - 无需前端传递用户ID

3. **并发安全**:
   - 后端使用原生SQL和事务确保并发安全
   - 前端无需额外处理并发问题

4. **数据初始化**:
   - 确保数据库中已存在library_seat_global表的初始数据
   - 建议在表中插入一条初始记录，设置合理的总座位数

## 测试建议

1. **功能测试**:
   - 测试进入座位功能（验证座位数增加）
   - 测试离开座位功能（验证座位数减少）
   - 测试边界情况（座位已满、无占用座位）
   - 测试获取状态功能

2. **性能测试**:
   - 模拟多用户同时操作
   - 测试接口响应时间

3. **异常测试**:
   - 测试网络中断情况
   - 测试数据库连接异常情况

通过以上代码和说明，前端开发者可以轻松集成图书馆座位管理功能，实现完整的座位状态管理和用户操作记录。