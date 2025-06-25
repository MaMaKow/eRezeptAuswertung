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

/**
 *
 * @author s3000
 */
class TokenData {

    private final String taskId;
    private final String accessCode;
    private final String created;
    private final String status;
    private final String json;
    private final ZonedDateTime createdDateTime;

    public TokenData(String taskId, String accessCode, String created, String status, String json) {
        this.taskId = taskId;
        this.accessCode = accessCode;
        this.created = created;
        this.status = status;
        this.json = json;
        this.createdDateTime = ZonedDateTime.parse(created);
    }

    public String getTaskId() {
        return taskId;
    }

    public String getAccessCode() {
        return accessCode;
    }

    public String getCreated() {
        return created;
    }

    public ZonedDateTime getCreatedDateTime() {
        return createdDateTime;
    }

    public String getStatus() {
        return status;
    }

    public String getJson() {
        return json;
    }
}
