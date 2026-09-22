# ========== 发布混淆规则（本模块 AAR 打包前生效） ==========
# 保留公开 API：MQTTManager / MQTTManagerAndroid / MqttEntity / MQTTListener 等
# public 类的类名与 public/protected 成员不混淆；内部实现会被 R8 混淆。
-keep public class com.baize.mqtt_lib.** { public *; protected *; }

# 保留 Kotlin 元数据，保证 Kotlin 调用方正常解析声明
-keep class kotlin.Metadata { *; }

# 保留注解、泛型签名、内部类关系等属性
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod, Exceptions
