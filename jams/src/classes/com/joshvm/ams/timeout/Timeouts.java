package com.joshvm.ams.timeout;

/**
 * 超时类
 */
public class Timeouts implements Runnable {

	private static boolean timing = false;
	private static int flag = 0;
	private static TimeOutCallback timeOutCallback1;
	public static Thread thread;
	private static int outTime1 = 20;

	public void startTimer() {
		timing = true;
		flag = 0;
		thread = new Thread(new Timeouts());
		thread.start();

		System.out.println(timing + "===startTimer=======");
	}

	public void setCallback(TimeOutCallback timeOutCallback) {
		timeOutCallback1 = timeOutCallback;
	}

	public void run() {

		while (flag <= outTime1 && timing) {

			System.out.println(timing + "===running=======" + flag);
			flag++;

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		if (timing) {
			System.out.println("===OutTime=======");
			timeOutCallback1.timeOut();
			timing = false;
		}

	}

	/**
	 * 是否正在计时
	 * 
	 * @return
	 */
	public boolean isTiming() {
		return timing;
	}

	/**
	 * 设置超时时间
	 * 
	 * @param outTime
	 *            单位秒
	 */
	public void setOutTime(int outTime) {
		outTime1 = outTime;
	}

	/**
	 * 撤回监听
	 */
	public void dismiss() {
		
		System.out.println("===========dismiss======");
		timeOutCallback1 = null;
		timing = false;

	}

}