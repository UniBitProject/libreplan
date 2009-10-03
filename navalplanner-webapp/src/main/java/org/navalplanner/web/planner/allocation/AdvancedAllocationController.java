/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.navalplanner.web.planner.allocation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.zkoss.ganttz.timetracker.ICellForDetailItemRenderer;
import org.zkoss.ganttz.timetracker.IConvertibleToColumn;
import org.zkoss.ganttz.timetracker.PairOfLists;
import org.zkoss.ganttz.timetracker.TimeTrackedTable;
import org.zkoss.ganttz.timetracker.TimeTrackedTableWithLeftPane;
import org.zkoss.ganttz.timetracker.TimeTracker;
import org.zkoss.ganttz.timetracker.TimeTrackerComponentWithoutColumns;
import org.zkoss.ganttz.timetracker.zoom.DetailItem;
import org.zkoss.ganttz.util.Interval;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.api.Column;

public class AdvancedAllocationController extends GenericForwardComposer {

    private Div insertionPointTimetracker;
    private Div insertionPointLeftPanel;
    private Div insertionPointRightPanel;

    private TimeTracker timeTracker;

    private TimeTrackerComponentWithoutColumns timeTrackerComponent;
    private Grid leftPane;
    private TimeTrackedTable<FakeData> table;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        createComponents();
        insertComponentsInLayout();
        timeTrackerComponent.afterCompose();
        table.afterCompose();
    }

    private void createComponents() {
        timeTracker = new TimeTracker(createExampleInterval());
        timeTrackerComponent = new TimeTrackerComponentWithoutColumns(
                timeTracker, "timeTracker");
        TimeTrackedTableWithLeftPane<FakeDataLeft, FakeData> timeTrackedTableWithLeftPane = new TimeTrackedTableWithLeftPane<FakeDataLeft, FakeData>(
                getDataSource(), getColumnsForLeft(), getLeftRenderer(),
                getRightRenderer(), timeTracker);

        table = timeTrackedTableWithLeftPane.getRightPane();
        leftPane = timeTrackedTableWithLeftPane
                .getLeftPane();
    }

    private void insertComponentsInLayout() {
        insertionPointRightPanel.appendChild(table);
        insertionPointLeftPanel.appendChild(leftPane);
        insertionPointTimetracker.appendChild(timeTrackerComponent);
    }

    public void onClick$zoomIncrease() {
        timeTracker.zoomIncrease();
    }

    public void onClick$zoomDecrease() {
        timeTracker.zoomDecrease();
    }

    private ICellForDetailItemRenderer<FakeColumn, FakeDataLeft> getLeftRenderer() {
        return new ICellForDetailItemRenderer<FakeColumn, FakeDataLeft>() {

            @Override
            public Component cellFor(FakeColumn column, FakeDataLeft data) {
                return new Label(column.getName() + data.getRowNumber());
            }
        };
    }

    private List<FakeColumn> getColumnsForLeft() {
        String[] names = { "A", "B", "C" };
        List<FakeColumn> result = new ArrayList<FakeColumn>();
        for (final String columnName : names) {
            result.add(createColumnWithLabel(columnName));
        }
        return result;
    }

    private FakeColumn createColumnWithLabel(final String columnName) {
        return new FakeColumn(columnName);
    }

    private Callable<PairOfLists<FakeDataLeft, FakeData>> getDataSource() {
        return new Callable<PairOfLists<FakeDataLeft, FakeData>>() {

            @Override
            public PairOfLists<FakeDataLeft, FakeData> call() throws Exception {
                List<FakeData> right = new ArrayList<FakeData>();
                List<FakeDataLeft> left = new ArrayList<FakeDataLeft>();
                for (int i = 0; i < 10; i++) {
                    right.add(new FakeData(6));
                    left.add(new FakeDataLeft(i + 1));
                }
                return new PairOfLists<FakeDataLeft, FakeData>(left, right);
            }
        };
    }

    private ICellForDetailItemRenderer<DetailItem, FakeData> getRightRenderer() {
        return new ICellForDetailItemRenderer<DetailItem, FakeData>() {

            @Override
            public Component cellFor(DetailItem item, FakeData data) {
                Label label = new Label();
                label.setValue(data.getHoursForDetailItem(item) + "h");
                return label;
            }
        };
    }

    private Interval createExampleInterval() {
        LocalDate start = new LocalDate(2008, 1, 1);
        LocalDate end = new LocalDate(2009, 1, 1);
        return new Interval(asDate(start), asDate(end));
    }

    private Date asDate(LocalDate start) {
        return start.toDateMidnight().toDate();
    }

}

class FakeColumn implements IConvertibleToColumn {
    private final String columnName;

    FakeColumn(String columnName) {
        this.columnName = columnName;
    }

    @Override
    public Column toColumn() {
        Column column = new org.zkoss.zul.Column();
        column.setLabel(columnName);
        return column;
    }

    public String getName() {
        return columnName;
    }
}

class FakeDataLeft {
    private final int row;

    FakeDataLeft(int row) {
        this.row = row;
    }

    public int getRowNumber() {
        return row;
    }

}

class FakeData {
    private final int hoursPerDay;

    FakeData(int hoursPerDay) {
        this.hoursPerDay = hoursPerDay;
    }

    public int getHoursForDetailItem(DetailItem detail) {
        Days daysBetween = Days.daysBetween(detail.getStartDate(), detail
                .getEndDate());
        return daysBetween.getDays() * hoursPerDay;
    }
}
