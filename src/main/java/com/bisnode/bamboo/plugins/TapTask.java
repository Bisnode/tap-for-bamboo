package com.bisnode.bamboo.plugins;

import com.atlassian.bamboo.build.logger.interceptors.ErrorMemorisingInterceptor;
import com.atlassian.bamboo.build.test.TestCollationService;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.task.TaskType;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

import java.net.URI;

@Scanned
public class TapTask implements TaskType {

    private final TestCollationService collationService;

    public TapTask(@ComponentImport TestCollationService collationService) {
        this.collationService = collationService;
    }

    @Override
    public TaskResult execute(TaskContext taskContext) {
        ErrorMemorisingInterceptor errors = new ErrorMemorisingInterceptor();
        taskContext.getBuildLogger().getInterceptorStack().add(errors);

        URI projectRoot = taskContext.getRootDirectory().toURI();
        String testsPattern = taskContext.getConfigurationMap().getOrDefault(TapConfigurator.PATTERN, "**/*.tap");
        String testsPath = projectRoot.relativize(projectRoot).getPath() + testsPattern;
        collationService.collateTestResults(taskContext, testsPath, new TapReports.Collector());

        TaskResult build = TaskResultBuilder.newBuilder(taskContext).checkTestFailures().build();
        taskContext.getBuildContext().getBuildResult().addBuildErrors(errors.getErrorStringList());

        return build;
    }

}
