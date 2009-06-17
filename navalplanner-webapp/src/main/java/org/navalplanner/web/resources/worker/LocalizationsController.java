package org.navalplanner.web.resources.worker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionSatisfaction;
import org.navalplanner.web.common.Util;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.api.Button;
import org.zkoss.zul.api.Listbox;
import org.zkoss.zul.api.Listitem;

/**
 * Subcontroller for assigning localizations <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class LocalizationsController extends GenericForwardComposer {

    private IWorkerModel workerModel;

    private Listbox activeSatisfactions;

    private Listbox criterionsNotAssigned;

    private Button unassignButton;

    private Button assignButton;

    LocalizationsController(IWorkerModel workerModel) {
        Validate.notNull(workerModel);
        this.workerModel = workerModel;
    }

    public List<CriterionSatisfaction> getLocalizationsHistory() {
        return workerModel.getLocalizationsAssigner().getHistoric();
    }

    public List<CriterionSatisfaction> getActiveSatisfactions() {
        return workerModel.getLocalizationsAssigner().getActiveSatisfactions();
    }

    public List<Criterion> getCriterionsNotAssigned() {
        return workerModel.getLocalizationsAssigner()
                .getCriterionsNotAssigned();
    }

    private void reloadLists() {
        Util.reloadBindings(activeSatisfactions, criterionsNotAssigned);
    }

    private static <T> List<T> extractValuesOf(
            Collection<? extends Listitem> items, Class<T> klass) {
        ArrayList<T> result = new ArrayList<T>();
        for (Listitem listitem : items) {
            result.add(klass.cast(listitem.getValue()));
        }
        return result;
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        unassignButton.addEventListener("onClick", new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                workerModel.unassignSatisfactions(
                        extractValuesOf(activeSatisfactions.getSelectedItems(),
                                CriterionSatisfaction.class));
                reloadLists();
            }

        });
        assignButton.addEventListener("onClick", new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                Set<Listitem> selectedItems = criterionsNotAssigned
                        .getSelectedItems();
                workerModel.assignCriteria(
                        extractValuesOf(selectedItems, Criterion.class));
                reloadLists();
            }
        });
    }

}