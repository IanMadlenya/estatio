package org.estatio.module.capex.contributions;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.SemanticsOf;

import org.estatio.capex.dom.invoice.IncomingInvoice;
import org.estatio.module.asset.dom.Property;
import org.estatio.module.invoice.dom.InvoiceRepository;
import org.estatio.module.party.dom.Party;

/**
 * This cannot be inlined (needs to be a mixin) because Party does not know about invoices.
 */
@Mixin(method="coll")
public class Party_findIncomingInvoicesByProperty {
    private final Party seller;
    public Party_findIncomingInvoicesByProperty(final Party seller) { this.seller = seller; }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(contributed= Contributed.AS_ACTION)
    public List<IncomingInvoice> coll(final Property property) {
        return invoiceRepository.findBySeller(seller)
                .stream()
                .filter(IncomingInvoice.class::isInstance)
                .map(IncomingInvoice.class::cast)
                .filter(x->x.getProperty()!=null && x.getProperty().equals(property))
                .collect(
                Collectors.toList());
    }
    @Inject
    InvoiceRepository invoiceRepository;
}