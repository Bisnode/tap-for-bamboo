package com.bisnode.bamboo.plugins;

import com.atlassian.bamboo.build.test.TestCollectionResult;
import com.atlassian.bamboo.build.test.TestCollectionResultBuilder;
import com.atlassian.bamboo.build.test.TestReportCollector;
import com.atlassian.bamboo.build.test.TestReportProvider;
import com.atlassian.bamboo.results.tests.TestResults;
import com.atlassian.bamboo.resultsummary.tests.TestCaseResultErrorImpl;
import com.atlassian.bamboo.resultsummary.tests.TestState;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tap4j.consumer.TapConsumerFactory;
import org.tap4j.model.Directive;
import org.tap4j.model.TestResult;
import org.tap4j.util.DirectiveValues;
import org.tap4j.util.StatusValues;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.atlassian.bamboo.resultsummary.tests.TestState.FAILED;
import static com.atlassian.bamboo.resultsummary.tests.TestState.SKIPPED;
import static com.atlassian.bamboo.resultsummary.tests.TestState.SUCCESS;

public class TapReports {

    public static class Provider implements TestReportProvider {

        private static final Logger log = LoggerFactory.getLogger(Provider.class);

        private final File logFile;

        public Provider(File logFile) {
            this.logFile = logFile;
        }

        @NotNull
        @Override
        public TestCollectionResult getTestCollectionResult() {
            Map<TestState, List<TestResults>> endResults = Stream.of(SKIPPED, FAILED, SUCCESS)
                    .collect(Collectors.toMap(Function.identity(), v -> new ArrayList<>()));

            List<TestResult> testResults = TapConsumerFactory.makeTap13Consumer().load(logFile).getTestResults();
            log.debug("Preparing to process {} test results", testResults.size());

            for (TestResult testResult : testResults) {
                TestResults bambooResult =
                        new TestResults(testResult.getDescription(), testResult.getDescription(), (Long) null);

                boolean skip = Optional.ofNullable(testResult.getDirective())
                        .map(Directive::getDirectiveValue)
                        .filter(val -> DirectiveValues.SKIP == val)
                        .isPresent();

                if (skip) {
                    endResults.computeIfPresent(SKIPPED, (key, val) -> {
                        bambooResult.setState(SKIPPED);
                        val.add(bambooResult);
                        return val;
                    });
                } else if (StatusValues.NOT_OK == testResult.getStatus()) {
                    testResult.getComments()
                            .forEach(comment -> bambooResult.addError(new TestCaseResultErrorImpl(comment.getText())));

                    endResults.computeIfPresent(FAILED, (key, val) -> {
                        bambooResult.setState(FAILED);
                        val.add(bambooResult);
                        return val;
                    });
                } else if (StatusValues.OK == testResult.getStatus()) {
                    endResults.computeIfPresent(SUCCESS, (key, val) -> {
                        bambooResult.setState(SUCCESS);
                        val.add(bambooResult);
                        return val;
                    });
                } else {
                    log.warn("Unknown state, TAP result neither OK, NOT_OK nor SKIPPED");
                }
            }

            return new TestCollectionResultBuilder()
                    .addFailedTestResults(endResults.get(FAILED))
                    .addSkippedTestResults(endResults.get(SKIPPED))
                    .addSuccessfulTestResults(endResults.get(SUCCESS)).build();
        }

    }

    public static class Collector implements TestReportCollector {

        @Override
        public TestCollectionResult collect(File file) {
            return new Provider(file).getTestCollectionResult();
        }

        @Override
        public Set<String> getSupportedFileExtensions() {
            return new HashSet<>(0);
        }

    }

}
