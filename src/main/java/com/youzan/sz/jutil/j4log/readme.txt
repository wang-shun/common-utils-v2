1.	J4LOG升级
a)	管理接口(zxcv/j4log/)方便DEBUG, 支持日志动态调整级别和查看当前日志详细配置信息
b)	废弃部分不安全接口（仅保留接口定义，以保证编译通过）
c)	加入日志免配置功能
d)	公共日志配置接入配置中心统一管理
（b c 需要小小改动j4log.property）
2.	引入jar版本上报功能，方便管理未接入MAVEN和长期未发布的模块对JAR的使用。

第二个特性自动引入，无需大家进行任何代码编写工作。下面主要介绍J4LOG

j4log.property 格式完全向下兼容，在需要启用c,d新特性时，要需要在文件开始加入发下内容即可：

#J4LOG版本号
j4log.version=2
#J4LOG日志默认根目录
j4log.root=/data/log/module
j4log.level=info
#默认日志文件路径
_j4log=INFO, /data/log/module/_j4log

j4log.root 是日志根目录，日志免配置功能是运行在这个基础之上：
日志根目录，它是输出日志的默认目录。
    系统会在以下几处依次检测日志根目录配置，直到检测到合法配置：
    1.从j4log.property中读取j4log.root项，例如j4log.root=/data/log/webapp_house.
    2.依次读取系统属性(public static String System.getProperty(String key)):
       server.root
       resin.root
       tac.root
       如果读到到server.root,日志根目录被设置为 /${server.root}/log。resin.root,tac.root同上。
    如果检测失败，则日志根目录被设置为null

日志免配置功能：
1.	没有配置或配置错误的日志当作本地日志处理。
2.	本地日志配置缺少日志级别或日志文件，则启用默认配置：
            默认日志级别:INFO,可以通过j4log.level属性来定义默认日志级别
            默认日志文件:${ j4log.root}/LogName
3.	公共日志的配置信息直接接入配置中心统一管理，同时也允许在j4log.property中进行配置（本地配置格式不变）。

公共日志写在配置中心j4log文件中（正式版按IDC划分，目前只有一个，希望大家不要建太多个正式版，否则不好管理）
172.27.34.100:8080/configcenter/FileManager?op=display&confFileName=j4log

配置格式：
#本地日志
LogName=LEVEL,{rewrite|final}
#远程日志
LogName=LEVEL,DIR,IP:PORT,writeLocalLostLog{true|false},writeLocalFullLog{true|false},{rewrite|final}

{rewrite|final}表示本配置能否被本地配置覆盖。Final表示不允许，rewrite表示允许。
writeLocalLostLog{true|false} 丢失的远程日志是否输出本地到本地
writeLocalFullLog{true|false}  是否输出全量的远程日志

示例如下：
kernelBotAccessLog=info,final
lib_version_report=info,libversion,172.27.39.47:10021,true,false,final
