# 测试帖子发布接口
$url = "http://localhost:8080/api/posts"
$headers = @{"Content-Type" = "application/json"}
$body = @{
    title = "测试帖子"
    content = "吸毒、贩毒"
    categoryId = 1
    userId = 1
    createTime = "2024-01-01T00:00:00"
} | ConvertTo-Json

Write-Host "测试发布帖子..."
$response = Invoke-RestMethod -Uri $url -Method POST -Headers $headers -Body $body
Write-Host "发布结果:"
$response | ConvertTo-Json -Depth 3

# 等待5秒让审核完成
Write-Host "等待审核完成..."
Start-Sleep -Seconds 5

# 测试获取帖子列表
$listUrl = "http://localhost:8080/api/posts"
Write-Host "获取帖子列表..."
$listResponse = Invoke-RestMethod -Uri $listUrl -Method GET
Write-Host "帖子列表:"
$listResponse.data.content | ConvertTo-Json -Depth 3
