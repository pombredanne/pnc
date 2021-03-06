/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.demo.data;

import com.google.common.base.Preconditions;
import org.jboss.pnc.datastore.repositories.internal.BuildConfigurationAuditedSpringRepository;
import org.jboss.pnc.datastore.repositories.SequenceHandlerRepository;
import org.jboss.pnc.model.*;
import org.jboss.pnc.model.ProductRelease.SupportLevel;
import org.jboss.pnc.spi.datastore.Datastore;
import org.jboss.pnc.spi.datastore.repositories.*;

import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import static org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates.withIdentifierAndChecksum;

/**
 * Data for the DEMO. Note: The database initialization requires two separate transactions in order for the build configuration
 * audit record to be created and then linked to a build record.
 */
@Singleton
public class DatabaseDataInitializer {

    public static final Logger log = Logger.getLogger(DatabaseDataInitializer.class.getName());

    private static final String PNC_PRODUCT_NAME = "Project Newcastle Demo Product";
    private static final String PNC_PRODUCT_VERSION_1 = "1.0";
    private static final String PNC_PRODUCT_VERSION_2 = "2.0";
    private static final String PNC_PRODUCT_RELEASE = "1.0.0.GA";
    private static final String PNC_PRODUCT_MILESTONE1 = "1.0.0.Build1";
    private static final String PNC_PRODUCT_MILESTONE2 = "1.0.0.Build2";
    private static final String PNC_PROJECT_1_NAME = "Project Newcastle Demo Project 1";
    private static final String PNC_PROJECT_BUILD_CFG_ID = "pnc-1.0.0.DR1";

    @Inject
    ArtifactRepository artifactRepository;

    @Inject
    ProjectRepository projectRepository;

    @Inject
    ProductRepository productRepository;

    @Inject
    BuildConfigurationRepository buildConfigurationRepository;

    @Inject
    BuildConfigurationAuditedSpringRepository buildConfigurationAuditedRepository;

    @Inject
    ProductVersionRepository productVersionRepository;

    @Inject
    ProductMilestoneRepository productMilestoneRepository;

    @Inject
    ProductReleaseRepository productReleaseRepository;

    @Inject
    BuildConfigurationSetRepository buildConfigurationSetRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    BuildRecordRepository buildRecordRepository;

    @Inject
    BuildConfigSetRecordRepository buildConfigSetRecordRepository;

    @Inject
    BuildEnvironmentRepository environmentRepository;

    @Inject
    SequenceHandlerRepository sequenceHandlerRepository;

    @Inject
    Datastore datastore;

    BuildConfiguration buildConfiguration1;

    BuildConfiguration buildConfiguration2;

    BuildConfigurationSet buildConfigurationSet1;

    ProductMilestone demoProductMilestone1;

    User demoUser;
    User pncAdminUser;

    public void verifyData() {
        // Check number of entities in DB
        Preconditions.checkState(projectRepository.count() > 0, "Expecting number of Projects > 0");
        Preconditions.checkState(productRepository.count() > 0, "Expecting number of Products > 0");
        Preconditions.checkState(buildConfigurationRepository.count() > 0, "Expecting number of BuildConfigurations > 0");
        Preconditions.checkState(productVersionRepository.count() > 0, "Expecting number of ProductVersions > 0");
        Preconditions.checkState(buildConfigurationSetRepository.count() > 0, "Expecting number of BuildRepositorySets > 0");
        Preconditions.checkState(artifactRepository.count() > 0, "Expecting number of Artifacts > 0");

        BuildConfiguration buildConfigurationDB = buildConfigurationRepository.queryAll().get(0);

        // Check that BuildConfiguration and BuildConfigurationSet have a ProductVersion associated
        Preconditions.checkState(
                buildConfigurationDB.getBuildConfigurationSets().iterator().next().getProductVersion() != null,
                "Product version of buildConfiguration must be not null");

        BuildConfigurationSet buildConfigurationSetDB = buildConfigurationSetRepository.queryAll().get(0);

        Preconditions.checkState(buildConfigurationSetDB.getProductVersion() != null,
                "Product version of buildConfigurationSet must be not null");

        // Check that mapping between Product and Build Configuration via BuildConfigurationSet is correct
        Preconditions.checkState(buildConfigurationSetDB.getProductVersion().getProduct().getName().equals(PNC_PRODUCT_NAME),
                "Product mapped to Project must be " + PNC_PRODUCT_NAME);
        Preconditions.checkState(buildConfigurationSetDB.getProductVersion().getVersion().equals(PNC_PRODUCT_VERSION_1),
                "Product version mapped to Project must be " + PNC_PRODUCT_VERSION_1);

        // Check that BuildConfiguration and BuildConfigurationSet have a ProductVersion associated
        Preconditions.checkState(buildConfigurationDB.getBuildConfigurationSets().iterator().next().getProductVersion()
                .getVersion().equals(PNC_PRODUCT_VERSION_1), "Product version mapped to BuildConfiguration must be "
                + PNC_PRODUCT_VERSION_1);
        Preconditions.checkState(buildConfigurationDB.getBuildConfigurationSets().iterator().next().getProductVersion()
                .getProduct().getName().equals(PNC_PRODUCT_NAME), "Product mapped to BuildConfiguration must be "
                + PNC_PRODUCT_NAME);

        // Check data of BuildConfiguration
        Preconditions.checkState(buildConfigurationDB.getProject().getName().equals(PNC_PROJECT_1_NAME),
                "Project mapped to BuildConfiguration must be " + PNC_PROJECT_1_NAME);

    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void initiliazeProjectProductData() {

        BuildEnvironment environment1Unsaved = BuildEnvironment.Builder.newBuilder()
                .name("Demo Environment 1")
                .description("Basic Java and Maven Environment")
                .attribute("JDK", "1.7.0")
                .attribute("OS", "Linux")
                .systemImageId("12345678")
                .systemImageRepositoryUrl("my.registry/newcastle")
                .systemImageType(SystemImageType.DOCKER_IMAGE)
                .build();
        BuildEnvironment environment1 = environmentRepository.save(environment1Unsaved);

        /*
         * All the bi-directional mapping settings are managed inside the Builders
         */
        // Example product and product version
        Product product = Product.Builder.newBuilder().name(PNC_PRODUCT_NAME).abbreviation("PNC")
                .description("Example Product for Project Newcastle Demo").productCode("PNC").pgmSystemName("newcastle")
                .build();
        product = productRepository.save(product);

        // Example product version, release, and milestone of the product
        ProductVersion productVersion1 = ProductVersion.Builder.newBuilder().version(PNC_PRODUCT_VERSION_1).product(product)
                .build();
        productVersion1 = productVersionRepository.save(productVersion1);

        ProductVersion productVersion2 = ProductVersion.Builder.newBuilder().version(PNC_PRODUCT_VERSION_2).product(product)
                .build();
        productVersion2 = productVersionRepository.save(productVersion2);

        final int DAYS_IN_A_WEEK = 7;
        final Date TODAY = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
        final Date ONE_WEEK_BEFORE_TODAY = Date.from(LocalDateTime.now().minusDays(DAYS_IN_A_WEEK).atZone(ZoneId.systemDefault()).toInstant());
        final Date ONE_WEEK_AFTER_TODAY = Date.from(LocalDateTime.now().plusDays(DAYS_IN_A_WEEK).atZone(ZoneId.systemDefault()).toInstant());

        demoProductMilestone1 = ProductMilestone.Builder.newBuilder().version(PNC_PRODUCT_MILESTONE1)
                .startingDate(ONE_WEEK_BEFORE_TODAY)
                .plannedEndDate(TODAY)
                .productVersion(productVersion1).build();
        demoProductMilestone1 = productMilestoneRepository.save(demoProductMilestone1);

        ProductMilestone demoProductMilestone2 = ProductMilestone.Builder.newBuilder().version(PNC_PRODUCT_MILESTONE2)
                .startingDate(TODAY)
                .plannedEndDate(ONE_WEEK_AFTER_TODAY)
                .productVersion(productVersion1).build();
        demoProductMilestone2 = productMilestoneRepository.save(demoProductMilestone2);

        ProductRelease productRelease = ProductRelease.Builder.newBuilder().version(PNC_PRODUCT_RELEASE)
                .productMilestone(demoProductMilestone1).supportLevel(SupportLevel.EARLYACCESS)
                .build();
        productRelease = productReleaseRepository.save(productRelease);

        productVersion1.setCurrentProductMilestone(demoProductMilestone1);
        productVersion1 = productVersionRepository.save(productVersion1);

        // Example projects
        Project project1 = Project.Builder.newBuilder().name(PNC_PROJECT_1_NAME)
                .description("Example Project for Newcastle Demo").projectUrl("https://github.com/project-ncl/pnc").build();
        Project project2 = Project.Builder.newBuilder().name("JBoss Modules").description("JBoss Modules Project")
                .projectUrl("https://github.com/jboss-modules/jboss-modules")
                .issueTrackerUrl("https://issues.jboss.org/browse/MODULES").build();
        Project project3 = Project.Builder.newBuilder().name("JBoss JavaEE Servlet Spec API")
                .description("JavaEE Servlet Spec API").projectUrl("https://github.com/jboss/jboss-servlet-api_spec")
                .issueTrackerUrl("https://issues.jboss.org/browse/JBEE").build();
        Project project4 = Project.Builder.newBuilder().name("Fabric8")
                .description("Integration platform for working with Apache ActiveMQ, Camel, CXF and Karaf in the cloud")
                .projectUrl("https://github.com/fabric8io/fabric8")
                .issueTrackerUrl("https://github.com/fabric8io/fabric8/issues").build();
        Project project5 = Project.Builder.newBuilder().name("Maven Plugin Test")
                .description("Sample Maven Project with plugins and external downloads")
                .projectUrl("https://github.com/rnc/mvn-plugin-test").build();

        projectRepository.save(project1);
        projectRepository.save(project2);
        projectRepository.save(project3);
        projectRepository.save(project4);
        projectRepository.save(project5);

        // Example build configurations
        buildConfiguration1 = BuildConfiguration.Builder.newBuilder().name(PNC_PROJECT_BUILD_CFG_ID).project(project1)
                .description("Test build config for project newcastle").buildEnvironment(environment1)
                .buildScript("mvn clean deploy -DskipTests=true").scmRepoURL("https://github.com/project-ncl/pnc.git")
                .productVersion(productVersion1).scmRevision("*/v0.2").build();
        buildConfiguration1 = buildConfigurationRepository.save(buildConfiguration1);

        buildConfiguration2 = BuildConfiguration.Builder.newBuilder().name("jboss-modules-1.5.0").project(project2)
                .description("Test config for JBoss modules build master branch.").buildEnvironment(environment1)
                .buildScript("mvn clean deploy -DskipTests=true").productVersion(productVersion1)
                .scmRepoURL("https://github.com/jboss-modules/jboss-modules.git")
                .scmRevision("9e7115771a791feaa5be23b1255416197f2cda38").build();
        buildConfiguration2 = buildConfigurationRepository.save(buildConfiguration2);

        BuildConfiguration buildConfiguration3 = BuildConfiguration.Builder.newBuilder().name("jboss-servlet-spec-api-1.0.1")
                .project(project3).description("Test build for jboss java servlet api").buildEnvironment(environment1)
                .buildScript("mvn clean deploy -DskipTests=true").productVersion(productVersion2)
                .scmRepoURL("https://github.com/jboss/jboss-servlet-api_spec.git").dependency(buildConfiguration2).build();
        buildConfiguration3 = buildConfigurationRepository.save(buildConfiguration3);

        BuildConfiguration buildConfiguration4 = BuildConfiguration.Builder.newBuilder().name("io-fabric8-2.2-SNAPSHOT")
                .project(project4).description("Test build for Fabric8").buildEnvironment(environment1)
                .buildScript("mvn clean deploy -DskipTests=true").scmRepoURL("https://github.com/fabric8io/fabric8.git")
                .build();
        buildConfiguration4 = buildConfigurationRepository.save(buildConfiguration4);

        BuildConfiguration buildConfiguration5 = BuildConfiguration.Builder.newBuilder().name("maven-plugin-test")
                .project(project5).description("Test build for Plugins with external downloads").buildEnvironment(environment1)
                .buildScript("mvn clean deploy").scmRepoURL("https://github.com/rnc/mvn-plugin-test.git")
                .build();
        buildConfiguration5 = buildConfigurationRepository.save(buildConfiguration5);

        // Build config set containing the three example build configs
        buildConfigurationSet1 = BuildConfigurationSet.Builder.newBuilder().name("Example Build Group 1")
                .buildConfiguration(buildConfiguration1).buildConfiguration(buildConfiguration2)
                .buildConfiguration(buildConfiguration3).productVersion(productVersion1).build();

        BuildConfigurationSet buildConfigurationSet2 = BuildConfigurationSet.Builder.newBuilder()
                .name("Fabric Build Group").buildConfiguration(buildConfiguration4).
                        productVersion(productVersion1).build();

        demoUser = User.Builder.newBuilder().username("demo-user").firstName("Demo First Name").lastName("Demo Last Name")
                .email("demo-user@pnc.com").build();

        pncAdminUser = User.Builder.newBuilder().username("pnc-admin").firstName("pnc-admin").lastName("pnc-admin")
                .email("pnc-admin@pnc.com").build();

        buildConfigurationSetRepository.save(buildConfigurationSet1);
        buildConfigurationSetRepository.save(buildConfigurationSet2);
        demoUser = userRepository.save(demoUser);
        pncAdminUser = userRepository.save(pncAdminUser);

    }

    /**
     * Build record needs to be initialized in a separate transaction so that the audited build configuration can be set.
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void initiliazeBuildRecordDemoData() {

        Artifact builtArtifact1 = Artifact.Builder.newBuilder().identifier("demo:built-artifact1:jar:1.0")
                .repoType(ArtifactRepo.Type.MAVEN).filename("demo built artifact 1")
                .checksum("abcd1234").build();
        Artifact builtArtifact2 = Artifact.Builder.newBuilder().identifier("demo:built-artifact2:jar:1.0")
                .repoType(ArtifactRepo.Type.MAVEN).filename("demo built artifact 2")
                .checksum("abcd2345").build();

        builtArtifact1 = artifactRepository.save(builtArtifact1);
        builtArtifact2 = artifactRepository.save(builtArtifact2);

        Artifact importedArtifact1 = Artifact.Builder.newBuilder().identifier("demo:imported-artifact1:jar:1.0")
                .repoType(ArtifactRepo.Type.MAVEN).filename("demo imported artifact 1").originUrl("http://central/import1.jar")
                .importDate(Date.from(Instant.now())).checksum("abcd1234")
                .deployUrl("http://google.pl/imported1").build();
        Artifact importedArtifact2 = Artifact.Builder.newBuilder().identifier("demo:imported-artifact2:jar:1.0")
                .repoType(ArtifactRepo.Type.MAVEN).filename("demo imported artifact 2").originUrl("http://central/import2.jar")
                .importDate(Date.from(Instant.now())).checksum("abcd1234")
                .deployUrl("http://google.pl/imported2").build();

        importedArtifact1 = artifactRepository.save(importedArtifact1);
        importedArtifact2 = artifactRepository.save(importedArtifact2);

        Set<BuildRecord> buildRecords = new HashSet<BuildRecord>();

        final int INITIAL_REVISION = 1;
        IdRev buildConfig1AuditIdRev = new IdRev(buildConfiguration1.getId(), INITIAL_REVISION);
        BuildConfigurationAudited buildConfigAudited1 = buildConfigurationAuditedRepository.findOne(buildConfig1AuditIdRev);
        if (buildConfigAudited1 != null) {

            int nextId = datastore.getNextBuildRecordId();
            log.info("####nextId: " + nextId);

            BuildRecord buildRecord1 = BuildRecord.Builder.newBuilder().id(nextId)
                    .latestBuildConfiguration(buildConfiguration1)
                    .buildConfigurationAudited(buildConfigAudited1)
                    .submitTime(Timestamp.from(Instant.now().minus(8, ChronoUnit.MINUTES)))
                    .startTime(Timestamp.from(Instant.now().minus(5, ChronoUnit.MINUTES)))
                    .endTime(Timestamp.from(Instant.now()))
                    .builtArtifact(builtArtifact1)
                    .builtArtifact(builtArtifact2)
                    .dependency(importedArtifact1)
                    .dependency(importedArtifact2)
                    .user(demoUser)
                    .buildLog("Very short demo log: The quick brown fox jumps over the lazy dog.")
                    .status(BuildStatus.SUCCESS)
                    .productMilestone(demoProductMilestone1)
                    .build();

            buildRecordRepository.save(buildRecord1);
            buildRecords.add(buildRecord1);

        }

        Artifact builtArtifact3 = Artifact.Builder.newBuilder().identifier("demo:built-artifact3:jar:1.0")
                .repoType(ArtifactRepo.Type.MAVEN).filename("demo built artifact 3")
                .checksum("abcd1234").deployUrl("http://google.pl/built3").build();
        Artifact builtArtifact4 = Artifact.Builder.newBuilder().identifier("demo:built-artifact4:jar:1.0")
                .repoType(ArtifactRepo.Type.MAVEN).filename("demo built artifact 4")
                .checksum("abcd1234").deployUrl("http://google.pl/built4").build();

        builtArtifact3 = artifactRepository.save(builtArtifact3);
        builtArtifact4 = artifactRepository.save(builtArtifact4);

        Artifact dependencyBuiltArtifact1 = artifactRepository
                .queryByPredicates(withIdentifierAndChecksum(builtArtifact1.getIdentifier(), builtArtifact1.getChecksum()));

        IdRev buildConfig2AuditIdRev = new IdRev(buildConfiguration2.getId(), INITIAL_REVISION);
        BuildConfigurationAudited buildConfigAudited2 = buildConfigurationAuditedRepository.findOne(buildConfig2AuditIdRev);
        if (buildConfigAudited2 != null) {

            int nextId = datastore.getNextBuildRecordId();
            log.info("####nextId: " + nextId);

            BuildRecord buildRecord2 = BuildRecord.Builder.newBuilder().id(nextId)
                    .latestBuildConfiguration(buildConfiguration2)
                    .buildConfigurationAudited(buildConfigAudited2)
                    .submitTime(Timestamp.from(Instant.now().minus(8, ChronoUnit.MINUTES)))
                    .startTime(Timestamp.from(Instant.now().minus(5, ChronoUnit.MINUTES)))
                    .endTime(Timestamp.from(Instant.now()))
                    .builtArtifact(builtArtifact3)
                    .builtArtifact(builtArtifact4)
                    .dependency(dependencyBuiltArtifact1)
                    .dependency(importedArtifact1)
                    .user(demoUser)
                    .buildLog("Very short demo log: The quick brown fox jumps over the lazy dog.")
                    .status(BuildStatus.SUCCESS)
                    .build();

            buildRecordRepository.save(buildRecord2);
            buildRecords.add(buildRecord2);
        }

        BuildConfigSetRecord buildConfigSetRecord1 = BuildConfigSetRecord.Builder.newBuilder()
                .buildConfigurationSet(buildConfigurationSet1)
                .startTime(Timestamp.from(Instant.now())).endTime(Timestamp.from(Instant.now())).user(demoUser)
                .status(BuildStatus.FAILED).build();
        buildConfigSetRecordRepository.save(buildConfigSetRecord1);

        BuildConfigSetRecord buildConfigSetRecord2 = BuildConfigSetRecord.Builder.newBuilder()
                .buildConfigurationSet(buildConfigurationSet1)
                .buildRecords(buildRecords).startTime(Timestamp.from(Instant.now())).endTime(Timestamp.from(Instant.now()))
                .user(demoUser).status(BuildStatus.SUCCESS).build();
        buildConfigSetRecordRepository.save(buildConfigSetRecord2);

        demoProductMilestone1 = productMilestoneRepository.queryById(demoProductMilestone1.getId());
        demoProductMilestone1.addDistributedArtifact(builtArtifact1);
        demoProductMilestone1.addDistributedArtifact(builtArtifact3);
        demoProductMilestone1.addDistributedArtifact(importedArtifact2);
        demoProductMilestone1 = productMilestoneRepository.save(demoProductMilestone1);

    }

}
