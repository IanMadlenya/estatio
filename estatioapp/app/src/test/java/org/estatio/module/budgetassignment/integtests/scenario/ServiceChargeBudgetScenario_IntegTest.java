package org.estatio.module.budgetassignment.integtests.scenario;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.fixturescripts.FixtureScript;

import org.estatio.module.budget.dom.budget.Budget;
import org.estatio.module.budget.dom.budgetcalculation.BudgetCalculation;
import org.estatio.module.budget.dom.budgetcalculation.BudgetCalculationRepository;
import org.estatio.module.budget.dom.budgetcalculation.BudgetCalculationService;
import org.estatio.module.budget.dom.budgetcalculation.BudgetCalculationType;
import org.estatio.module.budget.dom.budgetcalculation.Status;
import org.estatio.module.budget.dom.budgetitem.BudgetItem;
import org.estatio.module.budget.dom.keytable.KeyTable;
import org.estatio.module.budget.fixtures.budgets.enums.Budget_enum;
import org.estatio.module.budget.fixtures.partitioning.enums.Partitioning_enum;
import org.estatio.module.budgetassignment.contributions.Budget_Calculate;
import org.estatio.module.budgetassignment.contributions.Budget_Reconcile;
import org.estatio.module.budgetassignment.dom.calculationresult.BudgetCalculationResult;
import org.estatio.module.budgetassignment.dom.calculationresult.BudgetCalculationResultLink;
import org.estatio.module.budgetassignment.dom.calculationresult.BudgetCalculationResultLinkRepository;
import org.estatio.module.budgetassignment.dom.calculationresult.BudgetCalculationResultRepository;
import org.estatio.module.budgetassignment.dom.calculationresult.BudgetCalculationRun;
import org.estatio.module.budgetassignment.dom.calculationresult.BudgetCalculationRunRepository;
import org.estatio.module.budgetassignment.dom.override.BudgetOverrideForFlatRate;
import org.estatio.module.budgetassignment.dom.override.BudgetOverrideForMax;
import org.estatio.module.budgetassignment.dom.override.BudgetOverrideRepository;
import org.estatio.module.budgetassignment.dom.override.BudgetOverrideType;
import org.estatio.module.budgetassignment.dom.override.BudgetOverrideValue;
import org.estatio.module.budgetassignment.dom.override.BudgetOverrideValueRepository;
import org.estatio.module.budgetassignment.dom.service.BudgetAssignmentService;
import org.estatio.module.budgetassignment.dom.service.CalculationResultViewModel;
import org.estatio.module.budgetassignment.dom.service.DetailedCalculationResultViewmodel;
import org.estatio.module.budgetassignment.fixtures.override.enums.BudgetOverrideForFlatRate_enum;
import org.estatio.module.budgetassignment.fixtures.override.enums.BudgetOverrideForMax_enum;
import org.estatio.module.budgetassignment.integtests.BudgetAssignmentModuleIntegTestAbstract;
import org.estatio.module.charge.dom.Charge;
import org.estatio.module.charge.fixtures.charges.enums.Charge_enum;
import org.estatio.module.invoice.dom.PaymentMethod;
import org.estatio.module.lease.dom.InvoicingFrequency;
import org.estatio.module.lease.dom.Lease;
import org.estatio.module.lease.dom.LeaseItem;
import org.estatio.module.lease.dom.LeaseItemRepository;
import org.estatio.module.lease.dom.LeaseItemStatus;
import org.estatio.module.lease.dom.LeaseItemType;
import org.estatio.module.lease.dom.LeaseTermForServiceCharge;
import org.estatio.module.lease.fixtures.lease.enums.Lease_enum;

import static org.assertj.core.api.Assertions.assertThat;

public class ServiceChargeBudgetScenario_IntegTest extends BudgetAssignmentModuleIntegTestAbstract {

    @Inject
    BudgetCalculationService budgetCalculationService;

    @Inject
    BudgetAssignmentService budgetAssignmentService;

    @Inject
    LeaseItemRepository leaseItemRepository;

    @Inject
    BudgetOverrideRepository budgetOverrideRepository;

    @Inject
    BudgetOverrideValueRepository budgetOverrideValueRepository;

    @Inject
    BudgetCalculationRunRepository budgetCalculationRunRepository;

    @Inject
    BudgetCalculationResultRepository budgetCalculationResultRepository;

    @Inject
    BudgetCalculationResultLinkRepository budgetCalculationResultLinkRepository;

    @Inject
    BudgetCalculationRepository budgetCalculationRepository;

    @Before
    public void setupData() {
        runFixtureScript(new FixtureScript() {
            @Override
            protected void execute(final ExecutionContext executionContext) {
                executionContext.executeChild(this, Partitioning_enum.BudPartitioning2015.builder());

                executionContext.executeChildT(this, Lease_enum.BudPoison001Nl.builder());
                executionContext.executeChildT(this, Lease_enum.BudMiracle002Nl.builder());
                executionContext.executeChildT(this, Lease_enum.BudHello003Nl.builder());
                executionContext.executeChildT(this, Lease_enum.BudDago004Nl.builder());
                executionContext.executeChildT(this, Lease_enum.BudNlBank004Nl.builder());
                executionContext.executeChildT(this, Lease_enum.BudHyper005Nl.builder());
                executionContext.executeChildT(this, Lease_enum.BudHello006Nl.builder());
                executionContext.executeChildT(this, BudgetOverrideForFlatRate_enum.BudMiracle002Nl_2015.builder());
                executionContext.executeChildT(this, BudgetOverrideForMax_enum.BudPoison001Nl_2015.builder());
                executionContext.executeChildT(this, Charge_enum.NlMarketingIncoming.builder());
                executionContext.executeChildT(this, Charge_enum.NlMarketing.builder());
          }
        });
    }


    public static class Calculate extends ServiceChargeBudgetScenario_IntegTest {

        Budget budget;
        List<BudgetCalculation> calculations;
        List<BudgetCalculation> newCalculations;
        List<BudgetCalculation> assignedCalculations;
        List<BudgetCalculationRun> calculationRuns;
        List<CalculationResultViewModel> calculationResultViewModels;
        List<DetailedCalculationResultViewmodel> detailedCalculationResultViewmodels;

        Lease leasePoison;
        Lease leaseMiracle;
        Lease leaseHello3;
        Lease leaseDago;
        Lease leaseNlBank;
        Lease leaseHyper;
        Lease leaseHello6;
        BudgetOverrideForMax overrideForPoisonBudgetedAndActual;
        BudgetOverrideForFlatRate overrideForMiracleBudgeted;
        Charge invoiceCharge1;
        Charge invoiceCharge2;
        Charge invoiceChargeForMarketing;
        Charge incomingCharge1;
        Charge incomingCharge2;
        Charge incomingCharge3;
        Charge incomingChargeForMarketing;
        KeyTable keyTable2Truncated;

        @Before
        public void setup() {
            // given
            budget = Budget_enum.BudBudget2015.findUsing(serviceRegistry);

            //**IMPORTANT!** truncate keytable
            keyTable2Truncated = budget.getKeyTables().last();
            keyTable2Truncated.getItems().last().deleteBudgetKeyItem();

            leasePoison = Lease_enum.BudPoison001Nl.findUsing(serviceRegistry);
            leaseMiracle = Lease_enum.BudMiracle002Nl.findUsing(serviceRegistry);
            leaseHello3 = Lease_enum.BudHello003Nl.findUsing(serviceRegistry);
            leaseDago = Lease_enum.BudDago004Nl.findUsing(serviceRegistry);
            leaseNlBank = Lease_enum.BudNlBank004Nl.findUsing(serviceRegistry);
            leaseHyper = Lease_enum.BudHyper005Nl.findUsing(serviceRegistry);
            leaseHello6 = Lease_enum.BudHello006Nl.findUsing(serviceRegistry);
            invoiceCharge1 = Charge_enum.NlServiceCharge.findUsing(serviceRegistry);
            invoiceCharge2 = Charge_enum.NlServiceCharge2.findUsing(serviceRegistry);
            invoiceChargeForMarketing = Charge_enum.NlMarketing.findUsing(serviceRegistry);
            incomingCharge1 = Charge_enum.NlIncomingCharge1.findUsing(serviceRegistry);
            incomingCharge2 = Charge_enum.NlIncomingCharge2.findUsing(serviceRegistry);
            incomingCharge3 = Charge_enum.NlIncomingCharge3.findUsing(serviceRegistry);
            incomingChargeForMarketing = Charge_enum.NlMarketingIncoming.findUsing(serviceRegistry);
        }

        @Test
        public void fullScenarioTest() throws Exception {
            calculate();
            detailed_calculation();
            calculate_results_for_leases();
            calculate_and_assign();
            final_calculation_is_idempotent();
            assign_budget_when_updated_with_new_item_creates_no_calculation_run_at_the_moment();
            reconcile();
//            reconcilation_does_not_create_lease_item_and_term();
//            reconcile_when_audited_and_updated();
        }

        public static BigDecimal U1_BVAL_1 = new BigDecimal("1928.57");
        public static BigDecimal U1_BVAL_2 = new BigDecimal("964.29");
        public static BigDecimal U2_BVAL_1 = new BigDecimal("2857.14");
        public static BigDecimal U2_BVAL_2 = new BigDecimal("1928.57");
        public static BigDecimal U3_BVAL_1 = new BigDecimal("3785.71");
        public static BigDecimal U3_BVAL_2 = new BigDecimal("2892.86");
        public static BigDecimal U4_BVAL_1 = new BigDecimal("4714.29");
        public static BigDecimal U4_BVAL_2 = new BigDecimal("3857.14");
        public static BigDecimal U5_BVAL_1 = new BigDecimal("5642.86");
        public static BigDecimal U5_BVAL_2 = new BigDecimal("4821.43");
        public static BigDecimal U6_BVAL_1 = new BigDecimal("6571.43");
        public static BigDecimal U6_BVAL_2 = new BigDecimal("5785.71");
        public static BigDecimal U7_BVAL_1 = new BigDecimal("6500.00");
        public static BigDecimal U7_BVAL_2 = new BigDecimal("6750.00");
        public static BigDecimal BVAL_POISON_1 = new BigDecimal("1921.43");
        public static BigDecimal BVAL_MIRACLE_1 = new BigDecimal("1125.00");



        public void calculate() throws Exception {

            // given

            // when
            wrap(mixin(Budget_Calculate.class, budget)).calculate(false);

            // then
            calculations = budgetCalculationRepository.findByBudget(budget);
            calculationRuns = budgetCalculationRunRepository.findByBudget(budget);
            calculationResultViewModels = budgetAssignmentService.getCalculationResults(budget);

            assertThat(calculationRuns.size()).isEqualTo(6);
            assertThat(calculationResultViewModels.size()).isEqualTo(12);
            assertThat(calculations.size()).isEqualTo(33);
            assertThat(budgetedAmountFor(leasePoison, invoiceCharge1)).isEqualTo(U1_BVAL_1);
            assertThat(budgetedAmountFor(leasePoison, invoiceCharge2)).isEqualTo(U1_BVAL_2);
            assertThat(budgetedAmountFor(leaseMiracle, invoiceCharge1)).isEqualTo(U2_BVAL_1);
            assertThat(budgetedAmountFor(leaseMiracle, invoiceCharge2)).isEqualTo(U2_BVAL_2);
            assertThat(budgetedAmountFor(leaseHello3, invoiceCharge1)).isEqualTo(U3_BVAL_1);
            assertThat(budgetedAmountFor(leaseHello3, invoiceCharge2)).isEqualTo(U3_BVAL_2);
            assertThat(budgetedAmountFor(leaseDago, invoiceCharge1)).isEqualTo(new BigDecimal("11214.29"));
            assertThat(budgetedAmountFor(leaseDago, invoiceCharge1)).isEqualTo(U4_BVAL_1.add(U7_BVAL_1));
            assertThat(budgetedAmountFor(leaseDago, invoiceCharge2)).isEqualTo(new BigDecimal("10607.14"));
            assertThat(budgetedAmountFor(leaseDago, invoiceCharge2)).isEqualTo(U4_BVAL_2.add(U7_BVAL_2));
            assertThat(budgetedAmountFor(leaseNlBank, invoiceCharge1)).isEqualTo(U4_BVAL_1);
            assertThat(budgetedAmountFor(leaseNlBank, invoiceCharge2)).isEqualTo(U4_BVAL_2);
            assertThat(budgetedAmountFor(leaseHyper, invoiceCharge1)).isEqualTo(U5_BVAL_1);
            assertThat(budgetedAmountFor(leaseHyper, invoiceCharge2)).isEqualTo(U5_BVAL_2);

            calculations.stream().forEach(x->assertThat(x.getStatus()).isEqualTo(Status.NEW));

        }

        private BigDecimal budgetedAmountFor(final Lease lease, final Charge invoiceCharge){

            BigDecimal resultValue = BigDecimal.ZERO;

            for (CalculationResultViewModel result : resultsForLease(lease.getReference(), invoiceCharge.getReference())){
                resultValue = resultValue.add(result.getBudgetedValue());
            }

            return resultValue;
        }

        private List<CalculationResultViewModel> resultsForLease(final String leaseReference, final String invoiceChargeReference){
            return calculationResultViewModels.stream().filter(x ->x.getLeaseReference().equals(leaseReference) && x.getInvoiceCharge().equals(invoiceChargeReference)).collect(Collectors.toList());
        }

        public void detailed_calculation() throws Exception {


            // when
            detailedCalculationResultViewmodels = budgetAssignmentService.getDetailedCalculationResults(leaseDago, budget, BudgetCalculationType.BUDGETED);

            // then
            assertThat(detailedCalculationResultViewmodels.size()).isEqualTo(8);

            assertThat(detailedCalculationResultViewmodels.get(0).getValueForLease()).isEqualTo(new BigDecimal("1428.571430"));
            assertThat(detailedCalculationResultViewmodels.get(0).getInvoiceCharge()).isEqualTo(invoiceCharge1.getReference());

            assertThat(detailedCalculationResultViewmodels.get(1).getValueForLease()).isEqualTo(new BigDecimal("2285.714288"));
            assertThat(detailedCalculationResultViewmodels.get(1).getInvoiceCharge()).isEqualTo(invoiceCharge1.getReference());

            assertThat(detailedCalculationResultViewmodels.get(2).getValueForLease()).isEqualTo(new BigDecimal("571.428572"));
            assertThat(detailedCalculationResultViewmodels.get(2).getInvoiceCharge()).isEqualTo(invoiceCharge1.getReference());

            assertThat(detailedCalculationResultViewmodels.get(3).getValueForLease()).isEqualTo(new BigDecimal("428.571429"));
            assertThat(detailedCalculationResultViewmodels.get(3).getInvoiceCharge()).isEqualTo(invoiceCharge1.getReference());

            assertThat(detailedCalculationResultViewmodels.get(4).getValueForLease()).isEqualTo(new BigDecimal("2500.000000"));
            assertThat(detailedCalculationResultViewmodels.get(4).getInvoiceCharge()).isEqualTo(invoiceCharge1.getReference());

            assertThat(detailedCalculationResultViewmodels.get(5).getValueForLease()).isEqualTo(new BigDecimal("4000.000000"));
            assertThat(detailedCalculationResultViewmodels.get(5).getInvoiceCharge()).isEqualTo(invoiceCharge1.getReference());

            assertThat(detailedCalculationResultViewmodels.get(6).getValueForLease()).isEqualTo(new BigDecimal("3857.142861"));
            assertThat(detailedCalculationResultViewmodels.get(6).getInvoiceCharge()).isEqualTo(invoiceCharge2.getReference());

            assertThat(detailedCalculationResultViewmodels.get(7).getValueForLease()).isEqualTo(new BigDecimal("6750.000000"));
            assertThat(detailedCalculationResultViewmodels.get(7).getInvoiceCharge()).isEqualTo(invoiceCharge2.getReference());

            // and when
            detailedCalculationResultViewmodels = budgetAssignmentService.getDetailedCalculationResults(leaseMiracle, budget, BudgetCalculationType.BUDGETED);

            // then
            assertThat(detailedCalculationResultViewmodels.size()).isEqualTo(2);

            assertThat(detailedCalculationResultViewmodels.get(0).getEffectiveValueForLease()).isEqualTo(new BigDecimal("1125.00"));
            assertThat(detailedCalculationResultViewmodels.get(0).getValueForLease()).isEqualTo(new BigDecimal("2857.14"));
            assertThat(detailedCalculationResultViewmodels.get(0).getShortfall()).isEqualTo(new BigDecimal("1732.14"));
            assertThat(detailedCalculationResultViewmodels.get(0).getInvoiceCharge()).isEqualTo(invoiceCharge1.getReference());
            assertThat(detailedCalculationResultViewmodels.get(0).getIncomingCharge()).isEqualTo("Override for total NLD_SERVICE_CHARGE");

            assertThat(detailedCalculationResultViewmodels.get(1).getValueForLease()).isEqualTo(new BigDecimal("1928.571417"));
            assertThat(detailedCalculationResultViewmodels.get(1).getInvoiceCharge()).isEqualTo(invoiceCharge2.getReference());

        }

        public void calculate_results_for_leases() throws Exception {

            assertThat(budgetCalculationRunRepository.findByLease(leasePoison).size()).isEqualTo(1);
            assertThat(budgetCalculationRunRepository.findByLease(leaseMiracle).size()).isEqualTo(1);
            assertThat(budgetCalculationRunRepository.findByLease(leaseHello3).size()).isEqualTo(1);
            assertThat(budgetCalculationRunRepository.findByLease(leaseDago).size()).isEqualTo(1);
            assertThat(budgetCalculationRunRepository.findByLease(leaseNlBank).size()).isEqualTo(1);
            assertThat(budgetCalculationRunRepository.findByLease(leaseHyper).size()).isEqualTo(1);
            assertThat(budgetCalculationRunRepository.findByLease(leaseHello6).size()).isEqualTo(0);

            BudgetCalculationRun rPoison = budgetCalculationRunRepository.findByLease(leasePoison).get(0);
            assertThat(rPoison.getType()).isEqualTo(BudgetCalculationType.BUDGETED);
            assertThat(rPoison.getStatus()).isEqualTo(Status.NEW);
            assertThat(rPoison.getBudgetCalculationResults().size()).isEqualTo(2);

            BudgetCalculationResult cResPoison1 = budgetCalculationResultRepository.findUnique(rPoison, invoiceCharge1);
            assertThat(cResPoison1.getValue()).isEqualTo(BVAL_POISON_1);
            assertThat(cResPoison1.getShortfall()).isEqualTo(new BigDecimal("7.14"));

            BudgetCalculationResult cResPoison2 = budgetCalculationResultRepository.findUnique(rPoison, invoiceCharge2);
            assertThat(cResPoison2.getValue()).isEqualTo(U1_BVAL_2.setScale(2, BigDecimal.ROUND_HALF_UP));
            assertThat(cResPoison2.getShortfall()).isEqualTo(new BigDecimal("0.00"));

            BudgetCalculationRun rMiracle = budgetCalculationRunRepository.findByLease(leaseMiracle).get(0);
            assertThat(rMiracle.getType()).isEqualTo(BudgetCalculationType.BUDGETED);
            assertThat(rMiracle.getStatus()).isEqualTo(Status.NEW);
            assertThat(rMiracle.getBudgetCalculationResults().size()).isEqualTo(2);

            BudgetCalculationResult cResMiracle1 = budgetCalculationResultRepository.findUnique(rMiracle, invoiceCharge1);
            assertThat(cResMiracle1.getValue()).isEqualTo(BVAL_MIRACLE_1);
            assertThat(cResMiracle1.getShortfall()).isEqualTo(new BigDecimal("1732.14"));

            BudgetCalculationResult cResMiracle2 = budgetCalculationResultRepository.findUnique(rMiracle, invoiceCharge2);
            assertThat(cResMiracle2.getValue()).isEqualTo(U2_BVAL_2.setScale(2, BigDecimal.ROUND_HALF_UP));
            assertThat(cResMiracle2.getShortfall()).isEqualTo(new BigDecimal("0.00"));

            assertThat(budgetOverrideRepository.findByLease(leasePoison).size()).isEqualTo(1);
            overrideForPoisonBudgetedAndActual = (BudgetOverrideForMax) budgetOverrideRepository.findByLease(leasePoison).get(0);
            assertThat(overrideForPoisonBudgetedAndActual.getType()).isEqualTo(null);
            assertThat(overrideForPoisonBudgetedAndActual.getInvoiceCharge()).isEqualTo(invoiceCharge1);
            assertThat(overrideForPoisonBudgetedAndActual.getIncomingCharge()).isEqualTo(incomingCharge1);
            assertThat(overrideForPoisonBudgetedAndActual.getStartDate()).isEqualTo(budget.getStartDate());
            assertThat(overrideForPoisonBudgetedAndActual.getEndDate()).isNull();
            assertThat(overrideForPoisonBudgetedAndActual.getReason()).isEqualTo(BudgetOverrideType.CEILING.reason);
            assertThat(overrideForPoisonBudgetedAndActual.getMaxValue()).isEqualTo(new BigDecimal("350.00"));
            assertThat(overrideForPoisonBudgetedAndActual.getValues().size()).isEqualTo(1);
            assertThat(overrideForPoisonBudgetedAndActual.getValues().first().getValue()).isEqualTo(new BigDecimal("350.00"));
            assertThat(overrideForPoisonBudgetedAndActual.getValues().first().getType()).isEqualTo(BudgetCalculationType.BUDGETED);
            assertThat(overrideForPoisonBudgetedAndActual.getValues().first().getStatus()).isEqualTo(Status.NEW);

            assertThat(budgetOverrideRepository.findByLease(leaseMiracle).size()).isEqualTo(1);
            overrideForMiracleBudgeted = (BudgetOverrideForFlatRate) budgetOverrideRepository.findByLease(leaseMiracle).get(0);
            assertThat(overrideForMiracleBudgeted.getType()).isEqualTo(BudgetCalculationType.BUDGETED);
            assertThat(overrideForMiracleBudgeted.getInvoiceCharge()).isEqualTo(invoiceCharge1);
            assertThat(overrideForMiracleBudgeted.getIncomingCharge()).isNull();
            assertThat(overrideForMiracleBudgeted.getStartDate()).isEqualTo(budget.getStartDate());
            assertThat(overrideForMiracleBudgeted.getEndDate()).isNull();
            assertThat(overrideForMiracleBudgeted.getReason()).isEqualTo(BudgetOverrideType.FLATRATE.reason);
            assertThat(overrideForMiracleBudgeted.getValuePerM2()).isEqualTo(new BigDecimal("12.50"));
            assertThat(overrideForMiracleBudgeted.getWeightedArea()).isEqualTo(new BigDecimal("90.00"));
            assertThat(overrideForMiracleBudgeted.getValues().size()).isEqualTo(1);
            assertThat(overrideForMiracleBudgeted.getValues().first().getValue()).isEqualTo(new BigDecimal("1125.0000"));
            assertThat(overrideForMiracleBudgeted.getValues().first().getType()).isEqualTo(BudgetCalculationType.BUDGETED);
            assertThat(overrideForMiracleBudgeted.getValues().first().getStatus()).isEqualTo(Status.NEW);

        }

        public void calculate_and_assign() throws Exception {

            // when
            wrap(mixin(Budget_Calculate.class, budget)).calculate(true);

            // then
            validateLeaseItemsAndTerms(
                    leasePoison,
                    1,
                    Arrays.asList(BVAL_POISON_1, U1_BVAL_2.setScale(2, BigDecimal.ROUND_HALF_UP)),
                    Arrays.asList(null, null),
                    Budget_enum.BudBudget2015.getStartDate());
            validateLeaseItemsAndTerms(
                    leaseMiracle,
                    1,
                    Arrays.asList(BVAL_MIRACLE_1, U2_BVAL_2.setScale(2, BigDecimal.ROUND_HALF_UP)),
                    Arrays.asList(null, null),
                    Budget_enum.BudBudget2015.getStartDate());
            validateLeaseItemsAndTerms(
                    leaseHello3,
                    1,
                    Arrays.asList(U3_BVAL_1.setScale(2, BigDecimal.ROUND_HALF_UP), U3_BVAL_2.setScale(2, BigDecimal.ROUND_HALF_UP)),
                    Arrays.asList(null, null),
                    leaseHello3.getStartDate());
            validateLeaseItemsAndTerms(
                    leaseDago,
                    1,
                    Arrays.asList(U4_BVAL_1.add(U7_BVAL_1).setScale(2, BigDecimal.ROUND_HALF_UP), U4_BVAL_2.add(U7_BVAL_2).setScale(2, BigDecimal.ROUND_HALF_UP)),
                    Arrays.asList(null, null),
                    Budget_enum.BudBudget2015.getStartDate());
            validateLeaseItemsAndTerms(
                    leaseNlBank,
                    1,
                    Arrays.asList(U4_BVAL_1.setScale(2, BigDecimal.ROUND_HALF_UP), U4_BVAL_2.setScale(2, BigDecimal.ROUND_HALF_UP)),
                    Arrays.asList(null, null),
                    leaseNlBank.getStartDate());
            validateLeaseItemsAndTerms(
                    leaseHyper,
                    1,
                    Arrays.asList(U5_BVAL_1.setScale(2, BigDecimal.ROUND_HALF_UP), U5_BVAL_2.setScale(2, BigDecimal.ROUND_HALF_UP)),
                    Arrays.asList(null, null),
                    leaseHyper.getStartDate());

            assertThat(leaseItemRepository.findLeaseItemsByType(leaseHello6, LeaseItemType.SERVICE_CHARGE_BUDGETED).size()).isEqualTo(0);

            calculations = budgetCalculationRepository.findByBudget(budget);
            calculations.stream().filter(x->!x.getUnit().getReference().equals("BUD-006")).forEach(x->assertThat(x.getStatus()).isEqualTo(Status.ASSIGNED));
            calculations.stream().filter(x->x.getUnit().getReference().equals("BUD-006")).forEach(x->assertThat(x.getStatus()).isEqualTo(Status.NEW));

            newCalculations = calculations.stream().filter(x->x.getStatus()==Status.NEW).collect(Collectors.toList());
            assignedCalculations = calculations.stream().filter(x->x.getStatus()==Status.ASSIGNED).collect(Collectors.toList());
            assertThat(newCalculations.size()).isEqualTo(5);
            assertThat(assignedCalculations.size()).isEqualTo(28);

            BudgetOverrideForMax oPoison = (BudgetOverrideForMax) budgetOverrideRepository.findByLease(leasePoison).get(0);
            BudgetOverrideForFlatRate oMiracle = (BudgetOverrideForFlatRate) budgetOverrideRepository.findByLease(leaseMiracle).get(0);
            assertThat(oPoison.getValues().first().getStatus()).isEqualTo(Status.ASSIGNED);
            assertThat(oMiracle.getValues().first().getStatus()).isEqualTo(Status.ASSIGNED);

        }

        private void validateLeaseItemsAndTerms(final Lease lease, final int expectedNumberOfLinks, final List<BigDecimal> budgetedValues, final List<BigDecimal> auditedValues, final LocalDate startDate) {

            assertThat(leaseItemRepository.findLeaseItemsByType(lease, LeaseItemType.SERVICE_CHARGE).size()).isEqualTo(2);

            assertThat(lease.getItems().first().getCharge()).isEqualTo(invoiceCharge1);
            assertThat(lease.getItems().first().getStartDate()).isEqualTo(startDate);
            assertThat(lease.getItems().first().getPaymentMethod()).isEqualTo(PaymentMethod.DIRECT_DEBIT);
            assertThat(lease.getItems().first().getInvoicingFrequency()).isEqualTo(InvoicingFrequency.QUARTERLY_IN_ADVANCE);
            assertThat(lease.getItems().first().getStatus()).isEqualTo(LeaseItemStatus.ACTIVE);
            assertThat(lease.getItems().first().getTerms().size()).isEqualTo(1);

            LeaseTermForServiceCharge term1 = (LeaseTermForServiceCharge) lease.getItems().first().getTerms().first();
            assertThat(budgetCalculationResultLinkRepository.findByLeaseTerm(term1).size()).isEqualTo(expectedNumberOfLinks);
            assertThat(term1.getBudgetedValue()).isEqualTo(budgetedValues.get(0));
            assertThat(term1.getAuditedValue()).isEqualTo(auditedValues.get(0));
            assertThat(term1.getStartDate()).isEqualTo(startDate);
            assertThat(term1.getEndDate()).isEqualTo(Budget_enum.BudBudget2015.getEndDate());

            assertThat(lease.getItems().last().getCharge()).isEqualTo(invoiceCharge2);
            assertThat(lease.getItems().last().getStartDate()).isEqualTo(startDate);
            assertThat(lease.getItems().last().getPaymentMethod()).isEqualTo(PaymentMethod.DIRECT_DEBIT);
            assertThat(lease.getItems().last().getInvoicingFrequency()).isEqualTo(InvoicingFrequency.QUARTERLY_IN_ADVANCE);
            assertThat(lease.getItems().last().getStatus()).isEqualTo(LeaseItemStatus.ACTIVE);
            assertThat(lease.getItems().last().getTerms().size()).isEqualTo(1);

            LeaseTermForServiceCharge term2 = (LeaseTermForServiceCharge) lease.getItems().last().getTerms().first();
            assertThat(budgetCalculationResultLinkRepository.findByLeaseTerm(term2).size()).isEqualTo(expectedNumberOfLinks);
            assertThat(term2.getBudgetedValue()).isEqualTo(budgetedValues.get(1));
            assertThat(term2.getAuditedValue()).isEqualTo(auditedValues.get(1));
            assertThat(term2.getStartDate()).isEqualTo(startDate);
            assertThat(term2.getEndDate()).isEqualTo(Budget_enum.BudBudget2015.getEndDate());

        }

        public void final_calculation_is_idempotent() throws Exception {

            BudgetCalculation c1Before;
            BudgetCalculation c1After;
            BudgetCalculationRun r1Before;
            BudgetCalculationRun r1After;
            BudgetCalculationResult res1Before;
            BudgetCalculationResult res1After;
            BudgetOverrideValue v1Before;
            BudgetOverrideValue v1After;
            BudgetCalculationResultLink l1Before;
            BudgetCalculationResultLink l1After;

            // given
            assertThat(budgetCalculationRepository.allBudgetCalculations().size()).isEqualTo(33);
            c1Before = budgetCalculationRepository.allBudgetCalculations().get(0);
            assertThat(c1Before.getStatus()).isEqualTo(Status.ASSIGNED);

            assertThat(budgetCalculationRunRepository.allBudgetCalculationRuns().size()).isEqualTo(6);
            r1Before = budgetCalculationRunRepository.allBudgetCalculationRuns().get(0);
            assertThat(r1Before.getStatus()).isEqualTo(Status.ASSIGNED);

            assertThat(budgetCalculationResultRepository.allBudgetCalculationResults().size()).isEqualTo(12);
            res1Before = budgetCalculationResultRepository.allBudgetCalculationResults().get(0);

            assertThat(budgetOverrideValueRepository.allBudgetOverrideValues().size()).isEqualTo(2);
            v1Before = budgetOverrideValueRepository.allBudgetOverrideValues().get(0);
            assertThat(v1Before.getStatus()).isEqualTo(Status.ASSIGNED);

            assertThat(budgetCalculationResultLinkRepository.allBudgetCalculationResultLinks().size()).isEqualTo(12);
            l1Before = budgetCalculationResultLinkRepository.allBudgetCalculationResultLinks().get(0);
            assertThat(l1Before.getLeaseTermForServiceCharge().getLeaseItem().getStatus()).isEqualTo(LeaseItemStatus.ACTIVE);

            // when
            mixin(Budget_Calculate.class, budget).calculate(true); // we do not wrap here on purpose

            // then nothing should be changed
            assertThat(budgetCalculationRepository.allBudgetCalculations().size()).isEqualTo(33);
            c1After = budgetCalculationRepository.allBudgetCalculations().get(0);
            assertThat(c1Before).isEqualTo(c1After);

            assertThat(budgetCalculationRunRepository.allBudgetCalculationRuns().size()).isEqualTo(6);
            r1After = budgetCalculationRunRepository.allBudgetCalculationRuns().get(0);
            assertThat(r1Before).isEqualTo(r1After);

            assertThat(budgetCalculationResultRepository.allBudgetCalculationResults().size()).isEqualTo(12);
            res1After = budgetCalculationResultRepository.allBudgetCalculationResults().get(0);
            assertThat(res1Before).isEqualTo(res1After);

            assertThat(budgetOverrideValueRepository.allBudgetOverrideValues().size()).isEqualTo(2);
            v1After = budgetOverrideValueRepository.allBudgetOverrideValues().get(0);
            assertThat(v1Before).isEqualTo(v1After);

            assertThat(budgetCalculationResultLinkRepository.allBudgetCalculationResultLinks().size()).isEqualTo(12);
            l1After = budgetCalculationResultLinkRepository.allBudgetCalculationResultLinks().get(0);
            assertThat(l1Before).isEqualTo(l1After);
            assertThat(l1Before.getLeaseTermForServiceCharge().getLeaseItem()).isEqualTo(l1After.getLeaseTermForServiceCharge().getLeaseItem());

        }

        public void assign_budget_when_updated_with_new_item_creates_no_calculation_run_at_the_moment() throws Exception {

            // given
            final SortedSet<LeaseItem> leasePoisonItems = leasePoison.getItems();
            assertThat(leasePoisonItems.size()).isEqualTo(2);
            calculationRuns = budgetCalculationRunRepository.findByBudget(budget);
            assertThat(calculationRuns.size()).isEqualTo(6);

            // when
            BudgetItem marketingItem = wrap(budget).newBudgetItem(new BigDecimal("7000.00"), incomingChargeForMarketing);
            wrap(marketingItem).createPartitionItemForBudgeting(invoiceChargeForMarketing, keyTable2Truncated, new BigDecimal("100"));
            wrap(mixin(Budget_Calculate.class, budget)).calculate(true);

            // then
            calculations = budgetCalculationRepository.allBudgetCalculations();
            assertThat(calculations.size()).isEqualTo(39);

            newCalculations = calculations.stream().filter(x->x.getStatus()==Status.NEW).collect(Collectors.toList());
            assignedCalculations = calculations.stream().filter(x->x.getStatus()==Status.ASSIGNED).collect(Collectors.toList());
            assertThat(newCalculations.size()).isEqualTo(11);
            // and still
            assertThat(assignedCalculations.size()).isEqualTo(28);
            assertThat(calculationRuns.size()).isEqualTo(6);
            // thus still
            assertThat(leasePoisonItems.size()).isEqualTo(2);

        }

        public void reconcile() throws Exception {

            // given
            BudgetItem budgetItem1 = budget.findByCharge(incomingCharge1);
            BudgetItem budgetItem2 = budget.findByCharge(incomingCharge2);
            BudgetItem budgetItem3 = budget.findByCharge(incomingCharge3);
            BudgetItem budgetItem4 = budget.findByCharge(invoiceChargeForMarketing);
            wrap(budgetItem1).newValue(new BigDecimal("10000.00"), budget.getStartDate());
            wrap(budgetItem2).newValue(new BigDecimal("20000.00"), budget.getStartDate());
            wrap(budgetItem3).newValue(new BigDecimal("30000.00"), budget.getStartDate());
            wrap(budgetItem4).newValue(new BigDecimal("70000.00"), budget.getStartDate());

            // when
            wrap(mixin(Budget_Reconcile.class, budget)).reconcile(true);

            // then
            calculations = budgetCalculationRepository.allBudgetCalculations();
            assertThat(calculations.size()).isEqualTo(67); // not 78 (2x39) minus 11 new calculations that were deleted

            final List<BudgetCalculation> calcsForItem1 = budgetCalculationRepository.findByBudgetItemAndCalculationType(budgetItem1, BudgetCalculationType.ACTUAL);
            assertThat(calcsForItem1.size()).isEqualTo(7);
            final List<BudgetCalculation> calcsForItem2 = budgetCalculationRepository.findByBudgetItemAndCalculationType(budgetItem2, BudgetCalculationType.ACTUAL);
            assertThat(calcsForItem2.size()).isEqualTo(13);
            final List<BudgetCalculation> calcsForItem3 = budgetCalculationRepository.findByBudgetItemAndCalculationType(budgetItem3, BudgetCalculationType.ACTUAL);
            assertThat(calcsForItem3.size()).isEqualTo(13);
            final List<BudgetCalculation> calcsForItem4 = budgetCalculationRepository.findByBudgetItemAndCalculationType(budgetItem4, BudgetCalculationType.ACTUAL);
            assertThat(calcsForItem4.size()).isEqualTo(6);
            calcsForItem4.stream().forEach(x->assertThat(x.getStatus()).isEqualTo(Status.NEW)); // for the time being whilst assign_budget_when_updated_with_new_item_creates_no_calculation_run_at_the_moment() is passing

            calculationRuns = budgetCalculationRunRepository.findByBudget(budget);
            assertThat(calculationRuns.size()).isEqualTo(12);
            assertThat(budgetCalculationRunRepository.findByLease(leasePoison).size()).isEqualTo(2);
            assertThat(budgetCalculationRunRepository.findByLease(leaseMiracle).size()).isEqualTo(2);
            assertThat(budgetCalculationRunRepository.findByLease(leaseHello3).size()).isEqualTo(2);
            assertThat(budgetCalculationRunRepository.findByLease(leaseDago).size()).isEqualTo(2);
            assertThat(budgetCalculationRunRepository.findByLease(leaseNlBank).size()).isEqualTo(2);
            assertThat(budgetCalculationRunRepository.findByLease(leaseHyper).size()).isEqualTo(2);
            assertThat(budgetCalculationRunRepository.findByLease(leaseHello6).size()).isEqualTo(0);

            newCalculations = calculations.stream().filter(x->x.getStatus()==Status.NEW).collect(Collectors.toList());
            assignedCalculations = calculations.stream().filter(x->x.getStatus()==Status.ASSIGNED).collect(Collectors.toList());
            assertThat(newCalculations.size()).isEqualTo(11);
            assertThat(assignedCalculations.size()).isEqualTo(56);

            validateLeaseItemsAndTerms(
                    leasePoison,
                    2,
                    Arrays.asList(BVAL_POISON_1, U1_BVAL_2.setScale(2, BigDecimal.ROUND_HALF_UP)),
                    Arrays.asList(BVAL_POISON_1, U1_BVAL_2.setScale(2, BigDecimal.ROUND_HALF_UP)), // override also for type actual
                    Budget_enum.BudBudget2015.getStartDate());
            validateLeaseItemsAndTerms(
                    leaseMiracle,
                    2,
                    Arrays.asList(BVAL_MIRACLE_1, U2_BVAL_2.setScale(2, BigDecimal.ROUND_HALF_UP)),
                    Arrays.asList(U2_BVAL_1, U2_BVAL_2.setScale(2, BigDecimal.ROUND_HALF_UP)), // no override for type actual - just for type budgeted
                    Budget_enum.BudBudget2015.getStartDate());
            validateLeaseItemsAndTerms(
                    leaseHello3,
                    2,
                    Arrays.asList(U3_BVAL_1.setScale(2, BigDecimal.ROUND_HALF_UP), U3_BVAL_2.setScale(2, BigDecimal.ROUND_HALF_UP)),
                    Arrays.asList(U3_BVAL_1.setScale(2, BigDecimal.ROUND_HALF_UP), U3_BVAL_2.setScale(2, BigDecimal.ROUND_HALF_UP)),
                    leaseHello3.getStartDate());
            validateLeaseItemsAndTerms(
                    leaseDago,
                    2,
                    Arrays.asList(U4_BVAL_1.add(U7_BVAL_1).setScale(2, BigDecimal.ROUND_HALF_UP), U4_BVAL_2.add(U7_BVAL_2).setScale(2, BigDecimal.ROUND_HALF_UP)),
                    Arrays.asList(U4_BVAL_1.add(U7_BVAL_1).setScale(2, BigDecimal.ROUND_HALF_UP), U4_BVAL_2.add(U7_BVAL_2).setScale(2, BigDecimal.ROUND_HALF_UP)),
                    Budget_enum.BudBudget2015.getStartDate());
            validateLeaseItemsAndTerms(
                    leaseNlBank,
                    2,
                    Arrays.asList(U4_BVAL_1.setScale(2, BigDecimal.ROUND_HALF_UP), U4_BVAL_2.setScale(2, BigDecimal.ROUND_HALF_UP)),
                    Arrays.asList(U4_BVAL_1.setScale(2, BigDecimal.ROUND_HALF_UP), U4_BVAL_2.setScale(2, BigDecimal.ROUND_HALF_UP)),
                    leaseNlBank.getStartDate());
            validateLeaseItemsAndTerms(
                    leaseHyper,
                    2,
                    Arrays.asList(U5_BVAL_1.setScale(2, BigDecimal.ROUND_HALF_UP), U5_BVAL_2.setScale(2, BigDecimal.ROUND_HALF_UP)),
                    Arrays.asList(U5_BVAL_1.setScale(2, BigDecimal.ROUND_HALF_UP), U5_BVAL_2.setScale(2, BigDecimal.ROUND_HALF_UP)),
                    leaseHyper.getStartDate());

            assertThat(leaseItemRepository.findLeaseItemsByType(leaseHello6, LeaseItemType.SERVICE_CHARGE_BUDGETED).size()).isEqualTo(0);
        }

        public void reconcilation_does_not_create_lease_item_and_term() throws Exception {

        }

        public void reconcile_when_audited_and_updated() throws Exception {

        }

    }

}
