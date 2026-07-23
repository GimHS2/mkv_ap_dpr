/*
 *	File Name:	ThreadFactoryBuilder.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2017/09/30		2.2.0	create
**/

package com.irt.custom;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * As Java default, ThreadPool will have name "pool-xxx" pattern.
 *
 * This custom ThreadFactoryBuilder can set "customName-xxx" pattern<br>
 *
 *
 * {@link ThreadFactory} instance should not shared across thread pools.<br/>
 * ThreadFactory instance should be assigned to single thread pool.
 */
public class ThreadFactoryBuilder {
	private static ThreadFactory build( ThreadFactoryBuilder builder ) {
		final String namePrefix = builder.namePrefix;
		final Boolean daemon = builder.daemon;
		final Integer priority = builder.priority;
		final UncaughtExceptionHandler uncaughtExceptionHandler = builder.uncaughtExceptionHandler;
		final ThreadFactory backingThreadFactory = ( builder.backingThreadFactory != null ) ? builder.backingThreadFactory
				: Executors.defaultThreadFactory();

		final AtomicLong count = new AtomicLong(0);

		return new ThreadFactory() {
			@Override
			public Thread newThread( Runnable runnable ) {
				Thread thread = backingThreadFactory.newThread(runnable);
				if( namePrefix != null ) {
					thread.setName(namePrefix + "-" + count.getAndIncrement());
				}
				if( daemon != null ) {
					thread.setDaemon(daemon);
				}
				if( priority != null ) {
					thread.setPriority(priority);
				}
				if( uncaughtExceptionHandler != null ) {
					thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
				}
				return thread;
			}
		};
	}

	private String namePrefix = null;
	private boolean daemon = false;
	private int priority = Thread.NORM_PRIORITY;
	private ThreadFactory backingThreadFactory = null;

	private UncaughtExceptionHandler uncaughtExceptionHandler = null;

	public ThreadFactory build() {
		return build(this);
	}

	/**
	 * @param daemon
	 *            : set as daemon or not for the {@link ThreadFactory#newThread(Runnable)} from this {@link ThreadFactoryBuilder}
	 * @return
	 */
	public ThreadFactoryBuilder setDaemon( boolean daemon ) {
		this.daemon = daemon;
		return this;
	}

	/**
	 * @param namePrefix
	 *            : set custom pool name prefix (To override Java default Pool Name pattern)
	 * @return
	 */
	public ThreadFactoryBuilder setNamePrefix( String namePrefix ) {
		if( namePrefix == null ) {
			throw new NullPointerException();
		}
		this.namePrefix = namePrefix;
		return this;
	}

	/**
	 * @param priority
	 *            : set priority for the {@link ThreadFactory#newThread(Runnable)} from this {@link ThreadFactoryBuilder}
	 * @return
	 */
	public ThreadFactoryBuilder setPriority( int priority ) {
		if( priority < Thread.MIN_PRIORITY ) {
			throw new IllegalArgumentException(String.format(
					"Thread priority (%s) must be >= %s", priority,
					Thread.MIN_PRIORITY));
		}

		if( priority > Thread.MAX_PRIORITY ) {
			throw new IllegalArgumentException(String.format(
					"Thread priority (%s) must be <= %s", priority,
					Thread.MAX_PRIORITY));
		}

		this.priority = priority;
		return this;
	}

	/**
	 * @param backingThreadFactory
	 *            : set already created {@link ThreadFactory}. {{@link #setUncaughtExceptionHandler(UncaughtExceptionHandler)} is required.
	 * @return
	 */
	public ThreadFactoryBuilder setThreadFactory( ThreadFactory backingThreadFactory ) {
		if( null == uncaughtExceptionHandler ) {
			throw new NullPointerException(
					"BackingThreadFactory cannot be null");
		}
		this.backingThreadFactory = backingThreadFactory;
		return this;
	}

	/**
	 * @param uncaughtExceptionHandler
	 * @return
	 */
	public ThreadFactoryBuilder setUncaughtExceptionHandler( UncaughtExceptionHandler uncaughtExceptionHandler ) {
		if( null == uncaughtExceptionHandler ) {
			throw new NullPointerException(
					"UncaughtExceptionHandler cannot be null");
		}
		this.uncaughtExceptionHandler = uncaughtExceptionHandler;
		return this;
	}
}