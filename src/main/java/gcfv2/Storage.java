package gcfv2;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class Storage {
    private final Firestore connection;

    public Storage() throws IOException {
        FirestoreOptions firestoreOptions =
                FirestoreOptions.getDefaultInstance().toBuilder()
                        .setProjectId(System.getenv("PROJECT_ID"))
                        .setCredentials(GoogleCredentials.getApplicationDefault())
                        .build();
        connection = firestoreOptions.getService();
    }

    public void addItem(String userId, String item) throws ExecutionException, InterruptedException {
        DocumentReference docRef = connection.collection("todolist").document(userId);
        Map<String, Object> toDoList = new HashMap<>();
        toDoList.put(item, false);
        docRef.set(toDoList, SetOptions.merge()).get();
    }

    public void markItem(String userId, String item, boolean isDone) throws ExecutionException, InterruptedException {
        DocumentReference docRef = connection.collection("todolist").document(userId);
        Map<String, Object> toDoList = new HashMap<>();
        toDoList.put(item, isDone);
        docRef.update(toDoList).get();
    }

    public void deleteItem(String userId, String item) throws ExecutionException, InterruptedException {
        DocumentReference docRef = connection.collection("todolist").document(userId);
        Map<String, Object> toDoList = new HashMap<>();
        toDoList.put(item, FieldValue.delete());
        docRef.update(toDoList).get();
    }

    public Map<String, Boolean> getItems(String userId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = connection.collection("todolist").document(userId);
        DocumentSnapshot snapshot = docRef.get().get();
        if (snapshot.exists()) {
            Map<String, Object> data = snapshot.getData();
            return Objects.requireNonNull(data).entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> (boolean)entry.getValue()));
        } else {
            return Map.of();
        }
    }
}
