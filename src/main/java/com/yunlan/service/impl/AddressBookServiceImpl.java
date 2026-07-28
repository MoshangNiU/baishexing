package com.yunlan.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunlan.dto.AddressBookDTO;
import com.yunlan.entity.AddressBook;
import com.yunlan.mapper.AddressBookMapper;
import com.yunlan.service.AddressBookService;
import com.yunlan.service.AmapService;
import com.yunlan.utils.UserHolder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {

    @Override
    public List<AddressBook> getAddressBookPage(int page, int pageSize) {
        Long userId = UserHolder.get();
        LambdaQueryWrapper<AddressBook> wrapper = new LambdaQueryWrapper<AddressBook>()
                .eq(AddressBook::getUserId, userId)
                .orderByDesc(AddressBook::getIsDefault)
                .orderByDesc(AddressBook::getCreateTime)
                .last("LIMIT " + (page - 1) * pageSize + "," + pageSize);
        return this.list(wrapper);
    }

    @Override
    public AddressBook addAddress(AddressBookDTO dto) {
        Long userId = UserHolder.get();
        AddressBook address = BeanUtil.copyProperties(dto, AddressBook.class);
        address.setUserId(userId);

        if (address.getIsDefault() == null) {
            address.setIsDefault(0);
        }
        if (address.getIsDefault() == 1) {
            this.update(new LambdaUpdateWrapper<AddressBook>()
                    .eq(AddressBook::getUserId, userId)
                    .eq(AddressBook::getIsDefault, 1)
                    .set(AddressBook::getIsDefault, 0));
        }

        this.save(address);
        return address;
    }

    @Override
    public AddressBook getAddressDetail(Long id) {
        return this.getById(id);
    }

    @Override
    public AddressBook getDefaultAddress() {
        Long userId = UserHolder.get();
        return this.getOne(new LambdaQueryWrapper<AddressBook>()
                .eq(AddressBook::getUserId, userId)
                .eq(AddressBook::getIsDefault, 1));
    }

    @Override
    public void setDefaultAddress(Long id, int flag) {
        Long userId = UserHolder.get();
        if (flag == 1) {
            this.update(new LambdaUpdateWrapper<AddressBook>()
                    .eq(AddressBook::getUserId, userId)
                    .eq(AddressBook::getIsDefault, 1)
                    .set(AddressBook::getIsDefault, 0));
        }
        this.update(new LambdaUpdateWrapper<AddressBook>()
                .eq(AddressBook::getId, id)
                .eq(AddressBook::getUserId, userId)
                .set(AddressBook::getIsDefault, flag));
    }

    @Override
    public void batchDelete(List<Long> ids) {
        Long userId = UserHolder.get();
        this.remove(new LambdaQueryWrapper<AddressBook>()
                .eq(AddressBook::getUserId, userId)
                .in(AddressBook::getId, ids));
    }

    @Override
    public void updateAddress(Long id, AddressBookDTO dto) {
        Long userId = UserHolder.get();
        AddressBook address = this.getById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new IllegalArgumentException("地址不存在");
        }
        BeanUtil.copyProperties(dto, address);
        if (dto.getIsDefault() != null && dto.getIsDefault() == 1) {
            this.update(new LambdaUpdateWrapper<AddressBook>()
                    .eq(AddressBook::getUserId, userId)
                    .eq(AddressBook::getIsDefault, 1)
                    .set(AddressBook::getIsDefault, 0));
        }
        this.updateById(address);
    }

    @Resource
    private AmapService amapService;

    @Override
    public Map<String, Object> findDetailByLocation(String lat, String lng) {
        // Try AMap reverse geocode first
        Map<String, Object> result = amapService.reverseGeocode(lat, lng);
        if (result != null) {
            return result;
        }
        // AMap unavailable — return null; frontend shows coordinates as fallback
        return null;
    }
}
