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
	private static final int MAX_QUEUE_SIZE = 20;

	private static final int TIMEOUT = 5000;

	private final BlockingQueue<BTOperation> queue = new ArrayBlockingQueue<>(MAX_QUEUE_SIZE, true);

	private final Object waitLock = new Object();

	private volatile boolean isWaiting = false;
	private volatile boolean running = true;
	private volatile boolean paused = true;

	public static abstract class BTOperation {
		Throwable source;
		public BTOperation() {
			source = new Throwable("BT Operation scheduled");
		}

		abstract void run();
	}

	private final Runnable operation = () -> {
		BTOperation operation = null;
		try {

			while (running) {
				operation = pollNextOperation();
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
		} catch (Exception e) {
			Log.e(TAG, "Bluetooth error. Scheduled from: ", operation.source);
			Log.e(TAG, "Error: ", e);
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

	private BTOperation pollNextOperation() throws InterruptedException {
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
			try {
				queue.add(new BTOperation() {
					@Override
					void run() {
						op.run();
					}
				});
				queue.notifyAll();
			} catch (IllegalStateException e) {
				Log.w(TAG, "BT Queue is full.");
				queue.clear();
			}
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
