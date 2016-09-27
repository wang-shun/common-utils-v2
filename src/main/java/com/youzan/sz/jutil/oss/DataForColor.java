package com.youzan.sz.jutil.oss;

import java.util.HashMap;
import java.util.Map;

/**
 * 用于染色的数据
 * @author junehuang
 *
 */
public class DataForColor {
	Map<ColorType,String> map;
	public DataForColor(Map<ColorType,String> map){
		this.map = map;
	}

	public String getValue( ColorType type ){
		return map.get(type);
	}
	
	public Map<ColorType,String> getValues(){
		return new HashMap<ColorType,String>(map);
	}
}
