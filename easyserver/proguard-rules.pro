# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\AndroidSDK/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

#指定代码的压缩级别
-optimizationpasses 5

#包明不混合大小写
#dontusemixedcaseclassnames

#不去忽略非公共的库类
#-dontskipnonpubliclibraryclasses

#优化 不优化输入的类文件
#-dontoptimize

#预校验
#-dontpreverify

#混淆时是否记录日志
#-verbose

#混淆时所采用的算法
-optimizations !code/simplification/arithmetic,!field/,!class/merging/

#保护注解
#-keepattributes Annotation
#保持哪些类不被混淆
-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService
#如果有引用v4包可以添加下面这行
-keep public class * extends android.support.v4.app.Fragment

#-libraryjars libs/jcifs-1.3.14.1.jar

-keep class com.google.zxing.**{*;}
-keep class io.vov.vitamio.**{*;}

-dontwarn jcifs.**
-keep class jcifs.http.**{*;}
-keep class jcifs.**{*;}


#忽略警告
#-ignorewarning

#记录生成的日志数据,gradle build时在本项目根目录输出
#apk 包内所有 class 的内部结构
-dump class_files.txt
#未混淆的类和成员
#-printseeds seeds.txt

#列出从 apk 中删除的代码
#printusage unused.txt

#混淆前后的映射
-printmapping mapping.txt

# 保持 native 方法不被混淆
-keepclasseswithmembernames class * {
    native <methods>;
}

-keep public class * implements  org.ecjtu.easyserver.servlet.BaseServlet{*;}

-keep public class org.ecjtu.easyserver.server.ServerManager{*;}
-keep public class org.ecjtu.easyserver.server.util.AssetsUtil{*;}
-keep public class org.ecjtu.easyserver.server.DeviceInfo{*;}
-keep public class org.ecjtu.easyserver.server.impl.service.EasyServerService{*;}
-keep public class org.ecjtu.easyserver.server.impl.server.EasyServer{*;}
-keep public class org.ecjtu.easyserver.server.ConversionFactory{*;}

-keepnames class org.ecjtu.easyserver.server.impl.service.EasyServerService$* {*;}

-keep public class * extends android.app.Fragment
-keep public class * extends org.ecjtu.easyserver.server.util.cache.FileCacheHelper{*;}
-keep public class * extends android.os.IInterface{*;}
