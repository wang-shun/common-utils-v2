package com.youzan.sz.common.util;

import com.youzan.sz.common.model.EnumValue;
import com.youzan.sz.common.model.Select;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zefa on 16/4/15.
 */
public class EnumUtils {

    public static List<? extends Select> paseEnum2Select(Class<? extends EnumValue> enumObj, Class<? extends Select> select) throws IllegalAccessException, InstantiationException {
        List<Select> result = new ArrayList<>();
        for (EnumValue item : enumObj.getEnumConstants()) {
            Select s = select.newInstance();
            s.setValue(item.getValue());
            s.setDesc(item.getName());
            result.add(s);
        }
        return result;
    }
}
