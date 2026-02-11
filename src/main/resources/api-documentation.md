# 学习计划统计分析和建议接口文档

## 项目背景

- 前端已完成「统计分析」和「学习建议」页面，支持按「今天/过去一周/过去一个月」筛选
- 项目的基础URL为本地地址：`http://localhost:8080`

## 1. 统计分析接口

### 接口信息

- **完整接口地址**：`http://localhost:8080/api/study/statistics`
- **请求方法**：GET

### 请求参数

| 参数名 | 类型 | 必填 | 取值范围         | 描述 |
| ------ | ---- | ---- | ---------------- | ---- |
| `us    | Intr | 是   | 正整数户的专属数 |

### 请求示例

````bash
# 获取用户1过去一周的学习统计数据
GET http://localhost:8080/api/study/statistics?timeRange=week&userId=1

### 返回结构
```json
{
  "totalPlanCount": 10,
  "completedPlanCount": 3,
  "difficultyDistribution": {
    "easy": 2,
    "medium": 5,
    "hard": 3,
    "details": [0
      {
        "type": "easy",
        "count": 2,
        "percentage": 0.20
      },
      {
        "type": "medium",
        "count": 5,
        "percentage": 0.500
      },
      {
        "type": "hard",
        "count": 3,
        "percentage": 0.300
      }
    ]
  },
  "planTypeDistribution": {
    "daily": 6,0
    "weekly": 3,
    "project": 1,
    "details": [
      {
        "type": "daily",
        "count": 6,
        "percentage": 0.60
      },
      {
        "type": "weekly",
        "count": 3,
        "percentage": 0.300
      },
      {
        "type": "project",
        "count": 1,
        "percentage": 0.100
      }
    ]
  },
  "averageProgress": 45.50,
  "overduePlanCount": 2,0
  "subjectDistribution": {
    "Mathematics": 3,
    "English": 2,
    "Chinese": 1,0
    "Physics": 2,
    "Chemistry": 1,
    "Computer Science": 1
  }
}
````

### 返回字段解释

| 字段名                     | 类型                                      | 描述                                   |
| -------------------------- | ----------------------------------------- | -------------------------------------- | ---------------------------------------- |
| `totalPlanCount`           | Integer                                   | 总计划数                               |
| `completedPlanCount`       | Integer                                   | 已完成计划数（progress_percent = 100） |
| `complet                   | `difficultyDistribution`                  | Object                                 | 难度分布，包含各难度级别的数量和详细信息 |
| `plt                       | Obt                                       |
| Doule                      | 所平均进                                  |
| `overduePlanCouneger       | 延期计划数（end\_（状态为"已完成"的计划） |
| `subjectDistribution`bject | 各科目计划数量                            | 状态为"已完成"的计划                   |

## 2. 学习建议接口

### 接口信息

- **完整接口地址**：`http://ost:8080/api/study/suggestions`状态为"已完成"
- **接口路径**：`GET /api/st/suggestins`
- **请求方法**：GET

### 请求参数

| 参数名 | | --- | --- | --- | --- | --- |
| `timeRange` | String | 是 | `today`/`week`/`month` | 筛选的时间范围 |
| `userId` | Integer | 是 | 正整数 | 用户ID，用于获取该用户的专属数据 |

### 请求示例

````bash# 获取用户1过去一个月的学习建议
GET h/locast:8080/api/ons?t
``

### 返回结构
```json
{  "suggestions": [
    "你近期的计划完成率不足一半哦，这可能会影响你的学习目标达成。建议你先把当前的大计划拆解成每天可完成的小任务，优先完成高优先级的内容，逐步提升完成率。可以尝试使用番茄工作法，每次专注学习25分钟，然后休息5分钟，这样能提高学习效率和专注力。",
    "你目前有多个学习计划已经延期了，这可能会导致后续任务堆积。建议你花点时间检查一下时间安排，看看是任务量过大还是执行效率的问题，必要时可以调整计划的截止日期，给自己更合理的缓冲时间。同时，可以尝试使用时间管理工具，制定更详细的每日计划，提高时间利用效率。"
  ]
}
````

| 字段名        | 类型          | 描述                                                  |
| ------------- | ------------- | ----------------------------------------------------- |
| `suggestions` | Array<String> | 学习建议列表，根据用户数据匹配规则返回1-2条详细的建议 |

## 3. 建议规则说明

学习建议根据用户的学习数据匹配以下规则生成：

1. **完成率 < 50%**：建议用户拆解大计划，优先完成高优先级内容，使用番茄工作法提高效率

2. 2\*\*：建户检查划截止

3. **平均进度 > 80%**：建议用户保持当前节奏，定期回顾已学内容，巩固知识点

4. **每日计划占比 > 70%**：建议用户增加项目式学习计划，提升知识应用能力和解决问题的能力

5. **项目计划占比 < 10%**：建议用户每周安排1-2个项目式学习任务，检验和提升实战能力

6
2. **困难难度占比 > 60%**：建议用户适当搭配中等或低难度计划，平衡学习节奏，避免过度疲劳

3. **延期计划数 > 2**：建议用户检查时间安排，调整计划截止日期，使用时间管理工具提高效率

4. **平均进度 > 80%**：建议用户保持当前节奏，定期回顾已学内容，巩固知识点

5. **每日计划占比 > 70%**：建议用户增加项目式学习计划，提升知识应用能力和解决问题的能力

6. **项目计划占比 < 10%**：建议用户每周安排1-2个项目式学习任务，检验和提升实战能力

7. **简单难度占比 > 70%**：建议用户逐步增加中等难度计划，挑战自己，突破舒适区

根据用户数据匹配规则，返回1-2条最适合的详细建议。

## 4. 技术实现说明

### 4.1 时间范围计算
- **today**：从今天00:00:00到现在
- **week**：从7天前到现在
- **month**：从30天前到现在

### 4.2 数据筛选
- 所有统计数据仅针对状态为"已完成"的学习计划
- 时间筛选基于 `end_date` 字段，使用 PostgreSQL 的日期类型转换

### 4.3 计算逻辑
- **完成率**：已完成计划数 / 总计划数，保留两位小数
- **平均进度**：所有计划进度的平均值，保留两位小数
- **分布占比**：各类型数量 / 总计划数，保留两位小数

### 4.4 技术栈
- 后端：Spring Boot 3.5.8 + MyBatis + PostgreSQL
- 数据库：PostgreSQL 15.15
- 前端：Vue.js + Axios

## 5. 代码结构

```
├── src/main/java/com/school/
│   ├── config/
│   │   └── CorsConfig.java                 # CORS配置
│   ├── controller/
│   │   ├── StudyStatisticsController.java  # 统计分析接口控制器
│   │   └── StudySuggestionController.java   # 学习建议接口控制器
│   ├── entity/
│   │   └── StudyPlan.java                  # 学习计划实体类
│   ├── init/
│   │   └── StudyPlanDataInitializer.java   # 数据初始化类
│   ├── mapper/
│   │   ├── StudyPlanMapper.java            # 学习计划Mapper接口
│   │   └── StudyPlanMapper.xml             # 学习计划Mapper XML实现
│   ├── service/
│   │   ├── StudyStatisticsService.java     # 学习统计服务接口
│   │   ├── StudySuggestionService.java     # 学习建议服务接口
│   │   └── impl/
│   │       ├── StudyStatisticsServiceImpl.java # 学习统计服务实现
│   │       └── StudySuggestionServiceImpl.java # 学习建议服务实现
│   └── CampusLifePlatformApplication.java  # 应用主类
├── src/main/resources/
│   ├── mapper/
│   │   └── StudyPlanMapper.xml             # 学习计划Mapper XML实现
│   ├── api-documentation.md                # 接口文档
│   ├── application.yml                     # 应用配置文件
│   └── test-data.sql                       # 测试数据SQL
```

## 6. 数据库设计

### study_plans 表结构
| 字段名 | 数据类型 | 约束 | 描述 |
| --- | --- | --- | --- |
| `id` | `SERIAL` | `PRIMARY KEY` | 计划ID |
| `user_id` | `INTEGER` | `NOT NULL` | 用户ID |
| `title` | `VARCHAR(255)` | `NOT NULL` | 计划标题 |
| `description` | `TEXT` | | 计划描述 |
| `plan_type` | `VARCHAR(50)` | `NOT NULL` | 计划类型（daily/weekly/project） |
| `subject` | `VARCHAR(100)` | `NOT NULL` | 科目 |
| `difficulty` | `VARCHAR(50)` | `NOT NULL` | 难度（easy/medium/hard） |
| `status` | `VARCHAR(50)` | `NOT NULL` | 状态（已完成） |
| `progress_percent` | `INTEGER` | `NOT NULL` | 进度百分比 |
| `start_date` | `DATE` | `NOT NULL` | 开始日期 |
| `end_date` | `DATE` | `NOT NULL` | 结束日期 |

## 7. 运行说明

1. 确保 PostgreSQL 数据库已启动，并且 `study_plans` 表已存在
2. 确保项目依赖已安装（使用 Maven）
3. 启动 Spring Boot 应用
4. 使用 API 测试工具（如 Postman、curl 等）测试接口

## 8. 注意事项

- 接口返回的数据结构可能会根据用户数据的不同而有所变化
- 建议前端在接收数据时做好数据类型检查和默认值处理
- 所有统计数据仅针对状态为"已完成"的学习计划
- 时间筛选基于 `end_date` 字段，使用 PostgreSQL 的日期类型转换
- 如有任何问题，请联系后端开发人员

## 9. 前端调用示例

```javascript
// Vue + Axios 调用统计分析接口
axios.get('/api/study/statistics', {
  params: {
    timeRange: 'week',
    userId: 1
  }
})
.then(response => {
  console.log('统计数据:', response.data);
  // 处理统计数据
})
.catch(error => {
  console.error('获取统计数据失败:', error);
});

// Vue + Axios 调用学习建议接口
axios.get('/api/study/suggestions', {
  params: {
    timeRange: 'month',
    userId: 1
  }
})
.then(response => {
  console.log('学习建议:', response.data.suggestions);
  // 处理学习建议
})
.catch(error => {
  console.error('获取学习建议失败:', error);
});
```