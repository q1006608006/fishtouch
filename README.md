# fishtouch
目前仅提供format功能

## format：
maven插件，一键生成多环境配置（包括环境目录创建），生成assembly文件（用于打包），生成pom文件示例文件，生成run.bat\run.sh文件

### 使用方式：
pom文件配置：

```
     <build>
        <plugins>
            <plugin>
                <groupId>top.ivan.fishtouch</groupId>
                <artifactId>format</artifactId>
                <version>0.2.0</version>
                <configuration>
                    <environment>
                        <envs>dev,test,xa</envs>
                        <relative>../profiles</relative>
                    </environment>
                </configuration>
            </plugin>
         ...
         </plugins>
      </build>

```

**maven配置见pom.xml.example**


### 插件生成的目录结构：

![image](https://user-images.githubusercontent.com/31004882/157622406-0fffb4a2-1560-4bb4-ba5b-961137c7505a.png)

### 多环境配置：
例：
application.yml

`
  xxx.value: @testValue@
`

|可在对应的 profile/${dev}/profile.properties或${rootPath}/profile/${dev}/profile.properties文件中填入配置：
     <build>

`
  testValue="foo"
`

![image](https://user-images.githubusercontent.com/31004882/157624736-37c863a1-7650-4f5e-a7f7-80655c365fe5.png)


### 打包
  打包命令（根据需要自行添加参数，需配合"make-assembly:package"插件使用，详细参数可见动态生成的pom示例文件）：
  
｜ mvn install -P${dev}



