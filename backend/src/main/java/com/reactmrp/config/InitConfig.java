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
package com.reactmrp.config;

import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.sql.DataSource;

import org.junit.Test;

import com.github.drinkjava2.jbeanbox.ClassScanner;
import com.github.drinkjava2.jbeanbox.JBEANBOX;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jsqlbox.DB;
import com.github.drinkjava2.jsqlbox.DbContext;
import com.github.drinkjava2.jtransactions.tinytx.TinyTxConnectionManager;
import com.github.drinkjava2.myserverless.MyServerlessEnv;
import com.reactmrp.config.DataSourceConfig.DataSourceBox;
import com.reactmrp.entity.Power;
import com.reactmrp.entity.Role;
import com.reactmrp.entity.RolePower;
import com.reactmrp.entity.User;
import com.reactmrp.entity.UserRole;

import template.ExecuteSqlTemplate;
import template.JavaTemplate;
import template.JavaTxTemplate;
import template.QryArrayListTemplate;
import template.QryArrayTemplate;
import template.QryEntityListTemplate;
import template.QryEntityTemplate;
import template.QryListTemplate;
import template.QryMapListTemplate;
import template.QryMapTemplate;
import template.QryObjectTemplate;
import template.QryTitleArrayListTemplate;

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
        initMyServerlessTemplates(); //登记自定义的MyServerless模板
        initDataBase(); //删除并重建数据库
        insertUserAndPowers(); //插入初始用户、角色、权限
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
        MyServerlessEnv.registerMethodTemplate("executeSql", ExecuteSqlTemplate.class);
    }

    @SuppressWarnings("all")
    public static void initDataBase() {
        //初始化数据库， 本示例使用H2或MySql数据库，如使用其它数据库只要更改下面的DataSourceBox设置即可
        DataSource ds = JBEANBOX.getBean(DataSourceBox.class);

        //本项目使用jSqlBox作为DAO工具，以下是jSqlBox的配置
        DbContext.setGlobalNextAllowShowSql(true); //允许输出SQL日志到控制台
        Dialect.setGlobalNamingConversion(new ProjectNamingRule()); //全局表和字段名映射，表名列名为一对一关系，不作变换
        DbContext ctx = new DbContext(ds); //ctx是全局单例
        ctx.setConnectionManager(TinyTxConnectionManager.instance());// 事务配置
        DbContext.setGlobalDbContext(ctx);// 设定全局缺省上下文

        //创建数据库表
        List<Class> classes = ClassScanner.scanPackages("com.reactmrp.entity"); //扫描所有实体以创建数据库表

        for (int i = 0; i < 10; i++) //我够狠，先静默删库10遍，保证所有表格包括有关联约束关系的表格都全部删除
            classes.stream().distinct().forEach(e -> {
                for (String ddl : ctx.toDropDDL(e))
                    DB.gctx().quiteExecute(ddl); //静默执行
            });
        classes.stream().distinct().forEach(e -> { //然后新建所有表格
            for (String ddl : ctx.toCreateDDL(e))
                DB.gctx().quiteExecute(ddl);
        });

    }

    public static void insertUserAndPowers() {//插入种子用户、角色、权限
        //新建用户 
        User u = new User();
        u.setUserId("developer").setName("张三").setAvatar("https://s1.ax1x.com/2020/04/28/J5hUaT.jpg").setPassword(ProjectSecurity.encodePassword("123")).insert();
        u.setUserId("admin").setName("李四").setAvatar("https://s1.ax1x.com/2020/04/28/J5hUaT.jpg").setPassword(ProjectSecurity.encodePassword("123")).insert();
        u.setUserId("editor").setName("王二麻").setAvatar("https://zos.alipayobjects.com/rmsportal/ODTLcjxAfvqbxHnVXCYX.png").setPassword(ProjectSecurity.encodePassword("123")).insert();
        u.setUserId("guest").setName("孙小狗").setAvatar("https://zos.alipayobjects.com/rmsportal/ODTLcjxAfvqbxHnVXCYX.png").setPassword(ProjectSecurity.encodePassword("123")).insert();

        //新建角色Role
        new Role().setRoleName("developer").setRoleLevel(1).setRoleDescription("开发者，拥有系统内所有业务权限, 并允许动态执行前端发来的SQL和Java").insert();
        new Role().setRoleName("admin").setRoleLevel(2).setRoleDescription("管理员，拥有系统内除开发者权限外的所有业务权限").insert();
        new Role().setRoleName("editor").setRoleLevel(3).setRoleDescription("编辑者，可以看到除用户管理页面之外的所有页面").insert();
        new Role().setRoleName("guest").setRoleLevel(4).setRoleDescription("普通用户，仅能看到主版、作者博客、权限测试和关于作者四个页面").insert();

        //给用户添加角色
        UserRole ur = new UserRole();
        ur.setUserId("developer").setRoleName("developer").insert();
        ur.setUserId("developer").setRoleName("admin").insert(); //developer用户通常同时具有developer和admin两个角色
        ur.setUserId("admin").setRoleName("admin").insert();
        ur.setUserId("editor").setRoleName("editor").insert();
        ur.setUserId("guest").setRoleName("guest").insert();

        //新建权限名Power
        new Power().setPowerName("developer").insert();
        new Power().setPowerName("admin").insert();
        new Power().setPowerName("editor").insert();
        new Power().setPowerName("guest").insert();

        //给角色添加权限
        RolePower ra = new RolePower();
        ra.setRoleName("developer").setPowerName("developer").insert();//developer权限允许动态编译前端代码
        ra.setRoleName("admin").setPowerName("admin").insert();
        ra.setRoleName("editor").setPowerName("editor").insert();
        ra.setRoleName("guest").setPowerName("guest").insert();
    }

    @Test
    public void testInitDataBase() { //临时测试，也可用来在不重启后端的情况下重置数据库以供前端调试
        Dialect.setGlobalAllowReservedWords(false);
        initDataBase();
        //DbContext.gctx().setAllowShowSQL(true);
        insertUserAndPowers();
        List<String> powers = DB.qryList("select p.* from users u ", //
                " left join userrole ur on u.userId=ur.userId ", //
                " left join roles r on ur.roleName=r.roleName ", //
                " left join rolepower rp on rp.roleName=r.roleName ", //
                " left join powers p on p.powerName=rp.powerName ", //
                " where u.userId=", DB.que("developer"));
        for (String p : powers) {
            System.out.println(p);
        }

        List<Object> roles = DB.qryList("select r.* from users u ", //
                " left join userrole ur on u.userId=ur.userId ", //
                " left join roles r on ur.roleName=r.roleName ", //
                " where u.userId=", DB.que("developer"), " and r.roleName<>'developer' order by roleLevel");
        System.out.println(roles);
    }

}
