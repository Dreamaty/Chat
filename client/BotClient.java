package com.javarush.task.task30.task3008.client;

import com.javarush.task.task30.task3008.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class BotClient extends Client {

    public static void main(String[] args) {
        BotClient botClient = new BotClient();
        botClient.run();
    }

    @Override
    protected String getUserName() {
        return "date_bot_" + (int) (Math.random() * 100);
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    public class BotSocketThread extends SocketThread {

        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Hiiii chat. I'm bot. My commands: date, day, month, year, time, hour, minutes, seconds.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
            if (message.split(":").length == 2) {
                SimpleDateFormat dateFormat;
                switch (message.substring(message.indexOf(":") + 2)) {
                    case "date":
                        dateFormat = new SimpleDateFormat("d.MM.YYYY");
                        break;
                    case "day":
                        dateFormat = new SimpleDateFormat("d");
                        break;
                    case "month":
                        dateFormat = new SimpleDateFormat("MMMM");
                        break;
                    case "year":
                        dateFormat = new SimpleDateFormat("YYYY");
                        break;
                    case "time":
                        dateFormat = new SimpleDateFormat("H:mm:ss");
                        break;
                    case "Hour":
                        dateFormat = new SimpleDateFormat("H");
                        break;
                    case "minutes":
                        dateFormat = new SimpleDateFormat("m");
                        break;
                    case "seconds":
                        dateFormat = new SimpleDateFormat("s");
                        break;
                    default:
                        dateFormat = null;
                }
                if (dateFormat != null)
                    sendTextMessage("Information for " + message.substring(0, message.indexOf(':')) + ": " + dateFormat.format(Calendar.getInstance().getTime()));
            }
        }
    }
}
