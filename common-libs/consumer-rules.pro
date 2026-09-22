# ========== 消费方规则（随 AAR 传递，调用方 App 混淆时生效） ==========
# 本库 AAR 发布前已混淆，此处保证调用方 App 再混淆时不破坏本库公开 API
-keep public class com.ocamara.common_libs.** { public *; protected *; }
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod, Exceptions
