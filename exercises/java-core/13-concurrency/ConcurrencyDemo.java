/**
 * 第 13 章练习：线程与并发
 * 对照文件：concurrency-demo.js
 *
 * 练习要求：
 * 1. 亲手制造事故：两个线程各对共享 count 自增 10000 次，运行三次记录结果；
 *    然后用 synchronized 修复（恒为 20000），再用 AtomicInteger 实现一遍
 * 2. 写一个每秒打印心跳的线程，主线程 5 秒后 interrupt() 它，观察"体面停止"；
 *    再设为守护线程，看主线程结束后它的命运
 * 3. 用 ThreadPoolExecutor（核心 2、最大 4、队列 2、CallerRunsPolicy）提交 10 个
 *    耗时 1 秒的任务，打印每个任务被哪个线程执行——观察"核心→队列→临时工→拒绝"
 */
public class ConcurrencyDemo {

    public static void main(String[] args) throws InterruptedException {
        // 在这里写你的练习代码
    }
}
