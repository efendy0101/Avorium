# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\YourUser\AppData\Local\Android\Sdk\tools\proguard\proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools-proguard.html

# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleAnnotations, RuntimeInvisibleParameterAnnotations
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.codehaus.mojo.animal_sniffer.*

# Gson
-keepattributes AnnotationDefault, RuntimeVisibleAnnotations, InnerClasses, EnclosingMethod
-keep class com.google.gson.** { *; }
-keep class com.example.avorium.data.** { *; }

# OkHttp
-keepattributes Signature
-keepattributes InnerClasses
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**

# Hilt
-keep class kotlinx.coroutines.internal.MainDispatcherFactory { *; }
-keep class com.google.crypto.tink.shaded.protobuf.** { *; }
