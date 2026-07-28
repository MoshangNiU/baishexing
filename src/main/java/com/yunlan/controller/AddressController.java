package com.yunlan.controller;

import com.yunlan.common.Result;
import com.yunlan.dto.AddressBookDTO;
import com.yunlan.entity.AddressBook;
import com.yunlan.service.AddressBookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/customer/consumer/address-book")
@Api(tags = "地址簿模块")
public class AddressController {

    @Resource
    private AddressBookService addressBookService;

    @GetMapping("/page")
    @ApiOperation("地址簿分页查询")
    public Result<List<AddressBook>> page(@RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(addressBookService.getAddressBookPage(page, pageSize));
    }

    @PostMapping
    @ApiOperation("新增地址")
    public Result<AddressBook> add(@RequestBody AddressBookDTO dto) {
        return Result.success(addressBookService.addAddress(dto));
    }

    @GetMapping("/{id}")
    @ApiOperation("地址簿详情")
    public Result<AddressBook> detail(@PathVariable Long id) {
        return Result.success(addressBookService.getAddressDetail(id));
    }

    @PutMapping("/{id}")
    @ApiOperation("修改地址")
    public Result<Void> update(@PathVariable Long id, @RequestBody AddressBookDTO dto) {
        addressBookService.updateAddress(id, dto);
        return Result.success();
    }

    @GetMapping("/defaultAddress")
    @ApiOperation("获取默认地址")
    public Result<AddressBook> getDefault() {
        return Result.success(addressBookService.getDefaultAddress());
    }

    @PutMapping("/default")
    @ApiOperation("设置默认地址")
    public Result<Void> setDefault(@RequestParam Long id, @RequestParam int flag) {
        addressBookService.setDefaultAddress(id, flag);
        return Result.success();
    }

    @DeleteMapping("/batch")
    @ApiOperation("批量删除地址")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        addressBookService.batchDelete(ids);
        return Result.success();
    }
}
