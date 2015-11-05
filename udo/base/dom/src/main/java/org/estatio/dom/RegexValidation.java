/*
 *
 *  Copyright 2012-2014 Eurocommercial Properties NV
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.estatio.dom;

public final class RegexValidation {

    public static final String REFERENCE = "[-/_A-Z0-9]+";

    public static final class Currency {
        private Currency() {
        }

        public static final String REFERENCE = "[A-Z]+";
        public static final String REFERENCE_DESCRIPTION = "Only letters are allowed";
    }

    public static final class Person {
        private Person() {
        }

        public static final String REFERENCE = "[A-Z,0-9,_,-,/]+";
        public static final String REFERENCE_DESCRIPTION = "Only letters, numbers and these three symbols being: \"_\" , \"-\" and \"/\" are allowed";
        public static final String INITIALS = "[A-Z]+";
        public static final String INITIALS_DESCRIPTION = "Only letters are allowed";
    }

    public static final class Property {
        private Property() {
        }

        /* Only 3 letters */
        public static final String REFERENCE = "[A-Z]{3}";
        public static final String REFERENCE_DESCRIPTION = "Only 3 letters, e.g. XXX";
    }

    public static final class BankAccount {
        private BankAccount() {
        }

        public static final String IBAN = "[A-Z,0-9]+";
        public static final String IBAN_DESCRIPTION = "Only letters and numbers are allowed";
    }

    public static final class CommunicationChannel {
        private CommunicationChannel() {
        }

        public static final String PHONENUMBER = "[+]?[0-9 -]*";
        public static final String PHONENUMBER_DESCRIPTION = "Only numbers and these two symbols being \"-\" and \"+\" are allowed ";
        public static final String EMAIL = "[^@ ]*@{1}[^@ ]*[.]+[^@ ]*";
        public static final String EMAIL_DESCRIPTION = "Only one \"@\" symbol is allowed, followed by a domainname e.g. test@example.com";
    }

    public static final class Lease {
        private Lease() {
        }

        //(?=(?:.{11,15}|.{17}))([X,Z]{1}-)?([A-Z]{3}-([A-Z,0-9]{3,8})-[A-Z,0-9,\&+=_/-]{1,7})
        //public static final String REFERENCE = "(?=.{11,17})([A-Z]{1}-)?([A-Z]{3}-([A-Z,0-9]{3,8})-[A-Z,0-9,\\&+=_/-]{1,7})";
        public static final String REFERENCE = "^([X,Z]-)?(?=.{11,15}$)([A-Z]{3})-([A-Z,0-9]{3,8})-([A-Z,0-9,\\&+=_/-]{1,7})$";
        public static final String REFERENCE_DESCRIPTION = "only letters and numbers devided by at least 3 and at most 4 dashes:\"-\" with a total between 11 and 17 characters. "
                + "e.g.: 1 letter \"-\" 3 letters \"-\" 3-8 letters or numbers \"-\" 1-7 letters, numbers or one of the following symbols: \"&\", \"+\", \"=\", \"_\", \"/\", \"-\" OR\n"
                + "3 letters \"-\" 3-8 letters or numbers \"-\" 1-7 letters, numbers or one of the following symbols: & + = _ / -";
    }

    public static final class Unit {
        private Unit() {
        }

        public static final String REFERENCE = "(?=.{5,17})([A-Z]{1}-)?([A-Z]{3}-[A-Z,0-9,/,+,-]{1,11})";
        public static final String REFERENCE_DESCRIPTION = "only letters and numbers devided by at least 2 and at most 3 dashes:\"-\" with a total between 15 and 17 characters. "
                + "e.g.: 1 letter \"-\" 3 letters \"-\" 3-8 letters or numbers \"-\" 1-11 letters, numbers or one of the following symbols: \"/\", \"+\", \"-\" OR\n"
                + "3 letters \"-\" 3-8 letters or numbers \"-\" 1-11 letters, numbers or one of the following symbols: \"/\", \"+\", \"-\"";

    }
}