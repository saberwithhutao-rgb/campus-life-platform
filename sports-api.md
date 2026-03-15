# 体育馆场地预约模块接口文档

## 1. 概述

本文档描述了体育馆场地预约模块的后端RESTful接口规范。

### 1.1 模块功能

- 场馆信息查询
- 场地信息查询及状态管理
- 场地预约创建
- 场地占用（他人可占用超时未使用的预约）
- 场地离开（用户主动释放场地）
- 预约记录查询
- 场地当前预约查询

### 1.2 业务规则

1. **场地释放方式（仅保留以下三种）**：
   - 被他人占用：其他用户在预约开始30分钟后可占用该场地，原预约状态变为replaced
   - 主动离开：用户可提前离开场地，预约状态变为cancelled
   - 时间到期自动释放：预约结束时间到达后，系统自动将状态改为completed并释放场地

2. **场地占用规则**：
   - 占用他人预约必须在预约开始时间（reserveDate + startTime）后30分钟才能执行
   - 不能占用自己的预约
   - 只能占用状态为active且场地状态为reserved的预约
   - 占用前会记录详细的预约和场地状态信息，便于问题排查

3. **预约创建规则**：
   - 同一场地在同一日期的时间段仅允许一条有效预约（时间重叠检测）
   - 时间重叠判断标准：原开始时间 < 新结束时间 且 原结束时间 > 新开始时间
   - 不允许预约今日已过期的时间段，预约日期 + 开始时间 必须 晚于当前系统时间
   - 预约支持自由选择日期、开始时间、时长，结束时间由系统自动计算
   - 一个用户最多同时预约 3 个场地，当活跃预约数≥3时，不允许创建新预约
   - 活跃预约数统计：统计当前用户所有 status = 'active' 的预约/占用记录总数（包括 reservation 和 occupation 类型）

4. **定时任务**：
   - 系统每分钟自动检查过期的预约，将状态改为completed并释放场地

---

## 2. 基础数据查询接口

### 2.1 查询所有场馆

| 项目         | 内容                   |
| ------------ | ---------------------- |
| **接口名称** | 查询所有场馆           |
| **请求方式** | GET                    |
| **URL**      | `/api/sports/venues`   |
| **描述**     | 获取体育馆所有场馆信息 |

**请求参数**：无

**响应示例**：

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": 1,
      "venueType": "basketball",
      "venueName": "篮球馆",
      "totalCourts": 6,
      "description": "学校综合篮球馆"
    },
    {
      "id": 2,
      "venueType": "badminton",
      "venueName": "羽毛球馆",
      "totalCourts": 8,
      "description": "学校羽毛球馆"
    }
  ]
}
```

**字段说明**：

| 字段        | 类型    | 说明       |
| ----------- | ------- | ---------- |
| id          | Integer | 场馆ID     |
| venueType   | String  | 场馆类型   |
| venueName   | String  | 场馆名称   |
| totalCourts | Integer | 总场地数量 |
| description | String  | 场馆描述   |

---

### 2.2 根据场馆查场地

| 项目         | 内容                                 |
| ------------ | ------------------------------------ |
| **接口名称** | 根据场馆查场地                       |
| **请求方式** | GET                                  |
| **URL**      | `/api/sports/courts/venue/{venueId}` |
| **描述**     | 获取指定场馆下的所有场地及状态       |

**路径参数**：

| 参数名  | 类型    | 必填 | 说明   |
| ------- | ------- | ---- | ------ |
| venueId | Integer | 是   | 场馆ID |

**响应示例**：

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": 1,
      "venueId": 1,
      "courtCode": "场地1",
      "status": "available"
    },
    {
      "id": 2,
      "venueId": 1,
      "courtCode": "场地2",
      "status": "reserved"
    },
    {
      "id": 3,
      "venueId": 1,
      "courtCode": "场地3",
      "status": "occupied"
    }
  ]
}
```

**字段说明**：

| 字段      | 类型    | 说明     |
| --------- | ------- | -------- |
| id        | Integer | 场地ID   |
| venueId   | Integer | 场馆ID   |
| courtCode | String  | 场地编码 |
| status    | String  | 场地状态 |

**场地状态说明**：

| 状态值    | 说明                         |
| --------- | ---------------------------- |
| available | 可用（未被预约）             |
| reserved  | 已预约（已被预约但未被占用） |
| occupied  | 已占用（正在使用中）         |

---

### 2.3 查询场馆当前时段可用场地数

| 项目         | 内容                                               |
| ------------ | -------------------------------------------------- |
| **接口名称** | 查询场馆当前时段可用场地数                         |
| **请求方式** | GET                                                |
| **URL**      | `/api/sports/venues/{venueId}/available-courts`    |
| **描述**     | 查询指定场馆在当前系统时间所在时段内的可用场地数量 |

**路径参数**：

| 参数名  | 类型    | 必填 | 说明   |
| ------- | ------- | ---- | ------ |
| venueId | Integer | 是   | 场馆ID |

**响应示例**：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "totalCourts": 6,
    "usedCourts": 1,
    "availableCourts": 5
  }
}
```

**字段说明**：

| 字段            | 类型    | 说明                     |
| --------------- | ------- | ------------------------ |
| totalCourts     | Integer | 场馆总场地数             |
| usedCourts      | Integer | 当前时段已被预约的场地数 |
| availableCourts | Integer | 当前时段可用的场地数     |

**业务逻辑**：

1. 获取当前系统时间和日期
2. 查询该场馆在今天且当前时间正在使用中的预约（status = 'active'，且当前时间在预约的startTime和endTime之间）
3. usedCourts 为满足上述条件的不同 courtId 的数量，即当前正在使用的场地数
4. availableCourts = totalCourts - usedCourts
5. 未来预约：所有 reserveDate 不是今天，或者今天但当前时间还未到 startTime 的预约，不计入 usedCourts，不影响当前可用场地数

---

## 3. 预约管理接口

### 3.1 创建预约

| 项目         | 内容                                                                                                                                                                                                                                                    |
| ------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **接口名称** | 创建预约                                                                                                                                                                                                                                                |
| **请求方式** | POST                                                                                                                                                                                                                                                    |
| **URL**      | `/api/sports/reservations`                                                                                                                                                                                                                              |
| **描述**     | 创建新的场地预约，预约创建成功后场地状态变更为reserved                                                                                                                                                                                                  |
| **业务校验** | 1. 用户预约数量校验：一个用户最多同时预约 3 个场地，当活跃预约数≥3时，不允许创建新预约<br>2. 时间合法性校验：预约日期 + 开始时间 必须 晚于当前系统时间<br>3. 时间段重叠校验：同一场地、同一日期、时间段有任何重叠，且预约状态为active，则不允许重复预约 |

**请求体**：

```json
{
  "userId": 1,
  "courtId": 5,
  "venueId": 1,
  "reserveDate": "2026-03-10",
  "startTime": "14:00",
  "duration": 120,
  "endTime": "16:00"
}
```

**请求体参数说明**：

| 参数名      | 类型    | 必填 | 说明                                                                           |
| ----------- | ------- | ---- | ------------------------------------------------------------------------------ |
| userId      | Integer | 是   | 用户ID                                                                         |
| courtId     | Integer | 是   | 场地ID                                                                         |
| venueId     | Integer | 是   | 场馆ID                                                                         |
| reserveDate | String  | 是   | 预约日期，格式：yyyy-MM-dd                                                     |
| startTime   | String  | 是   | 开始时间，格式：HH:mm                                                          |
| duration    | Integer | 是   | 预约时长，单位：分钟                                                           |
| endTime     | String  | 否   | 结束时间，格式：HH:mm（系统会根据startTime和duration自动计算，无需客户端提供） |

**成功响应示例**：

```json
{
  "code": 2
```

00,
"msg": "success",
"data": {
"id": 10,
"userId": 1,
"courtId": 5,
"venueId": 1,
"reserveDate": "2026-03-10",
"startTime": "14:00",
"duration": 120,
"endTime": "16:00",
"type": "reservation",
"status": "active",
"createdAt": "2026-03-10T13:30:00",
"actualEndTime": null,
"actualDuration": null
}
}

````

**响应字段说明**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Integer | 预约ID |
| userId | Integer | 用户ID |
| courtId | Integer | 场地ID |
| venueId | Integer | 场馆ID |
| reserveDate | LocalDate | 预约日期 |
| startTime | LocalTime | 开始时间 |
| duration | Integer | 预约时长（分钟） |
| endTime | LocalTime | 结束时间 |
| type | String | 预约类型：reservation-预约，occupation-占用 |
| status | String | 预约状态 |
| createdAt | LocalDateTime | 创建时间 |
| actualEndTime | LocalDateTime | 实际结束时间（预约被占用/取消/完成时设置） |
| actualDuration | Integer | 实际使用时长（分钟） |

**错误响应示例**：

```json
{
  "code": 500,
  "msg": "创建预约失败：该时间段已被预约",
  "data": null
}
````

```json
{
  "code": 500,
  "msg": "创建预约失败：您已有正在进行的预约或占用，无法同时预约多个场地",
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

---

### 3.2 占用场地

| 项目         | 内容                                                                                                                                                                                                                                                                                |
| ------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **接口名称** | 占用场地                                                                                                                                                                                                                                                                            |
| **请求方式** | POST                                                                                                                                                                                                                                                                                |
| **URL**      | `/api/sports/reservations/{id}/occupy`                                                                                                                                                                                                                                              |
| **描述**     | 占用他人的预约场地，创建新的占用记录，原预约状态变更为replaced                                                                                                                                                                                                                      |
| **业务校验** | 1. 预约状态校验：预约状态必须为active<br>2. 操作者校验：不能占用自己的预约<br>3. 时间校验：预约开始时间（reserveDate + startTime）已超过30分钟<br>4. 场地状态校验：场地状态必须为reserved<br>5. 用户预约数量校验：一个用户最多同时预约 3 个场地，当活跃预约数≥3时，不允许占用新场地 |

**路径参数**：

| 参数名 | 类型    | 必填 | 说明           |
| ------ | ------- | ---- | -------------- |
| id     | Integer | 是   | 被占用的预约ID |

**查询参数**：

| 参数名 | 类型    | 必填 | 说明             |
| ------ | ------- | ---- | ---------------- |
| userId | Integer | 是   | 占用场地的用户ID |

**请求示例**：

```
POST /api/sports/reservations/10/occupy?userId=2
```

**成功响应示例**：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 11,
    "userId": 2,
    "courtId": 5,
    "venueId": 1,
    "reserveDate": "2026-03-10",
    "startTime": "14:00",
    "duration": 120,
    "endTime": "16:00",
    "type": "occupation",
    "status": "active",
    "createdAt": "2026-03-10T14:35:00",
    "actualEndTime": null,
    "actualDuration": null
  }
}
```

**错误响应示例**：

```json
{
  "code": 500,
  "msg": "占用场地失败：预约开始时间后30分钟才能占用",
  "data": null
}
```

```json
{
  "code": 500,
  "msg": "占用场地失败：您已有正在进行的预约或占用，无法同时占用多个场地",
  "data": null
}
```

```json
{
  "code": 500,
  "msg": "占用场地失败：场地状态不正确，当前状态为：available，仅 reserved 状态可被占用",
  "data": null
}
```

---

### 3.3 离开场地

| 项目         | 内容                                                               |
| ------------ | ------------------------------------------------------------------ |
| **接口名称** | 离开场地                                                           |
| **请求方式** | POST                                                               |
| **URL**      | `/api/sports/reservations/{id}/leave`                              |
| **描述**     | 用户主动离开场地，预约状态变更为cancelled，场地状态恢复为available |

**路径参数**：

| 参数名 | 类型    | 必填 | 说明   |
| ------ | ------- | ---- | ------ |
| id     | Integer | 是   | 预约ID |

**查询参数**：

| 参数名 | 类型    | 必填 | 说明                                 |
| ------ | ------- | ---- | ------------------------------------ |
| userId | Integer | 是   | 离开场地的用户ID（必须是预约人本人） |

**请求示例**：

```
POST /api/sports/reservations/10/leave?userId=1
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
  "msg": "离开场地失败：只能离开自己的预约",
  "data": null
}
```

---

### 3.4 查询用户预约记录

| 项目         | 内容                                                 |
| ------------ | ---------------------------------------------------- |
| **接口名称** | 查询用户预约记录                                     |
| **请求方式** | GET                                                  |
| **URL**      | `/api/sports/reservations/user/{userId}`             |
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
      "courtId": 5,
      "venueId": 1,
      "reserveDate": "2026-03-10",
      "startTime": "14:00",
      "duration": 120,
      "endTime": "16:00",
      "type": "reservation",
      "status": "replaced",
      "createdAt": "2026-03-10T13:30:00",
      "actualEndTime": "2026-03-10T14:35:00",
      "actualDuration": 65
    },
    {
      "id": 11,
      "userId": 2,
      "courtId": 5,
      "venueId": 1,
      "reserveDate": "2026-03-10",
      "startTime": "14:00",
      "duration": 120,
      "endTime": "16:00",
      "type": "occupation",
      "status": "active",
      "createdAt": "2026-03-10T14:35:00",
      "actualEndTime": null,
      "actualDuration": null
    }
  ]
}
```

---

### 3.5 查询场地当前预约

| 项目         | 内容                                           |
| ------------ | ---------------------------------------------- |
| **接口名称** | 查询场地当前预约                               |
| **请求方式** | GET                                            |
| **URL**      | `/api/sports/reservations/court/{courtId}`     |
| **描述**     | 获取指定场地当前的有效预约记录（状态为active） |

**路径参数**：

| 参数名  | 类型    | 必填 | 说明   |
| ------- | ------- | ---- | ------ |
| courtId | Integer | 是   | 场地ID |

**成功响应示例**：

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": 66,
      "userId": 1,
      "courtId": 1,
      "venueId": 1,
      "reserveDate": "2026-03-10",
      "startTime": "09:00:00",
      "endTime": "11:00:00",
      "status": "active",
      "duration": 120,
      "type": "reservation",
      "createdAt": "2026-03-09T10:00:00",
      "actualEndTime": null,
      "actualDuration": null
    },
    {
      "id": 68,
      "userId": 1,
      "courtId": 1,
      "venueId": 1,
      "reserveDate": "2026-03-10",
      "startTime": "16:00:00",
      "endTime": "17:00:00",
      "status": "active",
      "duration": 60,
      "type": "reservation",
      "createdAt": "2026-03-09T15:00:00",
      "actualEndTime": null,
      "actualDuration": null
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

### 4.1 场地状态

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

| 错误信息                                                                          | 原因说明                                   |
| --------------------------------------------------------------------------------- | ------------------------------------------ |
| 创建预约失败：该时间段已被预约                                                    | 所选场地在指定时间段内已有其他有效预约     |
| 创建预约失败：不能预约已过去的时间段                                              | 预约日期 + 开始时间 早于或等于当前系统时间 |
| 创建预约失败：您最多只能同时预约2个场地，请先完成或取消后再预约                   | 用户活跃预约数≥3                           |
| 占用场地失败：预约不存在                                                          | 预约ID不存在                               |
| 占用场地失败：该预约已被处理                                                      | 预约状态非active，无法再次占用             |
| 占用场地失败：不能占用自己的预约                                                  | 无法占用自己创建的预约                     |
| 占用场地失败：预约开始时间后30分钟才能占用                                        | 当前时间距离预约开始时间不足30分钟         |
| 占用场地失败：场地状态不正确，当前状态为：{courtStatus}，仅 reserved 状态可被占用 | 场地当前状态不是reserved                   |
| 占用场地失败：您最多只能同时预约2个场地，请先完成或取消后再预约                   | 用户活跃预约数≥3                           |
| 离开场地失败：预约不存在                                                          | 预约ID不存在                               |
| 离开场地失败：只能离开自己的预约                                                  | 操作者不是原预约人                         |
| 离开场地失败：该预约已结束或已取消                                                | 预约状态非active                           |
| 获取预约记录失败                                                                  | 查询用户预约记录时发生错误                 |
| 获取场地预约失败                                                                  | 查询场地预约时发生错误                     |

---

## 6. 定时任务

### 6.1 自动释放过期预约

| 项目         | 内容                                                                                |
| ------------ | ----------------------------------------------------------------------------------- |
| **任务名称** | 处理过期预约                                                                        |
| **执行频率** | 每分钟执行一次                                                                      |
| **处理逻辑** | 检查所有状态为active且结束时间小于当前时间的预约，自动将状态改为completed并释放场地 |

**处理流程**：

1. 查询所有 `status = 'active'` 且 `endTime < 当前时间` 的预约
2. 逐个将预约状态改为 `completed`
3. 设置 `actualEndTime` 为当前时间
4. 将对应场地状态恢复为 `available`

---

## 7. 数据模型

### 7.1 Venue（场馆）

| 字段        | 类型          | 说明       |
| ----------- | ------------- | ---------- |
| id          | Integer       | 场馆ID     |
| venueType   | String        | 场馆类型   |
| venueName   | String        | 场馆名称   |
| totalCourts | Integer       | 总场地数量 |
| description | String        | 场馆描述   |
| createdAt   | LocalDateTime | 创建时间   |
| updatedAt   | LocalDateTime | 更新时间   |

### 7.2 Court（场地）

| 字段      | 类型          | 说明       |
| --------- | ------------- | ---------- |
| id        | Integer       | 场地ID     |
| venueId   | Integer       | 所属场馆ID |
| courtCode | String        | 场地编码   |
| status    | String        | 场地状态   |
| createdAt | LocalDateTime | 创建时间   |
| updatedAt | LocalDateTime | 更新时间   |

### 7.3 VenueReservation（预约记录）

| 字段           | 类型          | 说明             |
| -------------- | ------------- | ---------------- |
| id             | Integer       | 预约ID           |
| userId         | Integer       | 用户ID           |
| courtId        | Integer       | 场地ID           |
| venueId        | Integer       | 场馆ID           |
| reserveDate    | LocalDate     | 预约日期         |
| startTime      | LocalTime     | 开始时间         |
| duration       | Integer       | 预约时长（分钟） |
| endTime        | LocalTime     | 结束时间         |
| type           | String        | 预约类型         |
| status         | String        | 预约状态         |
| createdAt      | LocalDateTime | 创建时间         |
| actualEndTime  | LocalDateTime | 实际结束时间     |
| actualDuration | Integer       | 实际使用时长     |

---

## 8. 接口汇总

| 序号 | 接口名称                   | 请求方式 | URL                                             |
| ---- | -------------------------- | -------- | ----------------------------------------------- |
| 1    | 查询所有场馆               | GET      | `/api/sports/venues`                            |
| 2    | 根据场馆查场地             | GET      | `/api/sports/courts/venue/{venueId}`            |
| 3    | 查询场馆当前时段可用场地数 | GET      | `/api/sports/venues/{venueId}/available-courts` |
| 4    | 创建预约                   | POST     | `/api/sports/reservations`                      |
| 5    | 占用场地                   | POST     | `/api/sports/reservations/{id}/occupy`          |
| 6    | 离开场地                   | POST     | `/api/sports/reservations/{id}/leave`           |
| 7    | 查询用户预约记录           | GET      | `/api/sports/reservations/user/{userId}`        |
| 8    | 查询场地当前预约           | GET      | `/api/sports/reservations/court/{courtId}`      |

---

_文档版本：v1.1_
_最后更新时间：2026-03-11_
