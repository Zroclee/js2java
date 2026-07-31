/**
 * 第 12 章练习：异常处理（JS 对照）
 * 对照文件：ExceptionDemo.java
 *
 * 练习要求：
 * 1. JS 版 divide：throw new Error('...')——注意 JS 可以 throw 任何值（甚至字符串），Java 只能抛 Throwable 子类
 * 2. 自定义 class InsufficientBalanceError extends Error，实现同样的取款逻辑
 * 3. 关键思考：JS 没有"受检异常"——函数可能抛什么全靠文档和运气，
 *    Java 的 throws 把"可能失败"写进了方法签名，你怎么看这个设计？
 */

// 在这里写你的 JS 实现
