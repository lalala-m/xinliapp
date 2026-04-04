Get-ChildItem "d:\AllProject\AndroidStudioProjects\TYY\TongYangYuan-Server\deploy\openim" -Recurse -Include "*.yml","*.yaml" | ForEach-Object {
    $content = [System.IO.File]::ReadAllText($_.FullName)
    $unix = $content -replace "`r`n", "`n" -replace "`r", "`n"
    [System.IO.File]::WriteAllText($_.FullName, $unix)
    Write-Host "Fixed: $($_.FullName)"
}
