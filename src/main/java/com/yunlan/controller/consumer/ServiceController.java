package com.yunlan.controller.consumer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunlan.common.Result;
import com.yunlan.dto.HotServeVO;
import com.yunlan.dto.HomeServeVO;
import com.yunlan.dto.ServeResDTO;
import com.yunlan.dto.ServeTypeVO;
import com.yunlan.dto.ServeDetailVO;
import com.yunlan.dto.ServeItemVO;
import com.yunlan.dto.ServeSearchDTO;
import com.yunlan.dto.WorkerRecommendVO;
import com.yunlan.dto.WorkerRegisterDTO;
import com.yunlan.entity.Evaluation;
import com.yunlan.entity.Orders;
import com.yunlan.entity.ServeCategory;
import com.yunlan.entity.ServeItem;
import com.yunlan.mapper.EvaluationMapper;
import com.yunlan.mapper.OrdersMapper;
import com.yunlan.service.ServeCategoryService;
import com.yunlan.service.ServeItemService;
import com.yunlan.service.WorkerRecommendService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/foundations/customer/serve")
@Api(tags = "服务模块")
public class ServiceController {

    @Resource
    private ServeCategoryService serveCategoryService;

    @Resource
    private ServeItemService serveItemService;

    @Resource
    private WorkerRecommendService workerRecommendService;

    @Resource
    private EvaluationMapper evaluationMapper;

    @Resource
    private OrdersMapper ordersMapper;

    private void enrichWorkerStats(com.yunlan.entity.WorkerRecommend w) {
        if (w == null || w.getId() == null) return;
        Long serveCount = ordersMapper.selectCount(
                new LambdaQueryWrapper<Orders>()
                        .eq(Orders::getWorkerId, w.getId())
                        .eq(Orders::getDeleted, 0));
        w.setServeCount(serveCount != null ? serveCount.intValue() : 0);

        List<Evaluation> evals = evaluationMapper.selectList(
                new LambdaQueryWrapper<Evaluation>()
                        .eq(Evaluation::getWorkerId, w.getId())
                        .eq(Evaluation::getStatus, 1)
                        .eq(Evaluation::getDeleted, 0));
        if (evals != null && !evals.isEmpty()) {
            double avg = evals.stream().mapToInt(e -> e.getStar() != null ? e.getStar() : 0).average().orElse(0.0);
            w.setRating(BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP));
        } else {
            w.setRating(BigDecimal.valueOf(5.0));
        }
    }

    @GetMapping("/firstPageServeList")
    @ApiOperation("首页服务图标列表（含子服务）")
    public Result<List<HomeServeVO>> getFirstPageServeList() {
        List<ServeCategory> categories = serveCategoryService.getFirstPageServeList();
        List<HomeServeVO> result = new ArrayList<>();
        for (ServeCategory cat : categories) {
            HomeServeVO vo = new HomeServeVO();
            vo.setServeTypeId(cat.getId());
            vo.setServeTypeIcon(cat.getImage() != null && !cat.getImage().isEmpty() ? cat.getImage() : cat.getIcon());
            vo.setServeTypeName(cat.getName());

            List<ServeItem> items = serveItemService.lambdaQuery()
                    .eq(ServeItem::getCategoryId, cat.getId())
                    .eq(ServeItem::getStatus, 1)
                    .list();

            List<ServeResDTO> resList = items.stream().map(item -> {
                ServeResDTO dto = new ServeResDTO();
                dto.setServeItemId(item.getId());
                dto.setId(item.getId());
                dto.setServeItemIcon(item.getImage());
                dto.setServeItemName(item.getName());
                return dto;
            }).collect(Collectors.toList());
            vo.setServeResDTOList(resList);
            result.add(vo);
        }
        return Result.success(result);
    }

    @GetMapping("/hotServeList")
    @ApiOperation("首页热门服务列表")
    public Result<List<HotServeVO>> getHotServeList() {
        List<ServeItem> items = serveItemService.getHotServeList();
        List<HotServeVO> result = items.stream().map(item -> {
            HotServeVO vo = new HotServeVO();
            vo.setId(item.getId());
            vo.setServeItemImg(item.getImage());
            vo.setServeItemName(item.getName());
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
            if (!evals.isEmpty()) {
                double avg = evals.stream().mapToInt(Evaluation::getStar).average().orElse(5.0);
                vo.setRating(BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP));
            } else {
                vo.setRating(BigDecimal.valueOf(5.0));
            }
            return vo;
        })
        // Sort by serveCount descending (most popular first)
        .sorted((a, b) -> {
            int ca = a.getServeCount() != null ? a.getServeCount() : 0;
            int cb = b.getServeCount() != null ? b.getServeCount() : 0;
            return cb - ca;
        })
        .collect(Collectors.toList());
        return Result.success(result);
    }

    @GetMapping("/search")
    @ApiOperation("搜索服务")
    public Result<List<ServeItemVO>> searchServe(String keyword, Long categoryId,
                                                  @RequestParam(defaultValue = "1") Integer page,
                                                  @RequestParam(defaultValue = "10") Integer pageSize,
                                                  String cityCode, String sortType) {
        ServeSearchDTO dto = new ServeSearchDTO();
        dto.setKeyword(keyword);
        dto.setCategoryId(categoryId);
        return Result.success(serveItemService.searchServe(dto));
    }

    @GetMapping("/serveTypeList")
    @ApiOperation("获取服务分类列表")
    public Result<List<ServeTypeVO>> getServeTypeList(@RequestParam(required = false) Long regionId) {
        List<ServeCategory> categories = serveCategoryService.getServeTypeList();
        List<ServeTypeVO> result = categories.stream().map(cat -> {
            ServeTypeVO vo = new ServeTypeVO();
            vo.setServeTypeId(cat.getId());
            vo.setServeTypeName(cat.getName());
            vo.setServeTypeImg(cat.getImage() != null && !cat.getImage().isEmpty() ? cat.getImage() : cat.getIcon());
            return vo;
        }).collect(Collectors.toList());
        return Result.success(result);
    }

    @GetMapping("/workerRecommend")
    @ApiOperation("获取阿姨推荐列表")
    public Result<List<WorkerRecommendVO>> getWorkerRecommend(@RequestParam(required = false) Long regionId) {
        List<WorkerRecommendVO> list = workerRecommendService.getRecommendList(regionId);
        if (list != null) {
            for (WorkerRecommendVO vo : list) {
                com.yunlan.entity.WorkerRecommend w = new com.yunlan.entity.WorkerRecommend();
                w.setId(vo.getId());
                w.setServeCount(vo.getServeCount());
                w.setRating(vo.getRating());
                enrichWorkerStats(w);
                vo.setServeCount(w.getServeCount());
                vo.setRating(w.getRating());
            }
        }
        return Result.success(list);
    }

    @GetMapping("/workerDetail/{id}")
    @ApiOperation("获取阿姨详情")
    public Result<WorkerRecommendVO> getWorkerDetail(@PathVariable Long id) {
        com.yunlan.entity.WorkerRecommend wr = workerRecommendService.getById(id);
        if (wr == null) {
            return Result.error("阿姨不存在");
        }
        enrichWorkerStats(wr);
        WorkerRecommendVO vo = new WorkerRecommendVO();
        vo.setId(wr.getId());
        vo.setName(wr.getName());
        vo.setAvatar(wr.getAvatar());
        vo.setExperienceYears(wr.getExperienceYears());
        vo.setSkills(wr.getSkills());
        vo.setRating(wr.getRating());
        vo.setPrice(wr.getPrice());
        vo.setServeCount(wr.getServeCount());
        return Result.success(vo);
    }

    @PostMapping("/workerRegister")
    @ApiOperation("服务人员登记注册")
    public Result<Long> workerRegister(@RequestBody WorkerRegisterDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            return Result.error("请填写姓名");
        }
        Long id = workerRecommendService.registerWorker(dto);
        return Result.success(id);
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询服务详情")
    public Result<ServeDetailVO> getServeById(@PathVariable Long id) {
        ServeDetailVO vo = serveItemService.getServeById(id);
        if (vo == null) {
            return Result.error("服务不存在");
        }
        // Populate category name
        ServeItem item = serveItemService.getById(id);
        if (item != null && item.getCategoryId() != null) {
            ServeCategory cat = serveCategoryService.getById(item.getCategoryId());
            if (cat != null) {
                vo.setCategoryName(cat.getName());
            }
        }
        return Result.success(vo);
    }
}
