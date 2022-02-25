# fishtouch
目前仅提供format功能

## format：
maven插件，一键生成多环境配置（包括环境目录创建），生成assembly文件（用于打包），生成pom文件示例文件，生成run.bat\run.sh文件

多环境配置：
例：
application.yml

`
  xxx.value: @testValue@
`

可在对应的 profile/${dev}/profile.properties或${rootPath}/profile/${dev}/profile.properties文件中填入配置：

`
  testValue="foo"
`

打包时使用命令（根据需要自行添加参数，需配合"make-assembly:package"插件使用，详细参数可见动态生成的pom示例文件）：

｜ mvn install -P${dev}

