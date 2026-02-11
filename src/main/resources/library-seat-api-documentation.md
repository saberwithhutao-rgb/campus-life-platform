# 图书馆座位管理 API 文档

## 接口列表

### 1. 进入座位接口
- **接口地址**: `/api/library/seat/enter`
- **请求方式**: POST
- **请求参数**: 无
- **返回示例**:
  ```json
  # 成功
  {
    "code": 200,
    "message": "进入座位成功",
    "data": {
      "id": 1,
      "totalSeats": 100,
      "occupiedSeats": 51
    }
  }

  # 失败 - 座位已满
  {
    "code": 500,
    "message": "座位已满，无法进入",
    "data": null
  }
  ```

### 2. 离开座位接口
- **接口地址**: `/api/library/seat/leave`
- **请求方式**: POST
- **请求参数**: 无
- **返回示例**:
  ```json
  # 成功
  {
    "code": 200,
    "message": "离开座位成功",
    "data": {
      "id": 1,
      "totalSeats": 100,
      "occupiedSeats": 50
    }
  }

  # 失败 - 无占用座位
  {
    "code": 500,
    "message": "无占用座位，无法离开",
    "data": null
  }
  ```

### 3. 获取座位状态接口
- **接口地址**: `/api/library/seat/status`
- **请求方式**: GET
- **请求参数**: 无
- **返回示例**:
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

## 数据库表结构

### library_seat_global 表
| 字段名 | 数据类型 | 描述 |
|-------|---------|------|
| id | SERIAL | 主键 |
| total_seats | INTEGER | 总座位数 |
| occupied_seats | INTEGER | 已占用座位数 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

### library_seat_operation 表
| 字段名 | 数据类型 | 描述 |
|-------|---------|------|
| id | SERIAL | 主键 |
| user_id | INTEGER | 用户ID（固定为1） |
| operation_type | INTEGER | 操作类型（1=进入/2=离开） |
| operation_time | TIMESTAMP | 操作时间 |
| created_at | TIMESTAMP | 创建时间 |

## 注意事项
1. 所有接口返回统一的 ResultVO 格式
2. 操作记录中的 user_id 固定为 1
3. 进入座位时会自动增加 occupied_seats 并插入操作记录
4. 离开座位时会自动减少 occupied_seats 并插入操作记录
5. 所有操作都包含边界校验，确保数据一致性