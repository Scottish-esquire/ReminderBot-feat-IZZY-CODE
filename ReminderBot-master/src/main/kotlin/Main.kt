import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.UpdatesListener
import com.pengrad.telegrambot.request.SendMessage
//import org.telegram.telegrambots.meta.bots.AbsSender;

import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toLocalTime
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*
import java.time.LocalDateTime
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

private const val TOKEN = "6269432954:AAGLvy6tfgCRBi2RjL8SvpGeSCMdKiZTibw"


object Main {

    private var bot = TelegramBot(TOKEN)
    var chatId: Long? = null


    @JvmStatic
    fun main(args: Array<String>) {
        startBotListener()
    }

    private fun checkNotification() {
            val currentTime = LocalDateTime.now()
            if (currentTime != null) {
                val message = currentTime.toString()
                bot.execute(SendMessage(chatId, message))
            }

    }
//    val scheduler = Executors.newSingleThreadScheduledExecutor()
    val timer = Timer("schedule", true);

//    В следующей строке ошибка, но она важна!!!
    // timer.scheduleAtFixedRate( { checkNotification() }, 0, 1, TimeUnit.MINUTES)

    private fun startBotListener() {

        var doINeedTextFromUser: Boolean = false
        var doINeedNotification: Boolean = false
        var date: String = ""
        var time: String = ""

        bot.setUpdatesListener({
            it.forEach {

                val text = it.message().text()
                chatId = it.message().chat().id()


                if (!doINeedTextFromUser) {
                    when (text) {

                        "/start" -> {
                            bot.execute(
                                SendMessage(
                                    chatId,
                                    "Привет. Чтобы сделать напоминание напиши /newnotification"
                                )
                            )
                        }

                        "/newnotification" -> {
                            bot.execute(SendMessage(chatId, "Напишите дату в формате 01.01.2020"))
                            doINeedTextFromUser = true
                            date = ""
                            time = ""

                        }

                        else ->
                            bot.execute(SendMessage(chatId, "Не понимаю тебя. Введи /start"))
                    }
                }

                else if (doINeedTextFromUser && doINeedNotification) {
                    try {
                        var formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
                        var stringToParse = text + ":00"
                        time = LocalTime.parse(stringToParse, formatter).format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                        doINeedTextFromUser = false
                        doINeedNotification = false
                    }

                    catch (ex: Exception) {
                        bot.execute(SendMessage(chatId, "Напишите существующее время в формате 12:59 (без секунд)"))
                    }


                    try {
                        var formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
                        var notification = LocalDateTime.parse(date + " " + time, formatter)
                        var dateWithTime =notification.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")).toString()
                        bot.execute(SendMessage(chatId, dateWithTime))
                    }

                    catch (ex: Exception) {
                        bot.execute(SendMessage(chatId, "Ошибка"))
                    }
                }

                else if (doINeedTextFromUser && !doINeedNotification) {
                    try {
                        var formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                        date = LocalDate.parse(text, formatter).format(DateTimeFormatter.ofPattern("dd.MM.yyyy")).toString()
                        bot.execute(SendMessage(chatId, "Напишите время в формате 12:59 (без секунд)"))
                        doINeedNotification = true
                    }

                    catch (ex: Exception) {
                        bot.execute(SendMessage(chatId, "Напишите существующую дату в формате 01.01.2020"))
                    }
                }

                println()
            }

            UpdatesListener.CONFIRMED_UPDATES_ALL
        },{

            bot.removeGetUpdatesListener()

            bot = TelegramBot(TOKEN)
            startBotListener()
        })
    }
}
