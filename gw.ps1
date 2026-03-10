$ErrorActionPreference = 'Stop'

$jdkHome = Resolve-Path "$PSScriptRoot\.tools\jdk-17.0.14+7"
$env:JAVA_HOME = $jdkHome.Path
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

& "$PSScriptRoot\gradlew.bat" @Args

