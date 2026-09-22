# ========== 消费方规则（随 AAR 传递，调用方 App 混淆时生效） ==========
-keep public class com.example.mq_lib.** { public *; protected *; }
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod, Exceptions
