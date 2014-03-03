package com.ketayao.fensy.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesUtils {

    private static final Logger logger = LoggerFactory.getLogger(PropertiesUtils.class);

    /**
     * 加载属性文件
     * @param propsPath
     * @return
     */
    public static Properties load(String propsPath) {
        Properties props = new Properties();
        InputStream is = null;
        try {
            String suffix = ".properties";
            if (propsPath.lastIndexOf(suffix) == -1) {
                propsPath += suffix;
            }
            is = PropertiesUtils.class.getClassLoader().getResourceAsStream(propsPath);
            if (is != null) {
                props.load(is);
            }
        } catch (IOException e) {
            logger.error("加载属性文件出错！propsPath：" + propsPath, e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                logger.error("释放资源出错！", e);
            }
        }
        return props;
    }

    /**
     * 加载属性文件，并转为 Map
     * @param propsPath
     * @return
     */
    public static Map<String, String> loadToMap(String propsPath) {
        Map<String, String> map = new HashMap<String, String>();
        Properties props = load(propsPath);
        for (String key : props.stringPropertyNames()) {
            map.put(key, props.getProperty(key));
        }
        return map;
    }

    /**
     * 获取字符型属性
     * @param props
     * @param key
     * @return
     */
    public static String getString(Properties props, String key) {
        return props.getProperty(key) != null ? props.getProperty(key) : "";
    }

    /**
     * 获取数值型属性
     * @param props
     * @param key
     * @return
     */
    public static int getNumber(Properties props, String key) {
    	return NumberUtils.toInt(props.getProperty(key), 0);
    }

    /**
     * 获取布尔型属性
     * @param props
     * @param key
     * @return
     */
    public static boolean getBoolean(Properties props, String key) {
    	return BooleanUtils.toBoolean(props.getProperty(key));
    }
}
