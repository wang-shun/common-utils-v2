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
    private static final Map<PermissionsIndexEnum, PermissionsEnum> rightsMap    = new HashMap<>();
    private static final Logger                                     LOGGER       = LoggerFactory
        .getLogger(PermissionsUtil.class);
    private static final String                                     RIGHTS_SPLIT = ",";

    /**
     * 校验重复权限,如果出现重复退出当前jvm
     */
    public static void checkPermissionsRepeat() {
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
    public static Long[] transferPermissions2LongArr(Collection<PermissionsEnum> collection) {

        if (CollectionUtils.isEmpty(collection)) {
            return null;
        }
        /*  Map<Integer,List<RightsEnum>> rightsEnumMap = collection.stream().collect(Collectors.groupingBy(new Function<RightsEnum, Integer>() {
            @Override
            public Integer apply(RightsEnum t) {
                return Integer.valueOf(t.getRightsIndex().getIndex());
            }
        }));
        */
        Map<Integer, List<PermissionsEnum>> rightsEnumMap = collection.stream()
            .collect(Collectors.groupingBy(e -> e.getPermissionsIndex().getIndex()));
        Integer maxIndex = rightsEnumMap.keySet().stream().max(Integer::compare).orElseGet(null);

        Long[] rightArr = new Long[maxIndex + 1];

        Set set = rightsEnumMap.entrySet();
        Iterator i = set.iterator();
        while (i.hasNext()) {
            Map.Entry<Integer, List<PermissionsEnum>> entry = (Map.Entry<Integer, List<PermissionsEnum>>) i.next();

            if (rightArr[entry.getKey().intValue()] == null) {
                rightArr[entry.getKey().intValue()] = 0L;
            }
            for (PermissionsEnum rightsEnum : entry.getValue()) {
                rightArr[entry.getKey().intValue()] = rightsEnum.getPermissionsIndex().getValue()
                                                      | rightArr[entry.getKey().intValue()];
            }

        }
        return rightArr;
    }

    /**
     * 权限long arr=>string
     * @param collection
     * @return
     */
    public static String transferPermissions2String(Collection<PermissionsEnum> collection) {

        Long[] rights = transferPermissions2LongArr(collection);
        if (rights == null) {
            return "";
        }
        StringBuilder rightsStr = new StringBuilder();
        for (Long right : rights) {

            if (rightsStr.length() != 0) {
                rightsStr.append(RIGHTS_SPLIT);
            }
            rightsStr.append(right);
        }

        return rightsStr.toString();
    }
    /**
     * 权限str=>long[]
     * @param permissionsStr
     * @return
     */
    public static Long[] transferPermissionsStr2LongArr(String permissionsStr) {

        if (StringUtils.isEmpty(permissionsStr)) {
            return null;
        }
        String[] rights = permissionsStr.split(RIGHTS_SPLIT);

        if (rights == null || rights.length == 0) {
            return null;
        }
        List<Long> rightList = new ArrayList<>(rights.length);
        for (String right : rights) {
            if (StringUtils.isEmpty(right)) {
                rightList.add(0L);
            } else {
                rightList.add(Long.valueOf(right));
            }
        }

        return (Long[]) rightList.toArray(new Long[rightList.size()]);
    }

    /**
     * 合并角色多个权限
     * @param permissionsStr
     * @return
     */
    public static Long[] mergePermissionStr2LongArr(String... permissionsStr) {

        if (permissionsStr == null || permissionsStr.length == 0) {
            return null;
        }
        List<Long[]> permissions = new ArrayList<>();
        int max = 0;
        for (String permission : permissionsStr) {

            Long[] permissonLong = PermissionsUtil.transferPermissionsStr2LongArr(permission);
            max = permissonLong == null ? max : permissonLong.length > max ? permissonLong.length : max;
            if (permissonLong == null) {
                permissions.add(permissonLong);
            }
        }
        Long[] permissionsLong = new Long[max];

        for (int i = 0; i < max; i++) {
            Long eachLong = 0L;
            for (Long[] each : permissions) {

                if (each.length >= (i + 1)  && (each[i] != null)){
                    eachLong = eachLong | each[i];
                }
            }
            permissionsLong[i] = eachLong;
        }
        return permissionsLong;
    }

    /**
     * 合并角色多个权限
     * @param permissionsStr
     * @return
     */
    public static String mergePermissionStr2Str(String... permissionsStr) {
        Long[] allPermisssion = mergePermissionStr2LongArr(permissionsStr);
        if (allPermisssion == null || allPermisssion.length == 0) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for(Long each:allPermisssion){
            if(stringBuilder.length() ==0){
                stringBuilder.append(each);
            }else {
                stringBuilder.append(RIGHTS_SPLIT+each);
            }
        }
        return stringBuilder.toString();
        /* List<String> allPermissonStr = Arrays.asList(allPermisssion).stream().map(t -> t.toString())
            .collect(Collectors.toList());
        return allPermissonStr.toArray(new String[allPermissonStr.size()]);
        */
    }
    /**
     * 合并角色多个权限
     * @param permissionsStr
     * @return
     */
    public static String transferPermissionLongArr2Str(Long... permissionsStr) {
        StringBuilder stringBuilder = new StringBuilder();
        for(Long each:permissionsStr){
            if(stringBuilder.length() ==0){
                stringBuilder.append(each);
            }else {
                stringBuilder.append(RIGHTS_SPLIT+each);
            }
        }
        return stringBuilder.toString();
    }

    public static void main(String[] ags) {

        // RigthsUtil.checkRightsRepeat();

        for (RolesEnum rolesEnum : RolesEnum.values()) {

            String str = transferPermissions2String(rolesEnum.getPermissions());
            System.out.println(str);
            Long[] longr = transferPermissionsStr2LongArr(str);
            if (longr != null) {
                for (Long r : longr) {
                    System.out.println(Long.toHexString(r));
                }
            }

        }
    }
}
