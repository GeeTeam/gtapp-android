
.. contents:: 目录

Demo演示
===================

1. 从Github中下载最新发布版项目到本地工作空间
#. 使用Eclipse+ADT的开发环境Import工作空间的两个项目
    1.  gt-android-sdk
         极验验证NativeApp的SDK，是一个Android Library
    #. gt-android-sdk-demo
         调用android-sdk的演示app程序。
#. 直接运行GtAppSdkDemo项目

使用以上步骤，用户可以一分钟运行Demo示例。



自建项目引用
===================

假设用户自建项目名称为：CustomerProject

1. 在极验官方主页www.geetest.com注册账号并申请相应的应用公钥，id:{{id}}
#. 将gt-android-sdk项目和CustomerProject项目Import到同一个工作空间
#. 将gt-android-sdk项目以Android Library的方式进行引用 右键项目-Properties-Android-Library-Add即可
#. 在项目三处TODO中替换成用户自已的处理代码。


回调函数及返回值
==================

函数：
.. code::

    gtResult(boolean success, String result) 

返回值：

1. success
    成功或者失败的值
#. result
    详细的返回信息，用于向客户服务器提交之后的SDK二次验证信息

        .. code::

           {
            "geetest_challenge": "5a8c21e206f5f7ba4fa630acf269d0ec4z", 
            "geetest_validate": "f0f541006215ac784859e29ec23d5b97", 
            "geetest_seccode": "f0f541006215ac784859e29ec23d5b97|jordan"
            }


    


发布日志
===================

2.15.4.16.1
---------------

1. 第一个版本，支持正常的本地验证功能。






