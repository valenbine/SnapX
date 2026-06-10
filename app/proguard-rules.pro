# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
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

-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions

-keep class com.example.snapx.** { *; }
-keepclassmembers class com.example.snapx.** { *; }

-keepclassmembers class * {
    public <methods>;
}

-keep class android.graphics.Bitmap { *; }
-keep class android.media.ImageReader { *; }
-keep class android.media.projection.MediaProjection { *; }

-keepclassmembers class * extends android.app.Service {
    public void onServiceConnected();
    public void onInterrupt();
}

-keep class org.opencv.** { *; }
-keepclassmembers class org.opencv.** { *; }

-dontwarn org.opencv.**
-dontwarn android.media.projection.**