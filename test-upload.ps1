# 创建一个WebClient对象
$webClient = New-Object System.Net.WebClient

# 准备上传的文件
$filePath = "C:\Users\18549\Desktop\思维导图.jpg"

# 发送请求
$url = "http://localhost:8080/api/upload/images"
try {
    # 使用UploadFile方法直接上传文件
    $response = $webClient.UploadFile($url, $filePath)
    $responseString = [System.Text.Encoding]::UTF8.GetString($response)
    Write-Host "Response: $responseString"
} catch {
    Write-Host "Error: $($_.Exception.Message)"
}
