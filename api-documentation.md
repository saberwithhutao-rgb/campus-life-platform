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

### 敏感词过滤说明

- **说明**：发布帖子、发表评论时，系统会自动检测内容是否包含敏感词汇，采用布隆过滤器 + DFA 双层过滤机制
- **拦截规则**：若检测到敏感词，接口直接返回错误码 500，msg 为 "内容包含敏感词汇，无法发布"，data 为 null
- **影响接口**：发布帖子（POST /posts）、发表评论（POST /comments）

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
    "userId": 1 // 用户ID
  }
  ```
- **响应格式**:

  ```json
  {
    "code": 200,
    "msg": "success",
    "data": {
      "id": 1,
      "title": "帖子标题",
      "content": "帖子内容",
      "category": {
        "id": 1,
        "name": "技术讨论",
        "code": "tech",
        "sort": 1,
        "status": 1
      },
      "userId": 1,
      "createTime": "2026-02-28T12:00:00",
      "comments": []
    }
  }
  ```

- **敏感词校验**：对 title/content 进行布隆过滤器 + DFA 双层敏感词检测，包含敏感词则拦截发布

- **错误响应示例**：
  ```json
  {
    "code": 500,
    "msg": "内容包含敏感词汇，无法发布",
    "data": null
  }
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
          "id": 1,
          "title": "帖子标题",
          "content": "帖子内容",
          "category": {
            "id": 1,
            "name": "技术讨论",
            "code": "tech",
            "sort": 1,
            "status": 1
          },
          "userId": 1,
          "createTime": "2026-02-28T12:00:00"
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
          "id": 1,
          "title": "帖子标题",
          "content": "帖子内容",
          "category": {
            "id": 1,
            "name": "技术讨论",
            "code": "tech",
            "sort": 1,
            "status": 1
          },
          "userId": 1,
          "createTime": "2026-02-28T12:00:00"
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
      "id": 1,
      "title": "帖子标题",
      "content": "帖子内容",
      "category": {
        "id": 1,
        "name": "技术讨论",
        "code": "tech",
        "sort": 1,
        "status": 1
      },
      "userId": 1,
      "createTime": "2026-02-28T12:00:00",
      "comments": [
        {
          "id": 1,
          "post": {
            "id": 1
          },
          "userId": 2,
          "content": "评论内容",
          "createTime": "2026-02-28T12:30:00"
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
    "postId": 1, // 帖子ID
    "content": "评论内容",
    "userId": 1 // 用户ID
  }
  ```
- **响应格式**:

  ```json
  {
    "code": 200,
    "msg": "success",
    "data": {
      "id": 1,
      "post": {
        "id": 1
      },
      "userId": 1,
      "content": "评论内容",
      "createTime": "2026-02-28T12:30:00"
    }
  }
  ```

- **敏感词校验**：对 content 进行布隆过滤器 + DFA 双层敏感词检测，包含敏感词则拦截发布

- **错误响应示例**：
  ```json
  {
    "code": 500,
    "msg": "内容包含敏感词汇，无法发布",
    "data": null
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
