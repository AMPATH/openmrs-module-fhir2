/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.translators.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Medication;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.DrugIngredient;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirConceptService;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;

@RunWith(MockitoJUnitRunner.class)
public class MedicationTranslatorImplTest {
	
	private static final String MEDICATION_UUID = "c0938432-1691-11df-97a5-7038c432aaba";
	
	private static final String DOSAGE_FORM_CONCEPT_UUID = "162553AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String DRUG_CONCEPT_UUID = "172553AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String INGREDIENT_CONCEPT_UUID = "182553AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	@Mock
	private FhirConceptService conceptService;
	
	@Mock
	private ConceptTranslator conceptTranslator;
	
	private MedicationTranslatorImpl medicationTranslator;
	
	private Drug drug;
	
	@Before
	public void setup() {
		drug = new Drug();
		medicationTranslator = new MedicationTranslatorImpl();
		medicationTranslator.setConceptService(conceptService);
		medicationTranslator.setConceptTranslator(conceptTranslator);
		
		Concept drugConcept = new Concept();
		drugConcept.setUuid(DRUG_CONCEPT_UUID);
		drug.setConcept(drugConcept);
		
		Concept dosageConcept = new Concept();
		dosageConcept.setUuid(DOSAGE_FORM_CONCEPT_UUID);
		drug.setDosageForm(dosageConcept);
	}
	
	@Test
	public void toFhirResource_shouldReturnNullWhenCalledWithNullObject() {
		Medication medication = medicationTranslator.toFhirResource(null);
		assertThat(medication, nullValue());
	}
	
	@Test
	public void toFhirResource_shouldTranslateUuidToId() {
		drug.setUuid(MEDICATION_UUID);
		Medication medication = medicationTranslator.toFhirResource(drug);
		assertThat(medication, notNullValue());
		assertThat(medication.getId(), equalTo(MEDICATION_UUID));
	}
	
	@Test
	public void toFhirResource_shouldTranslateDosageConceptToMedicationForm() {
		Medication medication = medicationTranslator.toFhirResource(drug);
		assertThat(medication, notNullValue());
		assertThat(medication.getForm().getCoding().size(), greaterThanOrEqualTo(1));
		assertThat(medication.getForm().getCoding().get(0).getCode(), equalTo(DOSAGE_FORM_CONCEPT_UUID));
	}
	
	@Test
	public void toFhirResource_shouldTranslateDrugIngredientToMedicationIngredient() {
		DrugIngredient ingredient = new DrugIngredient();
		Concept concept = new Concept();
		concept.setUuid(INGREDIENT_CONCEPT_UUID);
		ingredient.setIngredient(concept);
		drug.setIngredients(Collections.singleton((ingredient)));
		
		CodeableConcept codeableConcept = new CodeableConcept().addCoding(new Coding("", INGREDIENT_CONCEPT_UUID, ""));
		when(conceptTranslator.toFhirResource(concept)).thenReturn(codeableConcept);
		
		Medication medication = medicationTranslator.toFhirResource(drug);
		assertThat(medication, notNullValue());
		assertThat(medication.getIngredient().size(), greaterThanOrEqualTo(1));
		assertThat(medication.getIngredient().get(0).getItemCodeableConcept().getCoding().size(), greaterThanOrEqualTo(1));
		assertThat(medication.getIngredient().get(0).getItemCodeableConcept().getCoding().get(0).getCode(),
		    equalTo(INGREDIENT_CONCEPT_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldReturnNullWhenCalledWithNullObject() {
		Medication medication = medicationTranslator.toFhirResource(null);
		assertThat(medication, nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateIdToUuid() {
		Medication medication = new Medication();
		medication.setId(MEDICATION_UUID);
		medicationTranslator.toOpenmrsType(drug, medication);
		assertThat(drug, notNullValue());
		assertThat(drug.getUuid(), notNullValue());
		assertThat(drug.getUuid(), equalTo(MEDICATION_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldReturnNullWhenCalledWithNullCode() {
		Medication medication = new Medication();
		drug.setConcept(null);
		
		medicationTranslator.toOpenmrsType(drug, medication);
		assertThat(drug.getConcept(), nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateMedicationCodeToConcept() {
		Medication medication = new Medication();
		medication.setCode(new CodeableConcept()
		        .addCoding(new Coding(FhirConstants.MEDICATION_CODES_VALUE_SET_URI, DRUG_CONCEPT_UUID, "")));
		
		Concept drugConcept = new Concept();
		drugConcept.setUuid(DRUG_CONCEPT_UUID);
		
		when(conceptTranslator.toOpenmrsType(medication.getCode())).thenReturn(drugConcept);
		
		medicationTranslator.toOpenmrsType(drug, medication);
		assertThat(drug, notNullValue());
		assertThat(drug.getConcept(), notNullValue());
		assertThat(drug.getConcept().getUuid(), equalTo(DRUG_CONCEPT_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldReturnNullWhenCalledWithNullDose() {
		Medication medication = new Medication();
		drug.setDosageForm(null);
		
		medicationTranslator.toOpenmrsType(drug, medication);
		assertThat(drug.getDosageForm(), nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateFormToDrugDosageForm() {
		Medication medication = new Medication();
		medication.setForm(new CodeableConcept()
		        .addCoding(new Coding(FhirConstants.MEDICATION_FORM_VALUE_SET_URI, DOSAGE_FORM_CONCEPT_UUID, "")));
		
		Concept dosageConcept = new Concept();
		dosageConcept.setUuid(DOSAGE_FORM_CONCEPT_UUID);
		
		when(conceptTranslator.toOpenmrsType(medication.getForm())).thenReturn(dosageConcept);
		
		medicationTranslator.toOpenmrsType(drug, medication);
		assertThat(drug, notNullValue());
		assertThat(drug.getDosageForm(), notNullValue());
		assertThat(drug.getDosageForm().getUuid(), equalTo(DOSAGE_FORM_CONCEPT_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateMedicationIngredientsToDrugIngredients() {
		Medication medication = new Medication();
		Medication.MedicationIngredientComponent ingredient = new Medication.MedicationIngredientComponent();
		medication.addIngredient(
		    ingredient.setItem(new CodeableConcept().addCoding(new Coding("", INGREDIENT_CONCEPT_UUID, ""))));
		
		Concept ingredientConcept = new Concept();
		ingredientConcept.setUuid(INGREDIENT_CONCEPT_UUID);
		
		when(conceptService.getConceptByUuid(INGREDIENT_CONCEPT_UUID)).thenReturn(Optional.of(ingredientConcept));
		
		medicationTranslator.toOpenmrsType(drug, medication);
		assertThat(drug, notNullValue());
		assertThat(drug.getIngredients().size(), greaterThanOrEqualTo(1));
		assertThat(drug.getIngredients().iterator().next().getIngredient().getUuid(), equalTo(INGREDIENT_CONCEPT_UUID));
	}
}
