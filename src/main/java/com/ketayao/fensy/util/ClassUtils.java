package com.ketayao.fensy.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="mailto:ketayao@gmail.com">ketayao</a>
 * @since 2014年2月27日 下午4:37:41
 */
public abstract class ClassUtils {

	/**
	 * 扫描给定包的所有类
	 * 
	 * @param packageName
	 *            包名
	 * @param recursive
	 *            是否递归子包
	 * @return
	 */
	public static Collection<String> getClassNames(String packageName,
			boolean recursive) {
		String packagePath = packageName.replace('.', File.separatorChar);
		URL url = ClassUtils.class.getClassLoader().getResource(packagePath);

		String path = null;
		try {
			path = URLDecoder.decode(url.getPath(), "utf-8");// 处理空格等特殊字符
		} catch (UnsupportedEncodingException e) {
		}

		Collection<File> files = FileUtils.listFiles(new File(path),
				new String[] { "class" }, recursive);
		Collection<String> classNames = new HashSet<String>();
		for (File file : files) {
			String name = StringUtils.substringAfterLast(file.getPath(),
					packagePath);
			classNames.add(packageName
					+ StringUtils.substringBeforeLast(
							name.replace(File.separatorChar, '.'), ".class"));
		}

		return classNames;
	}
}
