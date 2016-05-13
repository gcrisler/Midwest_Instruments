package com.midwestinstruments.watermeter;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Created by byronh on 5/12/16.
 */
public class ExpireListTest {


	@Test
	public void testList() {
		ExpireList<String> list = new ExpireList<>();
		Assert.assertEquals(0, list.getList().size());
		list.add("test", 1000);
		Assert.assertEquals(1, list.getList().size());
		Assert.assertEquals("test", list.getList().get(0));
		list.add("test2", 1200);
		Assert.assertEquals(2, list.getList().size());
		list.update(1000);
		Assert.assertEquals(1, list.getList().size());
		Assert.assertEquals("test2", list.getList().get(0));
	}
}
