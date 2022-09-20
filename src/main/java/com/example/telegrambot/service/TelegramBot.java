package com.example.telegrambot.service;
import com.example.telegrambot.Util.Util;
import com.example.telegrambot.config.BotConfig;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;

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

    private  void sendWithoutUrl(Message message){
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        InlineKeyboardButton buttonS = new InlineKeyboardButton();
        InlineKeyboardButton buttonY = new InlineKeyboardButton();

        buttonS.setText("Sasha's activity");
        buttonS.setCallbackData("startSasha");
        buttonY.setText("Yura's activity");
        buttonY.setCallbackData("startYura");

        List<InlineKeyboardButton> buttons = new ArrayList<>();
        buttons.add(buttonS);
        buttons.add(buttonY);

        List <List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(buttons);

        keyboard.setKeyboard(rowList);
        try {
            execute(
                    SendMessage.builder()
                            .chatId(message.getChatId())
                            .parseMode("Markdown")
                            .text("Options:")
                            .replyMarkup(keyboard)
                            .build());
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage() && update.getMessage().hasText()){

            String messageText = update.getMessage().getText();
            Message command = update.getMessage();

            if ("/start".equals(messageText)) {
                startAnswer(command);
            } else {
                defaultAnswer(command);
            }
        }
        else if (update.hasCallbackQuery()){
            if (update.getCallbackQuery().getData().equals("startSasha")){
                sendDocument(update.getCallbackQuery().getMessage().getChatId(), "Sasha's activities", createSashasPDF());
            }else if(update.getCallbackQuery().getData().equals("startYura")){
                sendDocument(update.getCallbackQuery().getMessage().getChatId(), "Yura's's activities", createYurasPDF());
            }
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

    private static InputFile createSashasPDF() throws FileNotFoundException, DocumentException, SQLException, ClassNotFoundException {
        String fileName = //"/home/sashakashinskaya/SashasData.pdf";
                "d:\\java\\andersen\\SashasData.pdf";

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(fileName));
        document.open();

        PdfPTable table = new PdfPTable(2);
        PdfPCell c1 = new PdfPCell(new Phrase("Activity"));
        table.addCell(c1);
        c1 = new PdfPCell(new Phrase("Duration (hour)"));
        table.addCell(c1);
        table.setHeaderRows(1);

        PreparedStatement ps;
        ResultSet rs;
        String query = "SELECT * FROM activities WHERE student_name = 'Sasha' and status = 'valid'";
        ps = Util.getConnection().prepareStatement(query,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
        rs = ps.executeQuery();

        document.add(new Paragraph("Sasha's activity "+ LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yy"))));
        document.add(new Paragraph(" "));

        while (rs.next()) {

                    table.addCell(rs.getString("activity_type"));
                    table.addCell(rs.getString("prolongation"));
                    rs.updateString("status","unvalid");
                    rs.updateRow();

        }
        document.add(table);
        document.close();
        File file = new File(fileName);
        InputFile inputFile = new InputFile(file);

        return inputFile;
    }




    private static InputFile createYurasPDF() throws FileNotFoundException, DocumentException, SQLException, ClassNotFoundException {
        String fileName = //"/home/sashakashinskaya/YurasData.pdf";
                "d:\\java\\andersen\\YurasData.pdf";


        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(fileName));
        document.open();

        PdfPTable table = new PdfPTable(2);
        PdfPCell c1 = new PdfPCell(new Phrase("Activity"));
        table.addCell(c1);
        c1 = new PdfPCell(new Phrase("Duration (hour)"));
        table.addCell(c1);
        table.setHeaderRows(1);

        PreparedStatement ps;
        ResultSet rs;
        String query = "SELECT * FROM activities WHERE student_name = 'Yury' and status = 'valid'";
        ps = Util.getConnection().prepareStatement(query,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
        rs = ps.executeQuery();

        document.add(new Paragraph("Yury's activity " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yy"))));
        document.add(new Paragraph(" "));

        while (rs.next()) {

            table.addCell(rs.getString("activity_type"));
            table.addCell(rs.getString("prolongation"));
            rs.updateString("status","unvalid");
            rs.updateRow();

        }
        document.add(table);
        document.close();
        File file = new File(fileName);
        InputFile inputFile = new InputFile(file);

        return inputFile;
    }


    private void startAnswer(Message command){
        sendWithoutUrl(command);
    }

    private void defaultAnswer(Message command){
        try {
            execute(
                    SendMessage.builder()
                            .chatId(command.getChatId())
                            .parseMode("Markdown")
                            .text("Do not know such command!")
                            .build());
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}


