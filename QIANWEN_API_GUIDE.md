# 千问API集成指南

## 功能说明

学习建议功能已升级为使用阿里云千问大模型（通义千问）来生成个性化的学习建议。

## 配置步骤

### 1. 获取千问API密钥

1. 访问阿里云控制台：https://dashscope.console.aliyun.com/
2. 登录后，进入"API-KEY管理"页面
3. 创建API密钥（如果还没有的话）
4. 复制您的API密钥

### 2. 配置API密钥

在 `src/main/resources/application.yml` 文件中配置您的API密钥：

```yaml
qianwen:
  api:
    api-key: your-api-key-here  # 替换为您的实际API密钥
    endpoint: https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation
    model: qwen-max
    timeout: 30
```

或者，您可以通过环境变量配置（推荐用于生产环境）：

```bash
# Windows
set QIANWEN_API_KEY=your-api-key-here

# Linux/Mac
export QIANWEN_API_KEY=your-api-key-here
```

### 3. 重启项目

配置完成后，重启项目使配置生效：

```bash
mvn spring-boot:run
```

## 功能说明

### 工作原理

1. **收集学习数据**：从数据库中获取用户的学习统计数据
2. **构建提示词**：将统计数据格式化为AI友好的提示词
3. **调用千问API**：发送请求到阿里云千问大模型
4. **解析响应**：提取生成的学习建议
5. **返回给用户**：通过API接口返回给前端

### 数据统计信息

千问AI会基于以下数据生成建议：

- 总计划数
- 已完成计划数
- 完成率
- 平均进度
- 延期计划数
- 难度分布（easy/medium/hard）
- 计划类型分布（daily/weekly/monthly/project）
- 科目分布

### 生成建议的特点

- **个性化**：基于用户实际的学习数据
- **针对性强**：根据统计数据给出具体建议
- **鼓励性强**：语气友好，促进学习积极性
- **可执行性**：建议具体，容易实施
- **简洁明了**：每条建议不超过100字

## API端点

### 获取学习建议

**请求示例**：
```
GET http://localhost:8081/api/study/suggestions?timeRange=week&userId=1
```

**响应示例**：
```json
{
  "suggestions": [
    "你的完成率超过90%，表现很棒！继续保持...",
    "建议适当增加一些中等难度的学习计划..."
  ]
}
```

**参数说明**：
- `timeRange`: 时间范围，可选值：today/week/month
- `userId`: 用户ID

## 注意事项

1. **API密钥安全**：
   - 不要将API密钥提交到代码仓库
   - 在生产环境中使用环境变量
   - 定期更换API密钥

2. **费用考虑**：
   - 千问API按使用量收费
   - 建议缓存建议结果以减少API调用
   - 可以考虑使用更便宜的模型进行测试

3. **性能优化**：
   - 设置合理的超时时间
   - 考虑实现API调用限流
   - 添加错误重试机制

4. **备选方案**：
   - 如果API调用失败，可以返回默认建议
   - 考虑实现本地缓存策略

## 测试

### 1. 测试API连接

确保配置正确后，可以通过以下方式测试：

```bash
curl -X GET "http://localhost:8081/api/study/suggestions?timeRange=week&userId=1"
```

### 2. 查看日志

在应用日志中查看千问API的调用情况：
- 成功的调用
- 失败的错误信息
- 响应时间

## 故障排除

### 问题1：API密钥错误

**症状**：返回"API密钥无效"错误

**解决方案**：
- 检查API密钥是否正确复制
- 确认API密钥是否有效
- 检查是否使用了正确的环境变量

### 问题2：网络连接超时

**症状**：请求超时或连接失败

**解决方案**：
- 检查网络连接
- 增加timeout配置值
- 确认防火墙设置

### 问题3：API调用失败

**症状**：建议返回为空或默认文本

**解决方案**：
- 查看应用日志了解详细错误
- 检查API配额是否用完
- 确认模型名称是否正确

## 模型选择

当前配置使用 `qwen-max` 模型，您可以根据需要选择其他模型：

- `qwen-max`: 最强大的模型，适用于复杂任务
- `qwen-plus`: 平衡性能和成本
- `qwen-turbo`: 快速响应，成本较低

更多模型信息请参考阿里云官方文档。
