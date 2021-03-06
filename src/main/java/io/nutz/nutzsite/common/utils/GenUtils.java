package io.nutz.nutzsite.common.utils;


import io.nutz.nutzsite.common.config.GenConfig;
import io.nutz.nutzsite.common.constant.CommonMap;
import io.nutz.nutzsite.module.tool.gen.models.ColumnInfo;
import io.nutz.nutzsite.module.tool.gen.models.TableInfo;
import org.apache.velocity.VelocityContext;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 代码生成器 工具类
 *
 */
public class GenUtils
{
    /** 项目空间路径 */
    private static final String PROJECT_PATH = getProjectPath();


    /** html空间路径 */
    private static final String TEMPLATES_PATH = "main/resources/template";

    /**
     * 大小写范围
     */
    private static Pattern p = Pattern.compile("[A-Z]");

    /**
     * 设置列信息
     */
    public static List<ColumnInfo> transColums(List<ColumnInfo> columns)
    {
        // 列信息
        List<ColumnInfo> columsList = new ArrayList<>();
        for (ColumnInfo column : columns)
        {
            // 列名转换成Java属性名
            String attrName = StringUtils.convertToCamelCase(column.getColumnName());
            column.setAttrName(attrName);
            column.setAttrname(StringUtils.uncapitalize(attrName));

            // 列的数据类型，转换成Java类型
            String attrType = CommonMap.javaTypeMap.get(column.getDataType());
            column.setAttrType(attrType);

            columsList.add(column);
        }
        return columsList;
    }

    /**
     * 获取模板信息
     * 
     * @return 模板列表
     */
    public static VelocityContext getVelocityContext(TableInfo table)
    {
        // java对象数据传递到模板文件vm
        VelocityContext velocityContext = new VelocityContext();
        String packageName = GenConfig.getPackageName();
        velocityContext.put("tableName", table.getTableName());
        velocityContext.put("tableComment", replaceKeyword(table.getTableComment()));
        velocityContext.put("primaryKey", table.getPrimaryKey());
        velocityContext.put("className", table.getClassName());
        velocityContext.put("classname", table.getClassname());
        velocityContext.put("moduleName", getModuleName(packageName));
        velocityContext.put("columns", table.getColumns());
        velocityContext.put("package", packageName);
        velocityContext.put("author", GenConfig.getAuthor());
        velocityContext.put("datetime", DateUtils.getDate());
        return velocityContext;
    }


    /**
     * 获取 列表模板
     * @return
     */
    public static List<String> getListTemplates()
    {
        List<String> templates = new ArrayList<String>();
        templates.add("template/vm/list/java/Models.java.vm");
        templates.add("template/vm/list/java/Service.java.vm");
        templates.add("template/vm/list/java/Controller.java.vm");
        templates.add("template/vm/list/html/list.html.vm");
        templates.add("template/vm/list/html/add.html.vm");
        templates.add("template/vm/list/html/edit.html.vm");
        templates.add("template/vm/sql/sql.vm");
        return templates;
    }

    /**
     * 获取 树模板
     * @return
     */
    public static List<String> getTreeTemplates()
    {
        List<String> templates = new ArrayList<String>();
        templates.add("template/vm/tree/java/Models.java.vm");
        templates.add("template/vm/tree/java/Service.java.vm");
        templates.add("template/vm/tree/java/Controller.java.vm");
        templates.add("template/vm/tree/html/list.html.vm");
        templates.add("template/vm/tree/html/add.html.vm");
        templates.add("template/vm/tree/html/edit.html.vm");
        templates.add("template/vm/tree/html/tree.html.vm");
        templates.add("template/vm/sql/sql.vm");
        return templates;
    }

    /**
     * 表名转换成Java类名
     */
    public static String tableToJava(String tableName)
    {
        if (Boolean.valueOf(GenConfig.getAutoRemovePre()))
        {
            tableName = tableName.substring(tableName.indexOf("_") + 1);
        }
        if (StringUtils.isNotEmpty(GenConfig.getTablePrefix()))
        {
            tableName = tableName.replace(GenConfig.getTablePrefix(), "");
        }
        return StringUtils.convertToCamelCase(tableName);
    }

    /**
     * Java类名转换成表名
     * @param tableName
     * @return
     */
    public static String javaToTable(String tableName)
    {

        StringBuilder builder = new StringBuilder(tableName);
        Matcher mc = p.matcher(tableName);
        int i = 0;
        while (mc.find()) {
//            System.out.println(builder.toString());
//            System.out.println("mc.start():" + mc.start() + ", i: " + i);
//            System.out.println("mc.end():" + mc.start() + ", i: " + i);
            builder.replace(mc.start() + i, mc.end() + i, "_" + mc.group().toLowerCase());
            i++;
        }
        if ('_' == builder.charAt(0)) {
            builder.deleteCharAt(0);
        }
        return builder.toString();
    }


    /**
     * 获取列表 文件名
     * @param template
     * @param table
     * @param moduleName
     * @return
     */
    public static String getFileName(String template, TableInfo table, String moduleName)
    {
        // 小写类名
        String classname = table.getClassname();
        // 大写类名
        String className = table.getClassName();
        String javaPath = PROJECT_PATH;
        String htmlPath = TEMPLATES_PATH + "/" + moduleName + "/" + classname;

        if (StringUtils.isNotEmpty(classname))
        {
            javaPath =javaPath.replace(".", "/") + "/";
        }

        if (template.contains("Models.java.vm"))
        {
            return javaPath + "models" + "/" + className + ".java";
        }

        if (template.contains("Service.java.vm"))
        {
            return javaPath + "services" + "/" + className + "Service.java";
        }

        if (template.contains("Controller.java.vm"))
        {
            return javaPath + "controller" + "/" + className + "Controller.java";
        }

        if (template.contains("list.html.vm"))
        {
            return htmlPath + "/" + classname + ".html";
        }
        if (template.contains("add.html.vm"))
        {
            return htmlPath + "/" + "add.html";
        }
        if (template.contains("edit.html.vm"))
        {
            return htmlPath + "/" + "edit.html";
        }
        if (template.contains("tree.html.vm"))
        {
            return htmlPath + "/" + "tree.html";
        }
        if (template.contains("sql.vm"))
        {
            return classname + "Menu.sql";
        }
        return null;
    }

    /**
     * 获取模块名
     * 
     * @param packageName 包名
     * @return 模块名
     */
    public static String getModuleName(String packageName)
    {
        int lastIndex = packageName.lastIndexOf(".");
        int nameLength = packageName.length();
        String moduleName = StringUtils.substring(packageName, lastIndex + 1, nameLength);
        return moduleName;
    }

    public static String getProjectPath()
    {
        String packageName = GenConfig.getPackageName();
        StringBuffer projectPath = new StringBuffer();
        projectPath.append("main/java/");
        projectPath.append(packageName.replace(".", "/"));
        projectPath.append("/");
        return projectPath.toString();
    }

    public static String replaceKeyword(String keyword)
    {
        String keyName = keyword.replaceAll("(?:表|信息)", "");
        return keyName;
    }
}
