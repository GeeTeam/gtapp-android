====================================
Android-Dev
====================================

.. contents:: 目录

概述
================================================


1. 基于安卓4.0, 使用开发工具为android studio 1.5.1, 直接导入本目录即可看到app demo以及sdk的源码
2. 极验验证android sdk 通过对安卓的包装，方便安卓工程师集成极验验证的验证前端。
3. 演示项目提供了用户服务器的预处理以及完整的一次验证，并将客户端验证结果向示例的客户服务器上发起二次验证的完整通讯过程。
4. 不依赖任何第三方库
5. android端sdk必须与服务器部署代码配套使用，否者无法完成二次验证。`服务器部署代码请移步官网安装文档   <http://www.geetest.com>`__
6. 不支持android 2.3的原因: JavascriptInterface 在2.3导致webview 崩溃, 尝试过解决但请原谅我门的方案并没有效果 `相关资料   <https://code.google.com/p/android/issues/detail?id=12987>`__ 
7. 欢迎contributions.

验证主要分为三个部分：
	1.	从网站主服务器获取所需的验证数据(id,challenge,success)
	2.	核心验证过程
	3.	根据验证回调数据进行二次验证

Android SDK 主要完成过程:
	1.	给出默认的failback机制获取所需的验证数据(网站主可以根据自己的需求自己完成此过程)
	2.	完成核心验证过程
	3.	二次验证不是由sdk完成，而是网站主自己根据demo的逻辑来完成这一块的部署


通讯流程图
=======================================

.. image:: img/geetest_flow.png

技术实现展示
=======================================

.. image:: img/geetest_android.png

SDK的模块
=======================================

网络请求模块Geetest
-------------------------------------------------------------------

1. 构建Geetest模块

@Param String captchaURL 网站主配置用语请求验证参数的接口: api_1

@Param String validateURL 网站主配置用语二次验证的接口: api_2

.. code::

	public Geetest(String captchaURL, String validateURL);

2. 获取宕机状态

.. code::
	
	public boolean checkServer();

3. 获取用于验证的id

.. code::

	public String getGt();


4. 获取用于验证的challenge

.. code::

	public String getChallenge();

5. 验证完成提交数据进行二次验证

@param Map<String, String> params 二次验证的参数

@param String encode 编码格式

.. code::

	public String submitPostData(Map<String, String> params, String encode);

验证对话模块GtDialog
-------------------------------------------------------------------

1. 构建GtDialog

@param Context context 上下文

@param String id 验证id

@param String challenge 验证质询/流水号

@param Boolean success 宕机状态

.. code::

	public GtDialog (Context context, String id, String challenge, Boolean success);

2. 回调接口

.. code::

	public interface GtListener {

        //通知native验证已准备完毕
        void gtCallReady();

        //通知native关闭验证
        void closeGt();

        //通知native验证结果，并准备二次验证
        void gtResult(boolean success, String result);
    }

3. 调试模式

@param Boolean debug 是否使用调试模式

.. code::

	public void setDebug(Boolean debug);

(完)
