##概述

1. 基于安卓4.0, 使用开发工具为android studio 1.5.1, 直接导入本目录即可看到app demo以及sdk的源码
2. 极验验证android sdk 通过对安卓的包装，方便安卓工程师集成极验验证的验证前端。
3. 演示项目提供了用户服务器的预处理以及完整的一次验证，并将客户端验证结果向示例的客户服务器上发起二次验证的完整通讯过程。
4. 不依赖任何第三方库
5. android端sdk必须与服务器部署代码配套使用，否者无法完成二次验证。[服务器部署代码请移步官网安装文档](http://www.geetest.com/install/)
6. 不支持android 2.3的原因: JavascriptInterface 在2.3导致webview 崩溃, 尝试过解决但请原谅我们的方案并没有效果.[相关资料]   (https://code.google.com/p/android/issues/detail?id=12987)
7. 欢迎contributions.

###完整的验证中客户端包括以下三个部分：

	1.	从网站主服务器获取所需的验证数据(id,challenge,success)
	2.	核心验证过程
	3.	根据验证回调数据进行二次验证

###Android SDK主要包含以下过程:

	1.	给出了默认的failback机制, 获取所需的验证数据(网站主可以根据自己的需求自己完成此过程)
	2.	完成核心验证过程
	3.	二次验证不是由sdk完成, 而是网站主自己参照demo的数据流通来完成这一部分的部署