# 测试 /api/audit/violations 接口，触发异常

Write-Host "=== 测试 /api/audit/violations 接口 ==="
try {
    $response = Invoke-WebRequest -Uri 'http://localhost:8080/api/audit/violations?userId=1' -Method GET -UseBasicParsing
    Write-Host "响应状态码: $($response.StatusCode)"
    Write-Host "响应内容: $($response.Content)"
} catch {
    Write-Host "错误: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "响应体: $responseBody"
    }
}
