/*
 * Copyright (C) 2025 s3000
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.apotheke.erezeptauswertung;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.json.JSONObject;

/**
 *
 * @author s3000
 */
public class ERezept {

    // Versicherte
    private final String patientSurname;
    private final String patientForename;
    private final String insuranceName;
    private final String insuranceType;

    // Arzt
    private final String practitionerPrefix;
    private final String practitionerSurname;

    // Verordnung
    private final String medicationPrescriptionPzn;
    private final String medicationPrescriptionDescription;
    private final boolean medicationPrescriptionIsVaccine;

    // Abgabe
    private final String medicationDispensePzn;
    private final String medicationDispenseDescription;
    private final boolean medicationDispenseIsVaccine;
    private final ZonedDateTime dispenseTimestamp;

    // Sonstiges
    private final ZonedDateTime created;
    private final String taskId;
    private final String accessCode;
    private final int taskStatus;

    public ERezept(String jsonString) {
        JSONObject json = new JSONObject(jsonString);

        this.patientSurname = json.optString("patientSurname", "");
        this.patientForename = json.optString("patientForename", "");
        this.insuranceName = json.optString("insuranceName", "");

        JSONObject insuranceTypeObj = json.optJSONObject("insuranceType");
        this.insuranceType = insuranceTypeObj != null ? insuranceTypeObj.optString("code", "") : "";

        this.practitionerPrefix = json.optString("practitionerPrefix", "");
        this.practitionerSurname = json.optString("practitionerSurname", "");

        this.medicationPrescriptionPzn = json.optString("medicationPrescriptionPzn", "");
        this.medicationPrescriptionDescription = json.optString("medicationPrescriptionDescription", "");
        this.medicationPrescriptionIsVaccine = json.optBoolean("medicationPrescriptionIsVaccine", false);

        this.medicationDispensePzn = json.optString("medicationDispensePzn", "");
        this.medicationDispenseDescription = json.optString("medicationDispenseDescription", "");
        this.medicationDispenseIsVaccine = json.optBoolean("medicationDispenseIsVaccine", false);

        this.dispenseTimestamp = parseZonedDateTime(json.optString("dispenseTimestamp"));
        this.created = parseZonedDateTime(json.optString("created"));
        this.taskId = json.optString("taskId", "");
        this.accessCode = json.optString("accessCode", "");
        this.taskStatus = json.optInt("taskStatus", -1);
    }

    private ZonedDateTime parseZonedDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        return ZonedDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    // Getter für wichtige Felder
    public String getPatientFullName() {
        return patientForename + " " + patientSurname;
    }

    public String getInsuranceName() {
        return insuranceName + " (" + insuranceType + ")";
    }

    public String getMedicationPrescriptionText() {
        return medicationPrescriptionDescription + " [PZN: " + medicationPrescriptionPzn + "]";
    }

    public String getDispenseText() {
        return medicationDispenseDescription + " [PZN: " + medicationDispensePzn + "]";
    }

    public ZonedDateTime getCreated() {
        return created;
    }

    public ZonedDateTime getDispenseTimestamp() {
        return dispenseTimestamp;
    }

    public int getTaskStatus() {
        return taskStatus;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getAccessCode() {
        return accessCode;
    }

    public String getArztName() {
        return practitionerPrefix + " " + practitionerSurname;
    }

    public boolean isVaccine() {
        return medicationDispenseIsVaccine || medicationPrescriptionIsVaccine;
    }
}
