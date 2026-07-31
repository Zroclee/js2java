/**
 * 第 14 章练习：反射、注解与泛型
 * 对照文件：mini-ioc.js
 *
 * 练习要求：
 * 1. 写泛型方法 <T> List<T> filter(List<T> list, T exclude)：返回去掉指定元素的新列表
 * 2. 定义注解 @RequiresAuth(role = "admin")（RUNTIME 保留），
 *    用反射扫描一个类的所有方法，打印每个方法"是否需要鉴权、角色是什么"
 * 3. 扩展文档中的迷你容器：增加 @AutoInject 注解——注册 bean 时，
 *    被标记的字段用反射注入对应类型的实例（field.setAccessible + field.set）
 *    做完这题，你就理解了 @Autowired 的本质
 */
public class MiniIocDemo {

    public static void main(String[] args) {
        // 在这里写你的练习代码
    }
}
