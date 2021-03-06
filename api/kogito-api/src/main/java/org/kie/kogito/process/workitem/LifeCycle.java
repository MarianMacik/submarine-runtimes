/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.process.workitem;

import java.util.Collection;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;

/**
 * Complete life cycle that can be applied to work items. It defines
 * set of phases and allow to get access to each of them by id.
 *
 * @param <T> defines the type of data managed through this life cycle
 */
public interface LifeCycle<T> {

    /**
     * Returns phase by its id if exists.
     * @param phaseId phase id to be used for look up
     * @return life cycle phase if exists otherwise null
     */
    LifeCyclePhase phaseById(String phaseId);
    
    /**
     * Returns all phases associated with this life cycle
     * @return list of phases
     */
    Collection<LifeCyclePhase> phases();
    
    /**
     * Perform actual transition to the target phase defined via given transition
     * @param workItem work item that is being transitioned
     * @param manager work item manager for given work item
     * @param transition actual transition
     * @return returns work item data after the transition
     */
    T transitionTo(WorkItem workItem, WorkItemManager manager, Transition<T> transition);
    
    /**
     * Returns current data set for given work item 
     * @param workItem work item to get the data for
     * @return current data set
     */
    T data(WorkItem workItem);
}
