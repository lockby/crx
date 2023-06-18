# Retrofit2 with Jackson, Bouncy Castle and OpenJSSE
-dontwarn com.fasterxml.jackson.databind.**
-dontwarn okio.**
-dontwarn retrofit2.**

# Keep Jackson ObjectMapper and ObjectWriter
-keep class com.fasterxml.jackson.module.kotlin.** { *; }
-keep class com.fasterxml.jackson.databind.** { *; }
-keep class com.fasterxml.jackson.databind.ObjectMapper {
    public <methods>;
    protected <methods>;
}
-keep class com.fasterxml.jackson.databind.ObjectWriter {
    public ** writeValueAsString(**);
}

# Keep Bouncy Castle classes
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**
-keep class org.conscrypt.** { *; }
-dontwarn org.conscrypt.**
-keep class org.openjsse.** { *; }
-dontwarn org.openjsse.**

# Keep retrofit classes
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-keepclassmembers class ** {
    @retrofit2.http.* <methods>;
}

# Other common libraries
-keep class androidx.annotation.** { *; }
-keep class javax.inject.** { *; }
-keep class com.crstlnz.komikchino.data.model.** { *; }
# End Retrofit2 with Jackson

#by android studio
-dontwarn javax.servlet.ServletContainerInitializer
-dontwarn org.apache.bsf.BSFManager
#####################


-keepattributes Signature
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-if interface * { @retrofit2.http.* public *** *(...); }
-keep,allowoptimization,allowshrinking,allowobfuscation class <3>