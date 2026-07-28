package com.yunlan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunlan.entity.UserFavorite;
import java.util.List;
import java.util.Map;

public interface UserFavoriteService extends IService<UserFavorite> {
    boolean toggle(Long targetId, String targetType);
    List<Map<String, Object>> getMyFavorites(String targetType);
    boolean isFavorite(Long targetId, String targetType);
}
