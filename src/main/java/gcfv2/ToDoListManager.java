package gcfv2;

import com.google.gson.Gson;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.EditMessageReplyMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;

import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ToDoListManager {
    private final Storage dbConnect;
    private final TelegramBot bot = new TelegramBot(System.getenv("BOT_TOKEN"));

    public ToDoListManager(Storage dbConnect) {
        this.dbConnect = dbConnect;
    }

    public void addItem(Message message) throws Exception {
        deleteMessage(message.from().id(), message.messageId());
        deleteMessage(message.from().id(), message.messageId() - 1);
        dbConnect.addItem(message.from().id().toString(), message.text());
        SendResponse resp = sendToDoList(message.from().id());
    }

    public SendResponse sendToDoList(Long userId) throws ExecutionException, InterruptedException {
        InlineKeyboardMarkup inlineKeyboard = getToDoList(userId);
       return bot.execute(new SendMessage(userId, "\uD83D\uDDD2 *Your ToDo list*:").parseMode(ParseMode.Markdown).replyMarkup(inlineKeyboard));
    }

    private void updateToDoList(Long userId, int messageId) throws ExecutionException, InterruptedException {
        InlineKeyboardMarkup inlineKeyboard = getToDoList(userId);
        bot.execute(new EditMessageReplyMarkup(userId, messageId).replyMarkup(inlineKeyboard));
    }

    private InlineKeyboardMarkup getToDoList(Long userId) throws ExecutionException, InterruptedException {
        Map<String, Boolean> toDoList = dbConnect.getItems(userId.toString());
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        toDoList.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> inlineKeyboard.addRow(
                createButton(entry.getValue()? "\u2705" : "\u2B1C", new Data(entry.getValue()? "undone": "done", entry.getKey())),
                createButton(entry.getKey(), new Data(entry.getValue()? "undone": "done", entry.getKey())),
                createButton("\u274C", new Data("delete", entry.getKey()))));

        return inlineKeyboard.addRow(createButton("Add an item", new Data("add", "")));
    }

   public void askSendItem(CallbackQuery callbackQuery) {
        deleteMessage(callbackQuery.from().id(), callbackQuery.message().messageId());
        bot.execute(new SendMessage(callbackQuery.from().id(), "Please send an item name"));
    }

    public void deleteItem(CallbackQuery callbackQuery, String item) throws ExecutionException, InterruptedException {
        Long userId = callbackQuery.from().id();
        dbConnect.deleteItem(userId.toString(), item);
        updateToDoList(userId, callbackQuery.message().messageId());
    }

    public void markItem(CallbackQuery callbackQuery, String item, boolean isDone) throws ExecutionException, InterruptedException {
        Long userId = callbackQuery.from().id();
        dbConnect.markItem(userId.toString(), item, isDone);
        updateToDoList(userId, callbackQuery.message().messageId());
    }

    private InlineKeyboardButton createButton(String text, Data callbackData) {
        return new InlineKeyboardButton(text).callbackData(new Gson().toJson(callbackData));
    }

    private void deleteMessage(Long userId, int messageId) {
        bot.execute(new DeleteMessage(userId, messageId));
    }
}
