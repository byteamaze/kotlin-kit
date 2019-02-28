一、在项目build.gradle中加入以下配置 <br/>
------------------------------------------- 
```groovy
buildscript {
    ext {
        kotlin_version = '1.2.31' // kotlin版本号
        support_version = '27.1.1' // support包版本号
        realm_enabled = true // Realm数据库开关，不需要数据库则设置为false
        compileSdkVersion = 27 // SDK编译版本
        targetSdkVersion = 27 // SDK目标版本
        minSdkVersion = 16 // 最低支持的SDK版本
        versionCode = 1 // 应用版本CODE
        versionName = '1.0' // 应用版本名
    }
    dependencies {
        // kotlin支持
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
        // Realm数据库支持
        classpath "io.realm:realm-gradle-plugin:5.0.1"
        // 务必在app/build.gradle中添加
        // apply plugin: 'kotlin-kapt'
        // apply plugin: 'realm-android'
    }
}
```

二、在module/build.gradle中如下配置
-------------------------------------------
```groovy
android {
    // SDK版本，应用版本等使用根项目的预设值，使各个moudule保持统一
    compileSdkVersion rootProject.ext.compileSdkVersion
    
    defaultConfig {
        applicationId "com.domobile.pixeldraw"
        minSdkVersion rootProject.ext.minSdkVersion
        targetSdkVersion rootProject.ext.targetSdkVersion
        versionCode rootProject.ext.versionCode
        versionName rootProject.ext.versionName
    }
}
dependencies {    
  // 引用support包时，用$support_version代替版本号
  implementation "com.android.support:appcompat-v7:$support_version"
}
```

三、参考ArchDemo进行编码，仔细阅读以下说明：
-------------------------------------------
1. 继承Application，并在onCreate中加入ApplicationProxy.init(this)<br>
   之后所有用到Context的地方都使用app代替getContext()等非Activity的容器，防止出错<br/>
2. 数据处理统一放入Store中，通过发送事件维护各个模块数据一致性
   * 每个监听事件变化的类都需要注册：Dispatcher.get().register(observer),<br>
     并且在不需要监听的时候(例：activity.onDestroy())进行unregister
   * Store在创建单例时候注册到总线：Dispatcher.get().register(xxStore)，无需unregister
   * 外部发送事件到Store，使用ActionCreator.get().XXX()
   * Store发送事件到外部，使用emitEventChange(type, event)
3. 每个独立的数据库都继承BaseRealm，参考AppRealm实现单例操作数据库
4. 在非必要的情况下，统一使用Fragment实现页面跳转，降低应用的开销
5. 页面间的数据传输，数据大或者序列化复杂的情况下，<br>
   使用TransportProxy.get()的put/pop方法存取数据，<br>
   其他简单数据依然可以借助Intent传输，仓库示例

四、推荐的第三方库(版本号到官方github查看最新的)
-------------------------------------------
* 数据库-Realm：
<https://realm.io/cn/docs/java/latest>
```groovy
   // 已集成到lib_arch中
```

* JSON反序列化库-GSON：
<https://github.com/google/gson>
```groovy
implementation 'com.google.code.gson:gson:+'
```

* 网络库-OKHTTP：
<https://github.com/square/okhttp>
```groovy
implementation 'com.squareup.okhttp3:okhttp:+'
```

* IO库-OKIO：
<https://github.com/square/okio>
```groovy
implementation 'com.squareup.okio:okio:+'
```

* 图片缓存库-Glide：
<https://github.com/bumptech/glide>
```groovy
implementation 'com.github.bumptech.glide:glide:4.+'
annotationProcessor 'com.github.bumptech.glide:compiler:4.+'
```

* 网络请求库-Retrofit：
<http://square.github.io/retrofit/>
```groovy
implementation 'com.squareup.retrofit2:retrofit:2.+'
```

* 响应式编程库-RxKotlin：
<https://github.com/ReactiveX/RxKotlin>
```groovy
api 'io.reactivex.rxjava2:rxjava:2.2.7'
api 'io.reactivex.rxjava2:rxandroid:2.1.1'
api 'io.reactivex.rxjava2:rxkotlin:2.3.0'
```
