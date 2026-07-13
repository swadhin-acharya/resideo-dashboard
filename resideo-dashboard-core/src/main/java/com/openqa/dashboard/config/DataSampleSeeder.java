package com.openqa.dashboard.config;

import com.openqa.dashboard.model.entity.Execution;
import com.openqa.dashboard.model.entity.ExecutionFeature;
import com.openqa.dashboard.model.entity.ExecutionScenario;
import com.openqa.dashboard.model.entity.ExecutionStep;
import com.openqa.dashboard.model.entity.Project;
import com.openqa.dashboard.model.entity.UserEntity;
import com.openqa.dashboard.model.enums.ExecutionStatus;
import com.openqa.dashboard.model.enums.Platform;
import com.openqa.dashboard.repository.ExecutionFeatureRepository;
import com.openqa.dashboard.repository.ExecutionRepository;
import com.openqa.dashboard.repository.ExecutionScenarioRepository;
import com.openqa.dashboard.repository.ExecutionStepRepository;
import com.openqa.dashboard.repository.ProjectRepository;
import com.openqa.dashboard.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Component
@Order(1)
@ConditionalOnProperty(name = "openqa.seed-sample-data", havingValue = "true")
public class DataSampleSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSampleSeeder.class);

    private final ExecutionRepository executionRepository;
    private final ExecutionFeatureRepository featureRepository;
    private final ExecutionScenarioRepository scenarioRepository;
    private final ExecutionStepRepository stepRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    private final Random random = new Random(42);

    private static final String[] FEATURES = {"Login", "Dashboard", "Thermostat", "Settings", "Notifications", "Security", "Onboarding", "Schedule"};
    private static final String[] ENVIRONMENTS = {"QA", "STAGING", "PRODUCTION"};
    private static final Platform[] PLATFORMS = {Platform.ANDROID, Platform.IOS};
    private static final String[] SUITE_TYPES = {"REGRESSION", "SMOKE", "SANITY"};
    private static final String[] TRIGGERED_BY = {"Auto", "Manual"};
    private static final String[] SCENARIO_NAMES = {
        "User can log in with valid credentials",
        "User sees error on invalid password",
        "Dashboard loads within 2 seconds",
        "Temperature display shows correct value",
        "Schedule creation saves successfully",
        "Notification appears on threshold breach",
        "User can update profile settings",
        "Logout clears session data",
        "Device pairing completes successfully",
        "User can reset password via email",
        "Multi-user login works correctly",
        "System handles offline mode gracefully"
    };

    public DataSampleSeeder(ExecutionRepository executionRepository,
                            ExecutionFeatureRepository featureRepository,
                            ExecutionScenarioRepository scenarioRepository,
                            ExecutionStepRepository stepRepository,
                            ProjectRepository projectRepository,
                            UserRepository userRepository) {
        this.executionRepository = executionRepository;
        this.featureRepository = featureRepository;
        this.scenarioRepository = scenarioRepository;
        this.stepRepository = stepRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (executionRepository.count() > 0) {
            log.info("Sample data already exists, skipping.");
            return;
        }

        List<Project> projects = projectRepository.findAll();
        if (projects.isEmpty()) {
            log.warn("No projects found, skipping sample data seeding.");
            return;
        }
        Project project = projects.get(0);

        List<UserEntity> users = userRepository.findAll();
        if (users.isEmpty()) {
            log.warn("No users found, skipping sample data seeding.");
            return;
        }
        UserEntity user = users.get(0);

        log.info("Seeding sample execution data with backdated timestamps...");

        Instant baseDate = Instant.now().minus(60, ChronoUnit.DAYS);

        for (int day = 0; day < 60; day++) {
            int executionsToday = 1 + random.nextInt(3);
            Instant dayStart = baseDate.plus(day, ChronoUnit.DAYS);

            for (int e = 0; e < executionsToday; e++) {
                Execution exec = createExecution(project.getId(), user.getId(), dayStart, e);
                exec = executionRepository.save(exec);

                int featureCount = 1 + random.nextInt(3);
                for (int f = 0; f < featureCount && f < FEATURES.length; f++) {
                    ExecutionFeature feature = createFeature(exec.getId(), FEATURES[(day + f) % FEATURES.length]);
                    feature = featureRepository.save(feature);

                    int scenarioCount = 2 + random.nextInt(4);
                    for (int s = 0; s < scenarioCount; s++) {
                        ExecutionScenario scenario = createScenario(exec.getId(), feature.getId(), s);
                        scenario = scenarioRepository.save(scenario);

                        int stepCount = 1 + random.nextInt(5);
                        for (int st = 0; st < stepCount; st++) {
                            ExecutionStep step = createStep(scenario.getId(), st);
                            stepRepository.save(step);
                        }

                        updateFeatureCounts(feature, scenario.getStatus());
                        featureRepository.save(feature);
                    }
                }

                updateExecutionCounts(exec);
                executionRepository.save(exec);
            }
        }

        log.info("Sample data seeded successfully.");
    }

    private Execution createExecution(UUID projectId, UUID userId, Instant dayStart, int offset) {
        Execution exec = new Execution();
        exec.setProjectId(projectId);
        exec.setUserId(userId);
        exec.setEnvironment(ENVIRONMENTS[random.nextInt(ENVIRONMENTS.length)]);
        exec.setPlatform(PLATFORMS[random.nextInt(PLATFORMS.length)]);
        exec.setExecutionType(SUITE_TYPES[random.nextInt(SUITE_TYPES.length)]);
        exec.setTriggeredBy(TRIGGERED_BY[random.nextInt(TRIGGERED_BY.length)]);
        exec.setName("Execution " + dayStart.truncatedTo(ChronoUnit.DAYS) + " #" + (offset + 1));

        int statusRoll = random.nextInt(100);
        ExecutionStatus status;
        if (statusRoll < 70) status = ExecutionStatus.PASSED;
        else if (statusRoll < 90) status = ExecutionStatus.FAILED;
        else status = ExecutionStatus.ABORTED;
        exec.setStatus(status);

        long durationSecs = 30 + random.nextInt(600);
        exec.setDurationMs(durationSecs * 1000);

        Instant startTime = dayStart.plus(offset * 2, ChronoUnit.HOURS)
                .plus(random.nextInt(60), ChronoUnit.MINUTES);
        exec.setStartTime(startTime);
        exec.setEndTime(startTime.plus(durationSecs, ChronoUnit.SECONDS));

        exec.setTotalCount(0);
        exec.setPassCount(0);
        exec.setFailCount(0);
        exec.setSkipCount(0);

        exec.setCreatedAt(startTime);
        exec.setUpdatedAt(startTime);

        return exec;
    }

    private ExecutionFeature createFeature(UUID executionId, String featureName) {
        ExecutionFeature feature = new ExecutionFeature();
        feature.setExecutionId(executionId);
        feature.setFeatureName(featureName);
        feature.setUri("features/" + featureName.toLowerCase().replace(" ", "_") + ".feature");
        feature.setStatus("RUNNING");
        feature.setDurationMs(0L);
        feature.setPassCount(0);
        feature.setFailCount(0);
        feature.setSkipCount(0);
        return feature;
    }

    private ExecutionScenario createScenario(UUID executionId, UUID featureId, int index) {
        ExecutionScenario scenario = new ExecutionScenario();
        scenario.setExecutionId(executionId);
        scenario.setFeatureId(featureId);
        scenario.setScenarioName(SCENARIO_NAMES[(random.nextInt(SCENARIO_NAMES.length))]);

        int statusRoll = random.nextInt(100);
        String status;
        if (statusRoll < 75) status = "PASSED";
        else if (statusRoll < 92) status = "FAILED";
        else status = "SKIPPED";
        scenario.setStatus(status);

        scenario.setDurationMs(1000L + random.nextInt(30000));
        scenario.setTags("@smoke @regression");
        scenario.setDeviceName("Pixel 7");

        if ("FAILED".equals(status)) {
            scenario.setFailureReason("AssertionError: Expected element to be visible but was not");
        }

        return scenario;
    }

    private ExecutionStep createStep(UUID scenarioId, int index) {
        String[] stepKeywords = {"Given", "When", "Then", "And"};
        String[] stepTexts = {
            "user is logged in",
            "user navigates to dashboard",
            "system displays temperature",
            "user taps on settings",
            "system saves preference",
            "system shows confirmation",
            "user enters credentials",
            "system validates input",
            "page loads successfully",
            "data is refreshed"
        };

        ExecutionStep step = new ExecutionStep();
        step.setScenarioId(scenarioId);
        step.setStepName(stepKeywords[random.nextInt(stepKeywords.length)] + " " + stepTexts[random.nextInt(stepTexts.length)]);
        step.setStatus("PASSED");
        step.setDurationMs(100L + random.nextLong(2000));
        step.setLogText("Step executed successfully");

        return step;
    }

    private void updateFeatureCounts(ExecutionFeature feature, String scenarioStatus) {
        if ("PASSED".equals(scenarioStatus)) {
            feature.setPassCount(feature.getPassCount() + 1);
            feature.setDurationMs(feature.getDurationMs() + 1000L + random.nextInt(30000));
        } else if ("FAILED".equals(scenarioStatus)) {
            feature.setFailCount(feature.getFailCount() + 1);
        } else if ("SKIPPED".equals(scenarioStatus)) {
            feature.setSkipCount(feature.getSkipCount() + 1);
        }
        feature.setStatus(feature.getFailCount() > 0 ? "FAILED" : feature.getPassCount() > 0 ? "PASSED" : "RUNNING");
    }

    private void updateExecutionCounts(Execution exec) {
        List<ExecutionScenario> scenarios = scenarioRepository.findByExecutionId(exec.getId());
        int pass = 0, fail = 0, skip = 0;
        for (ExecutionScenario s : scenarios) {
            if ("PASSED".equals(s.getStatus())) pass++;
            else if ("FAILED".equals(s.getStatus())) fail++;
            else if ("SKIPPED".equals(s.getStatus())) skip++;
        }
        exec.setPassCount(pass);
        exec.setFailCount(fail);
        exec.setSkipCount(skip);
        exec.setTotalCount(pass + fail + skip);

        if (fail > 0) exec.setStatus(ExecutionStatus.FAILED);
        else if (pass > 0) exec.setStatus(ExecutionStatus.PASSED);
    }
}
