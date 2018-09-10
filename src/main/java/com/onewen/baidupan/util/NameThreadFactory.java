package com.onewen.baidupan.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程工厂
 * 
 * @author 梁光运
 * @date 2018年9月3日
 */
public class NameThreadFactory implements ThreadFactory{
	
	private final ThreadGroup group;
	
	private final AtomicInteger counter = new AtomicInteger(1);
	
	public NameThreadFactory(String name) {
		this.group = new ThreadGroup(name);
	}

	@Override
	public Thread newThread(Runnable r) {
		return new Thread(group, r, group.getName() +"-"+ counter.getAndIncrement());
	}

}
