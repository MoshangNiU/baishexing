package com.yunlan.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunlan.dto.ServeDetailVO;
import com.yunlan.dto.ServeItemVO;
import com.yunlan.dto.ServeSearchDTO;
import com.yunlan.entity.Evaluation;
import com.yunlan.entity.ServeItem;
import com.yunlan.mapper.EvaluationMapper;
import com.yunlan.mapper.ServeItemMapper;
import com.yunlan.service.ServeItemService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServeItemServiceImpl extends ServiceImpl<ServeItemMapper, ServeItem> implements ServeItemService {

    @Resource
    private EvaluationMapper evaluationMapper;

    @Override
    public List<ServeItem> getHotServeList() {
        LambdaQueryWrapper<ServeItem> wrapper = new LambdaQueryWrapper<ServeItem>()
                .eq(ServeItem::getHotStatus, 1)
                .eq(ServeItem::getStatus, 1);
        return this.list(wrapper);
    }

    @Override
    public List<ServeItemVO> searchServe(ServeSearchDTO dto) {
        LambdaQueryWrapper<ServeItem> wrapper = new LambdaQueryWrapper<ServeItem>()
                .eq(ServeItem::getStatus, 1);
        if (dto.getKeyword() != null && !dto.getKeyword().isEmpty()) {
            wrapper.like(ServeItem::getName, dto.getKeyword());
        }
        if (dto.getCategoryId() != null) {
            wrapper.eq(ServeItem::getCategoryId, dto.getCategoryId());
        }
        List<ServeItem> items = this.list(wrapper);
        return items.stream().map(this::toServeItemVO).collect(Collectors.toList());
    }

    @Override
    public ServeDetailVO getServeById(Long id) {
        ServeItem item = this.getById(id);
        if (item == null) return null;
        ServeDetailVO vo = new ServeDetailVO();
        vo.setServeItemId(item.getId());
        vo.setServeItemName(item.getName());
        vo.setServeItemImg(item.getImage());
        vo.setPrice(item.getPrice());
        vo.setOriginalPrice(item.getOriginalPrice());
        vo.setUnit(item.getUnit());
        vo.setDuration(item.getDuration());
        vo.setScope(item.getScope());
        vo.setTags(item.getTags());
        vo.setNotice(item.getNotice());
        vo.setDescription(item.getDescription());
        vo.setDetailImg(item.getDetailImg());
        // Build detail image list from comma-separated string
        java.util.List<String> detailImgs = new java.util.ArrayList<>();
        if (item.getDetailImg() != null && !item.getDetailImg().isEmpty()) {
            for (String s : item.getDetailImg().split(",")) {
                String t = s.trim();
                if (!t.isEmpty()) detailImgs.add(t);
            }
        }
        vo.setDetailImgList(detailImgs);
        // Build carousel images list from main image + detail images
        java.util.List<String> imgs = new java.util.ArrayList<>();
        if (item.getImage() != null) imgs.add(item.getImage());
        imgs.addAll(detailImgs);
        vo.setCarouselImages(imgs);

        // Compute serveCount and average rating from evaluations
        List<Evaluation> evals = evaluationMapper.selectList(
                new LambdaQueryWrapper<Evaluation>()
                        .eq(Evaluation::getServeItemId, item.getId())
                        .eq(Evaluation::getStatus, 1)
        );
        vo.setServeCount(evals.size());
        if (!evals.isEmpty()) {
            double avg = evals.stream().mapToInt(Evaluation::getStar).average().orElse(5.0);
            vo.setRating(BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP));
        } else {
            vo.setRating(BigDecimal.valueOf(5.0));
        }

        return vo;
    }

    public ServeItemVO toServeItemVO(ServeItem item) {
        ServeItemVO vo = new ServeItemVO();
        vo.setId(item.getId());
        vo.setServeItemName(item.getName());
        vo.setServeItemImg(item.getImage());
        vo.setPrice(item.getPrice());
        vo.setOriginalPrice(item.getOriginalPrice());
        vo.setUnit(item.getUnit());
        vo.setDescription(item.getDescription());

        // Compute serveCount and average rating from evaluations
        List<Evaluation> evals = evaluationMapper.selectList(
                new LambdaQueryWrapper<Evaluation>()
                        .eq(Evaluation::getServeItemId, item.getId())
                        .eq(Evaluation::getStatus, 1)
        );
        vo.setServeCount(evals.size());
        double avgRating = evals.stream()
                .mapToInt(Evaluation::getStar)
                .average()
                .orElse(5.0);
        vo.setRating(BigDecimal.valueOf(avgRating).setScale(1, RoundingMode.HALF_UP));
        return vo;
    }
}
