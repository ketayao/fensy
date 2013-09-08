package com.ketayao.fensy.test;

import java.util.Locale;

import org.apache.commons.lang3.LocaleUtils;
import org.junit.Assert;
import org.junit.Test;

public class LocalTest {
	@Test
	public void test() {
		Locale locale = LocaleUtils.toLocale("zh_CN");
		
		Assert.assertEquals("CN", locale.getCountry());
		Assert.assertEquals("zh", locale.getLanguage());
		
		System.out.println(locale.getLanguage() + "_" + locale.getCountry());
	}
}
