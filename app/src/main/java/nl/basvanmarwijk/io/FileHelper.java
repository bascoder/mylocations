/*
 * Copyright 2014 Bas van Marwijk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.basvanmarwijk.io;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Bas van Marwijk
 * @version 1.0 - creation
 * @since 25-6-2014
 */
public class FileHelper {

    private FileHelper() {

    }

    /**
     * Reads file and inserts line by line into StringBuilder
     *
     * @param fileURI the uri pointing to the file
     * @return the StringBuffer with content of file, uses newline character on
     * newlines. Use toString to convert to String
     * @throws IOException when a IOException occured
     */
    public static StringBuilder fileToStringBuilder(String fileURI)
            throws IOException {

        // puts filereader in buffered reader
        BufferedReader in = new BufferedReader(new FileReader(fileURI));
        return fileToStringBuilder(in);

    }

    /**
     * Returns StringBuilder from reader in
     *
     * @param in input reader
     * @return StringBuilder containing read data
     * @throws IOException i/o error
     */
    public static StringBuilder fileToStringBuilder(BufferedReader in)
            throws IOException {

        StringBuilder builder;
        try {
            String currentRegel;

            builder = new StringBuilder();
            while ((currentRegel = in.readLine()) != null) {
                builder.append(currentRegel);
                builder.append('\n');
            }
        } finally {
            if (in != null)
                in.close();
        }

        return builder;
    }

    public static StringBuilder fileToStringBuilder(FileDescriptor file)
            throws IOException {
        return fileToStringBuilder(new BufferedReader(new FileReader(file)));

    }

    public static StringBuilder fileToStringBuilder(InputStream in) throws IOException {
        StringBuilder builder = new StringBuilder();

        builder.append(IOUtils.toString(in));
        return builder;
    }

}
