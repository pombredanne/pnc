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
package org.jboss.pnc.mock.coordinator;

import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.BuildCoordinationStatus;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.BuildSetTask;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.exception.BuildConflictException;
import org.jboss.pnc.spi.exception.CoreException;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
@Alternative
public class BuildCoordinatorMock implements BuildCoordinator {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private List<BuildTask> activeTasks = new ArrayList<>();

    @Deprecated
    public BuildCoordinatorMock() {
        super();
    }

    @Override
    public BuildTask build(BuildConfiguration buildConfiguration, User user, boolean keepAliveOnFailure, boolean rebuildAll) throws BuildConflictException {
        logger.warn("Invoking unimplemented method build");
        return Mockito.mock(BuildTask.class);
    }

    @Override
    public BuildSetTask build(BuildConfigurationSet buildConfigurationSet, User user, boolean keepAliveOnFailure, boolean rebuildAll) throws CoreException {
        logger.warn("Invoking unimplemented method build");
        return Mockito.mock(BuildSetTask.class);
    }

    public List<BuildTask> getSubmittedBuildTasks() {
        return activeTasks;
    }

    @Override
    public void updateBuildStatus(BuildTask buildTask, BuildResult buildResult) {

    }

    @Override
    public void updateBuildTaskStatus(BuildTask task, BuildCoordinationStatus status) {
    }

    public void addActiveTask(BuildTask task) {
        activeTasks.add(task);
    }

    public void clearActiveTasks() {
        activeTasks.clear();
    }

    @Override
    public void start() {
        logger.info("Called start threads");
    }
}
