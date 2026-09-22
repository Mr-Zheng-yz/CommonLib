# ========== 发布混淆规则（本模块 AAR 打包前生效） ==========
# 保留公开 API：RabbitMqManager 及其嵌套的 Config/Builder/MessageListener/StateListener
# 均为 public，类名与 public/protected 成员不混淆；内部实现会被 R8 混淆。
-keep public class com.example.mq_lib.** { public *; protected *; }

# 保留注解、泛型签名、内部类关系等属性
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod, Exceptions
