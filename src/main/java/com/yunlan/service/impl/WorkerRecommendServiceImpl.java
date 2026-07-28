package com.yunlan.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunlan.dto.WorkerRecommendVO;
import com.yunlan.dto.WorkerRegisterDTO;
import com.yunlan.entity.WorkerRecommend;
import com.yunlan.mapper.WorkerRecommendMapper;
import com.yunlan.service.WorkerRecommendService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkerRecommendServiceImpl extends ServiceImpl<WorkerRecommendMapper, WorkerRecommend> implements WorkerRecommendService {

    @Override
    public List<WorkerRecommendVO> getRecommendList(Long regionId) {
        LambdaQueryWrapper<WorkerRecommend> wrapper = new LambdaQueryWrapper<WorkerRecommend>()
                .eq(WorkerRecommend::getStatus, 1)
                .orderByAsc(WorkerRecommend::getSort);

        if (regionId != null) {
            wrapper.eq(WorkerRecommend::getRegionId, regionId);
        }

        List<WorkerRecommend> list = this.list(wrapper);
        return list.stream()
                .map(item -> BeanUtil.copyProperties(item, WorkerRecommendVO.class))
                .collect(Collectors.toList());
    }

    @Override
    public Long registerWorker(WorkerRegisterDTO dto) {
        WorkerRecommend worker = new WorkerRecommend();
        worker.setName(dto.getName());
        worker.setAvatar(dto.getAvatar());
        worker.setExperienceYears(dto.getExperienceYears() != null ? dto.getExperienceYears() : 0);
        worker.setSkills(dto.getSkills());
        worker.setDescription(dto.getDescription());
        worker.setRegionId(dto.getRegionId());
        worker.setStatus(1);
        worker.setSort(99);
        worker.setRating(new java.math.BigDecimal("5.0"));
        worker.setServeCount(0);
        this.save(worker);
        return worker.getId();
    }
}
