# 测试修复后的 /api/audit/violations 接口

Write-Host "=== 测试 1: 提供有效的 userId 参数 ==="
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

Write-Host "\n=== 测试 2: 不提供 userId 参数 ==="
try {
    $response = Invoke-WebRequest -Uri 'http://localhost:8080/api/audit/violations' -Method GET -UseBasicParsing
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
