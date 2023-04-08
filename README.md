# PhigrosBot
基于Mirai Console

基于Phigros 2.4.7

至Phigros 2.5.1

### Java开发者使用PhigrosLibrary

下载项目源码

复制PhigrosLibrary目录到您的项目根目录

在您的项目根目录的settings.gradle添加一行

`include 'PhigrosLibrary'`

在需要引用PhigrosLibrary的项目的build.gradle里修改 dependencies

```groovy
dependencies {
    implementation project(':PhigrosLibrary')
}
```

### 功能
绘制B19成绩图

获取所有已打过的可推分曲的目标ACC

修改存档已打过歌分数

修改存档课题模式等级

修改存档data(1024MB以内)

添加存档头像

添加存档收藏品(未经测试)

### 快速使用

先搭建好Mirai Console

将PhigrosBot-0.0.3.mirai2.jar放入plugins目录

将data.zip解压至data/given.PhigrosBot 目录

重启Mirai Console

配置指令权限

### 指令列表

私聊发送25位SessionToken自动匹配并绑定

/p b19 生成B19图

/p expect 以转发消息形式发送所有可推分歌曲及目标ACC

/p data <MB> 修改data数

/p avater <头像名> 添加一个头像

/p collection <收藏品名> 为收藏品添加一条记录

/p challenge <课题分> 修改课题分 课题分为3位整数，彩48为548，金45为445。

/p modify <歌名> <难度> <分数> <ACC> <FC> 修改歌曲分数 ACC为小数，FC只有true和false两个选择

### Phigros QQ群
加入 282781492 闲聊