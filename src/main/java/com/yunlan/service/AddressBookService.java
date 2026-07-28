package com.yunlan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunlan.dto.AddressBookDTO;
import com.yunlan.entity.AddressBook;

import java.util.List;
import java.util.Map;

public interface AddressBookService extends IService<AddressBook> {
    List<AddressBook> getAddressBookPage(int page, int pageSize);
    AddressBook addAddress(AddressBookDTO dto);
    AddressBook getAddressDetail(Long id);
    AddressBook getDefaultAddress();
    void setDefaultAddress(Long id, int flag);
    void batchDelete(List<Long> ids);
    void updateAddress(Long id, AddressBookDTO dto);
    Map<String, Object> findDetailByLocation(String lat, String lng);
}
