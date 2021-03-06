/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.dao;

import javax.validation.constraints.NotNull;

import java.util.Collection;
import java.util.List;

import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import org.openmrs.Person;
import org.openmrs.PersonAttribute;

public interface FhirPersonDao {
	
	Person getPersonByUuid(@NotNull String uuid);
	
	List<PersonAttribute> getActiveAttributesByPersonAndAttributeTypeUuid(@NotNull Person person,
	        @NotNull String personAttributeTypeUuid);
	
	Collection<Person> searchForPeople(StringOrListParam name, TokenOrListParam gender, DateRangeParam birthDate,
	        StringOrListParam city, StringOrListParam state, StringOrListParam postalCode, StringOrListParam country,
	        SortSpec sort);
	
}
