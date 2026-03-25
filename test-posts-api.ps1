# 测试帖子列表接口的响应格式
$url = "http://localhost:8080/api/posts"
Write-Host "Testing URL: $url"
try {
    $response = Invoke-WebRequest -Uri $url -Method GET
    Write-Host "Status Code: $($response.StatusCode)"
    Write-Host "Content-Type: $($response.Headers['Content-Type'])"
    Write-Host "Response Body:"
    Write-Host $response.Content
} catch {
    Write-Host "Error: $($_.Exception.Message)"
}
