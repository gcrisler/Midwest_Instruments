package com.midwestinstruments.watermeter;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Queue up BTLE write operations to execute them one at a time.</p>
 * BLTE write operations must be done one at a time and the response of the previous
 * must be received before the next operation may start. To accomplish this simply,
 * put write operations in a queue so they may be done single-file.
 * Created by byronh on 5/17/16.
 */
public class BTOperationQueue {

	private static final String TAG = BTOperationQueue.class.getSimpleName();
	private static final int MAX_QUEUE_SIZE = 10;

	private static final int TIMEOUT = 5000;

	private final BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(MAX_QUEUE_SIZE, true);

	private final Object waitLock = new Object();

	private volatile boolean isWaiting = false;
	private volatile boolean running = true;
	private volatile boolean paused = true;

	private final Runnable operation = () -> {
		try {

			while (running) {
				Runnable operation = pollNextOperation();
				synchronized (waitLock) {
					operation.run();
					isWaiting = true;

					Timer timer = new Timer();
					timer.schedule(new TimerTask() {
						@Override
						public void run() {
							Log.w(TAG, "BT Operation took too long");
							markOperationComplete();
						}
					}, TIMEOUT);

					while(isWaiting) {
						waitLock.wait();
					}
					timer.cancel();
				}

			}
		} catch (InterruptedException e) {
			Log.i(TAG, "Queue interrupted");
		}
	};

	private final Thread thread = new Thread(operation);

	/**
	 * Make a new Queue and start it.
	 */
	public BTOperationQueue() {
		thread.setName("BT Write Operations");
		thread.setDaemon(true);
		thread.start();
	}

	private Runnable pollNextOperation() throws InterruptedException {
		synchronized (queue) {
			while (queue.size() == 0 || paused) {
				queue.wait(); // no operations right now. wait forever.
			}
			return queue.poll();
		}
	}

	public void setPaused(boolean paused) {
		synchronized (queue) {
			this.paused = paused;
			queue.notifyAll();
		}
	}

	/**
	 * Schedule an operation. Run it later.
	 * @param op the operation to run
	 */
	public void scheduleOperation(Runnable op) {
		synchronized (queue) {
			queue.add(op);
			queue.notifyAll();
		}
	}

	/**
	 * Call this when a response is received from the bluetooth operation.
	 */
	public void markOperationComplete() {
		synchronized (waitLock) {
			isWaiting = false;
			waitLock.notifyAll();
		}
	}

	/**
	 * Stop the queue.
	 */
	public void stopQueue() {
		running = false;
		reset();
	}

	/**
	 * Reset the queue
	 */
	public void reset() {
		synchronized(queue) {
			queue.clear();
			queue.notifyAll();
		}
		markOperationComplete();
	}
}
