package com.yunlan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunlan.dto.EvaluationDTO;
import com.yunlan.dto.EvaluationVO;
import com.yunlan.dto.UserReportDTO;
import com.yunlan.entity.Evaluation;

import java.util.List;
import java.util.Map;

public interface EvaluationService extends IService<Evaluation> {
    List<Map<String, Object>> findAllSystemInfo();
    void addEvaluation(EvaluationDTO dto);
    List<Evaluation> pageByCurrentUser(int page, int pageSize);
    void deleteEvaluation(Long id);
    List<EvaluationVO> pageByTarget(Long serveItemId, int page, int pageSize, Long userId);
    void likeOrCancel(EvaluationDTO dto);
    int countEvaluationByServeItemId(Long serveItemId);
    void userReport(UserReportDTO dto);
}
