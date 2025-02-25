[ ![Download](https://api.bintray.com/packages/32774707/maven/rxhttp2/images/download.svg) ](https://bintray.com/32774707/maven/rxhttp2/_latestVersion)

# RxHttp主要优势

  ***1. 30秒即可上手，学习成本极低***

  ***2. 史上最优雅的支持 Kotlin 协程***

  ***3. 史上最优雅的处理多个BaseUrl及动态BaseUrl***

  ***4. 史上最优雅的对错误统一处理，且不打破Lambda表达式***

  ***5. 史上最优雅的文件上传/下载/断点下载/进度监听，已适配Android 10***

  ***6. 支持Gson、Xml、ProtoBuf、FastJson等第三方数据解析工具***

  ***7. 支持Get、Post、Put、Delete等任意请求方式，可自定义请求方式***

  ***8. 支持在Activity/Fragment/View/ViewModel/任意类中，自动关闭请求***

  ***9. 支持全局加解密、添加公共参数及头部、网络缓存，均支持对某个请求单独设置***

# 请求三部曲

![image](https://github.com/liujingxing/okhttp-RxHttp/blob/master/screen/rxhttp_sequence_chart.jpg)

# 上手教程

30秒上手教程：[30秒上手新一代Http请求神器RxHttp](https://juejin.im/post/5cfcbbcbe51d455a694f94df)

协程文档：[RxHttp ，比Retrofit 更优雅的协程体验](https://juejin.im/post/5e77604fe51d4527066eb81a#heading-2)

掘金详细文档：[RxHttp 让你眼前一亮的Http请求框架](https://juejin.im/post/5ded221a518825125d14a1d4)

wiki详细文档：https://github.com/liujingxing/okhttp-RxHttp/wiki  (此文档会持续更新)


自动关闭请求用到的RxLife类，详情请查看[RxLife库](https://github.com/liujingxing/RxLife)

[更新日志](https://github.com/liujingxing/okhttp-RxHttp/wiki/%E6%9B%B4%E6%96%B0%E6%97%A5%E5%BF%97) &nbsp;&nbsp;&nbsp;&nbsp;
[已知问题](https://github.com/liujingxing/okhttp-RxHttp/wiki/%E5%B7%B2%E7%9F%A5%E9%97%AE%E9%A2%98) &nbsp;&nbsp;&nbsp;&nbsp;

[遇到问题，点击这里，99%的问题都能自己解决](https://github.com/liujingxing/okhttp-RxHttp/wiki/FAQ)


# 上手准备

***RxHttp&RxLife 交流群：378530627***

***[Maven依赖点击这里](https://github.com/liujingxing/okhttp-RxHttp/blob/master/maven_dependency.md)***

***1、RxHttp目前已适配`OkHttp 3.12.0 - 4.9.0`版本(4.3.0版本除外), 如你想要兼容21以下，请依赖`OkHttp 3.12.x`，该版本最低要求 API 9***

***2、asXxx方法内部是通过RxJava实现的，而RxHttp 2.2.0版本起，内部已剔除RxJava，如需使用，请自行依赖RxJava并告知RxHttp依赖的Rxjava版本***


## 必须
```java
//使用kapt依赖rxhttp-compiler时必须
apply plugin: 'kotlin-kapt'

android {
    //必须，java 8或更高
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation 'com.ljx.rxhttp:rxhttp:2.4.3'
    implementation 'com.squareup.okhttp3:okhttp:4.9.0' //rxhttp v2.2.2版本起，需要手动依赖okhttp
    kapt 'com.ljx.rxhttp:rxhttp-compiler:2.4.3' //生成RxHttp类，纯Java项目，请使用annotationProcessor代替kapt
 }
```

## 可选
```java
android {
    defaultConfig {
        javaCompileOptions {
            annotationProcessorOptions {
                arguments = [
                    //使用asXxx方法时必须，告知RxHttp你依赖的rxjava版本，可传入rxjava2、rxjava3
                    rxhttp_rxjava: 'rxjava3'， 
                    rxhttp_package: 'rxhttp'   //非必须，指定RxHttp类包名
                ]
            }
        }
    }
}
dependencies {
    implementation 'com.ljx.rxlife:rxlife-coroutine:2.0.1' //管理协程生命周期，页面销毁，关闭请求
    
    //rxjava2   (RxJava2/Rxjava3二选一，使用asXxx方法时必须)
    implementation 'io.reactivex.rxjava2:rxjava:2.2.8'
    implementation 'io.reactivex.rxjava2:rxandroid:2.1.1'
    implementation 'com.ljx.rxlife2:rxlife-rxjava:2.0.0' //管理RxJava2生命周期，页面销毁，关闭请求

    //rxjava3
    implementation 'io.reactivex.rxjava3:rxjava:3.0.6'
    implementation 'io.reactivex.rxjava3:rxandroid:3.0.0'
    implementation 'com.ljx.rxlife3:rxlife-rxjava:3.0.0' //管理RxJava3生命周期，页面销毁，关闭请求

    //非必须，根据自己需求选择 RxHttp默认内置了GsonConverter
    implementation 'com.ljx.rxhttp:converter-fastjson:2.4.3'
    implementation 'com.ljx.rxhttp:converter-jackson:2.4.3'
    implementation 'com.ljx.rxhttp:converter-moshi:2.4.3'
    implementation 'com.ljx.rxhttp:converter-protobuf:2.4.3'
    implementation 'com.ljx.rxhttp:converter-simplexml:2.4.3'
}
```

最后，***rebuild一下(此步骤是必须的)*** ，就会自动生成RxHttp类


# 混淆

`RxHttp v2.2.8`版本起，无需添加任何混淆规则(内部自带混淆规则)，v2.2.8以下版本，请[查看混淆规则](https://github.com/liujingxing/okhttp-RxHttp/wiki/关于混淆),并添加到自己项目中


# 小技巧

在这教大家一个小技巧，由于使用RxHttp发送请求都遵循请求三部曲，故我们可以在android studio 设置代码模版,如下

![image](https://github.com/liujingxing/RxHttp/blob/master/screen/templates.png)

如图设置好后，写代码时，输入rp,就会自动生成模版，如下：

![image](https://github.com/liujingxing/RxHttp/blob/master/screen/templates_demo.gif)


# Demo演示
<img src="https://github.com/liujingxing/RxHttp/blob/master/screen/screenrecorder-2019-11-27_22_56_26.gif" width = "240" height = "520" />

> 更多功能，请下载Demo体验

## Donations
如果它对你帮助很大，并且你很想支持库的后续开发和维护，那么你可以扫下方二维码随意打赏我，就当是请我喝杯咖啡或是啤酒，开源不易，感激不尽

![image](https://github.com/liujingxing/RxHttp/blob/master/screen/rxhttp_donate.png)


# Licenses
```
Copyright 2019 liujingxing

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
