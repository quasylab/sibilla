package it.unicam.quasylab.sibilla.core.runtime.synthesis;

public enum TaskName {
    COMPARATIVE, OPTIMAL_FEASIBILITY;

    public static TaskName getName(String name) {
        // Convert the input to uppercase and replace camelCase with underscores
        String formattedName = name.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase();

        try {
            return TaskName.valueOf(formattedName);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid task name: " + name);
        }
    }
}
