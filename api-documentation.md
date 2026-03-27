# 校园论坛后端API文档

## 基础信息

### 基础地址

`http://localhost:8080/api`

### 请求头说明

- `Content-Type`: `application/json`（用于POST请求）

### 分页参数说明

- `page`: 页码，从0开始，默认值为0
- `size`: 每页数量，默认值为10

### 统一返回格式

```json
{
  "code": 200, // 状态码
  "msg": "success", // 消息
  "data": {} // 数据
}
```

### 错误码说明

| 错误码 | 说明         |
| :----- | :----------- |
| 200    | 成功         |
| 403    | 权限不足     |
| 404    | 资源不存在   |
| 500    | 系统内部错误 |

### 内容审核说明

- **说明**：发布帖子、发表评论时，系统会自动检测内容是否包含敏感词汇，采用布隆过滤器 + DFA 双层过滤机制。通过本地过滤后，系统会异步调用百度云内容审核接口进行进一步审核。
- **拦截规则**：若检测到敏感词，接口直接返回错误码 500，msg 为 "内容包含敏感词汇，无法发布"，data 为 null
- **影响接口**：发布帖子（POST /posts）、发表评论（POST /comments）
- **审核状态**：
  - `audit_status=0`：待审核
  - `audit_status=1`：正常显示
  - `audit_status=2`：违规下架
  - `audit_status=3`：审核失败
- **查询规则**：所有查询接口会自动过滤 `audit_status=2` 的违规数据

### 百度云内容审核服务集成

- **API 调用流程**：
  1. 本地敏感词过滤通过后，系统异步调用百度云内容审核 API
  2. 文本审核：直接将文本内容发送给百度 API
  3. 图片审核：将本地图片转换为字节数组后发送给百度 API
  4. 解析百度返回的审核结果，映射为系统内部状态
  5. 根据审核结果更新内容状态（正常/违规/审核失败）

- **鉴权方式**：
  - 使用百度云平台的 AppID、API Key 和 Secret Key 进行身份验证
  - 配置文件位置：`application.yml` 中的 `baidu.cloud` 配置项

- **审核结果处理逻辑**：
  - 百度返回 `conclusionType=0`：合规 → 系统状态 1（正常显示）
  - 百度返回 `conclusionType=1`：疑似 → 系统状态 1（正常显示）
  - 百度返回 `conclusionType=2`：不合规 → 系统状态 2（违规下架）
  - 百度 API 调用失败或解析异常 → 系统状态 3（审核失败）

- **性能优化**：
  - 实现了 API 调用限流，控制在 1 QPS（根据百度免费版限制调整）
  - 加入了重试机制，最多尝试 3 次，每次间隔 2 秒
  - 图片审核前检查文件大小，确保不超过 4MB 限制
  - 异步处理审核任务，不阻塞主流程

- **错误处理**：
  - 网络异常：自动重试，达到最大重试次数后返回审核失败
  - 图片文件不存在：返回审核失败
  - 图片文件超过大小限制：返回审核失败
  - API 调用失败：记录错误日志，返回审核失败

## 图片上传接口

### 上传图片

- **请求方式**: POST
- **接口地址**: `/upload/images`
- **请求参数**:
  - `images`: 多文件上传（MultipartFile[]）
- **请求限制**:
  - 单文件大小 ≤ 50MB
  - 总请求大小 ≤ 50MB
- **响应格式**:
  ```json
  {
    "code": 200,
    "msg": "success",
    "data": [
      "/uploads/2026/03/21/04c747b3-9629-4bad-ae0b-e3d24c9b04d3.jpg",
      "/uploads/2026/03/21/086f0a91-e393-441e-ba6e-c1e30ac5ffd9.png"
    ]
  }
  ```
- **错误响应示例**:
  ```json
  {
    "code": 500,
    "msg": "File size exceeds 50MB limit",
    "data": null
  }
  ```

## 分类模块接口

### 查询所有分类列表

- **请求方式**: GET
- **接口地址**: `/categories`
- **请求参数**: 无
- **响应格式**:
  ```json
  {
    "code": 200,
    "msg": "success",
    "data": [
      {
        "id": 1,
        "name": "技术讨论",
        "code": "tech",
        "sort": 1,
        "status": 1
      },
      {
        "id": 2,
        "name": "生活分享",
        "code": "life",
        "sort": 2,
        "status": 1
      }
    ]
  }
  ```

## 帖子模块接口

### 发布帖子

- **请求方式**: POST
- **接口地址**: `/posts`
- **请求参数**:
  ```json
  {
    "title": "帖子标题",
    "content": "帖子内容",
    "categoryId": 1, // 分类ID
    "userId": 1, // 用户ID
    "imageUrls": ["/uploads/1.jpg", "/uploads/2.png", "/uploads/3.jpg"] // 图片URL列表（可选，支持多张图片）
  }
  ```
- **响应格式**:

  ```json
  {
    "code": 200,
    "msg": "发布成功",
    "data": {
      "id": 35,
      "title": "测试帖子多张图片2",
      "content": "这是一个测试帖子，包含多张图片",
      "categoryId": 1,
      "userId": 1,
      "auditStatus": 0, // 审核状态：0-待审核，1-正常，2-违规，3-审核失败
      "createTime": "2026-03-21T20:22:24.0795626",
      "comments": null,
      "images": [
        {
          "id": 26,
          "postId": 35,
          "imageUrl": "/uploads/1.jpg"
        },
        {
          "id": 27,
          "postId": 35,
          "imageUrl": "/uploads/2.png"
        },
        {
          "id": 28,
          "postId": 35,
          "imageUrl": "/uploads/3.jpg"
        }
      ]
    }
  }
  ```

- **敏感词校验**：对 title/content 进行布隆过滤器 + DFA 双层敏感词检测，包含敏感词则拦截发布，不对图片URL进行过滤
- **异步审核**：通过本地敏感词过滤后，系统会异步调用百度云内容审核接口对帖子标题、内容和图片进行进一步审核。审核结果会自动更新帖子的审核状态，违规内容会被自动下架。

- **错误响应示例**：

  ```json
  {
    "code": 500,
    "msg": "内容包含敏感词汇，无法发布",
    "data": null
  }
  ```

- **接口调用示例**：
  ```bash
  # 使用curl发布帖子
  curl -X POST http://localhost:8080/api/posts \
    -H "Content-Type: application/json" \
    -d '{
      "title": "今天天气真好",
      "content": "分享一下今天的好天气",
      "categoryId": 2,
      "userId": 1,
      "imageUrls": ["/uploads/2026/03/22/sunny.jpg"]
    }'
  ```

### 分页查询所有帖子

- **请求方式**: GET
- **接口地址**: `/posts`
- **请求参数**:
  - `page`: 页码，默认值为0
  - `size`: 每页数量，默认值为10
- **响应格式**:
  ```json
  {
    "code": 200,
    "msg": "success",
    "data": {
      "content": [
        {
          "id": 35,
          "title": "测试帖子多张图片2",
          "content": "这是一个测试帖子，包含多张图片",
          "categoryId": 1,
          "userId": 1,
          "auditStatus": 1, // 审核状态：0-待审核，1-正常，2-违规，3-审核失败
          "createTime": "2026-03-21T20:22:24.0795626",
          "images": [
            {
              "id": 26,
              "postId": 35,
              "imageUrl": "/uploads/1.jpg"
            },
            {
              "id": 27,
              "postId": 35,
              "imageUrl": "/uploads/2.png"
            },
            {
              "id": 28,
              "postId": 35,
              "imageUrl": "/uploads/3.jpg"
            }
          ]
        }
      ],
      "pageable": {
        "pageNumber": 0,
        "pageSize": 10,
        "sort": {
          "empty": false,
          "sorted": true,
          "unsorted": false
        },
        "offset": 0,
        "paged": true,
        "unpaged": false
      },
      "totalPages": 1,
      "totalElements": 1,
      "last": true,
      "size": 10,
      "number": 0,
      "sort": {
        "empty": false,
        "sorted": true,
        "unsorted": false
      },
      "first": true,
      "numberOfElements": 1,
      "empty": false
    }
  }
  ```

### 按分类分页查询帖子

- **请求方式**: GET
- **接口地址**: `/posts/category/{categoryId}`
- **请求参数**:
  - `categoryId`: 分类ID（路径参数）
  - `page`: 页码，默认值为0
  - `size`: 每页数量，默认值为10
- **响应格式**:
  ```json
  {
    "code": 200,
    "msg": "success",
    "data": {
      "content": [
        {
          "id": 35,
          "title": "测试帖子多张图片2",
          "content": "这是一个测试帖子，包含多张图片",
          "categoryId": 1,
          "userId": 1,
          "auditStatus": 1, // 审核状态：0-待审核，1-正常，2-违规，3-审核失败
          "createTime": "2026-03-21T20:22:24.0795626",
          "images": [
            {
              "id": 26,
              "postId": 35,
              "imageUrl": "/uploads/1.jpg"
            },
            {
              "id": 27,
              "postId": 35,
              "imageUrl": "/uploads/2.png"
            },
            {
              "id": 28,
              "postId": 35,
              "imageUrl": "/uploads/3.jpg"
            }
          ]
        }
      ],
      "pageable": {
        "pageNumber": 0,
        "pageSize": 10,
        "sort": {
          "empty": false,
          "sorted": true,
          "unsorted": false
        },
        "offset": 0,
        "paged": true,
        "unpaged": false
      },
      "totalPages": 1,
      "totalElements": 1,
      "last": true,
      "size": 10,
      "number": 0,
      "sort": {
        "empty": false,
        "sorted": true,
        "unsorted": false
      },
      "first": true,
      "numberOfElements": 1,
      "empty": false
    }
  }
  ```

### 获取帖子详情（包含评论）

- **请求方式**: GET
- **接口地址**: `/posts/{id}`
- **请求参数**:
  - `id`: 帖子ID（路径参数）
- **响应格式**:
  ```json
  {
    "code": 200,
    "msg": "success",
    "data": {
      "id": 35,
      "title": "测试帖子多张图片2",
      "content": "这是一个测试帖子，包含多张图片",
      "categoryId": 1,
      "userId": 1,
      "auditStatus": 1, // 审核状态：0-待审核，1-正常，2-违规，3-审核失败
      "createTime": "2026-03-21T20:22:24.0795626",
      "comments": [
        {
          "id": 30,
          "postId": 35,
          "userId": 1,
          "content": "测试评论多张图片",
          "auditStatus": 1, // 审核状态：0-待审核，1-正常，2-违规，3-审核失败
          "createTime": "2026-03-21T20:22:44.2754591",
          "images": [
            {
              "id": 12,
              "commentId": 30,
              "imageUrl": "/uploads/4.jpg"
            },
            {
              "id": 13,
              "commentId": 30,
              "imageUrl": "/uploads/5.png"
            },
            {
              "id": 14,
              "commentId": 30,
              "imageUrl": "/uploads/6.jpg"
            }
          ]
        }
      ],
      "images": [
        {
          "id": 26,
          "postId": 35,
          "imageUrl": "/uploads/1.jpg"
        },
        {
          "id": 27,
          "postId": 35,
          "imageUrl": "/uploads/2.png"
        },
        {
          "id": 28,
          "postId": 35,
          "imageUrl": "/uploads/3.jpg"
        }
      ]
    }
  }
  ```

### 删除帖子（只能本人删除）

- **请求方式**: DELETE
- **接口地址**: `/posts/{id}`
- **请求参数**:
  - `id`: 帖子ID（路径参数）
  - `userId`: 用户ID（查询参数）
- **响应格式**:
  ```json
  {
    "code": 200,
    "msg": "success",
    "data": null
  }
  ```

## 评论模块接口

### 发表评论

- **请求方式**: POST
- **接口地址**: `/comments`
- **请求参数**:
  ```json
  {
    "postId": 35, // 帖子ID
    "content": "评论内容",
    "userId": 1, // 用户ID
    "imageUrls": ["/uploads/4.jpg", "/uploads/5.png", "/uploads/6.jpg"] // 图片URL列表（可选，支持多张图片）
  }
  ```
- **响应格式**:

  ```json
  {
    "code": 200,
    "msg": "评论成功",
    "data": {
      "id": 30,
      "postId": 35,
      "userId": 1,
      "content": "测试评论多张图片",
      "auditStatus": 0, // 审核状态：0-待审核，1-正常，2-违规，3-审核失败
      "createTime": "2026-03-21T20:22:44.2754591",
      "images": [
        {
          "id": 12,
          "commentId": 30,
          "imageUrl": "/uploads/4.jpg"
        },
        {
          "id": 13,
          "commentId": 30,
          "imageUrl": "/uploads/5.png"
        },
        {
          "id": 14,
          "commentId": 30,
          "imageUrl": "/uploads/6.jpg"
        }
      ]
    }
  }
  ```

- **敏感词校验**：对 content 进行布隆过滤器 + DFA 双层敏感词检测，包含敏感词则拦截发布，不对图片URL进行过滤
- **异步审核**：通过本地敏感词过滤后，系统会异步调用百度云内容审核接口对评论内容和图片进行进一步审核。审核结果会自动更新评论的审核状态，违规内容会被自动下架。

- **错误响应示例**：

  ```json
  {
    "code": 500,
    "msg": "内容包含敏感词汇，无法发布",
    "data": null
  }
  ```

- **接口调用示例**：
  ```bash
  # 使用curl发表评论
  curl -X POST http://localhost:8080/api/comments \
    -H "Content-Type: application/json" \
    -d '{
      "postId": 35,
      "content": "这是一条测试评论",
      "userId": 1,
      "imageUrls": ["/uploads/2026/03/22/comment.jpg"]
    }'
  ```

### 根据帖子ID查询评论

- **请求方式**: GET
- **接口地址**: `/comments`
- **请求参数**:
  - `postId`: 帖子ID（查询参数）
- **响应格式**:
  ```json
  {
    "code": 200,
    "msg": "success",
    "data": [
      {
        "id": 30,
        "postId": 35,
        "userId": 1,
        "content": "测试评论多张图片",
        "auditStatus": 1, // 审核状态：0-待审核，1-正常，2-违规，3-审核失败
        "createTime": "2026-03-21T20:22:44.2754591",
        "images": [
          {
            "id": 12,
            "commentId": 30,
            "imageUrl": "/uploads/4.jpg"
          },
          {
            "id": 13,
            "commentId": 30,
            "imageUrl": "/uploads/5.png"
          },
          {
            "id": 14,
            "commentId": 30,
            "imageUrl": "/uploads/6.jpg"
          }
        ]
      }
    ]
  }
  ```

### 删除评论（只能本人删除）

- **请求方式**: DELETE
- **接口地址**: `/comments/{id}`
- **请求参数**:
  - `id`: 评论ID（路径参数）
  - `userId`: 用户ID（查询参数）
- **响应格式**:
  ```json
  {
    "code": 200,
    "msg": "success",
    "data": null
  }
  ```

## 审核模块接口

### 获取用户违规通知

- **请求方式**: GET
- **接口地址**: `/audit/violations`
- **请求参数**:
  - `userId`: 用户ID（查询参数）
- **响应格式**:
  ```json
  {
    "code": 200,
    "msg": "success",
    "data": [
      {
        "type": "post",
        "id": 35,
        "title": "测试帖子",
        "message": "您之前发布的帖子内容包含违规信息已自动下架",
        "createTime": "2026-03-21T20:22:24.0795626"
      },
      {
        "type": "comment",
        "id": 30,
        "postId": 35,
        "message": "您之前发布的评论内容包含违规信息已自动下架",
        "createTime": "2026-03-21T20:22:44.2754591"
      }
    ]
  }
  ```
- **错误响应示例**:
  ```json
  {
    "code": 400,
    "msg": "用户ID不能为空",
    "data": null
  }
  ```
  ```json
  {
    "code": 500,
    "msg": "获取违规通知失败：[错误信息]",
    "data": null
  }
  ```
