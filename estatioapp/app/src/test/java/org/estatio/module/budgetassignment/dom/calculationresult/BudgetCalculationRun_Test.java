/*
 * Copyright 2015 Yodo Int. Projects and Consultancy
 *
 * Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.estatio.module.budgetassignment.dom.calculationresult;

import org.junit.Test;

import org.incode.module.unittestsupport.dom.bean.AbstractBeanPropertiesTest;

import org.estatio.module.budget.dom.partioning.Partitioning;
import org.estatio.module.lease.dom.Lease;

public class BudgetCalculationRun_Test {

    public static class BeanProperties extends AbstractBeanPropertiesTest {

        @Test
        public void test() {
            final BudgetCalculationRun pojo = new BudgetCalculationRun();
            newPojoTester()
                    .withFixture(pojos(Partitioning.class, Partitioning.class))
                    .withFixture(pojos(Lease.class, Lease.class))
                    .exercise(pojo);
        }

    }

}
