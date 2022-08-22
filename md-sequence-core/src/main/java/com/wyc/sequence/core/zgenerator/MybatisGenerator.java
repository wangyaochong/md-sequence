package com.wyc.sequence.core.zgenerator;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

public class MybatisGenerator {
  public static void main(String[] args) throws IOException {
    String canonicalPath = new File("").getCanonicalPath();
    String outputBaseDirDir = canonicalPath + GeneratorConfig.srcMainJava;
    String javaPackageDir = "com.wyc";
    String xmlDir = javaPackageDir.replace(".", "/") + "/generated/mapper/xml/";
    FastAutoGenerator.create(
            "jdbc:mysql://"
                + GeneratorConfig.ip
                + ":3306/"
                + GeneratorConfig.dbName
                + "?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowMultiQueries=true",
            GeneratorConfig.userName,
            GeneratorConfig.password)
        .globalConfig(
            builder -> {
              builder
                  .author("wyc") // 设置作者
                  .disableOpenDir()
                  .enableSwagger() // 开启 swagger 模式
                  .fileOverride()
                  .outputDir(outputBaseDirDir); // 指定输出目录
            })
        .packageConfig(
            builder -> {
              builder
                  .parent(javaPackageDir) // 设置父包名
                  .moduleName("generated") // 设置父包模块名
                  .pathInfo(
                      Collections.singletonMap(
                          OutputFile.xml, outputBaseDirDir + xmlDir)); // 设置mapperXml生成路径
            })
        .strategyConfig(
            builder -> {
//              builder.addInclude("ind_ma");
              //                    builder.addInclude("t_simple") // 设置需要生成的表名
              //                            .addTablePrefix("t_", "c_"); // 设置过滤表前缀
              builder.entityBuilder().idType(IdType.AUTO).enableLombok().entityBuilder();
            })
        .templateEngine(new FreemarkerTemplateEngine()) // 使用Freemarker引擎模板，默认的是Velocity引擎模板
        .execute();
  }
}
