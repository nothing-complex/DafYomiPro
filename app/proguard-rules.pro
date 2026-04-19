# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep line numbers for debugging
-keepattributes SourceFile,LineNumberTable

# Keep custom view constructors
-keepclassmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

# Keep kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.dafyomi.pro.**$$serializer { *; }
-keepclassmembers class com.dafyomi.pro.** {
    *** Companion;
}
-keepclasseswithmembers class com.dafyomi.pro.** {
    kotlinx.serialization.KSerializer serializer(...);
}
