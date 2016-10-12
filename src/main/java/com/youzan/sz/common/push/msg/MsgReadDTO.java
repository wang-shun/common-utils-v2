
package com.youzan.sz.common.push.msg;

import java.util.List;

import com.youzan.sz.common.model.BaseDTO;
import com.youzan.sz.common.model.base.BaseStaffDTO;

/**
 *
 * Created by zhanguo on 2016/10/10.
 */
public class MsgReadDTO extends BaseStaffDTO {
    public MsgReadDTO() {
    }

    public MsgReadDTO(BaseStaffDTO baseStaffDTO) {
        super(baseStaffDTO);
    }

    /**
     * 目前支持以id来标记已读
     * */
    private List<String> ids;

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }
}
