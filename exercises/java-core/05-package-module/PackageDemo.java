package com.example.demo;

/**
 * 第 05 章练习：包与模块
 * 对照文件：package-demo.js
 *
 * 练习要求：
 * 1. 在项目里真正建两个包 com.example.order 与 com.example.user，
 *    user 包写 public 的 User 类和不写修饰符的 UserValidator 类
 * 2. 在 order 包的类里 import 并使用 User；再试试访问 UserValidator，观察编译错误
 * 3. 体会：包声明必须与目录一致、同包免 import、默认修饰符 = 同包可见
 * 4. 进阶：加 module-info.java，验证"public 但未 exports 的包外部不可见"
 */
public class PackageDemo {

    public static void main(String[] args) {
        // 在这里写你的练习代码
    }
}
