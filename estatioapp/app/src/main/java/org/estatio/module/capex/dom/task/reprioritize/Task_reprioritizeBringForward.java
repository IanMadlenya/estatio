package org.estatio.module.capex.dom.task.reprioritize;

import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.Lists;

import org.joda.time.LocalDateTime;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.clock.ClockService;

import org.estatio.module.capex.dom.task.Task;
import org.estatio.module.capex.dom.task.Task_abstract;

/**
 * TODO: inline this mixin.
 */
@Mixin(method = "act")
public class Task_reprioritizeBringForward extends Task_abstract {

    public Task_reprioritizeBringForward(final Task task) {
        super(task);
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(contributed = Contributed.AS_ACTION, cssClassFa = "fa-arrow-left")
    public Task act(final Integer bringForwardByDays) {
        final LocalDateTime newDateTime = task.getCreatedOn().minusDays(bringForwardByDays);
        task.setCreatedOn(newDateTime);
        return task;
    }

    public List<Integer> choices0Act() {
        return Lists.newArrayList(1,2,3,4,7,14,21,28);
    }

    public String disableAct() {
        return task.isCompleted() ? "Task has already been completed" : null;
    }

    @Inject
    ClockService clockService;

}
