package com.example.telegrambot.service;


import com.example.telegrambot.Util.Util;
import com.example.telegrambot.config.BotConfig;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;

    public TelegramBot(BotConfig config) {
        this.config = config;
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (messageText) {
                case "/start":
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "/data":
                    sendDocument(chatId, "Users list", getFile());
                    break;
                default:
                    sendMessage(chatId, "Sorry, not supported");
                    break;
            }
        }

    }

    private void startCommandReceived(long chatId, String name) {

        String answer = "Hi, " + name + ", nice to meet you!";

        sendMessage(chatId, answer);

    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        try {
            execute(message);
        } catch (TelegramApiException e) {

        }
    }

    @SneakyThrows
    public void sendDocument(long chatId, String caption, InputFile sendfile) {
        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(chatId);
        sendDocument.setCaption(caption);
        sendDocument.setDocument(sendfile);
        execute(sendDocument);
    }

    @SneakyThrows
    public InputFile getFile() throws FileNotFoundException {
        File file = ResourceUtils.getFile("classpath:static/data.txt");
        InputFile inputFile = new InputFile(file);

        return inputFile;
    }

    private static InputFile createPDF() throws FileNotFoundException, DocumentException, SQLException, ClassNotFoundException {
        String fileName = "D:\\Java\\Andersen\\data.pdf";

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(fileName));
        document.open();

        PreparedStatement ps = null;
        ResultSet rs = null;
        String query = "SELECT * FROM users";
        ps = Util.getConnection().prepareStatement(query);
        rs = ps.executeQuery();

        document.add(new Paragraph("Users List"));
        document.add(new Paragraph(" "));

        while (rs.next()) {
            Paragraph paragraph = new Paragraph(
                    rs.getString("id") + " " +
                            rs.getString("first_name") + " " +
                            rs.getString("last_name") + " " +
                            rs.getString("age")
            );
            document.add(paragraph);
            document.add(new Paragraph(" "));
        }

        document.close();
        File file = new File(fileName);
        InputFile inputFile = new InputFile(file);

        return inputFile;

    }

//    public static void main(String[] args) throws SQLException, DocumentException, FileNotFoundException, ClassNotFoundException {
//        createPDF();
//    }
}
