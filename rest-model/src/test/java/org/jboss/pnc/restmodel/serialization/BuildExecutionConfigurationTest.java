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

package org.jboss.pnc.restmodel.serialization;

import org.jboss.pnc.model.SystemImageType;
import org.jboss.pnc.rest.restmodel.BuildExecutionConfigurationRest;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildExecutionConfigurationTest {
    private final Logger log = LoggerFactory.getLogger(BuildExecutionConfigurationTest.class);

    @Test
    public void serializeAndDeserializeBuildResult() throws IOException, BuildDriverException {

        BuildExecutionConfiguration buildExecutionConfiguration = BuildExecutionConfiguration.build(
                1,
                "condent-id",
                1,
                "mvn clean install",
                "configuration name",
                "https://pathToRepo.git",
                "1111111",
                "https://pathToMirrorRepo.git",
                "2222222",
                "abcd1234",
                "image.repo.url/repo",
                SystemImageType.DOCKER_IMAGE,
                false
        );
        BuildExecutionConfigurationRest buildExecutionConfigurationREST = new BuildExecutionConfigurationRest(buildExecutionConfiguration);

        String buildExecutionConfigurationJson = buildExecutionConfigurationREST.toString();
        log.debug("Json : {}", buildExecutionConfigurationJson);

        BuildExecutionConfigurationRest buildExecutionConfigurationRestFromJson = new BuildExecutionConfigurationRest(buildExecutionConfigurationJson);
        BuildExecutionConfiguration buildExecutionConfigurationFromJson = buildExecutionConfigurationRestFromJson.toBuildExecutionConfiguration();
        String message = "Deserialized object does not match the original.";

        Assert.assertEquals(message, buildExecutionConfiguration.getId(), buildExecutionConfigurationFromJson.getId());
        Assert.assertEquals(message, buildExecutionConfiguration.getBuildScript(), buildExecutionConfigurationFromJson.getBuildScript());
        Assert.assertEquals(message, buildExecutionConfiguration.getName(), buildExecutionConfigurationFromJson.getName());
        Assert.assertEquals(message, buildExecutionConfiguration.getScmRepoURL(), buildExecutionConfigurationFromJson.getScmRepoURL());
        Assert.assertEquals(message, buildExecutionConfiguration.getScmRevision(), buildExecutionConfigurationFromJson.getScmRevision());
        Assert.assertEquals(message, buildExecutionConfiguration.getUserId(), buildExecutionConfigurationFromJson.getUserId());
    }


}
