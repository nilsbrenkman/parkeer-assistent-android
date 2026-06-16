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

# Preserve line numbers for readable release stack traces, then hide the
# original source file name so it doesn't leak in obfuscated builds.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ---------------------------------------------------------------------------
# kotlinx.serialization
# R8 has partial built-in support, but @Serializable classes are reflected
# over via their generated $$serializer / Companion, so keep those explicitly.
# ---------------------------------------------------------------------------
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

# Keep the generated serializers and serializer() accessors.
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclasseswithmembers class ** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Our wire models are (de)serialized by name — keep them and their synthetic
# serializer classes intact so field names survive obfuscation/shrinking.
-keep,includedescriptorclasses class nl.parkeerassistent.amsterdam.data.model.**$$serializer { *; }
-keep,allowobfuscation,allowoptimization @kotlinx.serialization.Serializable class nl.parkeerassistent.amsterdam.data.model.** {
    <fields>;
}

# ---------------------------------------------------------------------------
# Retrofit / OkHttp
# Both ship consumer rules, but Retrofit reflects over interface type args and
# (on older toolchains) needs Kotlin metadata + generic signatures preserved.
# ---------------------------------------------------------------------------
-keepattributes Signature, Exceptions, RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Keep our Retrofit service interfaces (annotations drive the proxies).
-keep,allowobfuscation interface nl.parkeerassistent.amsterdam.data.remote.*Api { *; }

# OkHttp pulls in optional Conscrypt/BouncyCastle classes that may be absent.
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# ---------------------------------------------------------------------------
# MapLibre GL Native (parking-meter map)
# The SDK ships consumer rules, but its JNI bridge calls back into Java/Kotlin
# by name, so keep the native-facing classes and silence its optional refs.
# ---------------------------------------------------------------------------
-keep class org.maplibre.android.** { *; }
-keep interface org.maplibre.android.** { *; }
-dontwarn org.maplibre.android.**