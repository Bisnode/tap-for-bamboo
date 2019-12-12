package com.bisnode.bamboo.plugins;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskConfigConstants;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Scanned
public class TapConfigurator extends AbstractTaskConfigurator {

    private static final Logger log = LoggerFactory.getLogger(TapConfigurator.class);
    public static final String PATTERN = "testPattern";

    @Override
    public void populateContextForEdit(Map<String, Object> context, TaskDefinition taskDefinition) {
        taskDefinition.getConfiguration().forEach(context::put);
    }

    @Override
    public void populateContextForCreate(Map<String, Object> context) {
        log.debug("populateContextForCreate called with arguments: {}", context);
    }

    @Override
    public void validate(ActionParametersMap params, ErrorCollection errorCollection) {
        log.debug("Validating actionParametersMap {}", params);
    }

    @Override
    public Map<String, String> generateTaskConfigMap(
            ActionParametersMap params,
            @Nullable TaskDefinition previousTaskDefinition
    ) {
        log.debug("generateTaskConfigMap called with arguments: {}, {}", params, previousTaskDefinition);
        Map<String, String> config = new HashMap<>(1);
        config.put(PATTERN, params.getString(PATTERN));
        return config;
    }
}
