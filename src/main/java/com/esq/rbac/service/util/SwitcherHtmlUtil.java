package com.esq.rbac.service.util;


import com.esq.rbac.service.application.vo.ApplicationInfoDetails;
import freemarker.cache.FileTemplateLoader;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class SwitcherHtmlUtil {


    private Template template;

    private String templatePath;


    public void setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
    }


    public void init() throws Exception {
        freemarker.template.Configuration ftlConfig = new freemarker.template.Configuration();
        FileTemplateLoader fileTemplateLoader = new FileTemplateLoader(
                new File(templatePath));
        ftlConfig.setTemplateLoader(fileTemplateLoader);
        template = ftlConfig.getTemplate("switcherHtml.ftl");
    }


    public String getSwitcherHtml(ApplicationInfoDetails[] appInfodetail) {

        final String htmlData;
        List<ApplicationInfoDetails> appDataList = new ArrayList<>();
        Map<String, Object> root = new HashMap<>();

        for(ApplicationInfoDetails appInfo: appInfodetail){
            appDataList.add(appInfo);
        }
        try{

            root.put("properties", appDataList);
            htmlData = processTemplate(root);

        } catch (Exception e) {
            log.error("getSwitcherHtml; Exception={}",e);
            return "";
        }

        return htmlData;
    }

    private String processTemplate(Map<String, Object> rootMap) throws Exception {
        StringWriter sw = new StringWriter();
        template.process(rootMap, sw);
        return sw.toString();
    }

}
