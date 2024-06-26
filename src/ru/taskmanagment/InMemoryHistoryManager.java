package ru.taskmanagment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {
    static Map<String, Node<Task>> history;

    public InMemoryHistoryManager() {
        history = new HashMap<>();
    }

    private Node<Task> head;
    private Node<Task> tail;
    private int size = 0;

    public void addTask(Task data) {
        final Node<Task> oldTail = tail;
        final Node<Task> newTail = new Node<>(oldTail, data, null);
        tail = newTail;
        if (oldTail == null)
            head = newTail;
        else
            oldTail.next = newTail;
        size++;
    }

    public void removeTask(Node<Task> erasedNode) {
        final Node<Task> next = erasedNode.next;
        final Node<Task> prev = erasedNode.prev;
        if (prev == null) {
            head = next;
        } else {
            prev.next = next;
            erasedNode.prev = null;
        }
        if (next == null) {
            tail = prev;
        } else {
            next.prev = prev;
            erasedNode.next = null;
        }
        erasedNode.data = null;
        size--;
    }

    public void removeAllHistory() {
        head = null;
        tail = null;
        history.clear();
    }

    @Override
    public void add(String id, Task task) {
        if (history.containsKey(id)) {
            remove(id);
        }
        addTask(task);
        Node<Task> tmpNode = tail;
        history.put(id, tmpNode);
    }

    @Override
    public ArrayList<Task> getHistory() {
        Node<Task> currentNode = head;
        ArrayList<Task> workArray = new ArrayList<>();
        while (currentNode != null) {
            workArray.add(currentNode.data);
            currentNode = currentNode.next;
        }
        return workArray;
    }

    @Override
    public void remove(String id) {
        if (history.containsKey(id)) {
            Node<Task> tmpNode = history.get(id);
            removeTask(tmpNode);
            history.remove(id);
        }
    }
}
