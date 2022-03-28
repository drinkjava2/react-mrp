/* Copyright 2018-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.demo;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.sql.DataSource;

import org.h2.jdbcx.JdbcConnectionPool;

import com.demo.entity.Account;
import com.demo.entity.DemoUser;
import com.github.drinkjava2.myserverless.MyServerlessEnv;
import com.github.drinkjava2.myserverless.util.MD5Util;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jsqlbox.DB;
import com.github.drinkjava2.jsqlbox.DbContext;
import com.github.drinkjava2.jtransactions.tinytx.TinyTxConnectionManager;

/**
 * InitConfig is a servlet, but the static method can also be called directly
 * InitConfig可以放在web.xml中启动，也可以直接调用它的静态方法来初始化数据库和MyServerless模板
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class InitConfig extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    public void init() throws ServletException {
        initMyServerlessTemplates();
        initDataBase();
    }

    public static void initMyServerlessTemplates() { //登记自定义的MyServerless模板
        MyServerlessEnv.registerMethodTemplate("java", JavaTemplate.class);
        MyServerlessEnv.registerMethodTemplate("javaTx", JavaTxTemplate.class);
        MyServerlessEnv.registerMethodTemplate("qryObject", QryObjectTemplate.class);
        MyServerlessEnv.registerMethodTemplate("qryArray", QryArrayTemplate.class);
        MyServerlessEnv.registerMethodTemplate("qryArrayList", QryArrayListTemplate.class);
        MyServerlessEnv.registerMethodTemplate("qryTitleArrayList", QryTitleArrayListTemplate.class);
        MyServerlessEnv.registerMethodTemplate("qryMap", QryMapTemplate.class);
        MyServerlessEnv.registerMethodTemplate("qryMapList", QryMapListTemplate.class);
        MyServerlessEnv.registerMethodTemplate("qryList", QryListTemplate.class);
        MyServerlessEnv.registerMethodTemplate("qryEntity", QryEntityTemplate.class);
        MyServerlessEnv.registerMethodTemplate("qryEntityList", QryEntityListTemplate.class);
    }

    public static void initDataBase() { //初始化数据库，为演示作准备
        //本示例使用H2内存数据库，如使用其它数据库只要更改下面的DataSource创建方法即可
        DataSource ds = JdbcConnectionPool.create("jdbc:h2:mem:DBName" + StrUtils.getRandomString(30) + ";MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0", "sa", "");

        //演示项目使用jSqlBox作为DAO工具，以下是jSqlBox的配置
        DbContext.resetGlobalVariants();
        DbContext.setGlobalNextDialect(Dialect.H2Dialect);//手工指定数据库方言
        DbContext.setGlobalNextAllowShowSql(true); //允许输出SQL日志到控制台
        Dialect.setGlobalAllowReservedWords(true); //允许entity字段里用SQL保留字来命名
        DbContext ctx = new DbContext(ds); 
        ctx.setConnectionManager(TinyTxConnectionManager.instance());// 事务相关
        DbContext.setGlobalDbContext(ctx);// 设定全局缺省上下文
        for (String ddl : ctx.toCreateDDL(Account.class, DemoUser.class))// 第一次要建表
            DB.exe(ddl);
        new Account().setId("A").setAmount(500).insert();// 准备测试数据
        new Account().setId("B").setAmount(500).insert();
        new Account().setId("C").setAmount(500).insert();

        DemoUser u = new DemoUser();
        u.setUsername("demo");
        u.setPassword(MD5Util.encryptMD5("123"));
        u.setPhoneNumber("001-123456789");
        u.setIdentity("312307239834534762345");
        u.insert();
    }

}
