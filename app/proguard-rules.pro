# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep line numbers for debugging
-keepattributes SourceFile,LineNumberTable

# Keep custom view constructors
-keepclassmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
