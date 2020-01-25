import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Duke {
    public static void main(String[] args) {
        String logo = " ____        _        \n"
                + "|  _ \\ _   _| | _____ \n"
                + "| | | | | | | |/ / _ \\\n"
                + "| |_| | |_| |   <  __/\n"
                + "|____/ \\__,_|_|\\_\\___|\n";

        // Start the duke program
        System.out.println("Hello from\n" + logo);
        System.out.println(
                "    ____________________________________________________________\n" +
                "     Hey friend! I'm Duke V2.0.0 at your service\n" +
                "     What can I do for you today?\n" +
                "    ____________________________________________________________\n"
        );

        BufferedReader bufferedReader = new BufferedReader (new InputStreamReader(System.in));
        String input;
        List<Task> tasks = getListFromFile();

        // Parse each command by user
        while (true) {
            // Get command entered by user
            try {
                input = bufferedReader.readLine();
            } catch (IOException e) {
                System.out.println("Error in reading input");
                break;
            }

            String typeOfCommand = input.split(" ")[0];
            // If user enters "bye"
            if (typeOfCommand.equals("bye")) {
                // Exit program
                prettyPrint("Sorry to see you go. Hope to see you again soon! (＾▽＾)／");
                return;
            }

            // Execute command entered by user
            try {
                tasks = executeCommand(input, tasks);
            } catch (DukeException e) {
                prettyPrint(e.getMessage());
            }
        }
    }

    /**
     * Run the duke program, parse each command entered by user
     * and execute the command
     * @param  input   the input by the user
     * @param  tasks   the list of all tasks
     * @return a list of updated tasks
     */
    public static List<Task> executeCommand(String input, List<Task> tasks) throws DukeException {
        String[] inputTokens = input.split(" ");
        switch (inputTokens[0]) {
            case "list":
                String dateString = (inputTokens.length > 1) ? inputTokens[1] : "";
                List<Task> filteredTasks = filterTasksByDate(tasks, dateString);
                prettyPrintList(filteredTasks);
                break;
            case "done":
                // Mark the task with given index as done
                int doneIndex;
                try {
                    doneIndex = Integer.parseInt(inputTokens[1]) - 1;
                } catch (Exception e) {
                    throw new DukeException("☹ OOPS!!! No such task index!");
                }
                if (doneIndex < tasks.size()) {
                    Task task = tasks.get(doneIndex);
                    task.markAsDone(true);
                    if (!writeListToFile(tasks)) {
                        throw new DukeException("☹ OOPS!!! Failed to save list!");
                    }
                    prettyPrint("Nice! I've marked this task as done: \n" +
                            "       " + task);
                } else {
                    throw new DukeException("☹ OOPS!!! No such task index!");
                }
                break;
            case "todo":
                // Add a To-do task
                if (inputTokens.length > 1) {
                    Task newTodoTask = new Todo(input.replaceFirst("^todo ", ""));
                    tasks.add(newTodoTask);
                    printAddTask(newTodoTask, tasks.size());
                } else {
                    throw new DukeException("☹ OOPS!!! The description of a todo cannot be empty.");
                }
                break;
            case "deadline":
                // Add a Deadline task
                String[] deadlineTokens = input.split(" /by ");
                // If there is a description and a deadline
                if (deadlineTokens.length > 1) {
                    String dateOrTime = deadlineTokens[1];
                    String description = deadlineTokens[0].replaceFirst("^deadline ", "");
                    Task newDeadlineTask = new Deadline(description, dateOrTime);
                    tasks.add(newDeadlineTask);
                    printAddTask(newDeadlineTask, tasks.size());
                } else {
                    throw new DukeException("☹ OOPS!!! Deadline tasks require a specific time or date.");
                }
                break;
            case "event":
                String[] eventTokens = input.split(" /at ");
                // If there is a description and a time/date
                if (eventTokens.length > 1) {
                    String dateOrTime = eventTokens[1];
                    String description = eventTokens[0].replaceFirst("^event ", "");
                    Task newEventTask = new Event(description, dateOrTime);
                    tasks.add(newEventTask);
                    printAddTask(newEventTask, tasks.size());
                } else {
                    throw new DukeException("☹ OOPS!!! Event tasks require a specific time and date.");
                }
                break;
            case "delete":
                int deleteIndex;
                try {
                    deleteIndex = Integer.parseInt(inputTokens[1]) - 1;
                } catch (Exception e) {
                    throw new DukeException("☹ OOPS!!! No such task index!");
                }
                if (deleteIndex < tasks.size()) {
                    Task deleteTask = tasks.get(deleteIndex);
                    tasks.remove(deleteIndex);
                    printDeleteTask(deleteTask, tasks.size());
                    if (!writeListToFile(tasks)) {
                        throw new DukeException("☹ OOPS!!! Failed to save list!");
                    }
                } else {
                    throw new DukeException("☹ OOPS!!! No such task index!");
                }
                break;
            default:
                // Cannot parse command, throw exception
                throw new DukeException("☹ OOPS!!! I'm sorry, but I don't know what that means :-(");
        }
        return tasks;
    }

    /**
     * Format the given line into a pretty format and print it
     *
     * @param  line   the line to be formatted
     */
    public static void prettyPrint(String line) {
        System.out.println(
                "    _____________________________DUKE___________________________\n" +
                "     " + line + "\n" +
                "    _________★゜・。。・゜゜・。。・゜☆゜・。。・゜゜・。。・゜★_______\n"
        );
    }

    public static void prettyPrintList(List<Task> tasks) {
        String tasksString = "";
        int size = tasks.size();

        // Print out all items in list
        if (size > 0) {
            tasksString += "Here are the tasks in your list:\n     ";
        }
        for (int i = 0; i < size; i++) {
            tasksString += (i + 1) + "." + tasks.get(i);
            if (i != size - 1) {
                tasksString += "\n     ";
            }
        }
        tasksString = tasksString.equals("") ? "There is nothing on your list." : tasksString;
        prettyPrint(tasksString);
    }

    /**
     * Format the task added into a pretty format and print it
     *
     * @param  task   the task added
     * @param  size   the total number of tasks
     */
    public static void printAddTask(Task task, Integer size) {
        String taskWord = (size > 1) ? "tasks" : "task";
        prettyPrint("Got it. I've added this task: \n" +
                "       " + task + "\n"+
                "     Now you have " + size + " " + taskWord + " in the list.");
    }

    /**
     * Format the task deleted into a pretty format and print it
     *
     * @param  task   the task deleted
     * @param  size   the total number of tasks
     */
    public static void printDeleteTask(Task task, Integer size) {
        String taskWord = (size > 1) ? "tasks" : "task";
        prettyPrint("Noted. I've removed this task: \n" +
                "       " + task + "\n" +
                "     Now you have " + size + " " + taskWord + " in the list.");
    }
}
