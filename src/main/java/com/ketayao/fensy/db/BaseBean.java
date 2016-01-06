package com.ketayao.fensy.db;

import java.io.Serializable;

/** 
 * @author 	<a href="mailto:ketayao@gmail.com">ketayao</a>
 * @since   2014年3月20日 下午3:14:16 
 */
public class BaseBean implements Serializable {

    private static final long serialVersionUID = 4882249450248965518L;

    @Override
    public int hashCode() {
        //return HashCodeBuilder.reflectionHashCode(this);
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        //return EqualsBuilder.reflectionEquals(this, obj);
        return false;
    }

    @Override
    public String toString() {
        //return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
        return null;
    }
}
