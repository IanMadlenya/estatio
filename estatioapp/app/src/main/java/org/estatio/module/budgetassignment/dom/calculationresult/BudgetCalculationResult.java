package org.estatio.module.budgetassignment.dom.calculationresult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Query;
import javax.jdo.annotations.Unique;
import javax.jdo.annotations.VersionStrategy;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Auditing;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Publishing;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.repository.RepositoryService;

import org.isisaddons.module.security.dom.tenancy.ApplicationTenancy;

import org.incode.module.base.dom.utils.TitleBuilder;

import org.estatio.module.base.dom.UdoDomainObject2;
import org.estatio.module.budget.dom.budgetcalculation.BudgetCalculation;
import org.estatio.module.budget.dom.budgetcalculation.BudgetCalculationRepository;
import org.estatio.module.budget.dom.budgetcalculation.Status;
import org.estatio.module.budget.dom.partioning.Partitioning;
import org.estatio.module.budgetassignment.dom.override.BudgetOverride;
import org.estatio.module.budgetassignment.dom.override.BudgetOverrideRepository;
import org.estatio.module.budgetassignment.dom.override.BudgetOverrideValue;
import org.estatio.module.charge.dom.Charge;
import org.estatio.module.lease.dom.Lease;
import org.estatio.module.lease.dom.occupancy.Occupancy;

import lombok.Getter;
import lombok.Setter;

@javax.jdo.annotations.PersistenceCapable(
        identityType = IdentityType.DATASTORE
        ,schema = "dbo" // Isis' ObjectSpecId inferred from @DomainObject#objectType
)
@javax.jdo.annotations.DatastoreIdentity(
        strategy = IdGeneratorStrategy.NATIVE,
        column = "id")
@javax.jdo.annotations.Version(
        strategy = VersionStrategy.VERSION_NUMBER,
        column = "version")
@Unique(name = "BudgetCalculationResult_partitioning_lease_invoiceCharge_UNQ", members = { "partitioning", "lease", "invoiceCharge" })
@javax.jdo.annotations.Queries({
        @Query(
                name = "findUnique", language = "JDOQL",
                value = "SELECT " +
                        "FROM org.estatio.module.budgetassignment.dom.calculationresult.BudgetCalculationResult " +
                        "WHERE partitioning == :partitioning && "
                        + "lease == :lease && "
                        + "invoiceCharge == :invoiceCharge"),
        @Query(
                name = "findByPartitioning", language = "JDOQL",
                value = "SELECT " +
                        "FROM org.estatio.module.budgetassignment.dom.calculationresult.BudgetCalculationResult " +
                        "WHERE partitioning == :partitioning "),
        @Query(
                name = "findByLease", language = "JDOQL",
                value = "SELECT " +
                        "FROM org.estatio.module.budgetassignment.dom.calculationresult.BudgetCalculationResult " +
                        "WHERE lease == :lease "),
        @Query(
                name = "findByLeaseAndPartitioning", language = "JDOQL",
                value = "SELECT " +
                        "FROM org.estatio.module.budgetassignment.dom.calculationresult.BudgetCalculationResult " +
                        "WHERE lease == :lease && partitioning == :partitioning ")
})

@DomainObject(
        objectType = "org.estatio.dom.budgetassignment.calculationresult.BudgetCalculationResult",
        auditing = Auditing.DISABLED,
        publishing = Publishing.DISABLED
)
public class BudgetCalculationResult extends UdoDomainObject2<BudgetCalculationResult> {

    public BudgetCalculationResult() {
        super("partitioning, lease, invoiceCharge");
    }

    public String title(){
        return TitleBuilder.start()
                .withName(getPartitioning())
                .withName(" ")
                .withName(getLease())
                .withName(" ")
                .withName(getInvoiceCharge())
                .toString();
    }

    @Getter @Setter
    @Column(name = "partitioningId", allowsNull = "false")
    private Partitioning partitioning;

    @Getter @Setter
    @Column(name = "leaseId", allowsNull = "false")
    private Lease lease;

    @Getter @Setter
    @Column(name = "chargeId", allowsNull = "false")
    private Charge invoiceCharge;

    @Getter @Setter
    @Column(allowsNull = "true", scale = 2)
    private BigDecimal value;

    @Getter @Setter
    @Column(allowsNull = "true", scale = 2)
    private BigDecimal shortfall;

    @Getter @Setter
    @Column(allowsNull = "false")
    private Status status;

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(contributed = Contributed.AS_ASSOCIATION)
    public List<BudgetOverrideValue> getOverrideValues(){
        List<BudgetOverrideValue> results = new ArrayList<>();
        for (BudgetOverride override : budgetOverrideRepository
                .findByLeaseAndInvoiceCharge(
                        getLease(),
                        getInvoiceCharge())){
            for (BudgetOverrideValue value : override.getValues()){
                if (value.getType() == getPartitioning().getType()
                    &&
                    getPartitioning()!=null
                    &&
                    override.getInterval().contains(getPartitioning().getInterval())
                        ) {
                    results.add(value);
                }
            }
        }
        return results;
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(contributed = Contributed.AS_ASSOCIATION)
    public List<BudgetCalculation> getBudgetCalculations(){
        List<BudgetCalculation> results = new ArrayList<>();
        for (Occupancy occupancy : getLease().getOccupancies()) {
            results.addAll(budgetCalculationRepository.findByPartitioningAndUnitAndInvoiceChargeAndType(getPartitioning(), occupancy.getUnit(), getInvoiceCharge(), getPartitioning().getType()));
        }
        return results;
    }

    @Programmatic
    public void calculate() throws IllegalArgumentException {

        validateOverrides();

        BigDecimal valueCalculatedByBudget = valueAsCalculatedByBudget();
        BigDecimal overrideValue = BigDecimal.ZERO;
        List<Charge> incomingChargesOnOverrides = new ArrayList<>();

        if (overrideValueForInvoiceCharge()!=null){
            // SCENARIO: one override for all
            overrideValue = overrideValue.add(overrideValueForInvoiceCharge().getValue());
        } else {
            // SCENARIO: overrides on incoming charge level
            BigDecimal valueToSubtract = BigDecimal.ZERO;
            for (BudgetOverrideValue value : getOverrideValues()) {
                incomingChargesOnOverrides.add(value.getBudgetOverride().getIncomingCharge());
                overrideValue = overrideValue.add(value.getValue());
            }
            for (Charge charge : incomingChargesOnOverrides){
                for (BudgetCalculation calculation : getBudgetCalculations().stream().filter(x->x.getIncomingCharge().equals(charge)).collect(Collectors.toList())){
                    valueToSubtract = valueToSubtract.add(calculation.getValue());
                }
            }
            overrideValue = overrideValue.add(valueCalculatedByBudget).subtract(valueToSubtract);
        }
        setValue(overrideValue.setScale(2, BigDecimal.ROUND_HALF_UP));
        setShortfall(valueCalculatedByBudget.subtract(overrideValue).setScale(2, BigDecimal.ROUND_HALF_UP));
    }

    void validateOverrides() throws IllegalArgumentException {
        budgetOverrideRepository.validateBudgetOverridesForLease(getLease(), getInvoiceCharge());
    }

    @Programmatic
    public BudgetOverrideValue overrideValueForInvoiceCharge(){
        return (getOverrideValues().size()==1 && getOverrideValues().get(0).getBudgetOverride().getIncomingCharge()==null)
                ?
                getOverrideValues().get(0)
                :
                null;
    }

    @Programmatic
    public BigDecimal valueAsCalculatedByBudget(){
        BigDecimal valueCalculatedByBudget = BigDecimal.ZERO;
        for (BudgetCalculation calculation : getBudgetCalculations()){
            valueCalculatedByBudget = valueCalculatedByBudget.add(calculation.getValue());
        }
        return valueCalculatedByBudget;
    }

    @Programmatic
    public void finalizeCalculationResult() {
        for (BudgetCalculation calculation : getBudgetCalculations()){
            calculation.finalizeCalculation();
        }
        for (BudgetOverrideValue overrideValue : getOverrideValues()){
            overrideValue.finalizeOverrideValue();
        }
        setStatus(Status.ASSIGNED);
    }

    @Programmatic
    public void remove() {
        repositoryService.removeAndFlush(this);
    }

    @Override
    public ApplicationTenancy getApplicationTenancy() {
        return getPartitioning().getApplicationTenancy();
    }

    @Inject
    BudgetOverrideRepository budgetOverrideRepository;

    @Inject
    private BudgetCalculationRepository budgetCalculationRepository;

    @Inject
    private RepositoryService repositoryService;


}

