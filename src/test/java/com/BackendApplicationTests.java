package com;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Hệ thống kiểm thử Backend - 4 Chức năng chính")
@SelectPackages("com.service")
public class BackendApplicationTests {
}