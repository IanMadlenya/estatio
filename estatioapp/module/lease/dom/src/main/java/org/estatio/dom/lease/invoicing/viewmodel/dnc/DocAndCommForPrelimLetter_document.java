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
package org.estatio.dom.lease.invoicing.viewmodel.dnc;

import org.apache.isis.applib.annotation.Mixin;

import org.estatio.invoice.dom.Constants;

@Mixin
public class DocAndCommForPrelimLetter_document extends DocAndCommAbstract_document<DocAndCommForPrelimLetter> {

    public DocAndCommForPrelimLetter_document(final DocAndCommForPrelimLetter docAndComm) {
        super(docAndComm, Constants.DOC_TYPE_REF_PRELIM);
    }

}
