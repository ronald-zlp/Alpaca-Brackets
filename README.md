# Alpaca Brackets

一个可运行的 IntelliJ IDEA 插件：为不同嵌套层级的 `()`、`[]`、`{}` 分配不同颜色，并增强当前括号作用域的可读性。

## 已实现功能

- 彩虹括号：不同嵌套深度使用不同颜色
- 当前括号对高亮：光标位于括号上或括号作用域内时，当前最内层括号对会被额外强调
- 语法感知：在支持 PSI 的语言里跳过字符串和注释中的括号
- Java 泛型增强：智能高亮真正的泛型 `< >`，不会误把比较运算符当成括号
- XML/HTML 标签增强：按标签嵌套层级高亮 `< >`，并能把光标定位到最内层标签作用域
- 错配提示：不匹配括号使用红色波浪线，并提供更明确的提示文案
- 跨语言生效：通过通用 annotator 和编辑器监听器工作

## 本地运行

```powershell
.\gw.ps1 runIde
```

如果你更想手动执行，也可以先切到仓库自带的 JDK：

```powershell
$env:JAVA_HOME = (Resolve-Path .\.tools\jdk-17.0.14+7).Path
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat runIde
```

## 打包插件

```powershell
.\gw.ps1 buildPlugin
```

输出包位置：`build/distributions/`

## 后续可扩展方向

- 只对指定语言启用
- 支持 XML/HTML 标签类括号高亮
- 对当前层级整段作用域做更轻量的背景提示
