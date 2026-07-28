package com.yunlan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunlan.dto.WorkerRecommendVO;
import com.yunlan.dto.WorkerRegisterDTO;
import com.yunlan.entity.WorkerRecommend;

import java.util.List;

public interface WorkerRecommendService extends IService<WorkerRecommend> {
    List<WorkerRecommendVO> getRecommendList(Long regionId);
    Long registerWorker(WorkerRegisterDTO dto);
}
