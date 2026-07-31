/**
 * 第 12 章练习：异常处理
 * 对照文件：exception-demo.js
 *
 * 练习要求：
 * 1. 写 divide(int a, int b)：b 为 0 时抛 IllegalArgumentException（带提示信息）；
 *    两个调用方分别用 try-catch 就地处理 和 throws 继续上交，体会两条路线
 * 2. 自定义 InsufficientBalanceException（继承 RuntimeException，带错误码 1001），
 *    写 withdraw(double balance, double amount) 在余额不足时抛出并捕获打印
 * 3. 用 catch 打印 e.getClass().getName()，观察 Files.readString 读不存在文件时
 *    抛的异常类型，说出它属于受检还是非受检，为什么
 */
public class ExceptionDemo {

    public static void main(String[] args) {
        // 在这里写你的练习代码
    }
}
