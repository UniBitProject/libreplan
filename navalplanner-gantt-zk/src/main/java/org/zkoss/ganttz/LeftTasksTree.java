package org.zkoss.ganttz;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zkoss.ganttz.LeftTasksTreeRow.ILeftTasksTreeNavigator;
import org.zkoss.ganttz.data.Task;
import org.zkoss.ganttz.data.TaskContainer;
import org.zkoss.ganttz.util.MutableTreeModel;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.OpenEvent;
import org.zkoss.zul.Tree;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;

public class LeftTasksTree extends HtmlMacroComponent {

    private final class TaskBeanRenderer implements TreeitemRenderer {
        public void render(Treeitem item, Object data) throws Exception {
            Task task = (Task) data;
            item.setOpen(isOpened(task));
            final int[] path = tasksTreeModel.getPath(tasksTreeModel.getRoot(),
                    task);
            String cssClass = "depth_" + path.length;
            LeftTasksTreeRow leftTasksTreeRow = LeftTasksTreeRow.create(task,
                    new TreeNavigator(tasksTreeModel, task));
            if (task.isContainer()) {
                expandWhenOpened((TaskContainer) task, item);
            }
            Component row = Executions.getCurrent().createComponents(
                    "~./ganttz/zul/leftTasksTreeRow.zul", item, null);
            leftTasksTreeRow.doAfterCompose(row);
            List<Object> rowChildren = row.getChildren();
            List<Treecell> treeCells = Planner.findComponentsOfType(
                    Treecell.class, rowChildren);
            for (Treecell cell : treeCells) {
                cell.setSclass(cssClass);
            }
            detailsForBeans.put(task, leftTasksTreeRow);
            deferredFiller.isBeingRendered(task, item);
        }

        private void expandWhenOpened(final TaskContainer taskBean,
                Treeitem item) {
            item.addEventListener("onOpen", new EventListener() {
                @Override
                public void onEvent(Event event) throws Exception {
                    OpenEvent openEvent = (OpenEvent) event;
                    taskBean.setExpanded(openEvent.isOpen());
                }
            });
        }

    }

    public boolean isOpened(Task task) {
        return task.isLeaf() || task.isExpanded();
    }

    private final class DetailsForBeans {
        private Map<Task, LeftTasksTreeRow> map = new HashMap<Task, LeftTasksTreeRow>();

        private Set<Task> focusRequested = new HashSet<Task>();

        public void put(Task task, LeftTasksTreeRow leftTasksTreeRow) {
            map.put(task, leftTasksTreeRow);
            if (focusRequested.contains(task)) {
                focusRequested.remove(task);
                leftTasksTreeRow.receiveFocus();
            }
        }

        public void requestFocusFor(Task task) {
            focusRequested.add(task);
        }

        public LeftTasksTreeRow get(Task taskbean) {
            return map.get(taskbean);
        }

    }

    private DetailsForBeans detailsForBeans = new DetailsForBeans();

    private final class TreeNavigator implements ILeftTasksTreeNavigator {
        private final int[] pathToNode;
        private final Task task;

        private TreeNavigator(TreeModel treemodel, Task task) {
            this.task = task;
            this.pathToNode = tasksTreeModel.getPath(tasksTreeModel.getRoot(),
                    task);
        }

        @Override
        public LeftTasksTreeRow getAboveRow() {
            Task parent = getParent(pathToNode);
            int lastPosition = pathToNode[pathToNode.length - 1];
            if (lastPosition != 0) {
                return getChild(parent, lastPosition - 1);
            } else if (tasksTreeModel.getRoot() != parent) {
                return getDetailFor(parent);
            }
            return null;
        }

        private LeftTasksTreeRow getChild(Task parent, int position) {
            Task child = tasksTreeModel.getChild(parent, position);
            return getDetailFor(child);
        }

        private LeftTasksTreeRow getDetailFor(Task child) {
            return detailsForBeans.get(child);
        }

        @Override
        public LeftTasksTreeRow getBelowRow() {
            if (isExpanded() && hasChildren()) {
                return getChild(task, 0);
            }
            for (ChildAndParent childAndParent : group(task, tasksTreeModel
                    .getParents(task))) {
                if (childAndParent.childIsNotLast()) {
                    return getDetailFor(childAndParent.getNextToChild());
                }
            }
            // it's the last one, it has none below
            return null;
        }

        public List<ChildAndParent> group(Task origin, List<Task> parents) {
            ArrayList<ChildAndParent> result = new ArrayList<ChildAndParent>();
            Task child = origin;
            Task parent;
            ListIterator<Task> listIterator = parents.listIterator();
            while (listIterator.hasNext()) {
                parent = listIterator.next();
                result.add(new ChildAndParent(child, parent));
                child = parent;
            }
            return result;
        }

        private class ChildAndParent {
            private final Task parent;

            private final Task child;

            private Integer positionOfChildCached;

            private ChildAndParent(Task child, Task parent) {
                this.parent = parent;
                this.child = child;
            }

            public Task getNextToChild() {
                return tasksTreeModel
                        .getChild(parent, getPositionOfChild() + 1);
            }

            public boolean childIsNotLast() {
                return getPositionOfChild() < numberOfChildrenForParent() - 1;
            }

            private int numberOfChildrenForParent() {
                return tasksTreeModel.getChildCount(parent);
            }

            private int getPositionOfChild() {
                if (positionOfChildCached != null)
                    return positionOfChildCached;
                int[] path = tasksTreeModel.getPath(parent, child);
                return positionOfChildCached = path[path.length - 1];
            }
        }

        private boolean hasChildren() {
            return task.isContainer() && task.getTasks().size() > 0;
        }

        private boolean isExpanded() {
            return task.isContainer() && task.isExpanded();
        }

        private Task getParent(int[] path) {
            Task current = tasksTreeModel.getRoot();
            for (int i = 0; i < path.length - 1; i++) {
                current = tasksTreeModel.getChild(current, path[i]);
            }
            return current;
        }

    }

    /**
     * This class is a workaround for an issue with zk {@link Tree}. Once the
     * tree is created, a node with more children can't be added. Only the top
     * element is added to the tree, although the element has children. The Tree
     * discards the adding event for the children because the parent says it's
     * not loaded. This is the condition that is not satisfied:<br />
     * <code>if(parent != null &&
        (!(parent instanceof Treeitem) || ((Treeitem)parent).isLoaded())){</code><br />
     * This problem is present in zk 3.6.1 at least.
     * @author Óscar González Fernández <ogonzalez@igalia.com>
     * @see Tree#onTreeDataChange
     */
    private class DeferredFiller {

        private Set<Task> pendingToAddChildren = new HashSet<Task>();

        public void addParentOfPendingToAdd(Task parent) {
            pendingToAddChildren.add(parent);
        }

        public void isBeingRendered(final Task parent, final Treeitem item) {
            if (!pendingToAddChildren.contains(parent))
                return;
            markLoaded(item);
            fillModel(parent, parent.getTasks(), false);
            pendingToAddChildren.remove(parent);
        }

        private void markLoaded(Treeitem item) {
            try {
                Method method = getSetLoadedMethod();
                method.invoke(item, true);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        private Method setLoadedMethod = null;

        private Method getSetLoadedMethod() {
            if (setLoadedMethod != null)
                return setLoadedMethod;
            try {
                Method method = Treeitem.class.getDeclaredMethod("setLoaded",
                        Boolean.TYPE);
                method.setAccessible(true);
                return setLoadedMethod = method;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static Log LOG = LogFactory.getLog(LeftTasksTree.class);

    private final DeferredFiller deferredFiller = new DeferredFiller();

    private final List<Task> tasks;

    private MutableTreeModel<Task> tasksTreeModel;

    private Tree tasksTree;

    private CommandContextualized<?> goingDownInLastArrowCommand;

    public LeftTasksTree(List<Task> tasks) {
        this.tasks = tasks;
    }

    private void fillModel(List<Task> tasks, boolean firstTime) {
        fillModel(this.tasksTreeModel.getRoot(), tasks, firstTime);
    }

    private void fillModel(Task parent, List<Task> children,
            final boolean firstTime) {
        for (Task node : children) {
            if (firstTime) {
                this.tasksTreeModel.add(parent, node);
                if (node.isContainer()) {
                    fillModel(node, node.getTasks(), firstTime);
                }
            } else {
                if (node.isContainer()) {
                    this.deferredFiller.addParentOfPendingToAdd(node);
                }
                // the node must be added after, so the multistepTreeFiller is
                // ready
                this.tasksTreeModel.add(parent, node);
            }
        }
    }

    Planner getPlanner() {
        return (Planner) getParent();
    }

    public void taskRemoved(Task taskRemoved) {
        tasksTreeModel.remove(taskRemoved);
    }

    private static Date threeMonthsLater(Date now) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MONTH, 3);
        return calendar.getTime();
    }

    @Override
    public void afterCompose() {
        setClass("listdetails");
        super.afterCompose();
        tasksTree = (Tree) getFellow("tasksTree");
        tasksTreeModel = MutableTreeModel.create(Task.class);
        fillModel(tasks, true);
        tasksTree.setModel(tasksTreeModel);
        tasksTree.setTreeitemRenderer(new TaskBeanRenderer());
    }

    void addTask(Task task) {
        fillModel(Arrays.asList(task), false);
        detailsForBeans.requestFocusFor(task);
    }

    public CommandContextualized<?> getGoingDownInLastArrowCommand() {
        return goingDownInLastArrowCommand;
    }

    public void setGoingDownInLastArrowCommand(
            CommandContextualized<?> goingDownInLastArrowCommand) {
        this.goingDownInLastArrowCommand = goingDownInLastArrowCommand;
    }

}
