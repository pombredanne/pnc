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
package org.jboss.pnc.rest.validation.exceptions.model;

import org.jboss.pnc.rest.restmodel.response.error.ErrorResponseDetails;
import org.jboss.pnc.rest.validation.exceptions.InvalidEntityException;

import javax.xml.bind.annotation.XmlType;

@XmlType
public class InvalidEntityDetailsRest implements ErrorResponseDetails {

    private InvalidEntityException invalidEntityException;
    private String field;

    public InvalidEntityDetailsRest() {
    }

    public InvalidEntityDetailsRest(InvalidEntityException invalidEntityException) {
        this.invalidEntityException = invalidEntityException;
        this.field = invalidEntityException.getField();
    }

    @Override
    public String getErrorType() {
        return invalidEntityException.getClass().getSimpleName();
    }

    @Override
    public String getMessage() {
        return invalidEntityException.getMessage();
    }

    public String getField() {
        return field;
    }
}