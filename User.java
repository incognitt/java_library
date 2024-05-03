import java.util.List;
import java.util.ArrayList;
import java.sql.*;

public class User {
    private int userId;
    private String lastName;
    private String firstName;
    private int birthYear;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    // Конструктор без параметров
    public User() {
        this.lastName = null;
        this.firstName = null;
        this.birthYear = 0;
    }

    public User(String lastName, String firstName, int birthYear) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.birthYear = birthYear;
    }

    public boolean issueBook(String url, String user, String password, String bookTitle, String borrowerFirstName, String borrowerLastName) {
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            // Проверяем, есть ли такая книга в наличии
            String sql = "SELECT * FROM books WHERE title = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, bookTitle);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                // Если книга есть в наличии, передаем все данные в конструктор Book
                String author = resultSet.getString("author");
                int year = resultSet.getInt("year");
                int quantity = resultSet.getInt("quantity");
                Book book = new Book(bookTitle, author, year, quantity, borrowerFirstName, borrowerLastName);

                // Передаем информацию о пользователе и переносим книгу в таблицу borrowed_books
                book.moveBookToBorrowed(url, user, password, borrowerFirstName, borrowerLastName);

                return true;
            } else {
                // Если книга отсутствует, выводим сообщение об ошибке
                System.out.println("Book '" + bookTitle + "' not found or not available.");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error issuing book to user!");
            e.printStackTrace();
            return false;
        }
    }



    public boolean returnBook(String url, String user, String password, String bookTitle) {
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            // Получаем информацию о книге из таблицы borrowed_books
            String selectSql = "SELECT * FROM borrowed_books WHERE book_title = ?";
            PreparedStatement selectStatement = connection.prepareStatement(selectSql);
            selectStatement.setString(1, bookTitle);
            ResultSet resultSet = selectStatement.executeQuery();

            if (resultSet.next()) {
                // Получаем данные о книге
                String author = resultSet.getString("author");
                int year = resultSet.getInt("year");
                int quantity = resultSet.getInt("quantity");

                // Вставляем данные в таблицу books
                String insertSql = "INSERT INTO books (title, author, year, quantity) VALUES (?, ?, ?, ?)";
                PreparedStatement insertStatement = connection.prepareStatement(insertSql);
                insertStatement.setString(1, bookTitle);
                insertStatement.setString(2, author);
                insertStatement.setInt(3, year);
                insertStatement.setInt(4, quantity);
                int rowsInserted = insertStatement.executeUpdate();

                if (rowsInserted > 0) {
                    System.out.println("Book '" + bookTitle + "' returned to library.");

                    // Удаляем книгу из таблицы borrowed_books
                    String deleteSql = "DELETE FROM borrowed_books WHERE book_title = ?";
                    PreparedStatement deleteStatement = connection.prepareStatement(deleteSql);
                    deleteStatement.setString(1, bookTitle);
                    int rowsDeleted = deleteStatement.executeUpdate();

                    if (rowsDeleted > 0) {
                        System.out.println("Book '" + bookTitle + "' removed from borrowed list.");
                    }

                    return true;
                }
            } else {
                System.out.println("Book '" + bookTitle + "' not found in borrowed list.");
            }
        } catch (SQLException e) {
            System.err.println("Error returning book by user!");
            e.printStackTrace();
        }
        return false;
    }


    public List<Book> getBorrowedBooks(String url, String user, String password) {
        List<Book> borrowedBooks = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            String sql = "SELECT * FROM borrowed_books WHERE borrower_last_name = ? AND borrower_first_name = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, this.lastName);
            statement.setString(2, this.firstName);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String title = resultSet.getString("book_title");
                String author = resultSet.getString("author");
                int year = resultSet.getInt("year");
                int quantity = resultSet.getInt("quantity");
                // Убрано получение borrower, так как этот столбец больше не используется
                Book book = new Book(title);
                borrowedBooks.add(book);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return borrowedBooks;
    }
}
