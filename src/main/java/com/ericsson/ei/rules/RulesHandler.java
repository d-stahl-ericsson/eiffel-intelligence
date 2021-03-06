package com.ericsson.ei.rules;

import java.io.File;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import com.ericsson.ei.jmespath.JmesPathInterface;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.annotation.PostConstruct;

@Component
public class RulesHandler {
    private static Logger log = LoggerFactory.getLogger(RulesHandler.class);

    @Value("${rules.path}") private String jsonFilePath;

    private JmesPathInterface jmesPathInterface = new JmesPathInterface();
    private static String jsonFileContent;
    private static JsonNode parsedJason;

    @PostConstruct public void init() {
        if (parsedJason == null) {
            try {
                jsonFileContent = FileUtils.readFileToString(new File(jsonFilePath));
                ObjectMapper objectmapper = new ObjectMapper();
                parsedJason = objectmapper.readTree(jsonFileContent);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public void setRulePath(String path) {
        this.jsonFilePath = path;
    }

    public RulesObject getRulesForEvent(String event) {
        String typeRule;
        JsonNode type;
        JsonNode result;
        Iterator<JsonNode> iter = parsedJason.iterator();

        while(iter.hasNext()) {
            JsonNode rule = iter.next();
            typeRule = rule.get("TypeRule").toString();

            // Remove the surrounding double quote signs
            typeRule = typeRule.replaceAll("^\"|\"$", "");

            result = jmesPathInterface.runRuleOnEvent(typeRule, event);
            type = rule.get("Type");

            if (result.equals(type)) {
                return new RulesObject(rule);
            }
        }
        return null;
    }

}
