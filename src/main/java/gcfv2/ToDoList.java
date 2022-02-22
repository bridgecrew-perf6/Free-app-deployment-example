package gcfv2;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import com.google.gson.Gson;
import com.pengrad.telegrambot.BotUtils;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;

public class ToDoList implements HttpFunction {
    private ToDoListManager manager = null;

    public void service(final HttpRequest request, final HttpResponse response) throws Exception {
        if (manager == null) {
            manager = new ToDoListManager(new Storage());
        }

        proceedRequest(request);
        response.getWriter().write("Success");
    }

    private void proceedRequest(HttpRequest request) throws Exception {
        Update update = BotUtils.parseUpdate(request.getReader());

        if (update.message() != null) {
            if (isCommand(update.message().text())) {
                proceedCommand(update.message());
            } else {
                manager.addItem(update.message());
            }
        } else if (update.callbackQuery() != null) {
            proceedCallbackQuery(update.callbackQuery());
        }
    }

    private boolean isCommand(String text) {
        return text.matches("^\\/((?!\\s)(?!\\/).)+");
    }

    private void proceedCommand(Message message) throws Exception {
        switch (message.text()) {
            case "/start": 
                manager.sendToDoList(message.from().id());
                break;
            // other commands' cases
        }
    }

    private void proceedCallbackQuery(CallbackQuery callbackQuery) throws Exception {
        Data data = new Gson().fromJson(callbackQuery.data(), Data.class);
        switch (data.method) {
            case "add":
                manager.askSendItem(callbackQuery);
                break;
            case "done":
                manager.markItem(callbackQuery, data.item, true);
                break;
            case "undone":
                manager.markItem(callbackQuery, data.item, false);
                break;
            case "delete":
                manager.deleteItem(callbackQuery, data.item);
                break;
        }
    }
}