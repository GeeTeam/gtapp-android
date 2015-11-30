
.. contents:: 目录

Demo演示
===================

1. 从Github中下载最新发布版项目到本地工作空间
#. 使用Android Studio IDE打开该项目
#. 其中gt-sdk是一个模块，供demo调用
#. 该项目是一个demo，直接点击运行可以执行，展示了gt-sdk里面的接口的使用方式

使用以上步骤，用户可以一分钟运行Demo示例。



自建项目引用
===================

假设用户自建项目名称为：CustomerProject

1. 在极验官方主页www.geetest.com注册账号并申请相应的应用公钥，id:{{id}}
#. 将gt-sdk目录复制到CustomerProject项目下
#. 将gt-sdk模块添加为CustomerProject项目的一个模块
#. 根据demo中的调用方式，将相应处的url和处理方式进行修改


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

2.15.11.30.1
---------------
1. 添加faiback功能
2. 使用Android Studio编写项目
3. 去掉了不需要的部分代码

2.15.4.16.1
---------------

1. 第一个版本，支持正常的本地验证功能。






