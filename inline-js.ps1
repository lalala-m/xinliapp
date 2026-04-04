$stomp = Get-Content 'd:\AllProject\AndroidStudioProjects\TYY\TongYangYuan\app\src\main\assets\js\stomp.min.js' -Raw
$appJs = Get-Content 'd:\AllProject\AndroidStudioProjects\TYY\TongYangYuan\app\src\main\assets\js\app.js' -Raw

$c = Get-Content 'd:\AllProject\AndroidStudioProjects\TYY\TongYangYuan\app\src\main\assets\chat.html' -Raw
$c = $c -replace '<script>/\* stomp inline \*/</script>', "<script>$stomp</script>"
$c = $c -replace '<script>/\* app inline \*/</script>', "<script>$appJs</script>"
Set-Content -Path 'd:\AllProject\AndroidStudioProjects\TYY\TongYangYuan\app\src\main\assets\chat.html' -Value $c -NoNewline -Encoding UTF8
Write-Host 'OK - stomp length:' $stomp.Length 'app.js length:' $appJs.Length
