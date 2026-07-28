package com.yunlan.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunlan.dto.EvaluationDTO;
import com.yunlan.dto.EvaluationVO;
import com.yunlan.dto.UserReportDTO;
import com.yunlan.entity.Evaluation;
import com.yunlan.entity.User;
import com.yunlan.mapper.EvaluationMapper;
import com.yunlan.service.EvaluationService;
import com.yunlan.service.UserService;
import com.yunlan.utils.UserHolder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EvaluationServiceImpl extends ServiceImpl<EvaluationMapper, Evaluation> implements EvaluationService {

    @Resource
    private UserService userService;

    @Override
    public List<Map<String, Object>> findAllSystemInfo() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> item1 = new HashMap<>();
        item1.put("id", 1);
        item1.put("name", "服务态度");
        list.add(item1);

        Map<String, Object> item2 = new HashMap<>();
        item2.put("id", 2);
        item2.put("name", "服务质量");
        list.add(item2);

        Map<String, Object> item3 = new HashMap<>();
        item3.put("id", 3);
        item3.put("name", "准时");
        list.add(item3);
        return list;
    }

    @Override
    public void addEvaluation(EvaluationDTO dto) {
        Long userId = UserHolder.get();
        Evaluation evaluation = new Evaluation();
        evaluation.setUserId(userId);
        evaluation.setOrderId(dto.getOrderId());
        evaluation.setServeItemId(dto.getServeItemId());
        evaluation.setContent(dto.getContent());
        evaluation.setPics(dto.getPics());
        evaluation.setStar(dto.getStar());
        evaluation.setLikeCount(0);
        evaluation.setStatus(1);
        this.save(evaluation);
    }

    @Override
    public List<Evaluation> pageByCurrentUser(int page, int pageSize) {
        Long userId = UserHolder.get();
        return this.list(new LambdaQueryWrapper<Evaluation>()
                .eq(Evaluation::getUserId, userId)
                .orderByDesc(Evaluation::getCreateTime)
                .last("LIMIT " + (page - 1) * pageSize + "," + pageSize));
    }

    @Override
    public void deleteEvaluation(Long id) {
        Long userId = UserHolder.get();
        this.remove(new LambdaQueryWrapper<Evaluation>()
                .eq(Evaluation::getId, id)
                .eq(Evaluation::getUserId, userId));
    }

    @Override
    public List<EvaluationVO> pageByTarget(Long serveItemId, int page, int pageSize, Long userId) {
        List<Evaluation> list = this.list(new LambdaQueryWrapper<Evaluation>()
                .eq(Evaluation::getServeItemId, serveItemId)
                .eq(Evaluation::getStatus, 1)
                .orderByDesc(Evaluation::getCreateTime)
                .last("LIMIT " + (page - 1) * pageSize + "," + pageSize));
        return list.stream().map(e -> convertToEvaluationVO(e)).collect(Collectors.toList());
    }

    @Override
    public void likeOrCancel(EvaluationDTO dto) {
        Evaluation evaluation = this.getById(dto.getEvaluationId());
        if (evaluation == null) throw new IllegalArgumentException("评价不存在");
        if (dto.getLikeFlag() != null && dto.getLikeFlag() == 1) {
            evaluation.setLikeCount((evaluation.getLikeCount() != null ? evaluation.getLikeCount() : 0) + 1);
        } else {
            evaluation.setLikeCount(Math.max(0, (evaluation.getLikeCount() != null ? evaluation.getLikeCount() : 0) - 1));
        }
        this.updateById(evaluation);
    }

    @Override
    public int countEvaluationByServeItemId(Long serveItemId) {
        long count = this.count(new LambdaQueryWrapper<Evaluation>()
                .eq(Evaluation::getServeItemId, serveItemId)
                .eq(Evaluation::getStatus, 1));
        return (int) count;
    }

    @Override
    public void userReport(UserReportDTO dto) {
        if (dto.getEvaluationId() == null) {
            throw new IllegalArgumentException("举报内容不能为空");
        }
    }

    private EvaluationVO convertToEvaluationVO(Evaluation evaluation) {
        EvaluationVO vo = new EvaluationVO();
        vo.setId(evaluation.getId());
        vo.setTotalScore(evaluation.getStar());
        vo.setContent(evaluation.getContent());
        vo.setCreateTime(evaluation.getCreateTime() != null ? evaluation.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
        vo.setIsLiked(false);

        // pictures array
        if (evaluation.getPics() != null && !evaluation.getPics().isEmpty()) {
            vo.setPictureArray(Arrays.asList(evaluation.getPics().split(",")));
        } else {
            vo.setPictureArray(new ArrayList<>());
        }

        // evaluator info
        EvaluationVO.EvaluatorInfo info = new EvaluationVO.EvaluatorInfo();
        info.setIsAnonymous(0);
        if (evaluation.getUserId() != null) {
            User user = userService.getById(evaluation.getUserId());
            if (user != null) {
                info.setAvatar(user.getAvatar());
                info.setNickName(user.getNickname());
            }
        }
        vo.setEvaluatorInfo(info);

        // statistics
        EvaluationVO.Statistics stats = new EvaluationVO.Statistics();
        stats.setLikeNumber(evaluation.getLikeCount() != null ? evaluation.getLikeCount() : 0);
        vo.setStatistics(stats);

        return vo;
    }
}
