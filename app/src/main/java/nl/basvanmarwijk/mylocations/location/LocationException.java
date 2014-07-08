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

package nl.basvanmarwijk.mylocations.location;

/**
 * Exception that occurs within LocationBridge
 *
 * @author Bas van Marwijk
 * @version 1
 * @see LocationBridge
 * @since revision 2
 */
public class LocationException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -8151395371906410243L;

    /**
     *
     */
    public LocationException() {
        super();
    }

    /**
     * @param detailMessage
     */
    public LocationException(String detailMessage) {
        super(detailMessage);

    }

    /**
     * @param throwable
     */
    public LocationException(Throwable throwable) {
        super(throwable);
    }

    /**
     * @param detailMessage
     * @param throwable
     */
    public LocationException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

}
