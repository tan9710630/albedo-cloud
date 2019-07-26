/**
 * Copyright &copy; 2018 <a href="https://github.com/somewhereMrli/albedo-boot">albedo-boot</a> All rights reserved.
 */
package com.albedo.java.modules.test.service;


import com.albedo.java.common.persistence.service.TreeVoService;
import com.albedo.java.modules.test.domain.TestTreeBook;
import com.albedo.java.modules.test.domain.vo.TestTreeBookDataVo;
import com.albedo.java.modules.test.repository.TestTreeBookRepository;


/**
 * 测试树书管Service 测试树书
 * @author sys
 * @version 2019-07-25 21:24:22
 */
public interface TestTreeBookService extends TreeVoService<TestTreeBookRepository, TestTreeBook, TestTreeBookDataVo>{


}