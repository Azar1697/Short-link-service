import java.awt.Desktop;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.*;

public class ShortLinkService {

    private static final Map<String, User> users = new HashMap<>();
    private static final Properties config = new Properties();

    static {
        try (InputStream input = new FileInputStream("config.properties")) {
            config.load(input);
        } catch (IOException e) {
            System.err.println("Error loading configuration: " + e.getMessage());
        }
    }

    private static final int DEFAULT_LIFETIME_HOURS = Integer.parseInt(config.getProperty("defaultLifetimeHours", "24"));
    private static final int DEFAULT_MAX_TRANSITIONS = Integer.parseInt(config.getProperty("defaultMaxTransitions", "10"));

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Приветствуем тебя, в моем Сервисе коротких ссылок!");

        while (true) {
            System.out.println("Выберите, что хотите сделать: 1 - Зарегистрироваться, 2 - Сократить ссылку, 3 - Отредактировать, 4 - Удалить, 5 - Посмотреть ссылку, 0 - Выйти");
            String option = scanner.nextLine().toLowerCase();

            switch (option) {
                case "1":
                    System.out.print("Введите свой ник/имя и получите свой уникальный UUID: ");
                    String username = scanner.nextLine();
                    User user = registerUser(username);
                    System.out.println("Поздравляю, ты зарегистрировался! Сохрани и не забудь свой UUID: " + user.getUuid());
                    break;

                case "2":
                    System.out.print("Введите свой UUID: ");
                    String uuid = scanner.nextLine();
                    user = users.get(uuid);
                    if (user == null) {
                        System.out.println("Не правильный UUID.((");
                        break;
                    }

                    System.out.print("Введи свою ссылку: ");
                    String longUrl = scanner.nextLine();

                    System.out.print("Введите максимальное количество переходов или нажмите Enter по умолчанию(2 раза ссылка открывается, действует 1 час).: ");
                    String maxTransitionsInput = scanner.nextLine();
                    int maxTransitions = maxTransitionsInput.isEmpty() ? DEFAULT_MAX_TRANSITIONS : Integer.parseInt(maxTransitionsInput);

                    String shortUrl = user.createShortLink(longUrl, maxTransitions, DEFAULT_LIFETIME_HOURS);
                    System.out.println("Твоя короткая ссылка: " + shortUrl);
                    break;

                case "3":
                    System.out.print("Введите свой UUID: ");
                    uuid = scanner.nextLine();
                    user = users.get(uuid);
                    if (user == null) {
                        System.out.println("Не правильный UUID((");
                        break;
                    }

                    System.out.print("Введи свою короткую ссылку для дальнейшего изменения: ");
                    String shortUrlToEdit = scanner.nextLine();

                    System.out.print("Введите новое количество возможных переходов по ссылку: ");
                    int newMaxTransitions = Integer.parseInt(scanner.nextLine());

                    System.out.print("Введите время в часах, которое будет храниться ссылка: ");
                    int newLifetime = Integer.parseInt(scanner.nextLine());

                    boolean edited = user.editShortLink(shortUrlToEdit, newMaxTransitions, newLifetime);
                    if (edited) {
                        System.out.println("Твоя короткая ссылка успешно обнавлена, поздравляю!).");
                    } else {
                        System.out.println("Что-то пошло не так((.");
                    }
                    break;

                case "4":
                    System.out.print("Введите свой UUID: ");
                    uuid = scanner.nextLine();
                    user = users.get(uuid);
                    if (user == null) {
                        System.out.println("Не правильный UUID((");
                        break;
                    }

                    System.out.print("Введи свою короткую ссылку для удаления: ");
                    String shortUrlToDelete = scanner.nextLine();

                    boolean deleted = user.deleteShortLink(shortUrlToDelete);
                    if (deleted) {
                        System.out.println("Короткая ссылочка удалена).");
                    } else {
                        System.out.println("Что-то не получилось.");
                    }
                    break;

                case "5":
                    System.out.print("Введи свою короткую ссылку для просмотра: ");
                    String shortUrlToBrowse = scanner.nextLine();
                    browse(shortUrlToBrowse);
                    break;

                case "0":
                    System.out.println("До новых встреч, возвращайся скорее)!");
                    return;

                default:
                    System.out.println("Ты ввел что-то не то, попробуй еще раз.");
            }
        }
    }

    private static User registerUser(String username) {
        User user = new User(username);
        users.put(user.getUuid(), user);
        return user;
    }

    public static String generateShortLink(String longUrl) {
        try {
            URL url = new URL("https://clck.ru/--");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Accept", "text/plain");

            String requestData = "url=" + longUrl;
            try (OutputStream os = connection.getOutputStream()) {
                os.write(requestData.getBytes());
                os.flush();
            }


            try (Scanner scanner = new Scanner(connection.getInputStream())) {
                if (scanner.hasNext()) {
                    return scanner.nextLine();
                }
            }
        } catch (Exception e) {
            System.err.println("Что-то пошло не так: " + e.getMessage());
        }
        return null;
    }

    private static void browse(String shortUrl) {
        for (User user : users.values()) {
            String originalUrl = user.getOriginalUrl(shortUrl);
            if (originalUrl != null) {
                try {
                    Desktop.getDesktop().browse(new URI(originalUrl));
                    System.out.println("Перенаправление: " + originalUrl);
                    return;
                } catch (Exception e) {
                    System.out.println("Почему-то не удалось посмотреть URL-адресс: " + e.getMessage());
                }
            }
        }
        System.out.println("Короткая ссылка не найдена или ее срок действия истек.");
    }
}
