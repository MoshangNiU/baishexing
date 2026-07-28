package com.yunlan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunlan.dto.ServeDetailVO;
import com.yunlan.dto.ServeItemVO;
import com.yunlan.dto.ServeSearchDTO;
import com.yunlan.entity.ServeItem;

import java.util.List;

public interface ServeItemService extends IService<ServeItem> {
    List<ServeItem> getHotServeList();
    List<ServeItemVO> searchServe(ServeSearchDTO dto);
    ServeDetailVO getServeById(Long id);
    ServeItemVO toServeItemVO(ServeItem item);
}
