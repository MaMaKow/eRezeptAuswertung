/*
 * Copyright (C) 2021 Mandelkow
 *
 * Dienstplan Apotheke
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.apotheke.erezeptauswertung;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Mandelkow
 * @see
 * https://medium.com/@sonaldwivedi/how-to-read-config-properties-file-in-java-6a501dc96b25
 * https://www.toolsqa.com/selenium-cucumber-framework/read-configurations-from-property-file/
 */
public class ReadPropertyFile {

    private Properties properties;
    private final String propertyFilePath = "Configuration.properties";

    public ReadPropertyFile() {
        properties = new Properties();
        try {
            FileInputStream fileInputStream = new FileInputStream(propertyFilePath);
            properties.load(fileInputStream);
        } catch (FileNotFoundException exception) {
            Logger.getLogger(ReadPropertyFile.class.getName()).log(Level.SEVERE, null, exception);
        } catch (IOException exception) {
            Logger.getLogger(ReadPropertyFile.class.getName()).log(Level.SEVERE, null, exception);
        }

    }

    public String getApiHost() {
        String property = properties.getProperty("apiHost");
        if (null != property) {
            return property;
        }
        return null;
    }

    public String getApiPort() {
        String property = properties.getProperty("apiPort");
        if (null != property) {
            return property;
        }
        return "3306";
    }

}
