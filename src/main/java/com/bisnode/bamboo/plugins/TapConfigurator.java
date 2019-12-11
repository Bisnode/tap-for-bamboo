package com.bisnode.bamboo.plugins;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskConfigConstants;
import com.atlassian.bamboo.task.TaskConfiguratorHelper;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.Maps;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@Scanned
public class TapConfigurator extends AbstractTaskConfigurator {

    private static final Logger _log = LoggerFactory.getLogger(TapConfigurator.class);
    public static final String PATTERN = "testPattern";

    private static final List<String> FIELDS_TO_COPY = TaskConfigConstants.DEFAULT_BUILDER_CONFIGURATION_KEYS;

    private final TaskConfiguratorHelper taskConfiguratorHelper;

    public TapConfigurator(@ComponentImport TaskConfiguratorHelper taskConfiguratorHelper) {
        this.taskConfiguratorHelper = taskConfiguratorHelper;
    }

    @Override
    public void populateContextForEdit(Map<String, Object> context, TaskDefinition taskDefinition) {
        taskConfiguratorHelper.populateContextWithConfiguration(context, taskDefinition, FIELDS_TO_COPY);
    }

    @Override
    public void populateContextForCreate(Map<String, Object> context) {
        _log.debug("populateContextForCreate called with arguments: {}", context);
    }

    @Override
    public void validate(ActionParametersMap params, ErrorCollection errorCollection) {
        _log.debug("Validating actionParametersMap {}", params);
    }

    @Override
    public Map<String, String> generateTaskConfigMap(
            ActionParametersMap params,
            @Nullable TaskDefinition previousTaskDefinition
    ) {
        _log.debug("generateTaskConfigMap called with arguments: {}, {}", params, previousTaskDefinition);
        Map<String,String> config = Maps.newHashMap();
        taskConfiguratorHelper.populateTaskConfigMapWithActionParameters(config, params, FIELDS_TO_COPY);
        return config;
    }
}
