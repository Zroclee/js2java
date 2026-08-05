/**
 * 第 01 章练习：类、字段、方法
 * 对照文件：counter.js
 *
 * 练习要求：
 * 1. 设计一个 Counter 类：私有字段 count；无参构造（默认 0）与有参构造（this(...) 串联）
 * 2. 两个重载方法 add() 和 add(int n)；add(int n) 中校验 n 不能为负
 * 3. 提供 getter；在 main 中演示两种构造与两种重载的调用
 */
public class Counter {
    private int count;

    public Counter() {
        this(0);
    }

    public Counter(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count 不能为负");
        }
        this.count = count;
    }

    public void add() {
        this.add(1);
    }

    public void add(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n 不能为负");
        }
        this.count += n;
    }

    public int getCount() {
        return count;
    }

    public static void main(String[] args) {
        // 在这里写你的练习代码
        Counter counter = new Counter(10);
        counter.add(5);
        System.out.println(counter.getCount());
    }
}
