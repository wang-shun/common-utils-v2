package com.youzan.sz.common.permission;

import com.youzan.sz.common.util.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by wangpan on 216/11/30.
 */
public class PermissionsUtil {
    private static final Map<PermissionsIndexEnum, PermissionsEnum> rightsMap = new HashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionsUtil.class);
    private static final String RIGHTS_SPLIT=",";
    /**
     * 校验重复权限,如果出现重复退出当前jvm
     */
    public static void checkRightsRepeat() {
        boolean hasRepeat = false;
        if (rightsMap.size() == 0) {
            synchronized (rightsMap) {
                if (rightsMap.size() == 0) {
                    for (PermissionsEnum right : PermissionsEnum.values()) {
                        Object obj = rightsMap.put(right.getPermissionsIndex(), right);
                        if (obj != null) {
                            LOGGER.error("权限重复定义key=({})", right.getPermissionsIndex());
                            hasRepeat = true;
                        }
                    }
                }
            }
        }
        if (hasRepeat) {
            System.exit(0);
        }
    }

    /**
     * 权限集合转long
     * @param collection
     * @return
     */
    public static  Long[] transferRights2LongArr(Collection <PermissionsEnum> collection){

        if(CollectionUtils.isEmpty(collection)){
            return null;
        }
      /*  Map<Integer,List<RightsEnum>> rightsEnumMap = collection.stream().collect(Collectors.groupingBy(new Function<RightsEnum, Integer>() {
            @Override
            public Integer apply(RightsEnum t) {
                return Integer.valueOf(t.getRightsIndex().getIndex());
            }
        }));
      */
        Map<Integer,List<PermissionsEnum>> rightsEnumMap = collection.stream().collect(Collectors.groupingBy(e->e.getPermissionsIndex().getIndex()));
        Integer maxIndex = rightsEnumMap.keySet().stream().max(Integer::compare).orElseGet(null);

        Long [] rightArr = new Long[maxIndex+1];

        Set set = rightsEnumMap.entrySet();
        Iterator i = set.iterator();
        while(i.hasNext()){
            Map.Entry<Integer, List<PermissionsEnum>> entry=(Map.Entry<Integer, List<PermissionsEnum>>)i.next();

            if(rightArr[entry.getKey().intValue()] == null){
                rightArr[entry.getKey().intValue()] = 0L;
            }
            for(PermissionsEnum rightsEnum:entry.getValue()){
                rightArr[entry.getKey().intValue()] = rightsEnum.getPermissionsIndex().getValue()|rightArr[entry.getKey().intValue()];
            }

        }
        return rightArr;
    }
    public static String transferRights2String(Collection <PermissionsEnum> collection){

        Long [] rights = transferRights2LongArr(collection);
        if(rights == null){
            return "";
        }
        StringBuilder rightsStr = new StringBuilder();
        for(Long right:rights){

            if(rightsStr.length() !=0 ){
                rightsStr.append(RIGHTS_SPLIT);
            }
            rightsStr.append(right);
        }

        return rightsStr.toString();
    }
    public static Long[] transferRigthsStr2LongArr(String rightsStr){

        if(StringUtils.isEmpty(rightsStr)){
            return null;
        }
        String [] rights = rightsStr.split(RIGHTS_SPLIT);

        if(rights == null || rights.length == 0){
            return null;
        }
        List<Long> rightList = new ArrayList<>(rights.length);
        for(String right:rights){
            if(StringUtils.isEmpty(right)){
                rightList.add(0L);
            }else {
                rightList.add(Long.valueOf(right));
            }
        }

        return (Long[])rightList.toArray(new Long[rightList.size()]);
    }

    public static void main(String [] ags){

       // RigthsUtil.checkRightsRepeat();

        for(RolesEnum rolesEnum: RolesEnum.values()){

            String str = transferRights2String(rolesEnum.getPermissions());
            System.out.println(str);
            Long[] longr = transferRigthsStr2LongArr(str);
            if(longr != null){
                for(Long r:longr){
                    System.out.println(Long.toHexString(r));
                }
            }

        }
    }
}
