# 图书馆座位预约模块接口文档

## 1. 概述

本文档描述了图书馆座位预约模块的后端RESTful接口规范。

### 1.1 模块功能

- 楼层信息查询
- 教室信息查询
- 座位信息查询及状态管理
- 座位预约创建
- 座位占用（他人可占用超时未使用的预约）
- 座位离开（用户主动释放座位）
- 预约记录查询
- 座位当前预约查询

### 1.2 业务规则

1. **座位释放方式（仅保留以下三种）**：
   - 被他人占用：其他用户在预约开始30分钟后可占用该座位，原预约状态变为replaced
   - 主动离开：用户可提前离开座位，预约状态变为cancelled
   - 时间到期自动释放：预约结束时间到达后，系统自动将状态改为completed并释放座位

2. **座位占用规则**：
   - 占用他人预约必须在预约开始时间（reserveDate + startTime）后30分钟才能执行
   - 不能占用自己的预约
   - 只能占用状态为active且座位状态为reserved的预约
   - 占用前会记录详细的预约和座位状态信息，便于问题排查

3. **预约创建规则**：
   - 同一座位在同一日期的时间段仅允许一条有效预约（时间重叠检测）
   - 时间重叠判断标准：原开始时间 < 新结束时间 且 原结束时间 > 新开始时间
   - 不允许预约今日已过期的时间段，预约日期 + 开始时间 必须 晚于当前系统时间
   - 预约支持自由选择日期、开始时间、时长，结束时间由系统自动计算

4. **用户预约数量限制**：
   - 一个用户最多只能同时存在 3 条状态为 active 的预约（包括 reservation 和 occupation 类型）
   - 当用户创建预约或占用座位时，系统会检查该用户的活跃预约数量
   - 若数量 >= 3，直接返回错误："您最多只能同时预约/占用3个座位，请完成现有预约后再操作"

5. **定时任务**：
   - 系统每分钟自动检查过期的预约，将状态改为completed并释放座位

---

## 2. 基础数据查询接口

### 2.1 查询所有楼层

| 项目         | 内容                   |
| ------------ | ---------------------- |
| **接口名称** | 查询所有楼层           |
| **请求方式** | GET                    |
| **URL**      | `/api/library/floors`  |
| **描述**     | 获取图书馆所有楼层信息 |

**请求参数**：无

**响应示例**：

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": 1,
      "floorNum": 1,
      "description": "一楼"
    },
    {
      "id": 2,
      "floorNum": 2,
      "description": "二楼"
    }
  ]
}
```

**字段说明**：

| 字段        | 类型    | 说明     |
| ----------- | ------- | -------- |
| id          | Integer | 楼层ID   |
| floorNum    | Integer | 楼层号   |
| description | String  | 楼层描述 |

---

### 2.2 根据楼层查教室

| 项目         | 内容                                      |
| ------------ | ----------------------------------------- |
| **接口名称** | 根据楼层查教室                            |
| **请求方式** | GET                                       |
| **URL**      | `/api/library/classrooms/floor/{floorId}` |
| **描述**     | 获取指定楼层下的所有教室信息              |

**路径参数**：

| 参数名  | 类型    | 必填 | 说明   |
| ------- | ------- | ---- | ------ |
| floorId | Integer | 是   | 楼层ID |

**响应示例**：

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": 1,
      "floorId": 1,
      "classroomName": "101",
      "seatCount": 32
    },
    {
      "id": 2,
      "floorId": 1,
      "classroomName": "102",
      "seatCount": 48
    }
  ]
}
```

**字段说明**：

| 字段          | 类型    | 说明     |
| ------------- | ------- | -------- |
| id            | Integer | 教室ID   |
| floorId       | Integer | 楼层ID   |
| classroomName | String  | 教室名称 |
| seatCount     | Integer | 座位数量 |

---

### 2.3 根据教室查座位

| 项目         | 内容                                         |
| ------------ | -------------------------------------------- |
| **接口名称** | 根据教室查座位                               |
| **请求方式** | GET                                          |
| **URL**      | `/api/library/seats/classroom/{classroomId}` |
| **描述**     | 获取指定教室内的所有座位及状态               |

**路径参数**：

| 参数名      | 类型    | 必填 | 说明   |
| ----------- | ------- | ---- | ------ |
| classroomId | Integer | 是   | 教室ID |

**响应示例**：

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": 1,
      "classroomId": 1,
      "seatCode": "A1",
      "status": "available"
    },
    {
      "id": 2,
      "classroomId": 1,
      "seatCode": "A2",
      "status": "reserved"
    },
    {
      "id": 3,
      "classroomId": 1,
      "seatCode": "A3",
      "status": "occupied"
    }
  ]
}
```

**字段说明**：

| 字段        | 类型    | 说明     |
| ----------- | ------- | -------- |
| id          | Integer | 座位ID   |
| classroomId | Integer | 教室ID   |
| seatCode    | String  | 座位编码 |
| status      | String  | 座位状态 |

**座位状态说明**：

| 状态值    | 说明                         |
| --------- | ---------------------------- |
| available | 可用（未被预约）             |
| reserved  | 已预约（已被预约但未被占用） |
| occupied  | 已占用（正在使用中）         |

---

### 2.4 查询教室当前时段可用座位数

| 项目         | 内容                                                    |
| ------------ | ------------------------------------------------------- |
| **接口名称** | 查询教室当前时段可用座位数                              |
| **请求方式** | GET                                                     |
| **URL**      | `/api/library/classrooms/{classroomId}/available-seats` |
| **描述**     | 查询指定教室在当前系统时间所在时段内的可用座位数量      |

**路径参数**：

| 参数名      | 类型    | 必填 | 说明   |
| ----------- | ------- | ---- | ------ |
| classroomId | Integer | 是   | 教室ID |

**响应示例**：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "totalSeats": 32,
    "usedSeats": 1,
    "availableSeats": 31
  }
}
```

**字段说明**：

| 字段           | 类型    | 说明                     |
| -------------- | ------- | ------------------------ |
| totalSeats     | Integer | 教室总座位数             |
| usedSeats      | Integer | 当前时段已被预约的座位数 |
| availableSeats | Integer | 当前时段可用的座位数     |

**业务逻辑**：

1. 获取当前系统时间和日期
2. 查询该教室在今天且当前时间正在使用中的预约（status = 'active'，且当前时间在预约的startTime和endTime之间）
3. usedSeats 为满足上述条件的不同 seatId 的数量，即当前正在使用的座位数
4. availableSeats = totalSeats - usedSeats
5. 未来预约：所有 reserveDate 不是今天，或者今天但当前时间还未到 startTime 的预约，不计入 usedSeats，不影响当前可用座位数

---

## 3. 预约管理接口

### 3.1 创建预约

| 项目         | 内容                                                                                                                                                                                                                                                           |
| ------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **接口名称** | 创建预约                                                                                                                                                                                                                                                       |
| **请求方式** | POST                                                                                                                                                                                                                                                           |
| **URL**      | `/api/library/reservations`                                                                                                                                                                                                                                    |
| **描述**     | 创建新的座位预约，预约创建成功后座位状态变更为reserved                                                                                                                                                                                                         |
| **业务校验** | 1. 时间合法性校验：预约日期 + 开始时间 必须 晚于当前系统时间<br>2. 时间段重叠校验：同一座位、同一日期、时间段有任何重叠，且预约状态为active，则不允许重复预约<br>3. 用户预约数量限制：检查用户活跃预约数量（包括reservation和occupation类型），最多同时存在3条 |
| **技术特性** | 支持Redis缓存、Redis限流、Redis分布式锁、数据库乐观锁                                                                                                                                                                                                          |

**请求体**：

```json
{
  "userId": 1,
  "seatId": 5,
  "classroomId": 1,
  "reserveDate": "2026-03-05",
  "startTime": "09:00",
  "duration": 120
}
```

**请求体参数说明**：

| 参数名      | 类型    | 必填 | 说明                                                                           |
| ----------- | ------- | ---- | ------------------------------------------------------------------------------ |
| userId      | Integer | 是   | 用户ID                                                                         |
| seatId      | Integer | 是   | 座位ID                                                                         |
| classroomId | Integer | 是   | 教室ID                                                                         |
| reserveDate | String  | 是   | 预约日期，格式：yyyy-MM-dd                                                     |
| startTime   | String  | 是   | 开始时间，格式：HH:mm                                                          |
| duration    | Integer | 是   | 预约时长，单位：分钟                                                           |
| endTime     | String  | 否   | 结束时间，格式：HH:mm（系统会根据startTime和duration自动计算，无需客户端提供） |

**成功响应示例**：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 10,
    "userId": 1,
    "seatId": 5,
    "classroomId": 1,
    "reserveDate": "2026-03-05",
    "startTime": "09:00",
    "duration": 120,
    "endTime": "11:00",
    "type": "reservation",
    "status": "active",
    "createdAt": "2026-03-05T08:30:00",
    "actualEndTime": null,
    "actualDurationMinutes": null,
    "version": 1
  }
}
```

**响应字段说明**：

| 字段                  | 类型          | 说明                                        |
| --------------------- | ------------- | ------------------------------------------- |
| id                    | Integer       | 预约ID                                      |
| userId                | Integer       | 用户ID                                      |
| seatId                | Integer       | 座位ID                                      |
| classroomId           | Integer       | 教室ID                                      |
| reserveDate           | LocalDate     | 预约日期                                    |
| startTime             | LocalTime     | 开始时间                                    |
| duration              | Integer       | 预约时长（分钟）                            |
| endTime               | LocalTime     | 结束时间                                    |
| type                  | String        | 预约类型：reservation-预约，occupation-占用 |
| status                | String        | 预约状态                                    |
| createdAt             | LocalDateTime | 创建时间                                    |
| actualEndTime         | LocalDateTime | 实际结束时间（预约被占用/取消/完成时设置）  |
| actualDurationMinutes | Integer       | 实际使用时长（分钟）                        |
| version               | Integer       | 乐观锁版本号，每次更新时自动递增            |

**错误响应示例**：

```json
{
  "code": 500,
  "msg": "创建预约失败：该时间段已被预约",
  "data": null
}
```

```json
{
  "code": 500,
  "msg": "创建预约失败：您已有正在进行的预约或占用，无法同时预约多个座位",
  "data": null
}
```

```json
{
  "code": 500,
  "msg": "创建预约失败：不能预约已过去的时间段",
  "data": null
}
```

```json
{
  "code": 500,
  "msg": "创建预约失败：您最多只能同时预约/占用3个座位，请完成现有预约后再操作",
  "data": null
}
```

```json
{
  "code": 500,
  "msg": "操作失败：该座位信息已被其他用户修改，请刷新页面后重试",
  "data": null
}
```

---

### 3.2 占用座位

| 项目         | 内容                                                                                                                                                                                                                                                                                       |
| ------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **接口名称** | 占用座位                                                                                                                                                                                                                                                                                   |
| **请求方式** | POST                                                                                                                                                                                                                                                                                       |
| **URL**      | `/api/library/reservations/{id}/occupy`                                                                                                                                                                                                                                                    |
| **描述**     | 占用他人的预约座位，创建新的占用记录，原预约状态变更为replaced                                                                                                                                                                                                                             |
| **业务校验** | 1. 预约状态校验：预约状态必须为active<br>2. 操作者校验：不能占用自己的预约<br>3. 时间校验：预约开始时间（reserveDate + startTime）已超过30分钟<br>4. 座位状态校验：座位状态必须为reserved<br>5. 用户预约数量限制：检查用户活跃预约数量（包括reservation和occupation类型），最多同时存在3条 |
| **技术特性** | 支持Redis缓存、Redis限流、Redis分布式锁、数据库乐观锁                                                                                                                                                                                                                                      |

**路径参数**：

| 参数名 | 类型    | 必填 | 说明           |
| ------ | ------- | ---- | -------------- |
| id     | Integer | 是   | 被占用的预约ID |

**查询参数**：

| 参数名 | 类型    | 必填 | 说明             |
| ------ | ------- | ---- | ---------------- |
| userId | Integer | 是   | 占用座位的用户ID |

**请求示例**：

```
POST /api/library/reservations/10/occupy?userId=2
```

**成功响应示例**：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 11,
    "userId": 2,
    "seatId": 5,
    "classroomId": 1,
    "reserveDate": "2026-03-05",
    "startTime": "09:00",
    "duration": 120,
    "endTime": "11:00",
    "type": "occupation",
    "status": "active",
    "createdAt": "2026-03-05T09:35:00",
    "actualEndTime": null,
    "actualDurationMinutes": null,
    "version": 1
  }
}
```

**错误响应示例**：

```json
{
  "code": 500,
  "msg": "占用座位失败：预约开始时间后30分钟才能占用",
  "data": null
}
```

```json
{
  "code": 500,
  "msg": "占用座位失败：您已有正在进行的预约或占用，无法同时占用多个座位",
  "data": null
}
```

```json
{
  "code": 500,
  "msg": "占用座位失败：座位状态不正确，当前状态为：available，仅 reserved 状态可被占用",
  "data": null
}
```

```json
{
  "code": 500,
  "msg": "占用座位失败：您最多只能同时预约/占用3个座位，请完成现有预约后再操作",
  "data": null
}
```

```json
{
  "code": 500,
  "msg": "操作失败：该座位信息已被其他用户修改，请刷新页面后重试",
  "data": null
}
```

---

### 3.3 离开座位

| 项目         | 内容                                                               |
| ------------ | ------------------------------------------------------------------ |
| **接口名称** | 离开座位                                                           |
| **请求方式** | POST                                                               |
| **URL**      | `/api/library/reservations/{id}/leave`                             |
| **描述**     | 用户主动离开座位，预约状态变更为cancelled，座位状态恢复为available |
| **技术特性** | 支持Redis缓存、Redis限流、Redis分布式锁、数据库乐观锁              |

**路径参数**：

| 参数名 | 类型    | 必填 | 说明   |
| ------ | ------- | ---- | ------ |
| id     | Integer | 是   | 预约ID |

**查询参数**：

| 参数名 | 类型    | 必填 | 说明                                 |
| ------ | ------- | ---- | ------------------------------------ |
| userId | Integer | 是   | 离开座位的用户ID（必须是预约人本人） |

**请求示例**：

```
POST /api/library/reservations/10/leave?userId=1
```

**成功响应示例**：

```json
{
  "code": 200,
  "msg": "success",
  "data": null
}
```

**错误响应示例**：

```json
{
  "code": 500,
  "msg": "离开座位失败：只能离开自己的预约",
  "data": null
}
```

```json
{
  "code": 500,
  "msg": "操作失败：该座位信息已被其他用户修改，请刷新页面后重试",
  "data": null
}
```

---

### 3.4 查询用户预约记录

| 项目         | 内容                                                 |
| ------------ | ---------------------------------------------------- |
| **接口名称** | 查询用户预约记录                                     |
| **请求方式** | GET                                                  |
| **URL**      | `/api/library/reservations/user/{userId}`            |
| **描述**     | 获取指定用户的所有预约记录（包括主动预约和占用记录） |

**路径参数**：

| 参数名 | 类型    | 必填 | 说明   |
| ------ | ------- | ---- | ------ |
| userId | Integer | 是   | 用户ID |

**响应示例**：

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": 10,
      "userId": 1,
      "seatId": 5,
      "classroomId": 1,
      "reserveDate": "2026-03-05",
      "startTime": "09:00",
      "duration": 120,
      "endTime": "11:00",
      "type": "reservation",
      "status": "replaced",
      "createdAt": "2026-03-05T08:30:00",
      "actualEndTime": "2026-03-05T09:35:00",
      "actualDurationMinutes": 65,
      "version": 2
    },
    {
      "id": 11,
      "userId": 2,
      "seatId": 5,
      "classroomId": 1,
      "reserveDate": "2026-03-05",
      "startTime": "09:00",
      "duration": 120,
      "endTime": "11:00",
      "type": "occupation",
      "status": "active",
      "createdAt": "2026-03-05T09:35:00",
      "actualEndTime": null,
      "actualDurationMinutes": null,
      "version": 1
    }
  ]
}
```

---

### 3.5 查询座位当前预约

| 项目         | 内容                                           |
| ------------ | ---------------------------------------------- |
| **接口名称** | 查询座位当前预约                               |
| **请求方式** | GET                                            |
| **URL**      | `/api/library/reservations/seat/{seatId}`      |
| **描述**     | 获取指定座位当前的有效预约记录（状态为active） |

**路径参数**：

| 参数名 | 类型    | 必填 | 说明   |
| ------ | ------- | ---- | ------ |
| seatId | Integer | 是   | 座位ID |

**成功响应示例**：

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": 66,
      "userId": 1,
      "seatId": 1,
      "reserveDate": "2026-03-08",
      "startTime": "09:00:00",
      "endTime": "11:00:00",
      "status": "active",
      "classroomId": 1,
      "duration": 120,
      "type": "reservation",
      "createdAt": "2026-03-07T10:00:00",
      "actualEndTime": null,
      "actualDurationMinutes": null,
      "version": 1
    },
    {
      "id": 68,
      "userId": 1,
      "seatId": 1,
      "reserveDate": "2026-03-07",
      "startTime": "16:00:00",
      "endTime": "17:00:00",
      "status": "active",
      "classroomId": 1,
      "duration": 60,
      "type": "reservation",
      "createdAt": "2026-03-07T15:00:00",
      "actualEndTime": null,
      "actualDurationMinutes": null,
      "version": 1
    }
  ]
}
```

**无有效预约响应示例**：

```json
{
  "code": 200,
  "msg": "success",
  "data": []
}
```

---

## 4. 状态说明

### 4.1 座位状态

| 状态值    | 说明                         |
| --------- | ---------------------------- |
| available | 可用（未被预约）             |
| reserved  | 已预约（已被预约但未被占用） |
| occupied  | 已占用（正在使用中）         |

### 4.2 预约状态

| 状态值    | 说明                             |
| --------- | -------------------------------- |
| active    | 活跃（正常使用中）               |
| completed | 已完成（预约时间到期后自动完成） |
| cancelled | 已取消（用户主动离开）           |
| replaced  | 已替换（被他人占用）             |

### 4.3 预约类型

| 类型值      | 说明         |
| ----------- | ------------ |
| reservation | 主动预约     |
| occupation  | 占用他人预约 |

---

## 5. 错误码

### 5.1 通用错误码

| 错误码 | 错误信息     | 说明                            |
| ------ | ------------ | ------------------------------- |
| 500    | 系统内部错误 | 操作失败的具体原因会在msg中说明 |

### 5.2 业务错误码

| 错误信息                                                                         | 原因说明                                   |
| -------------------------------------------------------------------------------- | ------------------------------------------ |
| 创建预约失败：该时间段已被预约                                                   | 所选座位在指定时间段内已有其他有效预约     |
| 创建预约失败：不能预约已过去的时间段                                             | 预约日期 + 开始时间 早于或等于当前系统时间 |
| 占用座位失败：预约不存在                                                         | 预约ID不存在                               |
| 占用座位失败：该预约已被处理                                                     | 预约状态非active，无法再次占用             |
| 占用座位失败：不能占用自己的预约                                                 | 无法占用自己创建的预约                     |
| 占用座位失败：预约开始时间后30分钟才能占用                                       | 当前时间距离预约开始时间不足30分钟         |
| 占用座位失败：座位状态不正确，当前状态为：{seatStatus}，仅 reserved 状态可被占用 | 座位当前状态不是reserved                   |
| 占用座位失败：您已有正在进行的预约或占用，无法同时占用多个座位                   | 用户已有活跃的预约或占用记录               |
| 创建预约失败：您最多只能同时预约/占用3个座位，请完成现有预约后再操作             | 用户活跃预约数量达到上限（3个）            |
| 占用座位失败：您最多只能同时预约/占用3个座位，请完成现有预约后再操作             | 用户活跃预约数量达到上限（3个）            |
| 离开座位失败：预约不存在                                                         | 预约ID不存在                               |
| 离开座位失败：只能离开自己的预约                                                 | 操作者不是原预约人                         |
| 离开座位失败：该预约已结束或已取消                                               | 预约状态非active                           |
| 获取预约记录失败                                                                 | 查询用户预约记录时发生错误                 |
| 获取座位预约失败                                                                 | 查询座位预约时发生错误                     |

---

## 6. 定时任务

### 6.1 自动释放过期预约

| 项目         | 内容                                                                                |
| ------------ | ----------------------------------------------------------------------------------- |
| **任务名称** | 处理过期预约                                                                        |
| **执行频率** | 每分钟执行一次                                                                      |
| **处理逻辑** | 检查所有状态为active且结束时间小于当前时间的预约，自动将状态改为completed并释放座位 |

**处理流程**：

1. 查询所有 `status = 'active'` 且 `endTime < 当前时间` 的预约
2. 逐个将预约状态改为 `completed`
3. 设置 `actualEndTime` 为当前时间
4. 将对应座位状态恢复为 `available`

---

## 7. 数据模型

### 7.1 Seat（座位）

| 字段        | 类型    | 说明       |
| ----------- | ------- | ---------- |
| id          | Integer | 座位ID     |
| classroomId | Integer | 所属教室ID |
| seatCode    | String  | 座位编码   |
| status      | String  | 座位状态   |

### 7.2 Reservation（预约记录）

| 字段                  | 类型          | 说明                             |
| --------------------- | ------------- | -------------------------------- |
| id                    | Integer       | 预约ID                           |
| userId                | Integer       | 用户ID                           |
| seatId                | Integer       | 座位ID                           |
| classroomId           | Integer       | 教室ID                           |
| reserveDate           | LocalDate     | 预约日期                         |
| startTime             | LocalTime     | 开始时间                         |
| duration              | Integer       | 预约时长（分钟）                 |
| endTime               | LocalTime     | 结束时间                         |
| type                  | String        | 预约类型                         |
| status                | String        | 预约状态                         |
| createdAt             | LocalDateTime | 创建时间                         |
| actualEndTime         | LocalDateTime | 实际结束时间                     |
| actualDurationMinutes | Integer       | 实际使用时长                     |
| version               | Integer       | 乐观锁版本号，每次更新时自动递增 |

---

## 8. 接口汇总

| 序号 | 接口名称                   | 请求方式 | URL                                                     |
| ---- | -------------------------- | -------- | ------------------------------------------------------- |
| 1    | 查询所有楼层               | GET      | `/api/library/floors`                                   |
| 2    | 根据楼层查教室             | GET      | `/api/library/classrooms/floor/{floorId}`               |
| 3    | 根据教室查座位             | GET      | `/api/library/seats/classroom/{classroomId}`            |
| 4    | 查询教室当前时段可用座位数 | GET      | `/api/library/classrooms/{classroomId}/available-seats` |
| 5    | 创建预约                   | POST     | `/api/library/reservations`                             |
| 6    | 占用座位                   | POST     | `/api/library/reservations/{id}/occupy`                 |
| 7    | 离开座位                   | POST     | `/api/library/reservations/{id}/leave`                  |
| 8    | 查询用户预约记录           | GET      | `/api/library/reservations/user/{userId}`               |
| 9    | 查询座位当前预约           | GET      | `/api/library/reservations/seat/{seatId}`               |

---

## 9. Java 虚拟线程技术实现

### 9.1 概述

为提高高并发场景下的性能，图书馆座位预约模块的核心接口已集成 Java 21 虚拟线程技术。虚拟线程是 Java 21 引入的轻量级线程，适合处理 I/O 密集型操作，如数据库查询、网络请求等。

### 9.2 使用虚拟线程的接口

| 接口名称         | URL                                       | 请求方式 |
| ---------------- | ----------------------------------------- | -------- |
| 创建预约         | `/api/library/reservations`               | POST     |
| 占用座位         | `/api/library/reservations/{id}/occupy`   | POST     |
| 离开座位         | `/api/library/reservations/{id}/leave`    | POST     |
| 查询用户预约记录 | `/api/library/reservations/user/{userId}` | GET      |
| 查询座位当前预约 | `/api/library/reservations/seat/{seatId}` | GET      |

### 9.3 虚拟线程配置参数

| 配置项         | 值                     | 说明                           |
| -------------- | ---------------------- | ------------------------------ |
| 线程池名称     | `virtualThreadPool`    | 虚拟线程池的 Bean 名称         |
| 平台线程核心数 | 10                     | 作为载体的平台线程池核心线程数 |
| 平台线程最大数 | 20                     | 作为载体的平台线程池最大线程数 |
| 队列容量       | 100                    | 平台线程池的队列容量           |
| 线程名称前缀   | `virtual-reservation-` | 虚拟线程的名称前缀             |

### 9.4 实现逻辑

1. **线程池配置**：创建 `VirtualThreadPoolConfig` 类，定义虚拟线程池 Bean
2. **接口标注**：在高并发接口上使用 `@Async("virtualThreadPool")` 注解
3. **返回值调整**：将接口返回类型改为 `CompletableFuture<Result<?>>` 以支持异步操作
4. **局部生效**：只在图书馆预约模块的核心接口使用，其他模块保持默认线程池

### 9.5 性能影响

- **吞吐量提升**：虚拟线程的轻量级特性使得系统能够同时处理更多的并发请求
- **响应时间优化**：I/O 操作期间线程不会被阻塞，提高了系统响应速度
- **资源利用率**：虚拟线程的创建和调度成本远低于传统线程，减少了系统资源消耗
- **可扩展性**：在高并发场景下，系统能够更好地应对流量峰值

---

_文档版本：v3.5_
_最后更新时间：2026-03-19_
