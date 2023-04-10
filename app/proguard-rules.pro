# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

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

# okhttp 4.7.0及以上版本混淆规则
-keepclassmembers class okhttp3.internal.Util {
    public static java.lang.String userAgent;
}

# okhttp 4.7.0以下版本混淆规则
-keepclassmembers class okhttp3.internal.Version {
    # 4.0.0<=version<4.7.0
    public static java.lang.String userAgent;
    # version<4.0.0
    public static java.lang.String userAgent();
}
# okhttp 4.0.0以下版本混淆规则
-keepclassmembers class okhttp3.internal.http.StatusLine {
    public static okhttp3.internal.http.StatusLine parse(java.lang.String);
}

# 4.0.0 <= version < 4.3.0
-keepclassmembers class okhttp3.internal.cache.DiskLruCache$Companion {
    public okhttp3.internal.cache.DiskLruCache create(
              okhttp3.internal.io.FileSystem, java.io.File, int, int, long);
}

# version < 4.0.0
-keepclassmembers class okhttp3.internal.cache.DiskLruCache {
    public static okhttp3.internal.cache.DiskLruCache create(
                  okhttp3.internal.io.FileSystem, java.io.File, int, int, long);
}

-keep class com.shuyu.gsyvideoplayer.video.** { *; }
-dontwarn com.shuyu.gsyvideoplayer.video.**
-keep class com.shuyu.gsyvideoplayer.video.base.** { *; }
-dontwarn com.shuyu.gsyvideoplayer.video.base.**
-keep class com.shuyu.gsyvideoplayer.utils.** { *; }
-dontwarn com.shuyu.gsyvideoplayer.utils.**
-keep class tv.danmaku.ijk.** { *; }
-dontwarn tv.danmaku.ijk.**
-keep class com.google.android.exoplayer2.** {*;}
-keep interface com.google.android.exoplayer2.**

-keep class com.shuyu.alipay.** {*;}
-keep interface com.shuyu.alipay.**

-keep public class * extends android.view.View{
    *** get*();
    void set*(***);
    public <init>(android.content.Context);
    public <init>(android.content.Context, java.lang.Boolean);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}