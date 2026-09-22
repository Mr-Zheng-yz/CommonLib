# ========== 发布混淆规则（本模块 AAR 打包前生效） ==========
# 保留公开 API：包下所有 public 类的类名及其 public/protected 成员不混淆，
# 调用方以此为准进行编译调用；private/包私有实现细节会被 R8 重命名混淆。
-keep public class com.ocamara.common_libs.** { public *; protected *; }

# 保留 Kotlin 元数据，保证 Kotlin 调用方正常解析声明
-keep class kotlin.Metadata { *; }

# 保留注解、泛型签名、内部类关系等属性
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod, Exceptions

# MultiStateLayout 等自定义 View 在 XML 中按全限定类名反射实例化，
# 其类名与 public 构造方法已被上面的 public 规则覆盖，无需额外 keep
