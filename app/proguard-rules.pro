# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/vini/Dev/lib/android/sdk/tools/proguard/proguard-android.txt
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

-dontoptimize
-dontobfuscate

-keep public class com.google.common.base.** {
    public *;
}

-keep public class com.google.common.collect.Sets
-keepclassmembers class com.google.common.collect.Sets** {
 *;
 }

 -keep public class com.google.common.collect.Collections2
 -keepclassmembers class com.google.common.collect.Collections2** {
  *;
  }

-keep public final class com.google.common.collect.Lists
-keepclassmembers class com.google.common.collect.Lists** {
 *;
 }

-keep public final class com.google.common.collect.Iterables
-keepclassmembers class com.google.common.collect.Iterables** {
 *;
 }

-keep public class com.google.common.collect.ImmutableList.** {
    public *;
}

-keep public class com.google.common.io.CharStreams {
    public *;
}

-keep public class com.google.common.collect.HashMultiset
-keepclassmembers class com.google.common.collect.HashMultiset** {
 *;
 }

-keep public class com.google.common.collect.HashBiMap
-keepclassmembers class com.google.common.collect.HashBiMap** {
 *;
 }

-keep public class javax.annotation.Nullable.** {
     public *;
 }

-keep public class com.google.common.util.** {
    public *;
}

-keep public class com.google.common.primitives.** {
    public *;
}

-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase