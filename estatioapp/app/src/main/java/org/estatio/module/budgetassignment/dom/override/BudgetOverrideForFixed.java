package org.estatio.module.budgetassignment.dom.override;

import java.math.BigDecimal;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.InheritanceStrategy;

import org.joda.time.LocalDate;

import org.isisaddons.module.security.dom.tenancy.ApplicationTenancy;

import org.estatio.module.budget.dom.budgetcalculation.BudgetCalculationType;

import lombok.Getter;
import lombok.Setter;

@javax.jdo.annotations.PersistenceCapable(
        schema = "dbo"     // Isis' ObjectSpecId inferred from @Discriminator
)
@javax.jdo.annotations.Inheritance(strategy = InheritanceStrategy.SUPERCLASS_TABLE)
@javax.jdo.annotations.Discriminator("org.estatio.dom.budgetassignment.override.BudgetOverrideForFixed")
public class BudgetOverrideForFixed extends BudgetOverride {

    @Getter @Setter
    @Column(scale = 2)
    private BigDecimal fixedValue;

    @Override BudgetOverrideValue valueFor(final LocalDate date, final BudgetCalculationType type) {
        return findOrCreateCalculation(fixedValue, type);
    }

    @Override
    public ApplicationTenancy getApplicationTenancy() {
        return getLease().getApplicationTenancy();
    }

}
