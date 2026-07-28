package com.yunlan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunlan.entity.ServeItem;
import com.yunlan.entity.UserFavorite;
import com.yunlan.entity.WorkerRecommend;
import com.yunlan.mapper.UserFavoriteMapper;
import com.yunlan.service.ServeItemService;
import com.yunlan.service.UserFavoriteService;
import com.yunlan.service.WorkerRecommendService;
import com.yunlan.utils.UserHolder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserFavoriteServiceImpl extends ServiceImpl<UserFavoriteMapper, UserFavorite> implements UserFavoriteService {

    @Resource
    private ServeItemService serveItemService;

    @Resource
    private WorkerRecommendService workerRecommendService;

    @Override
    public boolean toggle(Long targetId, String targetType) {
        Long userId = UserHolder.get();
        if (userId == null) return false;

        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getTargetId, targetId)
                .eq(UserFavorite::getTargetType, targetType);

        UserFavorite existing = this.getOne(wrapper);
        if (existing != null) {
            this.removeById(existing.getId());
            return false; // removed
        } else {
            UserFavorite fav = new UserFavorite();
            fav.setUserId(userId);
            fav.setTargetId(targetId);
            fav.setTargetType(targetType);
            this.save(fav);
            return true; // added
        }
    }

    @Override
    public List<Map<String, Object>> getMyFavorites(String targetType) {
        Long userId = UserHolder.get();
        if (userId == null) return new ArrayList<>();

        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId);
        if (targetType != null && !targetType.isEmpty()) {
            wrapper.eq(UserFavorite::getTargetType, targetType);
        }
        List<UserFavorite> favorites = this.list(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (UserFavorite fav : favorites) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", fav.getId());
            item.put("targetId", fav.getTargetId());
            item.put("targetType", fav.getTargetType());

            if ("serve_item".equals(fav.getTargetType()) || "service".equals(fav.getTargetType())) {
                ServeItem si = serveItemService.getById(fav.getTargetId());
                if (si != null) {
                    item.put("name", si.getName());
                    item.put("image", si.getImage());
                    item.put("price", si.getPrice());
                    item.put("rating", 5);
                }
            } else if ("worker_recommend".equals(fav.getTargetType()) || "auntie".equals(fav.getTargetType())) {
                WorkerRecommend wr = workerRecommendService.getById(fav.getTargetId());
                if (wr != null) {
                    item.put("name", wr.getName());
                    item.put("avatar", wr.getAvatar());
                    item.put("rating", wr.getRating());
                    item.put("price", wr.getPrice());
                    item.put("experience", wr.getExperienceYears() + "年");
                    item.put("age", 45);
                }
            }
            result.add(item);
        }
        return result;
    }

    @Override
    public boolean isFavorite(Long targetId, String targetType) {
        Long userId = UserHolder.get();
        if (userId == null) return false;
        return this.count(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getTargetId, targetId)
                .eq(UserFavorite::getTargetType, targetType)) > 0;
    }
}
