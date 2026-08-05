/**
 * 第 02 章练习：继承与多态
 * 对照文件：animal.js
 *
 * 练习要求：
 * 1. 设计 Animal 父类（name 字段 + speak() 方法），Dog / Cat 子类各自重写 speak()
 * 2. 所有重写方法加 @Override；体会加与不加的区别（故意写错参数试试）
 * 3. 用 Animal[] 数组 + 增强 for 演示多态；再用 instanceof 安全地调用子类独有方法
 */
public class AnimalDemo {

    public static void main(String[] args) {
        // 在这里写你的练习代码
    }
}

class Animal {
    protected String name;
    public void speak() {
        System.out.println("动物在说话");
    }
}

class Dog extends Animal {
    @Override
    public void speak() {
        System.out.println("汪汪汪");
    }
}