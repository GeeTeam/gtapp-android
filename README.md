##概述
> 需要在真机环境下调试

1. 基于安卓4.0, 使用开发工具为android studio 2.0.0, 直接导入本目录即可看到app demo以及sdk的源码
2. 极验验证android sdk 通过对安卓的包装，方便安卓工程师集成极验验证的验证前端
3. 演示项目提供了用户服务器的预处理以及完整的一次验证，并将客户端验证结果向示例的客户服务器上发起二次验证的完整通讯过程。
4. sdk 包括 GtDialog, GTWebview, Geetest, DimenTool四个文件
5. 不依赖任何第三方库
6. android端sdk必须与服务器部署代码配套使用，否者无法完成二次验证。[服务器部署代码请移步官网安装文档](http://www.geetest.com/install/)
7. 不支持android 2.3的原因: JavascriptInterface 在2.3导致webview 崩溃, 尝试过解决但请原谅我们的方案并没有效果.[相关资料]   (https://code.google.com/p/android/issues/detail?id=12987)
8. 欢迎contributions.

###完整的验证中客户端包括以下三个部分：

	1.	从网站主服务器获取所需的验证数据({id: ...,challenge: ...,success: ...})
	2.	核心验证过程
	3.	根据验证回调数据进行二次验证

###Android SDK主要包含以下过程:

	1.	给出了默认的failback机制, 获取所需的验证数据(网站主可以根据自己的需求自己完成此过程)
	2.	完成核心验证过程
	3.	二次验证不是由sdk完成, 而是网站主自己参照demo的数据流通来完成这一部分的部署

###权限需求
请在app/.../AndroidManifest.xml中添加以下权限

```
	<uses-permission android:name="android.permission.INTERNET" />
   	<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
	<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
	<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
	<uses-permission android:name="android.permission.READ_PHONE_STATE" />
```

###Maven
如需使用maven管理依赖, 需要在你的主工程文件里加入一下配置

```
<dependency>
  <groupId>gtapp.android</groupId>
  <artifactId>sdk</artifactId>
  <version>1.0.0</version>
  <type>pom</type>
</dependency>
```
使用gradle

```
dependencies {
	 compile 'gtapp.android:sdk:1.0.0'
	}
```

###已知问题

	1. 连接了无效的代理时, 可以获得验证数据, 但因未能加载gtReady()接口, 而导致无法展示验证。此情况极为极端, 暂未提供解决方案。
